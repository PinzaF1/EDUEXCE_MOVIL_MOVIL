package com.example.zavira_movil.progreso;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;

public class DiagnosticoHeaderAdapter extends RecyclerView.Adapter<DiagnosticoHeaderAdapter.VH> {

    private boolean tieneDiag = false;
    private Integer porcentajeGeneral = 0;
    private String fecha = null;

    public void setData(boolean tiene, Integer generalActual, String fechaOpcional) {
        this.tieneDiag = tiene;
        this.porcentajeGeneral = generalActual == null ? 0 : Math.max(0, Math.min(100, generalActual));
        this.fecha = fechaOpcional;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View v1 = LayoutInflater.from(p.getContext()).inflate(R.layout.item_diagnostico_header, p, false);
        return new VH(v1);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        h.tvHeaderTitulo.setText("Diagnóstico Inicial");
        h.tvHeaderNumero.setText(porcentajeGeneral + "%");
        h.tvHeaderEtiqueta.setText("General");

        if (fecha == null || fecha.trim().isEmpty()) {
            h.tvHeaderFecha.setVisibility(View.GONE);
        } else {
            h.tvHeaderFecha.setVisibility(View.VISIBLE);
            h.tvHeaderFecha.setText("Realizado el " + fecha);
        }
    }

    @Override public int getItemCount() { return 1; }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvHeaderTitulo, tvHeaderFecha, tvHeaderNumero, tvHeaderEtiqueta;
        VH(@NonNull View v){
            super(v);
            tvHeaderTitulo   = v.findViewById(R.id.tvHeaderTitulo);
            tvHeaderFecha    = v.findViewById(R.id.tvHeaderFecha);
            tvHeaderNumero   = v.findViewById(R.id.tvHeaderNumero);
            tvHeaderEtiqueta = v.findViewById(R.id.tvHeaderEtiqueta);
        }
    }
}
