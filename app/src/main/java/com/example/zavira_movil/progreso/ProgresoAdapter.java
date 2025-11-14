package com.example.zavira_movil.progreso;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;

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
    public ProgresoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_materia, parent, false);
        return new ProgresoAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgresoAdapter.ViewHolder holder, int position) {
        MateriaDetalle item = lista.get(position);

        holder.txtNombre.setText(item.getNombre());
        holder.txtProgreso.setText(item.getPorcentaje() + "%");
        holder.txtEtiqueta.setText(item.getEtiqueta());
        holder.progress.setProgress(item.getPorcentaje());

        // === Tinte por área (sin cambiar IDs ni estructura) ===
        int color = colorForArea(item.getNombre(), holder.itemView);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            holder.progress.setProgressTintList(ColorStateList.valueOf(color));
            holder.progress.setProgressBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.getContext(), android.R.color.darker_gray)));
        } else {
            Drawable d = holder.progress.getProgressDrawable();
            if (d != null) {
                d = d.mutate();
                DrawableCompat.setTint(d, color);
                holder.progress.setProgressDrawable(d);
            }
        }
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

    private int colorForArea(String area, View v) {
        if (area == null) {
            return ContextCompat.getColor(v.getContext(), R.color.area_matematicas);
        }
        String a = java.text.Normalizer.normalize(area, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+","")
                .trim().toLowerCase();

        // Isla del Conocimiento / Todas las áreas - Amarillo
        if (a.contains("conocimiento") || a.contains("isla") || 
            (a.contains("todas") && (a.contains("area") || a.contains("área")))) {
            return ContextCompat.getColor(v.getContext(), R.color.area_conocimiento);
        }

        if (a.startsWith("mate"))   return ContextCompat.getColor(v.getContext(), R.color.area_matematicas);
        if (a.startsWith("leng"))   return ContextCompat.getColor(v.getContext(), R.color.area_lenguaje);
        if (a.startsWith("cien"))   return ContextCompat.getColor(v.getContext(), R.color.area_ciencias);
        if (a.startsWith("soci"))   return ContextCompat.getColor(v.getContext(), R.color.area_sociales);
        if (a.startsWith("ingl"))   return ContextCompat.getColor(v.getContext(), R.color.area_ingles);

        // por defecto
        return ContextCompat.getColor(v.getContext(), R.color.area_matematicas);
    }
}
