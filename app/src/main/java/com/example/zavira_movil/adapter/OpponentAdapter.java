package com.example.zavira_movil.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.OpponentItem;

import java.util.ArrayList;
import java.util.List;

public class OpponentAdapter extends RecyclerView.Adapter<OpponentAdapter.VH> {

    public interface OnClick { void onSelect(OpponentItem op); }

    private final OnClick onClick;
    private final List<OpponentItem> items = new ArrayList<>();
    private String selectedId = null;

    public OpponentAdapter(OnClick onClick) { this.onClick = onClick; }

    public void setData(List<OpponentItem> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public void setSelectedId(String id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_oponent, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        OpponentItem o = items.get(pos);

        h.name.setText(o.getNombre());
        h.level.setText(o.getNivel());
        h.wins.setText(String.valueOf(o.getWins()));
        h.dot.setImageResource(o.isOnline()
                ? android.R.drawable.presence_online
                : android.R.drawable.presence_invisible);

        // --- color por nivel (franja izquierda) ---
        @ColorRes int stripeColor = colorForLevel(o.getNivel());
        h.levelStripe.setBackgroundColor(
                ContextCompat.getColor(h.itemView.getContext(), stripeColor)
        );

        // --- estado de selección (borde y fondo) ---
        boolean isSelected = o.getId() != null && o.getId().equals(selectedId);
        h.itemView.setBackgroundResource(isSelected
                ? R.drawable.card_selected_soft
                : R.drawable.card_normal
        );

        h.itemView.setOnClickListener(v -> {
            selectedId = o.getId();
            notifyDataSetChanged();
            onClick.onSelect(o);
        });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        View levelStripe;
        TextView name, level, wins;
        ImageView dot;
        VH(View v) {
            super(v);
            levelStripe = v.findViewById(R.id.levelStripe);
            name  = v.findViewById(R.id.name);
            level = v.findViewById(R.id.level);
            wins  = v.findViewById(R.id.count);
            dot   = v.findViewById(R.id.dot);
        }
    }

    // Mapea el texto del nivel a un color
    @ColorRes
    private int colorForLevel(String nivelRaw) {
        if (nivelRaw == null) return R.color.level_basic;
        String n = nivelRaw.trim().toLowerCase();
        // soporta “básico / basico”, “intermedio”, “avanzado”, “experto”
        if (n.startsWith("básico") || n.startsWith("basico")) return R.color.level_basic;
        if (n.startsWith("inter"))                            return R.color.level_intermediate;
        if (n.startsWith("avanz"))                            return R.color.level_advanced;
        if (n.startsWith("exper"))                            return R.color.level_expert;
        return R.color.level_basic; // fallback
    }
}
