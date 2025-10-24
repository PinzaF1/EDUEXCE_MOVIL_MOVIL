package com.example.zavira_movil.detalleprogreso;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FragmentDetalleAnalisis extends Fragment {

    private RecyclerView rv;
    private ProgresoDetalleResponse data;

    public void setData(ProgresoDetalleResponse data) {
        this.data = data;
        if (getView()!=null) bind();
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = inf.inflate(R.layout.fragment_detalle_analisis, c, false);
        rv = v.findViewById(R.id.rvAnalisis);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        if (data!=null) bind();
        return v;
    }

    private void bind() {
        List<AnalisisItem> items = new ArrayList<>();
        if (data.analisis != null) {
            if (data.analisis.fortalezas != null && !data.analisis.fortalezas.isEmpty())
                items.add(new AnalisisItem("Fortalezas", data.analisis.fortalezas));
            if (data.analisis.mejoras != null && !data.analisis.mejoras.isEmpty())
                items.add(new AnalisisItem("Áreas de Mejora", data.analisis.mejoras));
            if (data.analisis.recomendaciones != null && !data.analisis.recomendaciones.isEmpty())
                items.add(new AnalisisItem("Recomendaciones", data.analisis.recomendaciones));
        }
        if (items.isEmpty()) items.add(new AnalisisItem("Análisis", Arrays.asList("Sin datos")));
        rv.setAdapter(new AnalisisAdapter(items));
    }

    public static class AnalisisItem {
        public String titulo; public List<String> bullets;
        public AnalisisItem(String t, List<String> b){ titulo=t; bullets=b; }
    }
}
