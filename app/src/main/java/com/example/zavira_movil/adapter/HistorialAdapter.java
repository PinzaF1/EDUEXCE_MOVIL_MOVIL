package com.example.zavira_movil.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.HistorialItem;

import java.util.List;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.VH> {

    private final List<HistorialItem> data;

    public HistorialAdapter(List<HistorialItem> data) {
        this.data = data;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historial, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        HistorialItem it = data.get(position);
        h.txtTitulo.setText(it.getTitulo());
        h.txtDetalle.setText(it.getDetalle());
        h.txtFecha.setText(it.getFecha());
    }

    @Override public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtDetalle, txtFecha;
        VH(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTituloHistorial);
            txtDetalle = itemView.findViewById(R.id.txtDetalleHistorial);
            txtFecha = itemView.findViewById(R.id.txtFechaHistorial);
        }
    }
}
