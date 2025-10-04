package com.example.zavira_movil.progreso;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ProgresoPagerAdapter extends FragmentStateAdapter {

    public ProgresoPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new FragmentGeneral();   // Progreso global
            case 1:
                return new FragmentMaterias();  // Materias
            case 2:
                return new FragmentHistorial(); // Historial
            default:
                return new FragmentGeneral();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Tres tabs
    }
}
