// MateriasAdapter.java
package com.example.zavira_movil.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.MateriaDetalle;

import java.util.ArrayList;
import java.util.List;

public class MateriasAdapter extends RecyclerView.Adapter<MateriasAdapter.VH> {
    private final List<MateriaDetalle> lista = new ArrayList<>();

    public void setLista(List<MateriaDetalle> nueva) {
        lista.clear();
        if (nueva != null) lista.addAll(nueva);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_materia, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        MateriaDetalle m = lista.get(pos);
        h.tvNombre.setText(m.getNombre());
        h.tvPorcentaje.setText(m.getPorcentaje() + "%");
        h.tvEtiqueta.setText(m.getEtiqueta());
    }

    @Override public int getItemCount() { return lista.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvNombre, tvPorcentaje, tvEtiqueta;
        VH(@NonNull View itemView) {
            super(itemView);
            tvNombre     = itemView.findViewById(R.id.tvNombre);
            tvPorcentaje = itemView.findViewById(R.id.tvPorcentaje);
            tvEtiqueta   = itemView.findViewById(R.id.tvEtiqueta);
        }
    }
}
