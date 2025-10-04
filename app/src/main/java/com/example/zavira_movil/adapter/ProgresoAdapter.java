package com.example.zavira_movil.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.ProgresoMateria;

import java.util.List;

public class ProgresoAdapter extends RecyclerView.Adapter<ProgresoAdapter.VH> {

    private final List<ProgresoMateria> data;

    public ProgresoAdapter(List<ProgresoMateria> data) {
        this.data = data;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_materia, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        ProgresoMateria m = data.get(position);
        h.txtNombre.setText(m.getNombre());
        h.txtPorcentaje.setText(m.getProgreso() + "%");
        h.progress.setProgress(m.getProgreso());
    }

    @Override public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtNombre, txtPorcentaje;
        ProgressBar progress;
        VH(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombreMateria);
            txtPorcentaje = itemView.findViewById(R.id.txtPorcentajeMateria);
            progress = itemView.findViewById(R.id.progressMateria);
        }
    }
}
