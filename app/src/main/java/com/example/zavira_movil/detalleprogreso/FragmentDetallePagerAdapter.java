package com.example.zavira_movil.detalleprogreso;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class FragmentDetallePagerAdapter extends FragmentStateAdapter {

    private final Bundle args;

    public FragmentDetallePagerAdapter(@NonNull Fragment owner, Bundle args) {
        super(owner);
        this.args = args == null ? new Bundle() : args;
    }

    @NonNull @Override
    public Fragment createFragment(int position) {
        Fragment f;
        if (position == 0)       f = new FragmentDetalleResumen();
        else if (position == 1)  f = new FragmentDetallePreguntas();
        else                     f = new FragmentDetalleAnalisis();
        f.setArguments(new Bundle(args));
        return f;
    }

    @Override public int getItemCount() { return 3; }
}
