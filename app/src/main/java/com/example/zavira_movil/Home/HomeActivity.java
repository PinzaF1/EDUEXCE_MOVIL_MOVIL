package com.example.zavira_movil.Home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zavira_movil.R;
import com.example.zavira_movil.databinding.ActivityHomeBinding;
import com.example.zavira_movil.model.DemoData;
import com.example.zavira_movil.ui.ranking.RankingLogrosFragment;

import com.example.zavira_movil.Perfil.ProfileActivity;
import com.example.zavira_movil.ui.ranking.progreso.ProgresoFragment;
import com.example.zavira_movil.ui.ranking.progreso.RetosFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private SubjectAdapter adapter;

    private final ActivityResultLauncher<Intent> launcher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                    }
            );

    private static final Fragment BLANK_FRAGMENT = new Fragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lista principal de islas / materias
        binding.rvSubjects.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubjectAdapter(DemoData.getSubjects(), intent -> launcher.launch(intent));
        binding.rvSubjects.setAdapter(adapter);

        // Botón Isla Simulacro
        binding.btnIslaSimulacro.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, IslaSimulacroActivity.class);
            startActivity(i);
        });

        // Bottom navigation
        setupBottomNav(binding.bottomNav);

        // Pestaña por defecto: Islas
        if (savedInstanceState == null) {
            show(BLANK_FRAGMENT);
            applyTabVisibility(true);
            binding.bottomNav.setSelectedItemId(R.id.nav_islas);
        }
    }

    private void setupBottomNav(BottomNavigationView nav) {
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_islas) {
                applyTabVisibility(true);
                show(BLANK_FRAGMENT);
                return true;
            }

            // PERFIL es una Activity (no Fragment)
            if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, ProfileActivity.class));
                // devuelve false para no “cambiar” la selección del bottom nav
                return false;
            }

            Fragment f;
            if (id == R.id.nav_progreso) {
                f = new ProgresoFragment();
            } else if (id == R.id.nav_logros) {
                f = new RankingLogrosFragment();
            } else if (id == R.id.nav_retos) {
                f = new RetosFragment();
            } else {
                return false;
            }

            applyTabVisibility(false);
            show(f);
            return true;
        });
    }

    private void applyTabVisibility(boolean isIslas) {
        binding.rvSubjects.setVisibility(isIslas ? View.VISIBLE : View.GONE);
        binding.btnIslaSimulacro.setVisibility(isIslas ? View.VISIBLE : View.GONE);
    }

    private void show(Fragment f) {
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragmentContainer, f)
                .commit();
    }
}
