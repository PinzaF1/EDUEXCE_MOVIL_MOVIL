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
import com.example.zavira_movil.retos1vs1.RetosTabsAdapter;
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
        
        // Ocultar logo EduExce y campana en la Activity principal
        v.post(() -> ocultarTopBar());

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
    
    @Override
    public void onResume() {
        super.onResume();
        // Asegurar que el topBar esté oculto cada vez que el fragmento se muestra
        if (getView() != null && isAdded()) {
            getView().post(() -> ocultarTopBar());
        }
    }
    
    @Override
    public void onDestroyView() {
        // NO restaurar el topBar aquí porque puede estar navegando a otra Activity (Perfil)
        // El topBar solo debe restaurarse cuando cambias a otro fragment dentro de HomeActivity
        // HomeActivity se encargará de restaurarlo cuando sea necesario
        super.onDestroyView();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // NO restaurar el topBar en onPause
        // ProfileActivity es una Activity separada, así que HomeActivity queda en segundo plano
        // Si restauramos aquí, el topBar aparecería en segundo plano
    }
    
    private void ocultarTopBar() {
        if (getActivity() != null && isAdded()) {
            View topBar = getActivity().findViewById(R.id.topBar);
            if (topBar != null) {
                topBar.setVisibility(View.GONE);
            }
        }
    }
    
    private void restaurarTopBar() {
        if (getActivity() != null && isAdded()) {
            View topBar = getActivity().findViewById(R.id.topBar);
            if (topBar != null) {
                topBar.setVisibility(View.VISIBLE);
            }
        }
    }
}
