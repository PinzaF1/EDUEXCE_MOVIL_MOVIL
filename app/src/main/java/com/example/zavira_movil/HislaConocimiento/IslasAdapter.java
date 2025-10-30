package com.example.zavira_movil.HislaConocimiento;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;

import java.util.Collections;
import java.util.List;

public class IslasAdapter extends RecyclerView.Adapter<IslasAdapter.VH> {

    private final List<IslaSimulacroResponse.PreguntaDto> items;

    public IslasAdapter(List<IslaSimulacroResponse.PreguntaDto> items) {
        this.items = (items == null) ? Collections.emptyList() : items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_isla_pregunta, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        IslaSimulacroResponse.PreguntaDto p = items.get(position);

        String area = p.getArea() != null ? p.getArea() : "";
        String sub  = p.getSubtema() != null ? p.getSubtema() : "";
        if (h.tvTitulo != null) {
            h.tvTitulo.setText(sub.isEmpty() ? area : area + " • " + sub);
        }

        if (h.tvIndex != null) {
            h.tvIndex.setText(String.valueOf(position + 1) + ".");
        }

        if (h.tvEnunciado != null) {
            h.tvEnunciado.setText(p.getEnunciado() != null ? p.getEnunciado() : "");
        }

        List<String> ops = p.getOpciones();
        if (h.btnOp1 != null) h.btnOp1.setText(ops != null && ops.size() > 0 ? ops.get(0) : "");
        if (h.btnOp2 != null) h.btnOp2.setText(ops != null && ops.size() > 1 ? ops.get(1) : "");
        if (h.btnOp3 != null) h.btnOp3.setText(ops != null && ops.size() > 2 ? ops.get(2) : "");
        if (h.btnOp4 != null) h.btnOp4.setText(ops != null && ops.size() > 3 ? ops.get(3) : "");

        // Si solo quieres mostrar (sin selección), deshabilita los clicks:
        if (h.btnOp1 != null) h.btnOp1.setOnClickListener(null);
        if (h.btnOp2 != null) h.btnOp2.setOnClickListener(null);
        if (h.btnOp3 != null) h.btnOp3.setOnClickListener(null);
        if (h.btnOp4 != null) h.btnOp4.setOnClickListener(null);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvIndex, tvTitulo, tvEnunciado;
        Button btnOp1, btnOp2, btnOp3, btnOp4;

        VH(@NonNull View v) {
            super(v);
            tvIndex     = v.findViewById(R.id.tvIndex);      // opcional en tu layout
            tvTitulo    = v.findViewById(R.id.tvTitulo);
            tvEnunciado = v.findViewById(R.id.tvEnunciado);
            btnOp1      = v.findViewById(R.id.btnOp1);
            btnOp2      = v.findViewById(R.id.btnOp2);
            btnOp3      = v.findViewById(R.id.btnOp3);
            btnOp4      = v.findViewById(R.id.btnOp4);
        }
    }
}
