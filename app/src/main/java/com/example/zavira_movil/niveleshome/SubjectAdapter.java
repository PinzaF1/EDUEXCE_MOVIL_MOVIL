package com.example.zavira_movil.niveleshome;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseBooleanArray;

import com.example.zavira_movil.Home.IslaSimulacroActivity;
import com.example.zavira_movil.Home.LevelMiniAdapter;
import com.example.zavira_movil.R;
import com.example.zavira_movil.model.Subject;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import android.view.ViewGroup.LayoutParams;

/**
 * 0: Isla del Conocimiento (no expandible)
 * 1..N: Materias (expandibles)
 */
public class SubjectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnStartActivity { void launch(Intent i); }

    private static final int VIEW_TYPE_HEADER   = 0;
    private static final int VIEW_TYPE_SUBJECT  = 1;

    private final List<Subject> data;
    private final SparseBooleanArray expanded = new SparseBooleanArray();
    private final OnStartActivity onStartActivity;

    public SubjectAdapter(List<Subject> data, OnStartActivity onStartActivity) {
        this.data = (data == null) ? new ArrayList<>() : data;
        this.onStartActivity = onStartActivity;
    }

    @Override public int getItemViewType(int position) {
        return (position == 0) ? VIEW_TYPE_HEADER : VIEW_TYPE_SUBJECT;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_HEADER) {
            View v = inf.inflate(R.layout.item_simulacro_header, parent, false);
            return new HeaderVH(v);
        } else {
            View v = inf.inflate(R.layout.item_subject_card, parent, false);
            return new SubjectVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_HEADER) {
            bindHeader((HeaderVH) holder);
        } else {
            bindSubject((SubjectVH) holder, position - 1); // desplazamiento por el header
        }
    }

    @Override public int getItemCount() {
        return (data == null ? 0 : data.size()) + 1; // +1 por la isla
    }

    // ---------- Header (Isla del Conocimiento) ----------
    private void bindHeader(@NonNull HeaderVH h) {
        Context ctx = h.itemView.getContext();

        // MISMO borde que las otras tarjetas (gris), sin morado
        h.card.setStrokeWidth(3);
        h.card.setStrokeColor(Color.parseColor("#E0E3EA"));
        h.card.setCardBackgroundColor(Color.TRANSPARENT);
        h.card.setRippleColor(ColorStateList.valueOf(Color.parseColor("#1F29371A"))); // sutil

        // Click → abre simulacro
        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(ctx, IslaSimulacroActivity.class);
            ctx.startActivity(i);
        });
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        HeaderVH(@NonNull View v) {
            super(v);
            card = (MaterialCardView) v; // root del header es un MaterialCardView
        }
    }

    // ---------- Subject (materias) ----------
    private void bindSubject(@NonNull SubjectVH h, int subjectIndex) {
        Subject s = data.get(subjectIndex);
        Context ctx = h.itemView.getContext();

        // Ícono + Título (abajo de la imagen)
        h.tvTitle.setText(s.title);

        // Prioriza icono por título; si no hay match, usa el del modelo
        int iconRes = iconFor(s.title);
        if (iconRes == 0 && s.iconRes != 0) iconRes = s.iconRes;
        if (iconRes != 0) h.ivIcon.setImageResource(iconRes);
        h.ivIcon.setImageTintList(null);
        h.ivIcon.setVisibility(View.VISIBLE);
        h.ivIcon.setAlpha(1f);

        // Fondo: imagen completa por área (arriba) con ImageView + centerCrop
        @DrawableRes int bgRes = bgFor(s.title);
        if (bgRes != 0) setHeaderImage(h.header, bgRes);

        // Estado expandido/plegado
        int adapterPos = h.getBindingAdapterPosition();
        boolean isExpanded = expanded.get(adapterPos, false);
        h.rvInner.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        h.ivArrow.setRotation(isExpanded ? 90f : 0f);

        // Borde + ripple (color del área). Plegado = gris, expandido = color área
        int areaColor = colorFor(s.title);
        int grayLocked = Color.parseColor("#B6B9C2");
        h.card.setStrokeWidth(3);
        h.card.setStrokeColor(isExpanded ? areaColor : grayLocked);
        h.card.setRippleColor(ColorStateList.valueOf(areaColor));

        // Progreso (si tu modelo lo maneja)
        int percent = s.percent(); // o 0 si no aplica
        h.progress.setProgress(percent);

        // Toggle expand/collapse (header, fila de info y flecha)
        View.OnClickListener toggle = v -> {
            int posNow = h.getBindingAdapterPosition();
            if (posNow == RecyclerView.NO_POSITION) return;
            boolean now = !expanded.get(posNow, false);
            expanded.put(posNow, now);
            h.rvInner.setVisibility(now ? View.VISIBLE : View.GONE);
            h.ivArrow.animate().rotation(now ? 90f : 0f).setDuration(150).start();
            h.card.setStrokeColor(now ? areaColor : grayLocked);
        };
        h.ivArrow.setOnClickListener(toggle);
        h.rowHeader.setOnClickListener(toggle); // header (imagen)
        h.rowInfo.setOnClickListener(toggle);   // fila de ícono+nombre

        // Click tarjeta completa → detalle (si aplica en tu flujo)
        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(ctx, SubjectDetailActivity.class);
            i.putExtra("subject", s);
            onStartActivity.launch(i);
        });

        // Recycler interno (niveles)
        if (h.rvInner.getLayoutManager() == null) {
            h.rvInner.setLayoutManager(new LinearLayoutManager(ctx));
        }
        h.rvInner.setAdapter(new LevelMiniAdapter(s.levels, s, onStartActivity));
    }

    // ---- Helper: fija un ImageView centerCrop dentro de flHeader ----
    private void setHeaderImage(View flHeader, @DrawableRes int resId) {
        if (resId == 0 || flHeader == null) return;

        final int TAG_ID = R.id.tag_header_image; // define este id en res/values/ids.xml
        View tagged = (flHeader.getTag(TAG_ID) instanceof View) ? (View) flHeader.getTag(TAG_ID) : null;

        ImageView iv;
        if (tagged instanceof ImageView) {
            iv = (ImageView) tagged;
        } else {
            iv = new ImageView(flHeader.getContext());
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
            );
            if (flHeader instanceof FrameLayout) {
                ((FrameLayout) flHeader).addView(iv, 0, lp); // al fondo del header
            }
            flHeader.setTag(TAG_ID, iv);
        }
        iv.setImageResource(resId);
    }

    // ---- Imagen de fondo por área (5 materias) ----
    @DrawableRes
    private int bgFor(String title) {
        if (title == null) return 0;
        String t = title.toLowerCase().trim();

        if (t.contains("matem"))                                   return R.drawable.fondomatematicas;
        if (t.contains("lectura") || t.contains("lenguaje") || t.contains("espa"))
            return R.drawable.fondiespa;
        if (t.contains("social") || t.contains("ciudad"))          return R.drawable.fondosociales;
        if (t.contains("cien") || t.contains("biolo") || t.contains("fis") || t.contains("quim"))
            return R.drawable.fondonaturales;
        if (t.contains("ingl"))                                    return R.drawable.fondoingles;

        return R.drawable.fondiespa; // fallback
    }

    // ---- Color por área (para borde y ripple) ----
    private int colorFor(String title) {
        if (title == null) return Color.parseColor("#B6B9C2");
        String t = title.toLowerCase().trim();

        if (t.contains("matem"))                                   return Color.parseColor("#E53935"); // rojo
        if (t.contains("lectura") || t.contains("lenguaje") || t.contains("espa"))
            return Color.parseColor("#1E88E5"); // azul
        if (t.contains("social") || t.contains("ciudad"))          return Color.parseColor("#FB8C00"); // naranja
        if (t.contains("cien") || t.contains("biolo") || t.contains("fis") || t.contains("quim"))
            return Color.parseColor("#43A047"); // verde
        if (t.contains("ingl"))                                    return Color.parseColor("#8E24AA"); // morado

        return Color.parseColor("#B6B9C2");
    }

    // ---- Icono por área (usa tus drawables .png) ----
    @DrawableRes
    private int iconFor(String title) {
        if (title == null) return 0;
        String t = title.toLowerCase().trim();

        // NOMBRES EXACTOS en res/drawable (sin la extensión .png)
        if (t.contains("matem"))                                   return R.drawable.calculator; // calculator.png
        if (t.contains("lectura") || t.contains("lenguaje") || t.contains("espa"))
            return R.drawable.lectu;      // lectu.png
        if (t.contains("social") || t.contains("ciudad"))          return R.drawable.sociale;   // sociales.png
        if (t.contains("cien") || t.contains("biolo") || t.contains("fis") || t.contains("quim")
                || t.contains("naturales"))                        return R.drawable.naturales;  // naturales.png
        if (t.contains("ingl"))                                    return R.drawable.english;     // ingles.png

        return 0;
    }

    // ---- Color del header de la isla ----
    private int headerColor() {
        return Color.TRANSPARENT; // sin morado
    }

    static class SubjectVH extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final View header;         // flHeader (imagen arriba)
        final View rowHeader;      // usamos flHeader como "click area" para toggle
        final View rowInfo;        // fila de ícono + nombre (debajo de la imagen)
        final ImageView ivIcon, ivArrow;
        final TextView tvTitle;
        final ProgressBar progress;
        final RecyclerView rvInner;

        SubjectVH(@NonNull View v) {
            super(v);
            card      = (MaterialCardView) v; // root del item
            header    = v.findViewById(R.id.flHeader);
            rowHeader = v.findViewById(R.id.flHeader);
            rowInfo   = v.findViewById(R.id.rowInfo);
            ivIcon    = v.findViewById(R.id.ivIcon);
            ivArrow   = v.findViewById(R.id.ivArrow);
            tvTitle   = v.findViewById(R.id.tvTitle);
            progress  = v.findViewById(R.id.progress);
            rvInner   = v.findViewById(R.id.rvSubtopics);
        }
    }
}
