package com.example.zavira_movil.adapter;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.*;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.zavira_movil.progreso.FragmentDetalleAnalisis;
import com.example.zavira_movil.progreso.FragmentDetallePreguntas;
import com.example.zavira_movil.progreso.FragmentDetalleResumen;

public class DetalleSimuPagerAdapter extends FragmentStateAdapter {

    private final Bundle args;
    public DetalleSimuPagerAdapter(@NonNull Fragment owner, Bundle args) {
        super(owner);
        this.args = (args==null ? new Bundle() : args);
    }

    @NonNull @Override
    public Fragment createFragment(int position) {
        Fragment f;
        if (position == 0) f = new FragmentDetalleResumen();
        else if (position == 1) f = new FragmentDetallePreguntas();
        else f = new FragmentDetalleAnalisis();
        f.setArguments(new Bundle(args));
        return f;
    }

    @Override public int getItemCount() { return 3; }
}
