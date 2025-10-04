package com.example.zavira_movil;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.zavira_movil.adapter.RetosTabsAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class RetosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retosactivity);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        View overlayContainer = findViewById(R.id.container);

        viewPager.setAdapter(new com.example.zavira_movil.adapter.RetosTabsAdapter(this));
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Crear Reto" : "Recibidos");
        }).attach();

        // Mostrar/ocultar overlay según backstack
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            boolean hasBackStack = getSupportFragmentManager().getBackStackEntryCount() > 0;
            overlayContainer.setVisibility(hasBackStack ? View.VISIBLE : View.GONE);
        });
    }
}
