package com.example.zavira_movil.HislaConocimiento;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;

import java.util.ArrayList;
import java.util.List;

public class PregListaAdapter extends RecyclerView.Adapter<PregListaAdapter.VH> {

    public interface OnSelect {
        void onSelect(int position, String letra);
    }

    private final List<PregItem> items;
    private final OnSelect callback;

    public PregListaAdapter(List<PregItem> items, OnSelect cb) {
        this.items = (items == null) ? new ArrayList<>() : items;
        this.callback = cb;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_isla_pregunta, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        PregItem it = items.get(pos);
        h.tvIndex.setText(it.orden + ".");
        String titulo = (it.area == null ? "" : it.area) + (it.subtema == null || it.subtema.isEmpty() ? "" : " • " + it.subtema);
        h.tvTitulo.setText(titulo);
        h.tvEnun.setText(it.enunciado == null ? "" : it.enunciado);

        // Textos opciones (con prefijo A/B/C/D)
        h.btnA.setText(pref(0, it.opciones));
        h.btnB.setText(pref(1, it.opciones));
        h.btnC.setText(pref(2, it.opciones));
        h.btnD.setText(pref(3, it.opciones));

        // Estado seleccionado
        marcar(h, it.seleccion);

        View.OnClickListener l = v -> {
            String letra = "A";
            if (v == h.btnB) letra = "B";
            else if (v == h.btnC) letra = "C";
            else if (v == h.btnD) letra = "D";
            it.seleccion = letra;
            marcar(h, letra);
            if (callback != null) callback.onSelect(pos, letra);
        };

        h.btnA.setOnClickListener(l);
        h.btnB.setOnClickListener(l);
        h.btnC.setOnClickListener(l);
        h.btnD.setOnClickListener(l);
    }

    @Override public int getItemCount() { return items.size(); }

    private void marcar(VH h, String letra) {
        h.btnA.setSelected("A".equals(letra));
        h.btnB.setSelected("B".equals(letra));
        h.btnC.setSelected("C".equals(letra));
        h.btnD.setSelected("D".equals(letra));
    }

    private String pref(int i, List<String> ops) {
        String t = (ops != null && ops.size() > i) ? String.valueOf(ops.get(i)) : "";
        String[] abcd = {"A", "B", "C", "D"};
        if (t.matches("^\\s*[A-Da-d][\\.)]\\s+.*")) {
            // ya viene con prefijo, no dupliques
            return t.trim();
        }
        return abcd[i] + ". " + t;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvIndex, tvTitulo, tvEnun;
        Button btnA, btnB, btnC, btnD;
        VH(@NonNull View v) {
            super(v);
            tvIndex = v.findViewById(R.id.tvIndex);
            tvTitulo= v.findViewById(R.id.tvTitulo);
            tvEnun  = v.findViewById(R.id.tvEnunciado);
            btnA    = v.findViewById(R.id.btnOp1);
            btnB    = v.findViewById(R.id.btnOp2);
            btnC    = v.findViewById(R.id.btnOp3);
            btnD    = v.findViewById(R.id.btnOp4);
        }
    }
}
