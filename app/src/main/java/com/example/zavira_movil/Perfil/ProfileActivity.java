package com.example.zavira_movil.Perfil;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.zavira_movil.LoginActivity;
import com.example.zavira_movil.R;
import com.example.zavira_movil.local.TokenManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class ProfileActivity extends AppCompatActivity {

    private static final String KEY_SELECTED = "selected_tab";
    private static final int TAB_PERFIL = R.id.btnPerfil;
    private static final int TAB_CONFIG = R.id.btnConfiguracion;

    private MaterialButtonToggleGroup segmentedTabs;
    private MaterialButton btnPerfil, btnConfiguracion;

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

        // Referencias del segmented control
        segmentedTabs    = findViewById(R.id.segmentedTabs);
        btnPerfil        = findViewById(R.id.btnPerfil);
        btnConfiguracion = findViewById(R.id.btnConfiguracion);

        // Listener de cambio
        if (segmentedTabs != null) {
            segmentedTabs.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (!isChecked) return;
                swapTo(checkedId);
            });
        }

        // Estado inicial
        if (savedInstanceState == null) {
            // Selecciona PERFIL por defecto y carga su fragment
            if (segmentedTabs != null && btnPerfil != null) {
                segmentedTabs.check(btnPerfil.getId());
            }
            swapTo(TAB_PERFIL);
        } else {
            // Restaurar pestaña seleccionada
            int last = savedInstanceState.getInt(KEY_SELECTED, TAB_PERFIL);
            if (segmentedTabs != null) segmentedTabs.check(last);
            // Asegurar que el fragment visible corresponde a la pestaña
            swapTo(last, /*force=*/false);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Guardar pestaña seleccionada
        if (segmentedTabs != null) {
            @IdRes int checked = segmentedTabs.getCheckedButtonId();
            outState.putInt(KEY_SELECTED, checked == -1 ? TAB_PERFIL : checked);
        } else {
            outState.putInt(KEY_SELECTED, TAB_PERFIL);
        }
    }

    private void swapTo(@IdRes int checkedId) {
        swapTo(checkedId, /*force=*/true);
    }

    private void swapTo(@IdRes int checkedId, boolean force) {
        Fragment target;
        String tag;

        if (checkedId == TAB_CONFIG) {
            target = getSupportFragmentManager().findFragmentByTag("config");
            if (target == null) target = new ConfiguracionFragment();
            tag = "config";
        } else {
            // PERFIL
            target = getSupportFragmentManager().findFragmentByTag("perfil");
            if (target == null) target = new PerfilFragment(); // Usa tu PerfilFragment con el consumo ya hecho
            tag = "perfil";
        }

        // Si ya está visible y no queremos forzar, no reemplazar
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
