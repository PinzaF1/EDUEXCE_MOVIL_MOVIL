package com.example.zavira_movil.niveleshome;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseBooleanArray;

import com.example.zavira_movil.Home.IslaSimulacroActivity;
import com.example.zavira_movil.Home.LevelMiniAdapter;
import com.example.zavira_movil.R;
import com.example.zavira_movil.model.Subject;

import java.util.ArrayList;
import java.util.List;

/**
 * Primer ítem: tarjeta "Isla del Conocimiento" (no expandible).
 * Siguientes ítems: tus materias normales con expansión a niveles.
 */
public class SubjectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnStartActivity { void launch(Intent i); }

    private static final int VIEW_TYPE_HEADER   = 0; // Isla del Conocimiento
    private static final int VIEW_TYPE_SUBJECT  = 1; // Materias normales

    private final List<Subject> data;
    private final SparseBooleanArray expanded = new SparseBooleanArray(); // guarda por posición de adapter
    private final OnStartActivity onStartActivity;

    public SubjectAdapter(List<Subject> data, OnStartActivity onStartActivity) {
        this.data = (data == null) ? new ArrayList<>() : data;
        this.onStartActivity = onStartActivity;
    }

    // ---------- ViewTypes ----------
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
            bindSubject((SubjectVH) holder, position - 1); // <- OJO: desplazamiento
        }
    }

    @Override public int getItemCount() {
        return (data == null ? 0 : data.size()) + 1; // +1 por el header
    }

    // ---------- Header (Isla del Conocimiento) ----------
    private void bindHeader(@NonNull HeaderVH h) {
        h.itemView.setOnClickListener(v -> {
            Context ctx = v.getContext();
            Intent i = new Intent(ctx, IslaSimulacroActivity.class);
            ctx.startActivity(i);
        });
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        HeaderVH(@NonNull View v) { super(v); }
    }

    // ---------- Subject (tu código original con mínimo ajuste) ----------
    private void bindSubject(@NonNull SubjectVH h, int subjectIndex) {
        Subject s = data.get(subjectIndex);
        Context ctx = h.itemView.getContext();

        // Header e info principal
        h.tvTitle.setText(s.title);
        h.ivIcon.setImageResource(s.iconRes);
        h.tvModules.setText(s.done + "/" + s.total + " niveles");
        h.tvPercent.setText(s.percent() + "%");
        h.progress.setProgress(s.percent());
        if (s.headerDrawableRes != 0) {
            h.header.setBackgroundResource(s.headerDrawableRes);
        }

        // Recycler interno con los 5 niveles
        if (h.rvInner.getLayoutManager() == null) {
            h.rvInner.setLayoutManager(new LinearLayoutManager(ctx));
        }
        h.rvInner.setAdapter(new LevelMiniAdapter(s.levels, s, onStartActivity));

        // Expandir/colapsar (guardamos con la POSICIÓN REAL en el adapter)
        int adapterPos = h.getBindingAdapterPosition();
        boolean isExpanded = expanded.get(adapterPos, false);
        h.rvInner.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        h.ivArrow.setRotation(isExpanded ? 90f : 0f);

        View.OnClickListener toggle = v -> {
            int posNow = h.getBindingAdapterPosition();
            if (posNow == RecyclerView.NO_POSITION) return;
            boolean now = !expanded.get(posNow, false);
            expanded.put(posNow, now);
            h.rvInner.setVisibility(now ? View.VISIBLE : View.GONE);
            h.ivArrow.animate().rotation(now ? 90f : 0f).setDuration(150).start();
        };

        h.ivArrow.setOnClickListener(toggle);
        h.rowHeader.setOnClickListener(toggle);

        // Tocar toda la tarjeta abre detalle
        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(ctx, SubjectDetailActivity.class);
            i.putExtra("subject", s);
            onStartActivity.launch(i);
        });
    }

    static class SubjectVH extends RecyclerView.ViewHolder {
        View header, rowHeader;
        ImageView ivIcon, ivArrow;
        TextView tvTitle, tvModules, tvPercent;
        ProgressBar progress;
        RecyclerView rvInner;

        SubjectVH(@NonNull View v) {
            super(v);
            header     = v.findViewById(R.id.flHeader);
            rowHeader  = v.findViewById(R.id.flHeader);
            ivIcon     = v.findViewById(R.id.ivIcon);
            ivArrow    = v.findViewById(R.id.ivArrow);
            tvTitle    = v.findViewById(R.id.tvTitle);
            tvModules  = v.findViewById(R.id.tvModules);
            tvPercent  = v.findViewById(R.id.tvPercent);
            progress   = v.findViewById(R.id.progress);
            rvInner    = v.findViewById(R.id.rvSubtopics);
        }
    }
}
