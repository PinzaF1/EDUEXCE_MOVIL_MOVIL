// app/src/main/java/com/example/zavira_movil/adapter/RetosRecibidosAdapter.java
package com.example.zavira_movil.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.RetoListItem;

import java.util.ArrayList;
import java.util.List;

public class RecibidosAdapter extends RecyclerView.Adapter<RecibidosAdapter.VH> {

    public interface OnAceptarClick {
        void onAceptar(RetoListItem item);
    }

    private final List<RetoListItem> data = new ArrayList<>();
    private final OnAceptarClick listener;

    public RecibidosAdapter(OnAceptarClick listener) {
        this.listener = listener;
    }

    public void setData(List<RetoListItem> nuevos) {
        data.clear();
        if (nuevos != null) data.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reto_recibido, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        RetoListItem it = data.get(pos);

        String nombre = (it.getCreador()!=null && it.getCreador().getNombre()!=null)
                ? it.getCreador().getNombre() : "Alguien";
        h.tvTitulo.setText(nombre + " te desafió");

        String sub = (it.getArea()!=null ? it.getArea() : "") +
                (it.getEstado()!=null ? " • " + it.getEstado() : "");
        h.tvSub.setText(sub);

        // Mostrar botón solo si está pendiente (para evitar aceptar algo ya en curso o finalizado)
        boolean mostrarAceptar = "pendiente".equalsIgnoreCase(it.getEstado());
        h.btnAceptar.setVisibility(mostrarAceptar ? View.VISIBLE : View.GONE);

        h.btnAceptar.setOnClickListener(v -> {
            if (listener != null) listener.onAceptar(it);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvSub;
        Button btnAceptar;
        VH(@NonNull View v) {
            super(v);
            tvTitulo   = v.findViewById(R.id.tvTitulo);
            tvSub      = v.findViewById(R.id.tvSub);
            btnAceptar = v.findViewById(R.id.btnAceptar);
        }
    }
}
