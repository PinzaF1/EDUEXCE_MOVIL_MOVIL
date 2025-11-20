package com.example.zavira_movil;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PreguntasAdapter extends RecyclerView.Adapter<PreguntasAdapter.ViewHolder> {

    private final Context context;
    private List<PreguntaAcademica> preguntas;
    private String[] selecciones; // "A"/"B"/"C"/"D" por posición
    private java.util.List<Integer> preguntasFaltantes = new java.util.ArrayList<>();
    private String areaActual = "";
    private int preguntaInicial = 0;

    public PreguntasAdapter(Context context, List<PreguntaAcademica> preguntas) {
        this.context = context;
        setPreguntas(preguntas, null, "", 0);
    }
    
    public void marcarPreguntasFaltantes(java.util.List<Integer> faltantes) {
        this.preguntasFaltantes = new java.util.ArrayList<>(faltantes);
        notifyDataSetChanged();
    }

    /** Carga preguntas del bloque y preselecciona desde el mapa (idPregunta -> "A"/"B"/"C"/"D") */
    public void setPreguntas(List<PreguntaAcademica> nuevas, Map<String, String> preseleccion) {
        setPreguntas(nuevas, preseleccion, "", 0);
    }
    
    /** Carga preguntas del bloque con área y número inicial */
    public void setPreguntas(List<PreguntaAcademica> nuevas, Map<String, String> preseleccion, String area, int preguntaInicial) {
        this.preguntas = nuevas != null ? nuevas : new ArrayList<>();
        this.selecciones = new String[this.preguntas.size()];
        this.preguntasFaltantes.clear(); // Limpiar preguntas faltantes al cambiar de bloque
        this.areaActual = area;
        this.preguntaInicial = preguntaInicial;
        if (preseleccion != null) {
            for (int i = 0; i < this.preguntas.size(); i++) {
                String id = this.preguntas.get(i).getIdPregunta();
                if (preseleccion.containsKey(id)) {
                    this.selecciones[i] = preseleccion.get(id);
                }
            }
        }
        android.util.Log.d("PreguntasAdapter", "setPreguntas: " + this.preguntas.size() + " preguntas para área: " + area);
        notifyDataSetChanged();
    }

    /** Vuelca las selecciones actuales al mapa global (idPregunta -> "A"/"B"/"C"/"D") */
    public void collectSeleccionesTo(Map<String, String> destino) {
        for (int i = 0; i < preguntas.size(); i++) {
            if (selecciones[i] != null && !selecciones[i].isEmpty()) {
                destino.put(preguntas.get(i).getIdPregunta(), selecciones[i]);
            }
        }
    }

    public String[] getSelecciones() { return selecciones; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_question, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        PreguntaAcademica p = preguntas.get(position);
        int numeroPregunta = preguntaInicial + position + 1;
        
        // Obtener colores del área (como en QuizQuestionsAdapter)
        int areaColor = obtenerColorAreaDesdeRecursos(areaActual);
        int areaColorPastel = obtenerColorAreaPastelDesdeRecursos(areaActual);
        
        // Actualizar número y área en el header
        if (h.tvNumeroArea != null) {
            h.tvNumeroArea.setText(String.valueOf(numeroPregunta));
            // El cuadrado usa el color del área
            android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
            bg.setColor(areaColor);
            float density = context.getResources().getDisplayMetrics().density;
            bg.setCornerRadius(4 * density); // 4dp para esquinas redondeadas
            h.tvNumeroArea.setBackground(bg);
        }
        if (h.tvAreaNombre != null) {
            h.tvAreaNombre.setText(areaActual);
            // El texto del área usa el color del área
            h.tvAreaNombre.setTextColor(areaColor);
        }
        
        // Actualizar fondo pastel del CardView (como en QuizQuestionsAdapter)
        if (h.cardPregunta != null) {
            h.cardPregunta.setCardBackgroundColor(areaColorPastel);
        }
        
        // Actualizar enunciado - siempre en negro
        if (h.tvEnunciado != null) {
            h.tvEnunciado.setText(p.getEnunciado());
            h.tvEnunciado.setTextColor(android.graphics.Color.parseColor("#000000")); // Negro
        } else if (h.tvPregunta != null) {
            h.tvPregunta.setText(numeroPregunta + ". " + p.getEnunciado());
        }

        // Marcar con borde rojo si falta responder
        boolean faltaResponder = preguntasFaltantes.contains(position);
        if (faltaResponder) {
            if (h.cardPregunta != null) {
                h.cardPregunta.setCardBackgroundColor(android.graphics.Color.parseColor("#FFF5F5"));
            } else if (h.itemContainer != null) {
            h.itemContainer.setBackgroundResource(R.drawable.bg_item_pregunta_error);
            }
        } else {
            if (h.cardPregunta != null) {
                h.cardPregunta.setCardBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"));
            } else if (h.itemContainer != null) {
            h.itemContainer.setBackgroundResource(R.drawable.bg_item_pregunta);
            }
        }

        // Actualizar opciones
        if (h.optionA != null && h.textA != null && h.circleA != null) {
            // Usar el nuevo layout con opciones A, B, C, D
            actualizarOpcionesNuevoLayout(h, position, p);
        } else if (h.rgOpciones != null) {
            // Usar el layout antiguo con RadioGroup
        h.rgOpciones.removeAllViews();
        for (Opcion op : p.getOpciones()) {
            RadioButton rb = new RadioButton(context);
            rb.setText(op.getKey() + ") " + op.getText());
            rb.setChecked(op.getKey().equals(selecciones[position]));
            rb.setOnClickListener(v -> {
                selecciones[position] = op.getKey();
                for (int j = 0; j < h.rgOpciones.getChildCount(); j++) {
                    RadioButton otro = (RadioButton) h.rgOpciones.getChildAt(j);
                    otro.setChecked(otro == v);
                }
                // Remover borde rojo cuando se responde
                if (preguntasFaltantes.contains(position)) {
                    preguntasFaltantes.remove(Integer.valueOf(position));
                        if (h.cardPregunta != null) {
                            h.cardPregunta.setCardBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"));
                        } else if (h.itemContainer != null) {
                    h.itemContainer.setBackgroundResource(R.drawable.bg_item_pregunta);
                        }
                }
            });
            h.rgOpciones.addView(rb);
            }
        }
    }
    
    private void actualizarOpcionesNuevoLayout(ViewHolder h, int position, PreguntaAcademica p) {
        String seleccionada = selecciones[position] != null ? selecciones[position] : "";
        List<Opcion> opciones = p.getOpciones();
        
        // Configurar cada opción si existe
        if (opciones.size() > 0 && h.optionA != null && h.circleA != null && h.textA != null) {
            configurarOpcion(h.optionA, h.circleA, h.textA, "A", opciones.get(0).getText(), seleccionada.equals("A"), position);
        }
        if (opciones.size() > 1 && h.optionB != null && h.circleB != null && h.textB != null) {
            configurarOpcion(h.optionB, h.circleB, h.textB, "B", opciones.get(1).getText(), seleccionada.equals("B"), position);
        }
        if (opciones.size() > 2 && h.optionC != null && h.circleC != null && h.textC != null) {
            configurarOpcion(h.optionC, h.circleC, h.textC, "C", opciones.get(2).getText(), seleccionada.equals("C"), position);
        }
        if (opciones.size() > 3 && h.optionD != null && h.circleD != null && h.textD != null) {
            configurarOpcion(h.optionD, h.circleD, h.textD, "D", opciones.get(3).getText(), seleccionada.equals("D"), position);
        }
    }
    
    private void configurarOpcion(android.view.View optionLayout, TextView circle, TextView text, String letra, String texto, boolean seleccionada, int position) {
        text.setText(texto);
        
        // Obtener color del área para el círculo seleccionado
        int colorArea = obtenerColorArea(areaActual);
        
        if (seleccionada) {
            // Usar color del área para el círculo seleccionado
            circle.setBackgroundColor(colorArea);
            circle.setTextColor(android.graphics.Color.WHITE);
        } else {
            circle.setBackgroundResource(R.drawable.bg_option_circle_unselected_gray);
            circle.setTextColor(android.graphics.Color.parseColor("#666666"));
        }
        
        optionLayout.setOnClickListener(v -> {
            selecciones[position] = letra;
            notifyItemChanged(position);
            
            // Remover borde rojo cuando se responde
            if (preguntasFaltantes.contains(position)) {
                preguntasFaltantes.remove(Integer.valueOf(position));
                notifyItemChanged(position);
            }
        });
    }
    
    private int obtenerColorAreaDesdeRecursos(String area) {
        if (area == null) return Color.parseColor("#957DAD");
        String a = area.toLowerCase().trim();
        if (a.contains("matem")) {
            return ContextCompat.getColor(context, R.color.area_matematicas);
        } else if (a.contains("lengua") || a.contains("lectura") || a.contains("leng") || a.contains("espa") || a.contains("critica")) {
            return ContextCompat.getColor(context, R.color.area_lenguaje);
        } else if (a.contains("social") || a.contains("soci") || a.contains("ciudad")) {
            return ContextCompat.getColor(context, R.color.area_sociales);
        } else if (a.contains("cien") || a.contains("biolo") || a.contains("fis") || a.contains("quim")) {
            return ContextCompat.getColor(context, R.color.area_ciencias);
        } else if (a.contains("ingl") || a.contains("ing")) {
            return ContextCompat.getColor(context, R.color.area_ingles);
        } else {
            return Color.parseColor("#957DAD");
        }
    }
    
    private int obtenerColorAreaPastelDesdeRecursos(String area) {
        if (area == null) return Color.parseColor("#F3E5F5");
        String a = area.toLowerCase().trim();
        if (a.contains("matem")) {
            return ContextCompat.getColor(context, R.color.area_matematicas_pastel);
        } else if (a.contains("lengua") || a.contains("lectura") || a.contains("leng") || a.contains("espa") || a.contains("critica")) {
            return ContextCompat.getColor(context, R.color.area_lenguaje_pastel);
        } else if (a.contains("social") || a.contains("soci") || a.contains("ciudad")) {
            return ContextCompat.getColor(context, R.color.area_sociales_pastel);
        } else if (a.contains("cien") || a.contains("biolo") || a.contains("fis") || a.contains("quim")) {
            return ContextCompat.getColor(context, R.color.area_ciencias_pastel);
        } else if (a.contains("ingl") || a.contains("ing")) {
            return ContextCompat.getColor(context, R.color.area_ingles_pastel);
        } else {
            return Color.parseColor("#F3E5F5");
        }
    }
    
    private int obtenerColorArea(String area) {
        return obtenerColorAreaDesdeRecursos(area);
    }
    
    private int obtenerDrawableArea(String area) {
        if (area == null) return R.drawable.bg_option_circle_purple;
        String areaLower = area.toLowerCase();
        if (areaLower.contains("matem")) return R.drawable.bg_option_circle_red;
        if (areaLower.contains("leng") || areaLower.contains("lect")) return R.drawable.bg_option_circle_blue;
        if (areaLower.contains("cien")) return R.drawable.bg_option_circle_green;
        if (areaLower.contains("soci")) return R.drawable.bg_option_circle_orange;
        if (areaLower.contains("ing")) return R.drawable.bg_option_circle_purple;
        return R.drawable.bg_option_circle_purple;
    }
    
    private int obtenerColorAreaClaro(String area) {
        return obtenerColorAreaPastelDesdeRecursos(area);
    }

    @Override
    public int getItemCount() { 
        int count = preguntas != null ? preguntas.size() : 0;
        android.util.Log.d("PreguntasAdapter", "getItemCount: " + count);
        return count;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPregunta;
        RadioGroup rgOpciones;
        View itemContainer;
        
        // Nuevos elementos del layout item_question.xml
        androidx.cardview.widget.CardView cardPregunta;
        TextView tvNumeroArea;
        TextView tvAreaNombre;
        TextView tvEnunciado;
        android.view.View optionA, optionB, optionC, optionD;
        TextView circleA, circleB, circleC, circleD;
        TextView textA, textB, textC, textD;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPregunta = itemView.findViewById(R.id.tvPregunta);
            rgOpciones = itemView.findViewById(R.id.rgOpciones);
            itemContainer = itemView;
            
            // Intentar encontrar elementos del nuevo layout
            cardPregunta = itemView.findViewById(R.id.cardPregunta);
            tvNumeroArea = itemView.findViewById(R.id.tvNumeroArea);
            tvAreaNombre = itemView.findViewById(R.id.tvAreaNombre);
            tvEnunciado = itemView.findViewById(R.id.tvEnunciado);
            optionA = itemView.findViewById(R.id.optionA);
            optionB = itemView.findViewById(R.id.optionB);
            optionC = itemView.findViewById(R.id.optionC);
            optionD = itemView.findViewById(R.id.optionD);
            circleA = itemView.findViewById(R.id.circleA);
            circleB = itemView.findViewById(R.id.circleB);
            circleC = itemView.findViewById(R.id.circleC);
            circleD = itemView.findViewById(R.id.circleD);
            textA = itemView.findViewById(R.id.textA);
            textB = itemView.findViewById(R.id.textB);
            textC = itemView.findViewById(R.id.textC);
            textD = itemView.findViewById(R.id.textD);
        }
    }
}
