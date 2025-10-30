package com.example.zavira_movil.progreso;

import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.VH> {
    // callback para abrir detalle SOLO con la flecha
    public interface OnItemClick { void onClick(HistorialItem item); }
    private OnItemClick onItemClick;
    public void setOnItemClick(OnItemClick l) { this.onItemClick = l; }

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

        // Texto base
        h.tvMateria.setText(it.getMateria());
        h.tvNivel.setText(it.getNivel());
        h.tvFecha.setText(formatFecha(it.getFecha()));

        // Puntico por materia (a la izquierda del nombre)
        int subjectColor = colorForSubject(h.itemView.getContext(), it.getMateria());
        GradientDrawable dot = new GradientDrawable();
        dot.setShape(GradientDrawable.OVAL);
        dot.setColor(subjectColor);
        int dotSize = dp(h.itemView, 10);
        dot.setSize(dotSize, dotSize);
        dot.setBounds(0, 0, dotSize, dotSize);
        h.tvMateria.setCompoundDrawablesRelative(dot, null, null, null);
        h.tvMateria.setCompoundDrawablePadding(dp(h.itemView, 8));

        // SOLO la FLECHA navega
        if (h.ivFlecha != null) {
            h.ivFlecha.setOnClickListener(v -> {
                int p = h.getBindingAdapterPosition();
                if (p != RecyclerView.NO_POSITION && onItemClick != null) {
                    onItemClick.onClick(data.get(p));
                }
            });
        }

        // 🚫 Sin acciones en el resto de la celda
        h.itemView.setOnClickListener(null);
        h.tvMateria.setOnClickListener(null);
        h.tvNivel.setOnClickListener(null);
        h.tvFecha.setOnClickListener(null);
        h.itemView.setClickable(false);
        h.tvMateria.setClickable(false);
        h.tvNivel.setClickable(false);
        h.tvFecha.setClickable(false);
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvMateria, tvNivel, tvFecha;
        ImageView ivFlecha;

        VH(@NonNull View v) {
            super(v);
            tvMateria = v.findViewById(R.id.tvMateria);
            tvNivel   = v.findViewById(R.id.tvNivel);
            tvFecha   = v.findViewById(R.id.tvFecha);
            ivFlecha  = v.findViewById(R.id.ivFlecha);
        }
    }

    // ---------- Helpers ----------
    private int colorForSubject(android.content.Context ctx, String materiaRaw) {
        if (materiaRaw == null) return ContextCompat.getColor(ctx, R.color.subject_default);
        String n = materiaRaw.trim().toLowerCase(Locale.getDefault());
        if (n.contains("mate"))   return ContextCompat.getColor(ctx, R.color.area_matematicas);
        if (n.contains("leng") || n.contains("lect") || n.contains("comuni"))
            return ContextCompat.getColor(ctx, R.color.area_lenguaje);
        if (n.contains("cien") || n.contains("biol") || n.contains("fis") || n.contains("quím") || n.contains("quim"))
            return ContextCompat.getColor(ctx, R.color.area_ciencias);
        if (n.contains("socia") || n.contains("hist") || n.contains("filo") || n.contains("ciudad"))
            return ContextCompat.getColor(ctx, R.color.area_sociales);
        if (n.contains("ingl") || n.contains("english"))
            return ContextCompat.getColor(ctx, R.color.area_ingles);
        return ContextCompat.getColor(ctx, R.color.subject_default);
    }

    private int dp(View view, int dp) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, view.getResources().getDisplayMetrics()));
    }

    private String formatFecha(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        if (s.isEmpty()) return "";
        try {
            if (s.matches("^\\d{10,}$")) {
                long epoch = Long.parseLong(s);
                Date d = new Date(s.length() == 10 ? epoch * 1000L : epoch);
                return outFmt().format(d);
            }
        } catch (Exception ignored) {}
        String[] patterns = new String[] {
                "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                "yyyy-MM-dd'T'HH:mm:ssX",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
        };
        for (String p : patterns) {
            try {
                SimpleDateFormat in = new SimpleDateFormat(p, Locale.getDefault());
                if (p.contains("X")) in.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date d = in.parse(s);
                if (d != null) return outFmt().format(d);
            } catch (ParseException ignored) {}
        }
        return s;
    }

    private SimpleDateFormat outFmt() {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }
}
