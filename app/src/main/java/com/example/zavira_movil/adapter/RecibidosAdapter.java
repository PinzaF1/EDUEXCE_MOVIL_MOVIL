package com.example.zavira_movil.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.zavira_movil.R;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class RecibidosAdapter extends RecyclerView.Adapter<RecibidosAdapter.VH> {

    // Modelo mínimo. Si ya tienes tu propio modelo, úsalo; solo respeta idReto.
    public static class Item {
        public String idReto, nombre, materia, fecha;
        public Item(String idReto, String nombre, String materia, String fecha) {
            this.idReto = idReto; this.nombre = nombre; this.materia = materia; this.fecha = fecha;
        }
    }

    public interface OnActionListener {
        void onAccept(String idReto);
        void onReject(String idReto); //  callback para el botón Rechazar
    }

    private final List<Item> data = new ArrayList<>();
    private OnActionListener listener;

    public void setOnActionListener(OnActionListener l) { this.listener = l; }
    public void submit(List<Item> items) { data.clear(); if (items!=null) data.addAll(items); notifyDataSetChanged(); }
    public void removeById(String id) {
        int idx = -1; for (int i=0;i<data.size();i++) if (data.get(i).idReto.equals(id)) { idx=i; break; }
        if (idx>=0) { data.remove(idx); notifyItemRemoved(idx); }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reto_recibido, parent, false); //  el ITEM, no el fragment
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Item it = data.get(pos);
        h.tvFrom.setText(it.nombre + " te ha retado");
        h.tvMateria.setText("en " + it.materia);
        h.tvFecha.setText(it.fecha != null ? it.fecha : "");

        h.btnAceptar.setOnClickListener(v -> { if (listener != null) listener.onAccept(it.idReto); });
        h.btnRechazar.setOnClickListener(v -> { if (listener != null) listener.onReject(it.idReto); }); // 👈 CLICK RECHAZAR
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvFrom, tvMateria, tvFecha;
        MaterialButton btnAceptar, btnRechazar;
        VH(@NonNull View v) {
            super(v);
            tvFrom = v.findViewById(R.id.tvFrom);
            tvMateria = v.findViewById(R.id.tvMateria);
            tvFecha = v.findViewById(R.id.tvFecha);
            btnAceptar = v.findViewById(R.id.btnAceptar);
            btnRechazar = v.findViewById(R.id.btnRechazar);
        }
    }
}
