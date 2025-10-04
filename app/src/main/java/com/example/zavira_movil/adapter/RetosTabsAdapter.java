package com.example.zavira_movil.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.zavira_movil.progreso.FragmentReto;       // Tab 1: Crear Reto
import com.example.zavira_movil.progreso.FragmentRetosRecibidos;  // Tab 2: Recibidos (visual)

public class RetosTabsAdapter extends FragmentStateAdapter {

    public RetosTabsAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) return new FragmentReto();
        else return new FragmentRetosRecibidos();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
