package com.example.zavira_movil.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.AceptarRetoResponse;

import java.util.ArrayList;
import java.util.List;

public class PreguntasAdapter extends RecyclerView.Adapter<PreguntasAdapter.VH> {

    private final List<AceptarRetoResponse.Pregunta> data = new ArrayList<>();

    /** Recibe las preguntas completas del reto */
    public void setData(List<AceptarRetoResponse.Pregunta> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pregunta, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        AceptarRetoResponse.Pregunta p = data.get(pos);

        // Texto principal de la pregunta
        h.tvEnunciado.setText(p.enunciado != null ? p.enunciado : "Pregunta sin texto");

        // Metadatos: área, subtema, dificultad...
        StringBuilder meta = new StringBuilder();
        if (p.area != null) meta.append(p.area);
        if (p.subtema != null) meta.append(" • ").append(p.subtema);
        if (p.dificultad != null) meta.append(" • ").append(p.dificultad);
        h.tvMeta.setText(meta.toString());

        // Muestra las opciones (solo modo lectura, sin callback)
        h.optionsAdapter.submit(p.opciones, null);
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvEnunciado, tvMeta;
        RecyclerView rvOpciones;
        OptionAdapter optionsAdapter;

        VH(@NonNull View itemView) {
            super(itemView);
            tvEnunciado = itemView.findViewById(R.id.tvEnunciado);
            tvMeta      = itemView.findViewById(R.id.tvMeta);
            rvOpciones  = itemView.findViewById(R.id.rvOpciones);

            // OptionAdapter ya usa AceptarRetoResponse.Opcion
            optionsAdapter = new OptionAdapter(key -> {}); // sin acción, solo mostrar
            rvOpciones.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            rvOpciones.setAdapter(optionsAdapter);
        }
    }
}
