package com.example.zavira_movil.progreso;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.zavira_movil.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ProgresoHostActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progreso); // tu XML con @id/tabLayout y @id/viewPager

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Usa el adapter que ya tienes. Si usas TabsAdapter, cambia la línea de abajo.
        viewPager.setAdapter(new ProgresoPagerAdapter(this));
        // Alternativa si prefieres: viewPager.setAdapter(new TabsAdapter(this));

        viewPager.setOffscreenPageLimit(3);

        // AQUÍ renombramos la segunda pestaña a “Diagnóstico inicial”
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Resumen");
                    break;
                case 1:
                    tab.setText("Diagnóstico inicial"); // ← antes decía “Materias”
                    break;
                case 2:
                    tab.setText("Historial");
                    break;
            }
        }).attach();
    }
}
