package com.example.zavira_movil.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.HistorialItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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

        // Texto base
        h.tvMateria.setText(it.getMateria());
        h.tvNivel.setText(it.getNivel());
        h.tvFecha.setText(formatFecha(it.getFecha())); // ← FECHA FORMATEADA

        // Puntico por materia (igual que antes)
        int subjectColor = colorForSubject(h.itemView.getContext(), it.getMateria());
        GradientDrawable dot = new GradientDrawable();
        dot.setShape(GradientDrawable.OVAL);
        dot.setColor(subjectColor);
        int dotSize = dp(h.itemView.getContext(), 10);
        dot.setSize(dotSize, dotSize);
        dot.setBounds(0, 0, dotSize, dotSize);
        h.tvMateria.setCompoundDrawablesRelative(dot, null, null, null);
        h.tvMateria.setCompoundDrawablePadding(dp(h.itemView.getContext(), 8));

        // Porcentaje coloreado por NIVEL (como botón)
        int displayPercent = resolvePercentWithMin(it.getPorcentaje(), it.getNivel());
        h.tvPorcentaje.setText(displayPercent + "%");

        int levelColor = colorForLevel(h.itemView.getContext(), it.getNivel());
        GradientDrawable pill = new GradientDrawable();
        pill.setShape(GradientDrawable.RECTANGLE);
        pill.setColor(levelColor);
        pill.setCornerRadius(dp(h.itemView.getContext(), 14));
        pill.setStroke(dp(h.itemView.getContext(), 1), darken(levelColor, 0.80f));

        h.tvPorcentaje.setBackground(pill);
        h.tvPorcentaje.setTextColor(Color.WHITE);
        int padH = dp(h.itemView.getContext(), 10);
        int padV = dp(h.itemView.getContext(), 6);
        h.tvPorcentaje.setPadding(padH, padV, padH, padV);
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

    // ---------- Helpers de porcentaje/nivel ----------
    private int resolvePercentWithMin(Integer backendPercent, String nivelRaw) {
        int p = backendPercent == null ? 0 : Math.max(0, Math.min(100, backendPercent));
        int min = minForLevel(nivelRaw);
        return Math.max(p, min);
    }

    private int minForLevel(String nivelRaw) {
        if (nivelRaw == null) return 0;
        String n = nivelRaw.trim().toLowerCase(Locale.getDefault());
        if (n.contains("básico") || n.contains("basico")) return 40;
        if (n.contains("intermedio")) return 60;
        if (n.contains("avanzado")) return 80;
        if (n.contains("experto")) return 90;
        return 0;
    }

    private int colorForLevel(Context ctx, String nivelRaw) {
        if (nivelRaw == null) return ContextCompat.getColor(ctx, R.color.level_basic);
        String n = nivelRaw.trim().toLowerCase(Locale.getDefault());
        if (n.contains("básico") || n.contains("basico"))
            return ContextCompat.getColor(ctx, R.color.level_basic);        // Rojo
        if (n.contains("intermedio"))
            return ContextCompat.getColor(ctx, R.color.level_intermediate); // Amarillo/Naranja
        if (n.contains("avanzado"))
            return ContextCompat.getColor(ctx, R.color.level_advanced);     // Verde
        if (n.contains("experto"))
            return ContextCompat.getColor(ctx, R.color.level_expert);       // Azul
        return ContextCompat.getColor(ctx, R.color.level_basic);
    }

    private int colorForSubject(Context ctx, String materiaRaw) {
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

    private int dp(Context ctx, int dp) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, ctx.getResources().getDisplayMetrics()));
    }

    private int darken(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color)   * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color)  * factor);
        return Color.argb(a, Math.max(0,r), Math.max(0,g), Math.max(0,b));
    }

    // ---------- FECHA: formateo robusto a dd/MM/yyyy ----------
    private String formatFecha(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        if (s.isEmpty()) return "";

        // Si viene en epoch (ms)
        try {
            // evita NumberFormatException con valores que no sean puros números
            if (s.matches("^\\d{10,}$")) { // 10+ dígitos
                long epoch = Long.parseLong(s);
                Date d = new Date(s.length() == 10 ? epoch * 1000L : epoch); // si son 10 dígitos, asume segundos
                return outFmt().format(d);
            }
        } catch (Exception ignored) {}

        // Intenta varios formatos comunes
        String[] patterns = new String[] {
                "yyyy-MM-dd'T'HH:mm:ss.SSSX", // ISO con milisegundos y zona (Z o ±hh:mm)
                "yyyy-MM-dd'T'HH:mm:ssX",     // ISO sin milisegundos
                "yyyy-MM-dd HH:mm:ss",        // con espacio
                "yyyy-MM-dd"                  // solo fecha
        };

        for (String p : patterns) {
            try {
                SimpleDateFormat in = new SimpleDateFormat(p, Locale.getDefault());
                // Si el patrón lleva zona (X), parsea en UTC para seguridad
                if (p.contains("X")) in.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date d = in.parse(s);
                if (d != null) return outFmt().format(d);
            } catch (ParseException ignored) {}
        }

        // Si nada funcionó, devuelve como viene
        return s;
    }

    private SimpleDateFormat outFmt() {
        SimpleDateFormat out = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return out;
    }
}
