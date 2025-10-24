package com.example.zavira_movil.retos1vs1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;

import java.util.ArrayList;
import java.util.List;

public class RecibidosAdapter extends RecyclerView.Adapter<RecibidosAdapter.VH> {

    public interface OnAceptarClick { void onAceptar(RetoListItem item); }
    public interface OnRechazarClick { void onRechazar(RetoListItem item); }

    private static class DisponibilidadLocal {
        private static boolean disponible = true;
        static boolean isDisponible() { return disponible; }
        static void setDisponible(boolean v) { disponible = v; }
    }

    private final List<RetoListItem> data = new ArrayList<>();
    private final OnAceptarClick aceptarListener;
    private OnRechazarClick rechazarListener;

    public RecibidosAdapter(OnAceptarClick listener) {
        this.aceptarListener = listener;
    }

    public void setOnRechazarClick(OnRechazarClick listener) {
        this.rechazarListener = listener;
    }

    public void setData(List<RetoListItem> nuevos) {
        data.clear();
        if (nuevos != null) data.addAll(nuevos);
        notifyDataSetChanged();
    }

    public void removeAt(int position) {
        if (position >= 0 && position < data.size()) {
            data.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, getItemCount() - position);
        }
    }

    public static boolean isUsuarioDisponible() { return DisponibilidadLocal.isDisponible(); }
    public static void setUsuarioDisponible(boolean disponible) { DisponibilidadLocal.setDisponible(disponible); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reto_recibido, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        RetoListItem it = data.get(pos);

        String nombre = (it.getCreador()!=null && it.getCreador().getNombre()!=null)
                ? it.getCreador().getNombre() : "Alguien";
        h.tvTitulo.setText(nombre + " te desafió");

        String area   = it.getArea()   != null ? it.getArea()   : "";
        String estado = it.getEstado() != null ? it.getEstado() : "";
        h.tvSub.setText(area + (estado.isEmpty() ? "" : " • " + estado));

        boolean pendiente = "pendiente".equalsIgnoreCase(estado);

        // Texto del botón para el retado
        h.btnAceptar.setText("Comenzar reto");

        h.btnAceptar.setVisibility(pendiente ? View.VISIBLE : View.GONE);
        if (h.btnRechazar != null) {
            h.btnRechazar.setVisibility(pendiente ? View.VISIBLE : View.GONE);
        }

        h.btnAceptar.setOnClickListener(v -> {
            if (aceptarListener != null) aceptarListener.onAceptar(it);
            // DisponibilidadLocal.setDisponible(false); // opcional
        });

        if (h.btnRechazar != null) {
            h.btnRechazar.setOnClickListener(v -> {
                if (rechazarListener != null) rechazarListener.onRechazar(it);
                DisponibilidadLocal.setDisponible(true);
                int adapterPos = h.getBindingAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) removeAt(adapterPos);
            });
        }
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvSub;
        Button btnAceptar, btnRechazar;
        VH(@NonNull View v) {
            super(v);
            tvTitulo    = v.findViewById(R.id.tvTitulo);
            tvSub       = v.findViewById(R.id.tvSub);
            btnAceptar  = v.findViewById(R.id.btnAceptar);
            btnRechazar = v.findViewById(R.id.btnRechazar);
        }
    }
}
