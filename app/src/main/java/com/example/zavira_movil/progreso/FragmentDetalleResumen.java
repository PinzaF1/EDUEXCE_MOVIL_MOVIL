package com.example.zavira_movil.progreso;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.zavira_movil.R;

public class FragmentDetalleResumen extends Fragment {

    public FragmentDetalleResumen() { super(R.layout.fragment_detalle_resumen); }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);
        int porcentaje = getArguments()!=null ? getArguments().getInt("porcentaje", 0) : 0;

        TextView tvMsg = v.findViewById(R.id.tvResumenMsg);
        tvMsg.setText(porcentaje >= 60 ? "¡Buen trabajo!" : "¡Vamos, tú puedes!");
        // Aquí puedes renderizar más estadísticas cuando tengas el endpoint de detalle.
    }
}
