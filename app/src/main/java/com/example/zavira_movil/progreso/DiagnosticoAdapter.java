package com.example.zavira_movil.progreso;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.google.android.material.card.MaterialCardView;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DiagnosticoAdapter extends RecyclerView.Adapter<DiagnosticoAdapter.VH> {

    public static class AreaItem {
        public final String nombre;
        public final int inicial;
        public final int actual;
        public final int delta; // reservado

        public AreaItem(String n, int i, int a, int d) {
            nombre = n; inicial = i; actual = a; delta = d;
        }
    }

    private final List<AreaItem> data = new ArrayList<>();

    public void setData(List<AreaItem> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int vType) {
        View v = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_diagnostico_area, p, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        AreaItem it = data.get(pos);
        Context ctx = h.itemView.getContext();

        // Valores clamp
        int ini = clamp(it.inicial);
        int act = clamp(it.actual);

        // Textos
        h.tvAreaChip.setText(it.nombre);
        h.tvInicialValor.setText(ini + " %");
        h.tvActualValor.setText(act + " %");

        // Colores por área
        int areaColor = colorForArea(it.nombre, ctx);
        int badgeSoft = withAlpha(areaColor, 0.15f);

        // Chip del título
        ViewCompat.setBackgroundTintList(h.tvAreaChip, ColorStateList.valueOf(areaColor));

        // Borde de la tarjeta
        h.cardArea.setStrokeColor(areaColor);
        h.cardArea.setStrokeWidth(dp(ctx, 2)); // por si en XML está en 0

        // Badge + icono
        h.badgeIcon.setCardBackgroundColor(badgeSoft);
        h.ivAreaIcon.setImageResource(drawableForArea(it.nombre));

        // Barras
        int track = ContextCompat.getColor(ctx, R.color.progress_track_light);
        int initialProg = ContextCompat.getColor(ctx, R.color.text_muted);

        // Inicial -> gris
        h.pbInicial.setProgress(ini);
        h.pbInicial.setProgressTintList(ColorStateList.valueOf(initialProg));
        h.pbInicial.setProgressBackgroundTintList(ColorStateList.valueOf(track));

        // Actual -> color del área
        h.pbActual.setProgress(act);
        h.pbActual.setProgressTintList(ColorStateList.valueOf(areaColor));
        h.pbActual.setProgressBackgroundTintList(ColorStateList.valueOf(track));

        // tvCompare existe pero no se usa
        h.tvCompare.setVisibility(View.GONE);
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        MaterialCardView cardArea, badgeIcon;
        TextView tvAreaChip, tvInicialValor, tvActualValor, tvCompare;
        ProgressBar pbInicial, pbActual;
        ImageView ivAreaIcon;

        VH(@NonNull View v) {
            super(v);
            cardArea      = v.findViewById(R.id.cardArea);
            badgeIcon     = v.findViewById(R.id.badgeIcon);
            ivAreaIcon    = v.findViewById(R.id.ivAreaIcon);

            tvAreaChip     = v.findViewById(R.id.tvAreaChip);
            tvInicialValor = v.findViewById(R.id.tvInicialValor);
            tvActualValor  = v.findViewById(R.id.tvActualValor);
            tvCompare      = v.findViewById(R.id.tvCompare);

            pbInicial      = v.findViewById(R.id.pbInicial);
            pbActual       = v.findViewById(R.id.pbActual);
        }
    }

    // -------- helpers --------
    private int clamp(int x) { return Math.max(0, Math.min(100, x)); }

    private int withAlpha(int color, float alpha) {
        int a = Math.round(255 * alpha);
        return Color.argb(a, Color.red(color), Color.green(color), Color.blue(color));
    }

    private int dp(Context c, int v) {
        return Math.round(c.getResources().getDisplayMetrics().density * v);
    }

    private String norm(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+","")
                .toLowerCase(Locale.ROOT);
    }

    private int colorForArea(String nombre, Context c) {
        String a = norm(nombre);
        
        // Isla del Conocimiento / Todas las áreas - Amarillo
        if (a.contains("conocimiento") || a.contains("isla") || 
            (a.contains("todas") && (a.contains("area") || a.contains("área")))) {
            return ContextCompat.getColor(c, R.color.area_conocimiento);
        }
        
        if (a.startsWith("mate")) return ContextCompat.getColor(c, R.color.area_matematicas);
        if (a.startsWith("leng") || a.startsWith("lect")) return ContextCompat.getColor(c, R.color.area_lenguaje);
        if (a.startsWith("cien")) return ContextCompat.getColor(c, R.color.area_ciencias);
        if (a.startsWith("soci")) return ContextCompat.getColor(c, R.color.area_sociales);
        if (a.startsWith("ingl") || a.startsWith("eng")) return ContextCompat.getColor(c, R.color.area_ingles);
        return ContextCompat.getColor(c, R.color.subject_default);
    }

    private int drawableForArea(String nombre) {
        String a = norm(nombre);
        if (a.startsWith("mate")) return R.drawable.calculator;
        if (a.startsWith("leng") || a.startsWith("lect")) return R.drawable.lectu;
        if (a.startsWith("cien")) return R.drawable.naturales;
        if (a.startsWith("soci")) return R.drawable.sociale;
        if (a.startsWith("ingl") || a.startsWith("eng")) return R.drawable.english;
        return R.drawable.calculator; // fallback
    }
}
