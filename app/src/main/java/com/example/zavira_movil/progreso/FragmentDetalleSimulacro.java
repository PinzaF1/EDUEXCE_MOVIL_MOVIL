package com.example.zavira_movil.progreso;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.zavira_movil.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class FragmentDetalleSimulacro extends Fragment {

    public FragmentDetalleSimulacro() { super(R.layout.fragment_detalle_simulacro); }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);

        // Datos que recibes desde Historial (pásalos en el Bundle)
        String materia   = getArguments()!=null ? getArguments().getString("materia","") : "";
        int porcentaje   = getArguments()!=null ? getArguments().getInt("porcentaje", 0) : 0;
        String nivel     = getArguments()!=null ? getArguments().getString("nivel","") : "";
        String fecha     = getArguments()!=null ? getArguments().getString("fecha","") : "";
        // String intentoId = getArguments()!=null ? getArguments().getString("intentoId","") : "";

        ((TextView)v.findViewById(R.id.tvMateriaChip)).setText(materia);
        ((TextView)v.findViewById(R.id.tvFecha)).setText(fecha);
        ((TextView)v.findViewById(R.id.tvScore)).setText(porcentaje + "%");
        ((TextView)v.findViewById(R.id.tvNivel)).setText(nivel);

        TabLayout tabs   = v.findViewById(R.id.tabsDetalle);
        ViewPager2 pager = v.findViewById(R.id.pagerDetalle);
        pager.setAdapter(new FragmentDetallePagerAdapter(this, getArguments()));

        new TabLayoutMediator(tabs, pager, (tab, pos) -> {
            if (pos==0) tab.setText("Resumen");
            else if (pos==1) tab.setText("Preguntas");
            else tab.setText("Análisis");
        }).attach();

        v.findViewById(R.id.btnBack).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager().popBackStack());

        v.findViewById(R.id.btnRepetir).setOnClickListener(x -> {
            // TODO: lógica para repetir simulacro (si la tienes)
        });
    }
}
