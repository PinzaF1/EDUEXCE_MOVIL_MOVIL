// com/example/zavira_movil/adapter/HistorialAdapter.java
package com.example.zavira_movil.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.zavira_movil.R;
import com.example.zavira_movil.model.HistorialItem;
import java.util.ArrayList;
import java.util.List;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.VH> {
    private final List<HistorialItem> data = new ArrayList<>();

    public void setData(List<HistorialItem> nuevos) {
        data.clear();
        if (nuevos != null) data.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historial, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        HistorialItem it = data.get(pos);
        h.tvMateria.setText(it.getMateria());
        h.tvPorcentaje.setText(it.getPorcentaje() + "%");
        h.tvNivel.setText(it.getNivel());
        h.tvFecha.setText(it.getFecha());
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvMateria, tvPorcentaje, tvNivel, tvFecha;
        VH(@NonNull View v) {
            super(v);
            tvMateria    = v.findViewById(R.id.tvMateria);
            tvPorcentaje = v.findViewById(R.id.tvPorcentaje);
            tvNivel      = v.findViewById(R.id.tvNivel);
            tvFecha      = v.findViewById(R.id.tvFecha);
        }
    }
}
