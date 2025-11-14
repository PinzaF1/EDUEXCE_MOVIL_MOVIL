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
        
        // CRÍTICO: Establecer el color naranja de la status bar cuando el fragment se crea
        if (getActivity() != null) {
            v.post(() -> {
                establecerStatusBarNaranja();
            });
        }

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
        
        // CRÍTICO: Establecer el color naranja de la status bar cuando el fragment está visible
        if (getActivity() != null && isAdded()) {
            // Asegurar que la status bar sea naranja para Retos
            getActivity().runOnUiThread(() -> {
                establecerStatusBarNaranja();
            });
        }
    }
    
    @Override
    public void onStart() {
        super.onStart();
        // También establecer el color cuando el fragment inicia
        if (getActivity() != null && isAdded()) {
            getActivity().runOnUiThread(() -> {
                establecerStatusBarNaranja();
            });
        }
    }
    
    /**
     * Establece el color blanco de la status bar para Retos
     */
    private void establecerStatusBarNaranja() {
        if (getActivity() == null || !isAdded()) {
            return;
        }
        
        android.view.Window window = getActivity().getWindow();
        if (window == null) {
            return;
        }
        
        int blancoColor = android.graphics.Color.WHITE;
        
        // CRÍTICO: Limpiar TODAS las flags que puedan interferir
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        
        // Establecer el color blanco INMEDIATAMENTE
        window.setStatusBarColor(blancoColor);
        
        // Obtener las flags actuales del sistema UI
        int currentFlags = window.getDecorView().getSystemUiVisibility();
        
        // Configurar el estilo del texto (oscuro para fondo blanco)
        // IMPORTANTE: NO usar LAYOUT_FULLSCREEN porque oculta el color de la status bar
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            androidx.core.view.WindowInsetsControllerCompat windowInsetsController = 
                androidx.core.view.WindowCompat.getInsetsController(window, window.getDecorView());
            if (windowInsetsController != null) {
                // Para fondo blanco, usar texto oscuro
                windowInsetsController.setAppearanceLightStatusBars(true);
            }
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Limpiar todas las flags problemáticas
            int newFlags = currentFlags;
            // Activar LIGHT_STATUS_BAR (texto oscuro para fondo blanco)
            newFlags = newFlags | android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            // Eliminar LAYOUT_FULLSCREEN (permite que el contenido se dibuje detrás de la status bar, ocultando el color)
            newFlags = newFlags & ~android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            // Mantener LAYOUT_STABLE para estabilidad
            newFlags = newFlags | android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            // Mantener las flags de navegación si existen
            if ((currentFlags & android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) != 0) {
                newFlags = newFlags | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            }
            if ((currentFlags & android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0) {
                newFlags = newFlags | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }
            if ((currentFlags & android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) != 0) {
                newFlags = newFlags | android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            window.getDecorView().setSystemUiVisibility(newFlags);
        }
        
        // Forzar aplicación múltiple del color con diferentes delays
        window.getDecorView().post(() -> {
            window.setStatusBarColor(blancoColor);
            android.util.Log.d("RetosFragment", "Post 0ms: Status bar color establecido a BLANCO");
        });
        
        window.getDecorView().postDelayed(() -> {
            window.setStatusBarColor(blancoColor);
            int actualColor = window.getStatusBarColor();
            android.util.Log.d("RetosFragment", "Post 50ms: Status bar color verificado: " + 
                String.format("#%08X", actualColor));
        }, 50);
        
        window.getDecorView().postDelayed(() -> {
            window.setStatusBarColor(blancoColor);
            android.util.Log.d("RetosFragment", "Post 100ms: Status bar color re-establecido");
        }, 100);
        
        window.getDecorView().postDelayed(() -> {
            window.setStatusBarColor(blancoColor);
            int actualColor = window.getStatusBarColor();
            android.util.Log.d("RetosFragment", "Post 300ms: Status bar color final verificado: " + 
                String.format("#%08X", actualColor) + " (esperado: BLANCO)");
        }, 300);
        
        window.getDecorView().postDelayed(() -> {
            window.setStatusBarColor(blancoColor);
        }, 500);
        
        android.util.Log.d("RetosFragment", "Status bar configurada a BLANCO desde fragment");
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
