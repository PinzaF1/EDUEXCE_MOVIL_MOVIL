package com.example.zavira_movil.detalleprogreso;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;

public class FragmentDetalleResumen extends Fragment {
    private TextView tvMensaje, tvNivelActual, tvNivelActualValue;
    private android.view.View llNivelActual;
    private RecyclerView rvStats;
    private ProgresoDetalleResponse data;
    private String materia;

    public void setData(ProgresoDetalleResponse data) {
        this.data = data;
        if (getView() != null && isAdded()) bind();
    }
    
    public void setMateria(String materia) {
        this.materia = materia;
        if (getView() != null && isAdded()) bind();
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = inf.inflate(R.layout.fragment_detalle_resumen, c, false);
        tvMensaje = v.findViewById(R.id.tvMensaje);
        tvNivelActual = v.findViewById(R.id.tvNivelActual);
        tvNivelActualValue = v.findViewById(R.id.tvNivelActualValue);
        llNivelActual = v.findViewById(R.id.llNivelActual);
        rvStats = v.findViewById(R.id.rvStats);
        if (getContext() != null) {
            rvStats.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        if (data != null && isAdded()) bind();
        return v;
    }

    private void bind() {
        if (!isAdded() || getContext() == null) return;
        if (data == null || data.resumen == null || data.header == null) return;
        if (tvMensaje == null) return;
        
        // Mensaje centrado con signos de admiración y negrita
        String mensaje = data.resumen.mensaje != null ? data.resumen.mensaje.trim() : "";
        if (!mensaje.isEmpty()) {
            // Agregar signo de apertura si no existe
            if (!mensaje.startsWith("¡")) {
                mensaje = "¡" + mensaje;
            }
            // Agregar signo de cierre si no existe
            if (!mensaje.endsWith("!") && !mensaje.endsWith("¡")) {
                mensaje = mensaje + "!";
            }
        }
        tvMensaje.setText(mensaje);
        tvMensaje.setGravity(Gravity.CENTER);
        tvMensaje.setTypeface(null, Typeface.BOLD);
        
        // Nivel actual: negrita y alineado con estadísticas
        // Ocultar si es "Todas las áreas" o "Isla del Conocimiento"
        if (materia != null) {
            String m = materia.toLowerCase().trim();
            boolean esTodasLasAreas = m.contains("conocimiento") || m.contains("isla") || 
                (m.contains("todas") && (m.contains("area") || m.contains("área")));
            
            if (esTodasLasAreas) {
                if (llNivelActual != null) {
                    llNivelActual.setVisibility(View.GONE);
                }
            } else {
                if (llNivelActual != null) {
                    llNivelActual.setVisibility(View.VISIBLE);
                }
                if (tvNivelActual != null) {
                    tvNivelActual.setText("Nivel actual:");
                    tvNivelActual.setTypeface(null, Typeface.BOLD);
                }
                if (tvNivelActualValue != null) {
                    tvNivelActualValue.setText(String.valueOf(data.resumen.nivelActual));
                    tvNivelActualValue.setTypeface(null, Typeface.BOLD);
                }
            }
        } else {
            if (llNivelActual != null) {
                llNivelActual.setVisibility(View.VISIBLE);
            }
            if (tvNivelActual != null) {
                tvNivelActual.setText("Nivel actual:");
                tvNivelActual.setTypeface(null, Typeface.BOLD);
            }
            if (tvNivelActualValue != null) {
                tvNivelActualValue.setText(String.valueOf(data.resumen.nivelActual));
                tvNivelActualValue.setTypeface(null, Typeface.BOLD);
            }
        }

        if (rvStats != null) {
            rvStats.setAdapter(new ResumenAdapter(
                    data.header.total,
                    data.header.correctas,
                    data.header.incorrectas,
                    data.header.tiempo_total_seg
            ));
        }
    }
}
