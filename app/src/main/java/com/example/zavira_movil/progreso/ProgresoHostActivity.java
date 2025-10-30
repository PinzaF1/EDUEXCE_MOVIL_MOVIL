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
        setContentView(R.layout.activity_progreso);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Usa el pager que ya tienes
        viewPager.setAdapter(new ProgresoPagerAdapter(this));
        viewPager.setOffscreenPageLimit(3);

        // Nombres de pestañas
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Resumen"); break;
                case 1: tab.setText("Diagnóstico inicial"); break; // ← aquí
                case 2: tab.setText("Historial"); break;
            }
        }).attach();

        // Failsafe por si algo intenta poner “Materias”
        tabLayout.post(() -> {
            if (tabLayout.getTabAt(1) != null) {
                tabLayout.getTabAt(1).setText("Diagnóstico inicial");
            }
        });
    }
}
