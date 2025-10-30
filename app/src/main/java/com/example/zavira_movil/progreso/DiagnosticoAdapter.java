package com.example.zavira_movil.progreso;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.zavira_movil.R;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DiagnosticoAdapter extends RecyclerView.Adapter<DiagnosticoAdapter.VH> {

    public static class AreaItem {
        public final String nombre;
        public final int inicial;
        public final int actual;
        public final int delta; // por si luego lo quieres

        public AreaItem(String n, int i, int a, int d) { nombre=n; inicial=i; actual=a; delta=d; }
    }

    private final List<AreaItem> data = new ArrayList<>();
    public void setData(List<AreaItem> items){ data.clear(); if(items!=null) data.addAll(items); notifyDataSetChanged(); }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vType) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_diagnostico_area, p, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        AreaItem it = data.get(pos);

        h.tvAreaChip.setText(it.nombre);
        h.tvInicialValor.setText(Math.max(0, Math.min(100, it.inicial)) + "%");
        h.tvActualValor.setText(Math.max(0, Math.min(100, it.actual)) + "%");

        int color = colorForArea(it.nombre, h.itemView.getContext());
        ViewCompat.setBackgroundTintList(h.tvAreaChip, ColorStateList.valueOf(color));
    }

    @Override public int getItemCount(){ return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvAreaChip, tvInicialValor, tvActualValor;
        VH(@NonNull View v){
            super(v);
            tvAreaChip    = v.findViewById(R.id.tvAreaChip);
            tvInicialValor= v.findViewById(R.id.tvInicialValor);
            tvActualValor = v.findViewById(R.id.tvActualValor);
        }
    }

    private int colorForArea(String nombre, Context c){
        String a = nombre==null? "" : Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+","").toLowerCase(Locale.ROOT);

        if (a.startsWith("mate")) return ContextCompat.getColor(c, R.color.area_matematicas);
        if (a.startsWith("leng") || a.startsWith("lect")) return ContextCompat.getColor(c, R.color.area_lenguaje);
        if (a.startsWith("cien")) return ContextCompat.getColor(c, R.color.area_ciencias);
        if (a.startsWith("soci")) return ContextCompat.getColor(c, R.color.area_sociales);
        if (a.startsWith("ingl")) return ContextCompat.getColor(c, R.color.area_ingles);
        return ContextCompat.getColor(c, R.color.area_matematicas);
    }
}
