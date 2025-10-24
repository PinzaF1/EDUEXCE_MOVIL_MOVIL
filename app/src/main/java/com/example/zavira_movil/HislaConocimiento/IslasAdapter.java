package com.example.zavira_movil.HislaConocimiento;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        h.tvTitulo.setText(sub.isEmpty() ? area : area + " • " + sub);

        h.tvEnunciado.setText(p.getEnunciado() != null ? p.getEnunciado() : "");

        List<String> ops = p.getOpciones();
        h.op1.setText(ops != null && ops.size() > 0 ? ops.get(0) : "");
        h.op2.setText(ops != null && ops.size() > 1 ? ops.get(1) : "");
        h.op3.setText(ops != null && ops.size() > 2 ? ops.get(2) : "");
        h.op4.setText(ops != null && ops.size() > 3 ? ops.get(3) : "");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvEnunciado, op1, op2, op3, op4;

        VH(@NonNull View v) {
            super(v);
            tvTitulo    = v.findViewById(R.id.tvTitulo);
            tvEnunciado = v.findViewById(R.id.tvEnunciado);
            op1         = v.findViewById(R.id.tvOp1);
            op2         = v.findViewById(R.id.tvOp2);
            op3         = v.findViewById(R.id.tvOp3);
            op4         = v.findViewById(R.id.tvOp4);
        }
    }
}
