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
import com.example.zavira_movil.adapter.RetosTabsAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class RetosFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflamos el MISMO layout de la antigua Activity (mantiene el diseño 1:1)
        return inflater.inflate(R.layout.activity_retosactivity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);

        TabLayout tabLayout = v.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = v.findViewById(R.id.viewPager);
        View overlayContainer = v.findViewById(R.id.container);

        // Adapter con constructor que acepta Fragment (ver RetosTabsAdapter abajo)
        viewPager.setAdapter(new RetosTabsAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) ->
                tab.setText(position == 0 ? "Crear Reto" : "Recibidos")
        ).attach();

        // Mostrar/ocultar overlay según backstack de este fragment (hijos)
        if (overlayContainer != null) {
            getChildFragmentManager().addOnBackStackChangedListener(() -> {
                boolean hasBackStack = getChildFragmentManager().getBackStackEntryCount() > 0;
                overlayContainer.setVisibility(hasBackStack ? View.VISIBLE : View.GONE);
            });
        }
    }
}
