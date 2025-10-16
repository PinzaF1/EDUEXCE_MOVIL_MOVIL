package com.example.zavira_movil.detalleprogreso;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;

public class ResumenAdapter extends RecyclerView.Adapter<ResumenAdapter.VH> {

    private final int total, correctas, incorrectas, tiempoSeg;

    public ResumenAdapter(int total, int correctas, int incorrectas, int tiempoSeg) {
        this.total = total; this.correctas = correctas; this.incorrectas = incorrectas; this.tiempoSeg = tiempoSeg;
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_resumen_estad, p, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        if (pos == 0) { h.title.setText("Preguntas respondidas"); h.value.setText(String.valueOf(total)); }
        else if (pos == 1) { h.title.setText("Respuestas correctas"); h.value.setText(String.valueOf(correctas)); }
        else if (pos == 2) { h.title.setText("Respuestas incorrectas"); h.value.setText(String.valueOf(incorrectas)); }
        else { h.title.setText("Tiempo empleado"); h.value.setText(toMin(tiempoSeg)); }
    }

    @Override public int getItemCount() { return 4; }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, value;
        VH(View v){ super(v); title=v.findViewById(R.id.tvTitle); value=v.findViewById(R.id.tvValue); }
    }

    private String toMin(int seg){ int m=seg/60, s=seg%60; return m>0? (m+" min"+(s>0?" "+s+"s":"")): s+"s"; }
}
