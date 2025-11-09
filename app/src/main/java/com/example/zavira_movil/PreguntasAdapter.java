package com.example.zavira_movil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class PreguntasAdapter extends RecyclerView.Adapter<PreguntasAdapter.ViewHolder> {

    private final Context context;
    private List<PreguntaAcademica> preguntas;
    private String[] selecciones; // "A"/"B"/"C"/"D" por posición
    private java.util.List<Integer> preguntasFaltantes = new java.util.ArrayList<>();

    public PreguntasAdapter(Context context, List<PreguntaAcademica> preguntas) {
        this.context = context;
        setPreguntas(preguntas, null);
    }
    
    public void marcarPreguntasFaltantes(java.util.List<Integer> faltantes) {
        this.preguntasFaltantes = new java.util.ArrayList<>(faltantes);
        notifyDataSetChanged();
    }

    /** Carga preguntas del bloque y preselecciona desde el mapa (idPregunta -> "A"/"B"/"C"/"D") */
    public void setPreguntas(List<PreguntaAcademica> nuevas, Map<String, String> preseleccion) {
        this.preguntas = nuevas;
        this.selecciones = new String[nuevas.size()];
        this.preguntasFaltantes.clear(); // Limpiar preguntas faltantes al cambiar de bloque
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
        View v = LayoutInflater.from(context).inflate(R.layout.item_pregunta, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        PreguntaAcademica p = preguntas.get(position);
        h.tvPregunta.setText((position + 1) + ". " + p.getEnunciado());

        // Marcar con borde rojo si falta responder
        boolean faltaResponder = preguntasFaltantes.contains(position);
        if (faltaResponder) {
            h.itemContainer.setBackgroundResource(R.drawable.bg_item_pregunta_error);
        } else {
            h.itemContainer.setBackgroundResource(R.drawable.bg_item_pregunta);
        }

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
                    h.itemContainer.setBackgroundResource(R.drawable.bg_item_pregunta);
                }
            });
            h.rgOpciones.addView(rb);
        }
    }

    @Override
    public int getItemCount() { return preguntas.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPregunta;
        RadioGroup rgOpciones;
        View itemContainer;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPregunta = itemView.findViewById(R.id.tvPregunta);
            rgOpciones = itemView.findViewById(R.id.rgOpciones);
            itemContainer = itemView; // Guardar referencia al contenedor principal
        }
    }
}
