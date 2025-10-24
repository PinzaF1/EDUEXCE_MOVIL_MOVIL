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
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_materia, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        MateriaDetalle m = lista.get(pos);
        h.tvNombre.setText(m.getNombre());
        h.tvPorcentaje.setText(m.getPorcentaje() + "%");
        h.tvEtiqueta.setText(m.getEtiqueta());
        h.pb.setProgress(m.getPorcentaje());

        // === Tinte por área (misma lógica que ProgresoAdapter) ===
        int color = colorForArea(m.getNombre(), h.itemView);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            h.pb.setProgressTintList(ColorStateList.valueOf(color));
            h.pb.setProgressBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(h.itemView.getContext(), android.R.color.darker_gray)));
        } else {
            Drawable d = h.pb.getProgressDrawable();
            if (d != null) {
                d = d.mutate();
                DrawableCompat.setTint(d, color);
                h.pb.setProgressDrawable(d);
            }
        }
    }

    @Override public int getItemCount() { return lista.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvNombre, tvPorcentaje, tvEtiqueta;
        ProgressBar pb;

        VH(@NonNull View itemView) {
            super(itemView);
            tvNombre     = itemView.findViewById(R.id.tvNombreMateria);
            tvPorcentaje = itemView.findViewById(R.id.tvProgreso);
            tvEtiqueta   = itemView.findViewById(R.id.tvEtiqueta);
            pb           = itemView.findViewById(R.id.progressMateria);
        }
    }

    private int colorForArea(String area, View v) {
        if (area == null) {
            return ContextCompat.getColor(v.getContext(), R.color.area_matematicas);
        }
        String a = java.text.Normalizer.normalize(area, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+","")
                .trim().toLowerCase();

        if (a.startsWith("mate"))   return ContextCompat.getColor(v.getContext(), R.color.area_matematicas);
        if (a.startsWith("leng"))   return ContextCompat.getColor(v.getContext(), R.color.area_lenguaje);
        if (a.startsWith("cien"))   return ContextCompat.getColor(v.getContext(), R.color.area_ciencias);
        if (a.startsWith("soci"))   return ContextCompat.getColor(v.getContext(), R.color.area_sociales);
        if (a.startsWith("ingl"))   return ContextCompat.getColor(v.getContext(), R.color.area_ingles);

        // por defecto
        return ContextCompat.getColor(v.getContext(), R.color.area_matematicas);
    }
}