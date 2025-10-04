package com.example.zavira_movil.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.PreguntaReto;

import java.util.ArrayList;
import java.util.List;

public class PreguntasAdapter extends RecyclerView.Adapter<PreguntasAdapter.VH> {

    private final List<PreguntaReto> data = new ArrayList<>();

    public void setData(List<PreguntaReto> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pregunta, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        PreguntaReto p = data.get(pos);

        h.tvEnunciado.setText(p.getPregunta());

        StringBuilder meta = new StringBuilder();
        if (p.getArea() != null) meta.append(p.getArea());
        if (p.getSubtema() != null) meta.append(" • ").append(p.getSubtema());
        if (p.getDificultad() != null) meta.append(" • ").append(p.getDificultad());
        if (p.getEstilo_kolb() != null) meta.append(" • Kolb: ").append(p.getEstilo_kolb());
        h.tvMeta.setText(meta.toString());

        // Este OptionAdapter solo muestra opciones (no selecciona nada aquí)
        h.optionsAdapter.submit(p.getOpciones(), null);
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvEnunciado, tvMeta;
        RecyclerView rvOpciones;
        OptionAdapter optionsAdapter;

        VH(@NonNull View itemView) {
            super(itemView);
            tvEnunciado = itemView.findViewById(R.id.tvEnunciado);
            tvMeta      = itemView.findViewById(R.id.tvMeta);
            rvOpciones  = itemView.findViewById(R.id.rvOpciones);

            optionsAdapter = new OptionAdapter(key -> {}); // callback vacío para listado
            rvOpciones.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            rvOpciones.setAdapter(optionsAdapter);
        }
    }
}
