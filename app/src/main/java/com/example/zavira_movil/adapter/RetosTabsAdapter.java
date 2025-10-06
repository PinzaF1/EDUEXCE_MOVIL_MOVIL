package com.example.zavira_movil.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.zavira_movil.progreso.FragmentReto;
import com.example.zavira_movil.progreso.FragmentRetosRecibidos;

public class RetosTabsAdapter extends FragmentStateAdapter {

    public RetosTabsAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    // NUEVO: para usarlo desde RetosFragment
    public RetosTabsAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return (position == 0) ? new FragmentReto() : new FragmentRetosRecibidos();
    }

    @Override
    public int getItemCount() { return 2; }
}
