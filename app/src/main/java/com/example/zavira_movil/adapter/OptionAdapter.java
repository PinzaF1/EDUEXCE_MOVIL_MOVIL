package com.example.zavira_movil.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.AceptarRetoResponse;

import java.util.ArrayList;
import java.util.List;

public class OptionAdapter extends RecyclerView.Adapter<OptionAdapter.VH> {

    public interface OnPick { void onSelect(String key); }

    private final OnPick onPick;
    private List<AceptarRetoResponse.Opcion> items = new ArrayList<>();
    private String selectedKey;

    public OptionAdapter(OnPick onPick) {
        this.onPick = onPick;
    }

    /** Carga las opciones de la pregunta y, si existe, la ya seleccionada */
    public void submit(List<AceptarRetoResponse.Opcion> list, String selected) {
        items = (list != null) ? list : new ArrayList<>();
        selectedKey = selected;
        notifyDataSetChanged();
    }

    /** Devuelve la key actualmente seleccionada (útil si lo necesitas) */
    public String getSelectedKey() { return selectedKey; }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_opcion, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        AceptarRetoResponse.Opcion o = items.get(pos);

        // Limpia listeners para evitar loops al hacer setChecked
        h.radio.setOnCheckedChangeListener(null);

        h.key.setText(o.key);
        h.text.setText(o.text);
        h.radio.setChecked(o.key.equals(selectedKey));

        View.OnClickListener click = v -> select(o.key);
        h.itemView.setOnClickListener(click);
        h.radio.setOnClickListener(click);
    }

    private void select(String key) {
        if (key == null) return;
        // Selección única
        selectedKey = key;
        notifyDataSetChanged();
        if (onPick != null) onPick.onSelect(key);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView key, text;
        RadioButton radio;
        VH(@NonNull View v) {
            super(v);
            key = v.findViewById(R.id.optKey);
            text = v.findViewById(R.id.optText);
            radio = v.findViewById(R.id.optRadio);
        }
    }
}
