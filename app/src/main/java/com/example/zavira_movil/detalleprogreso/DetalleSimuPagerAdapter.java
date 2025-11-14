package com.example.zavira_movil.detalleprogreso;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class DetalleSimuPagerAdapter extends FragmentStateAdapter {

    private final FragmentDetalleResumen resumen = new FragmentDetalleResumen();
    private final FragmentDetallePreguntas preguntas = new FragmentDetallePreguntas();
    private final FragmentDetalleAnalisis analisis = new FragmentDetalleAnalisis();
    private String materia;

    public DetalleSimuPagerAdapter(@NonNull FragmentActivity fa) { super(fa); }

    @NonNull @Override public Fragment createFragment(int position) {
        if (position == 0) return resumen;
        if (position == 1) return preguntas;
        return analisis;
    }

    @Override public int getItemCount() { return 3; }

    public void setData(ProgresoDetalleResponse data) {
        resumen.setData(data);
        preguntas.setData(data);
        analisis.setData(data);
    }
    
    public void setMateria(String materia) {
        this.materia = materia;
        resumen.setMateria(materia);
    }
}
