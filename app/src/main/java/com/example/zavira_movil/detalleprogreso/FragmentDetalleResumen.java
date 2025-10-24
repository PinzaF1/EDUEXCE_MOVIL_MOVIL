package com.example.zavira_movil.detalleprogreso;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;

public class FragmentDetalleResumen extends Fragment {
    private TextView tvMensaje, tvNivelActual;
    private RecyclerView rvStats;
    private ProgresoDetalleResponse data;

    public void setData(ProgresoDetalleResponse data) {
        this.data = data;
        if (getView()!=null) bind();
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = inf.inflate(R.layout.fragment_detalle_resumen, c, false);
        tvMensaje = v.findViewById(R.id.tvMensaje);
        tvNivelActual = v.findViewById(R.id.tvNivelActual);
        rvStats = v.findViewById(R.id.rvStats);
        rvStats.setLayoutManager(new LinearLayoutManager(getContext()));
        if (data!=null) bind();
        return v;
    }

    private void bind() {
        tvMensaje.setText(data.resumen.mensaje);
        tvNivelActual.setText("Nivel actual: " + data.resumen.nivelActual);

        rvStats.setAdapter(new ResumenAdapter(
                data.header.total,
                data.header.correctas,
                data.header.incorrectas,
                data.header.tiempo_total_seg
        ));
    }
}
