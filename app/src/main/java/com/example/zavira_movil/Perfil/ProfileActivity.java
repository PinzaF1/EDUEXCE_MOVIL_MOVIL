package com.example.zavira_movil.Perfil;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.zavira_movil.LoginActivity;
import com.example.zavira_movil.R;
import com.example.zavira_movil.local.TokenManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;

public class ProfileActivity extends AppCompatActivity {

    private static final String KEY_SELECTED = "selected_tab";
    private static final int TAB_PERFIL = 0;   // índice
    private static final int TAB_CONFIG = 1;

    private TabLayout tabLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Si no hay token -> Login
        if (TokenManager.getToken(this) == null) {
            goLogin();
            return;
        }

        setContentView(R.layout.acrivity_profile);

        // Toolbar
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Perfil");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (topAppBar != null) {
            topAppBar.setNavigationOnClickListener(v -> finish());
        }

        // Tabs (ya vienen en el XML con TabItem)
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                swapTo(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });

        // Estado inicial
        if (savedInstanceState == null) {
            TabLayout.Tab t = tabLayout.getTabAt(TAB_PERFIL);
            if (t != null) t.select();
            swapTo(TAB_PERFIL);
        } else {
            int last = savedInstanceState.getInt(KEY_SELECTED, TAB_PERFIL);
            TabLayout.Tab t = tabLayout.getTabAt(last);
            if (t != null) t.select();
            swapTo(last, /*force=*/false);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        int idx = tabLayout.getSelectedTabPosition();
        if (idx == TabLayout.Tab.INVALID_POSITION) idx = TAB_PERFIL;
        outState.putInt(KEY_SELECTED, idx);
    }

    private void swapTo(int tabIndex) { swapTo(tabIndex, true); }

    private void swapTo(int tabIndex, boolean force) {
        Fragment target;
        String tag;

        if (tabIndex == TAB_CONFIG) {
            target = getSupportFragmentManager().findFragmentByTag("config");
            if (target == null) target = new ConfiguracionFragment();
            tag = "config";
        } else {
            target = getSupportFragmentManager().findFragmentByTag("perfil");
            if (target == null) target = new PerfilFragment();
            tag = "perfil";
        }

        if (!force) {
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            if (current != null && current.getClass() == target.getClass()) return;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, target, tag)
                .commit();
    }

    private void goLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
