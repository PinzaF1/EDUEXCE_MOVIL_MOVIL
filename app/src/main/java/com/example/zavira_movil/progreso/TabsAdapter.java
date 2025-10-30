package com.example.zavira_movil.progreso;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TabsAdapter extends FragmentStateAdapter {

    public TabsAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0: return new FragmentGeneral();            // Resumen
            case 1: return new FragmentDiagnosticoInicial(); // ← AQUÍ (antes: FragmentMaterias)
            case 2: return new FragmentHistorial();          // Historial
            default: return new FragmentGeneral();
        }
    }

    @Override
    public int getItemCount() { return 3; }

    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0: return "Resumen";
            case 1: return "Diagnóstico inicial";
            case 2: return "Historial";
            default: return "";
        }
    }
}
