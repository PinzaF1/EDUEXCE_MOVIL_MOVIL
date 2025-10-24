package com.example.zavira_movil.detalleprogreso;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;

import java.util.List;

public class AnalisisAdapter extends RecyclerView.Adapter<AnalisisAdapter.VH> {

    private final List<FragmentDetalleAnalisis.AnalisisItem> items;
    public AnalisisAdapter(List<FragmentDetalleAnalisis.AnalisisItem> items){ this.items = items; }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_analisis_bloque, p, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        FragmentDetalleAnalisis.AnalisisItem it = items.get(pos);
        h.tvTitulo.setText(it.titulo);
        h.tvBullets.setText("• " + join(it.bullets));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvBullets;
        VH(View v){ super(v); tvTitulo=v.findViewById(R.id.tvTitulo); tvBullets=v.findViewById(R.id.tvBullets); }
    }

    private String join(List<String> list){
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<list.size();i++){ if(i>0) sb.append("\n• "); sb.append(list.get(i)); }
        return sb.toString();
    }
}
