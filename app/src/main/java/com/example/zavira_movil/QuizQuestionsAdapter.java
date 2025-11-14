package com.example.zavira_movil;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.model.Question;

import java.util.ArrayList;
import java.util.List;

public class QuizQuestionsAdapter extends RecyclerView.Adapter<QuizQuestionsAdapter.VH> {

    private final List<Question> data = new ArrayList<>();
    private final List<String> marcadas = new ArrayList<>(); // "A".."D"
    private String areaNombre;
    private boolean forzarAmarillo = false; // Flag para forzar color amarillo en Isla del Conocimiento

    public QuizQuestionsAdapter(List<Question> preguntas, String areaNombre) {
        this(preguntas, areaNombre, null);
    }
    
    private int numeroPreguntaActual = 1; // Número de pregunta actual (1-indexed)
    
    public QuizQuestionsAdapter(List<Question> preguntas, String areaNombre, String respuestaPreseleccionada) {
        this(preguntas, areaNombre, respuestaPreseleccionada, 1);
    }
    
    public QuizQuestionsAdapter(List<Question> preguntas, String areaNombre, String respuestaPreseleccionada, int numeroPregunta) {
        this(preguntas, areaNombre, respuestaPreseleccionada, numeroPregunta, false);
    }
    
    public QuizQuestionsAdapter(List<Question> preguntas, String areaNombre, String respuestaPreseleccionada, int numeroPregunta, boolean forzarAmarillo) {
        if (preguntas != null) data.addAll(preguntas);
        for (int i = 0; i < data.size(); i++) {
            // Si hay una respuesta pre-seleccionada para la primera pregunta, usarla
            if (i == 0 && respuestaPreseleccionada != null) {
                marcadas.add(respuestaPreseleccionada);
            } else {
                marcadas.add(null);
            }
        }
        this.areaNombre = areaNombre;
        this.numeroPreguntaActual = numeroPregunta;
        this.forzarAmarillo = forzarAmarillo;
    }
    
    
    public List<String> getMarcadas() { return marcadas; }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_question, p, false);
        VH holder = new VH(view);
        // Calcular colores con el contexto correcto
        if (areaNombre != null) {
            String a = areaNombre.toLowerCase().trim();
            // Si forzarAmarillo está activado, siempre usar color amarillo (para Isla del Conocimiento)
            if (forzarAmarillo) {
                holder.areaColor = Color.parseColor("#F59E0B");
                holder.areaColorPastel = Color.parseColor("#FFF9E6");
            } else if (a.contains("isla")) {
                // Color amarillo para Isla del Conocimiento
                holder.areaColor = Color.parseColor("#F59E0B");
                holder.areaColorPastel = Color.parseColor("#FFF9E6");
            } else if (a.contains("matem")) {
                holder.areaColor = ContextCompat.getColor(p.getContext(), R.color.area_matematicas);
                holder.areaColorPastel = ContextCompat.getColor(p.getContext(), R.color.area_matematicas_pastel);
            } else if (a.contains("lengua") || a.contains("lectura") || a.contains("espa") || a.contains("critica")) {
                holder.areaColor = ContextCompat.getColor(p.getContext(), R.color.area_lenguaje);
                holder.areaColorPastel = ContextCompat.getColor(p.getContext(), R.color.area_lenguaje_pastel);
            } else if (a.contains("social") || a.contains("ciudad")) {
                holder.areaColor = ContextCompat.getColor(p.getContext(), R.color.area_sociales);
                holder.areaColorPastel = ContextCompat.getColor(p.getContext(), R.color.area_sociales_pastel);
            } else if (a.contains("cien") || a.contains("biolo") || a.contains("fis") || a.contains("quim")) {
                holder.areaColor = ContextCompat.getColor(p.getContext(), R.color.area_ciencias);
                holder.areaColorPastel = ContextCompat.getColor(p.getContext(), R.color.area_ciencias_pastel);
            } else if (a.contains("ingl")) {
                holder.areaColor = ContextCompat.getColor(p.getContext(), R.color.area_ingles);
                holder.areaColorPastel = ContextCompat.getColor(p.getContext(), R.color.area_ingles_pastel);
            } else {
                holder.areaColor = Color.parseColor("#957DAD");
                holder.areaColorPastel = Color.parseColor("#F3E5F5");
            }
        }
        return holder;
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Question q = data.get(pos);
        h.tvEnunciado.setText(q.enunciado != null ? q.enunciado : "");
        
        // Configurar área y número
        if (h.tvAreaNombre != null && areaNombre != null) {
            h.tvAreaNombre.setText(areaNombre);
            h.tvAreaNombre.setTextColor(h.areaColor);
        }
        if (h.tvNumeroArea != null) {
            // Usar el número de pregunta actual en lugar de pos + 1
            h.tvNumeroArea.setText(String.valueOf(numeroPreguntaActual));
            h.tvNumeroArea.setBackgroundColor(h.areaColor);
        }
        
        // Configurar fondo pastel del área
        if (h.cardPregunta != null) {
            h.cardPregunta.setCardBackgroundColor(h.areaColorPastel);
        }

        // Configurar opciones
        configurarOpcion(h.optionA, h.circleA, h.textA, q, 0, "A", pos);
        configurarOpcion(h.optionB, h.circleB, h.textB, q, 1, "B", pos);
        configurarOpcion(h.optionC, h.circleC, h.textC, q, 2, "C", pos);
        configurarOpcion(h.optionD, h.circleD, h.textD, q, 3, "D", pos);
    }
    
    private void configurarOpcion(LinearLayout optionLayout, TextView circle, TextView text, Question q, int index, String key, int pos) {
        if (q.opciones == null || q.opciones.size() <= index || q.opciones.get(index) == null) {
            optionLayout.setVisibility(View.GONE);
            return;
        }
        
        optionLayout.setVisibility(View.VISIBLE);
        text.setText(q.opciones.get(index).text != null ? q.opciones.get(index).text : "");
        
        String saved = marcadas.get(pos);
        boolean isSelected = key.equals(saved);
        
        // Aplicar estilos según selección
        if (isSelected) {
            // Gris cuando está seleccionada
            optionLayout.setSelected(true);
            optionLayout.setBackgroundResource(R.drawable.bg_option_selected_green);
            circle.setBackgroundResource(R.drawable.bg_option_circle_selected_green);
            circle.setTextColor(Color.WHITE);
            text.setTextColor(Color.parseColor("#6B7280"));
        } else {
            // Gris cuando no está seleccionada
            optionLayout.setSelected(false);
            optionLayout.setBackgroundResource(R.drawable.bg_option_unselected_white);
            circle.setBackgroundResource(R.drawable.bg_option_circle_unselected_gray);
            circle.setTextColor(Color.parseColor("#666666"));
            text.setTextColor(Color.parseColor("#111827"));
        }
        
        // Click listener
        optionLayout.setOnClickListener(v -> {
            marcadas.set(pos, key);
            notifyItemChanged(pos);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvEnunciado, tvAreaNombre, tvNumeroArea;
        LinearLayout optionA, optionB, optionC, optionD;
        TextView circleA, circleB, circleC, circleD;
        TextView textA, textB, textC, textD;
        androidx.cardview.widget.CardView cardPregunta;
        int areaColor;
        int areaColorPastel;
        
        VH(@NonNull View v) {
            super(v);
            tvEnunciado = v.findViewById(R.id.tvEnunciado);
            tvAreaNombre = v.findViewById(R.id.tvAreaNombre);
            tvNumeroArea = v.findViewById(R.id.tvNumeroArea);
            cardPregunta = v.findViewById(R.id.cardPregunta);
            
            optionA = v.findViewById(R.id.optionA);
            optionB = v.findViewById(R.id.optionB);
            optionC = v.findViewById(R.id.optionC);
            optionD = v.findViewById(R.id.optionD);
            
            circleA = v.findViewById(R.id.circleA);
            circleB = v.findViewById(R.id.circleB);
            circleC = v.findViewById(R.id.circleC);
            circleD = v.findViewById(R.id.circleD);
            
            textA = v.findViewById(R.id.textA);
            textB = v.findViewById(R.id.textB);
            textC = v.findViewById(R.id.textC);
            textD = v.findViewById(R.id.textD);
        }
    }
}
