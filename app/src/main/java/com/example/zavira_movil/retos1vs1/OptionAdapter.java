package com.example.zavira_movil.retos1vs1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;

import java.util.ArrayList;
import java.util.List;

public class OptionAdapter extends RecyclerView.Adapter<OptionAdapter.VH> {

    public interface OnPick { void onSelect(String key); }

    private final OnPick onPick;
    private List<String> items = new ArrayList<>(); // strings puro
    private String selectedKey;

    public OptionAdapter(OnPick onPick) { this.onPick = onPick; }

    // recibe strings y la selección actual (A,B,C,D)
    public void submit(List<String> list, String selected) {
        items = (list != null) ? list : new ArrayList<>();
        selectedKey = selected;
        notifyDataSetChanged();
    }

    public String getSelectedKey() { return selectedKey; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_opcion, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        // key A/B/C/D...
        String key = String.valueOf((char)('A' + pos));
        String text = items.get(pos);
        boolean isSelected = key.equals(selectedKey);

        h.radio.setOnCheckedChangeListener(null);
        h.key.setText(key + ".");
        h.text.setText(text != null ? text : "");
        h.radio.setChecked(isSelected);

        // Aplicar estilos según si está seleccionada o no
        if (isSelected) {
            // Fondo gris claro, texto gris oscuro, mostrar checkmark
            h.itemView.setSelected(true);
            h.key.setTextColor(0xFF6B7280); // Gris oscuro
            h.text.setTextColor(0xFF6B7280); // Gris oscuro
            h.checkmark.setVisibility(View.VISIBLE);
        } else {
            // Fondo gris claro, texto gris oscuro, ocultar checkmark
            h.itemView.setSelected(false);
            h.key.setTextColor(0xFF666666); // Gris oscuro
            h.text.setTextColor(0xFF666666); // Gris oscuro
            h.checkmark.setVisibility(View.GONE);
        }

        View.OnClickListener click = v -> select(key);
        h.itemView.setOnClickListener(click);
        h.radio.setOnClickListener(click);
    }

    private void select(String key) {
        if (key == null) return;
        selectedKey = key;
        notifyDataSetChanged();
        if (onPick != null) onPick.onSelect(key);
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView key, text;
        RadioButton radio;
        ImageView checkmark;
        VH(@NonNull View v) {
            super(v);
            key = v.findViewById(R.id.optKey);
            text = v.findViewById(R.id.optText);
            radio = v.findViewById(R.id.optRadio);
            checkmark = v.findViewById(R.id.optCheckmark);
        }
    }
}
