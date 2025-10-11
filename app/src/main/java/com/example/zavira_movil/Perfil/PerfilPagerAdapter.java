package com.example.zavira_movil.Perfil;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PerfilPagerAdapter extends FragmentStateAdapter {

    public PerfilPagerAdapter(@NonNull Fragment host) {
        super(host);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ? new PerfilFragment() : new ConfiguracionFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
