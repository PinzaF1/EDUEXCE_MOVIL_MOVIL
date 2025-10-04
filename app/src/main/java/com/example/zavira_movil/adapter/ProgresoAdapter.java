package com.example.zavira_movil.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.MateriaDetalle;

import java.util.List;

public class ProgresoAdapter extends RecyclerView.Adapter<ProgresoAdapter.ViewHolder> {

    private List<MateriaDetalle> lista;

    public ProgresoAdapter(List<MateriaDetalle> lista) {
        this.lista = lista;
    }

    public void setLista(List<MateriaDetalle> nueva) {
        this.lista = nueva;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_materia, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MateriaDetalle item = lista.get(position);
        holder.txtNombre.setText(item.getNombre());
        holder.txtProgreso.setText(item.getPorcentaje() + "%");
        holder.txtEtiqueta.setText(item.getEtiqueta());
        holder.progress.setProgress(item.getPorcentaje());
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtProgreso, txtEtiqueta;
        ProgressBar progress;

        ViewHolder(View itemView) {
            super(itemView);
            txtNombre   = itemView.findViewById(R.id.tvNombreMateria);
            txtProgreso = itemView.findViewById(R.id.tvProgreso);
            txtEtiqueta = itemView.findViewById(R.id.tvEtiqueta);
            progress    = itemView.findViewById(R.id.progressMateria);
        }
    }
}
