package com.example.zavira_movil.ui.ranking.progreso;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.zavira_movil.R;
import com.example.zavira_movil.progreso.ProgresoPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ProgresoFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Reutilizamos el layout de la Activity
        return inflater.inflate(R.layout.activity_progreso, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);

        TabLayout tabLayout = v.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = v.findViewById(R.id.viewPager);

        //  Usa el ctor que recibe Fragment (este)
        viewPager.setAdapter(new ProgresoPagerAdapter(this));
        viewPager.setOffscreenPageLimit(3);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Resumen"); break;
                case 1: tab.setText("Diagnóstico"); break; // ← renombrado
                case 2: tab.setText("Historial"); break;
            }
        }).attach();

        // Si quieres que abra directamente la pestaña de Diagnóstico:
        // viewPager.setCurrentItem(1, false);
    }
}
