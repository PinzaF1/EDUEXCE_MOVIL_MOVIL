package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zavira_movil.Home.IslaSimulacroActivity;
import com.example.zavira_movil.Home.SubjectAdapter;
import com.example.zavira_movil.databinding.ActivityHomeBinding;
import com.example.zavira_movil.model.DemoData;
import com.example.zavira_movil.ui.ranking.RankingLogrosFragment;
import com.example.zavira_movil.ui.ranking.home.IslasFragment;
import com.example.zavira_movil.ui.ranking.perfil.PerfilFragment;
import com.example.zavira_movil.ui.ranking.progreso.ProgresoFragment;
import com.example.zavira_movil.ui.ranking.progreso.RetosFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    private SubjectAdapter adapter;

    private final ActivityResultLauncher<Intent> launcher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Acción campana
        binding.btnBell.setOnClickListener(v ->
                Toast.makeText(this, "Notificaciones pronto 😊", Toast.LENGTH_SHORT).show());



        // Configurar RecyclerView (Islas)
        binding.rvSubjects.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubjectAdapter(DemoData.getSubjects(), intent -> launcher.launch(intent));
        binding.rvSubjects.setAdapter(adapter);

        // Botón Isla Simulacro (solo se muestra en Islas; igual lo configuramos aquí)
        binding.btnIslaSimulacro.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, IslaSimulacroActivity.class);
            startActivity(i);
        });

        // Bottom nav
        setupBottomNav(binding.bottomNav);

        // Por defecto: Islas
        if (savedInstanceState == null) {
            show(new IslasFragment());
            binding.bottomNav.setSelectedItemId(R.id.nav_islas);
            applyTabVisibility(true); // 👈 asegura ocultar/mostrar vistas iniciales
        }
    }

    private void setupBottomNav(BottomNavigationView nav) {
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment f;
            boolean isIslas;
            if (id == R.id.nav_islas) {
                f = new IslasFragment();
                isIslas = true;
            } else if (id == R.id.nav_progreso) {
                f = new ProgresoFragment();
                isIslas = false;
            } else if (id == R.id.nav_logros) {
                f = new RankingLogrosFragment();
                isIslas = false;
            } else if (id == R.id.nav_retos) {
                f = new RetosFragment();
                isIslas = false;
            } else if (id == R.id.nav_perfil) {
                f = new PerfilFragment();
                isIslas = false;
            } else {
                return false;
            }

            // Muestra fragment y ajusta visibilidad de vistas de Islas
            show(f);
            applyTabVisibility(isIslas);
            return true;
        });
    }

    /** Muestra/oculta SOLO las vistas propias de Islas para que no "sangren" bajo otros fragments */
    private void applyTabVisibility(boolean isIslas) {
        binding.rvSubjects.setVisibility(isIslas ? View.VISIBLE : View.GONE);
        binding.btnIslaSimulacro.setVisibility(isIslas ? View.VISIBLE : View.GONE);
        // El topBar y el FAB quedan como estaban (se ven en todas las pestañas)
    }

    private void show(Fragment f) {
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragmentContainer, f)
                .commit();
    }
}
