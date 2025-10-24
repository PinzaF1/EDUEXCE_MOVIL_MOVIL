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
            case 0: return new FragmentGeneral();
            case 1: return new FragmentMaterias();
            case 2: return new FragmentHistorial();
            default: return new FragmentGeneral();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
