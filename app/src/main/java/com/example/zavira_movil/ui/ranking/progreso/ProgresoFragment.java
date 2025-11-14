package com.example.zavira_movil.ui.ranking.progreso;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
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
        
        // Ocultar logo EduExce y campana en la Activity principal
        v.post(() -> ocultarTopBar());
        
        // CRÍTICO: Establecer el color azul oscuro de la status bar cuando el fragment se crea
        if (getActivity() != null) {
            v.post(() -> {
                establecerStatusBarAzulOscuro();
            });
        }

        TabLayout tabLayout = v.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = v.findViewById(R.id.viewPager);
        
        // Configurar padding para el TabLayout para que se extienda hasta la status bar
        // El TabLayout se extenderá hasta arriba y el padding dinámico evitará que el contenido quede debajo de la status bar
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(tabLayout, (view, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            // Aplicar padding superior igual a la altura de la status bar + padding adicional (16dp)
            int paddingHorizontal = (int) (16 * getResources().getDisplayMetrics().density);
            int paddingTop = systemBars.top + (int) (16 * getResources().getDisplayMetrics().density);
            int paddingBottom = (int) (16 * getResources().getDisplayMetrics().density);
            view.setPadding(paddingHorizontal, paddingTop, paddingHorizontal, paddingBottom);
            return insets;
        });

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
    
    @Override
    public void onResume() {
        super.onResume();
        // Asegurar que el topBar esté oculto
        if (getView() != null) {
            getView().post(() -> ocultarTopBar());
        }
        
        // CRÍTICO: Establecer el color azul oscuro de la status bar cuando el fragment está visible
        if (getActivity() != null && isAdded()) {
            // Asegurar que la status bar sea azul oscuro para Progreso
            getActivity().runOnUiThread(() -> {
                establecerStatusBarAzulOscuro();
            });
        }
    }
    
    @Override
    public void onStart() {
        super.onStart();
        // También establecer el color cuando el fragment inicia
        if (getActivity() != null && isAdded()) {
            getActivity().runOnUiThread(() -> {
                establecerStatusBarAzulOscuro();
            });
        }
    }
    
    @Override
    public void onDestroyView() {
        // Restaurar logo EduExce y campana al salir del fragmento
        restaurarTopBar();
        super.onDestroyView();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Asegurar que se restaure al pausar también
        restaurarTopBar();
    }
    
    /**
     * Oculta el topBar (logo EduExce y campana) de HomeActivity
     */
    private void ocultarTopBar() {
        if (getActivity() != null && isAdded()) {
            View topBar = getActivity().findViewById(R.id.topBar);
            if (topBar != null) {
                topBar.setVisibility(View.GONE);
            }
        }
    }
    
    /**
     * Restaura la visibilidad del topBar (logo EduExce y campana) de HomeActivity
     */
    private void restaurarTopBar() {
        if (getActivity() != null && isAdded()) {
            View topBar = getActivity().findViewById(R.id.topBar);
            if (topBar != null) {
                topBar.setVisibility(View.VISIBLE);
            }
        }
    }
    
    /**
     * Establece el color azul oscuro de la status bar para Progreso
     */
    private void establecerStatusBarAzulOscuro() {
        if (getActivity() == null || !isAdded()) {
            return;
        }
        
        android.view.Window window = getActivity().getWindow();
        if (window == null) {
            return;
        }
        
        int azulOscuroColor = android.graphics.Color.parseColor("#2563EB");
        
        // CRÍTICO: Limpiar TODAS las flags que puedan interferir
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        
        // Establecer el color azul oscuro INMEDIATAMENTE
        window.setStatusBarColor(azulOscuroColor);
        
        // Obtener las flags actuales del sistema UI
        int currentFlags = window.getDecorView().getSystemUiVisibility();
        
        // Configurar el estilo del texto (claro para fondo azul oscuro)
        // IMPORTANTE: NO usar LAYOUT_FULLSCREEN porque oculta el color de la status bar
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            androidx.core.view.WindowInsetsControllerCompat windowInsetsController = 
                androidx.core.view.WindowCompat.getInsetsController(window, window.getDecorView());
            if (windowInsetsController != null) {
                // Para fondo azul oscuro, usar texto claro (blanco)
                windowInsetsController.setAppearanceLightStatusBars(false);
            }
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Limpiar todas las flags problemáticas
            int newFlags = currentFlags;
            // Desactivar LIGHT_STATUS_BAR (texto claro para fondo azul oscuro)
            newFlags = newFlags & ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
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
            window.setStatusBarColor(azulOscuroColor);
            android.util.Log.d("ProgresoFragment", "Post 0ms: Status bar color establecido a AZUL OSCURO");
        });
        
        window.getDecorView().postDelayed(() -> {
            window.setStatusBarColor(azulOscuroColor);
            int actualColor = window.getStatusBarColor();
            android.util.Log.d("ProgresoFragment", "Post 50ms: Status bar color verificado: " + 
                String.format("#%08X", actualColor));
        }, 50);
        
        window.getDecorView().postDelayed(() -> {
            window.setStatusBarColor(azulOscuroColor);
            android.util.Log.d("ProgresoFragment", "Post 100ms: Status bar color re-establecido");
        }, 100);
        
        window.getDecorView().postDelayed(() -> {
            window.setStatusBarColor(azulOscuroColor);
            int actualColor = window.getStatusBarColor();
            android.util.Log.d("ProgresoFragment", "Post 300ms: Status bar color final verificado: " + 
                String.format("#%08X", actualColor) + " (esperado: AZUL OSCURO)");
        }, 300);
        
        window.getDecorView().postDelayed(() -> {
            window.setStatusBarColor(azulOscuroColor);
        }, 500);
        
        android.util.Log.d("ProgresoFragment", "Status bar configurada a AZUL OSCURO desde fragment");
    }
}
