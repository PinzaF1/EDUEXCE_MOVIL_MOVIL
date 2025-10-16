package com.example.zavira_movil.progreso;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // ⬅️ para ivFlecha
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
    // callback para abrir detalle
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

        // Puntico por materia
        int subjectColor = colorForSubject(h.itemView.getContext(), it.getMateria());
        GradientDrawable dot = new GradientDrawable();
        dot.setShape(GradientDrawable.OVAL);
        dot.setColor(subjectColor);
        int dotSize = dp(h.itemView.getContext(), 10);
        dot.setSize(dotSize, dotSize);
        dot.setBounds(0, 0, dotSize, dotSize);
        h.tvMateria.setCompoundDrawablesRelative(dot, null, null, null);
        h.tvMateria.setCompoundDrawablePadding(dp(h.itemView.getContext(), 8));

        // Porcentaje “pill” (SOLO display)
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

        // 🚫 El porcentaje ya NO tiene acción
        h.tvPorcentaje.setOnClickListener(null);
        h.tvPorcentaje.setClickable(false);

        // ✅ La FLECHA ejecuta la MISMA acción (abrir detalle)
        if (h.ivFlecha != null) {
            h.ivFlecha.setOnClickListener(v -> {
                int p = h.getBindingAdapterPosition();
                if (p != RecyclerView.NO_POSITION && onItemClick != null) {
                    onItemClick.onClick(data.get(p));
                }
            });
        }

        // 👍 Mantengo el click en toda la celda (como ya está funcionando)
        h.itemView.setOnClickListener(v -> {
            int p = h.getBindingAdapterPosition();
            if (p != RecyclerView.NO_POSITION && onItemClick != null) {
                onItemClick.onClick(data.get(p));
            }
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvMateria, tvPorcentaje, tvNivel, tvFecha;
        ImageView ivFlecha; // ⬅️ referencia a la flecha (ImageView)

        VH(@NonNull View v) {
            super(v);
            tvMateria    = v.findViewById(R.id.tvMateria);
            tvPorcentaje = v.findViewById(R.id.tvPorcentaje);
            tvNivel      = v.findViewById(R.id.tvNivel);
            tvFecha      = v.findViewById(R.id.tvFecha);
            ivFlecha     = v.findViewById(R.id.ivFlecha); // ⬅️ debe existir en item_historial.xml
        }
    }

    // ---------- Helpers ----------
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
            return ContextCompat.getColor(ctx, R.color.level_basic);
        if (n.contains("intermedio"))
            return ContextCompat.getColor(ctx, R.color.level_intermediate);
        if (n.contains("avanzado"))
            return ContextCompat.getColor(ctx, R.color.level_advanced);
        if (n.contains("experto"))
            return ContextCompat.getColor(ctx, R.color.level_expert);
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
