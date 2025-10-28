package com.example.zavira_movil.retos1vs1;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.Opcion;
import com.example.zavira_movil.PreguntaAcademica;
import com.example.zavira_movil.R;

import java.util.List;
import java.util.Map;

public class PreguntasAdapter extends RecyclerView.Adapter<PreguntasAdapter.ViewHolder> {

    private final Context context;
    private List<PreguntaAcademica> preguntas;
    private String[] selecciones; // "A"/"B"/"C"/"D" por posición

    public PreguntasAdapter(Context context, List<PreguntaAcademica> preguntas) {
        this.context = context;
        setPreguntas(preguntas, null);
    }

    /** Carga preguntas del bloque y preselecciona desde el mapa */
    public void setPreguntas(List<PreguntaAcademica> nuevas, Map<String, String> preseleccion) {
        this.preguntas = nuevas;
        this.selecciones = new String[nuevas.size()];
        if (preseleccion != null) {
            for (int i = 0; i < nuevas.size(); i++) {
                String id = nuevas.get(i).getIdPregunta();
                if (preseleccion.containsKey(id)) {
                    this.selecciones[i] = preseleccion.get(id);
                }
            }
        }
        notifyDataSetChanged();
    }

    /** Vuelca selecciones al mapa global */
    public void collectSeleccionesTo(Map<String, String> destino) {
        for (int i = 0; i < preguntas.size(); i++) {
            if (selecciones[i] != null && !selecciones[i].isEmpty()) {
                destino.put(preguntas.get(i).getIdPregunta(), selecciones[i]);
            }
        }
    }

    public String[] getSelecciones() {
        return selecciones;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_pregunta, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        PreguntaAcademica p = preguntas.get(position);
        h.tvPregunta.setText((position + 1) + ". " + p.getEnunciado());

        h.rgOpciones.removeAllViews();

        for (Opcion op : p.getOpciones()) {
            RadioButton rb = new RadioButton(context);
            rb.setText(op.getKey() + ") " + op.getText());

            // Texto azul
            rb.setTextColor(Color.parseColor("#004AAD"));

            // Mantener selección previa
            rb.setChecked(op.getKey().equals(selecciones[position]));

            rb.setOnClickListener(v -> {
                selecciones[position] = op.getKey();
                notifyItemChanged(position);
            });

            h.rgOpciones.addView(rb);
        }
    }

    @Override
    public int getItemCount() {
        return preguntas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPregunta;
        RadioGroup rgOpciones;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPregunta = itemView.findViewById(R.id.tvPregunta);
            rgOpciones = itemView.findViewById(R.id.rgOpciones);
        }
    }
}
