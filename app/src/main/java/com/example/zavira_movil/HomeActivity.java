package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.zavira_movil.ui.ranking.home.IslasFragment;
import com.example.zavira_movil.ui.progreso.ProgresoFragment;
import com.example.zavira_movil.ui.ranking.RankingLogrosFragment;
import com.example.zavira_movil.ui.ranking.perfil.PerfilFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    private SubjectAdapter adapter;

    // Launcher para refrescar niveles desbloqueados
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

        // Acción FAB perfil
        binding.fabPerfil.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        // Configurar RecyclerView
        binding.rvSubjects.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubjectAdapter(DemoData.getSubjects(), intent -> launcher.launch(intent));
        binding.rvSubjects.setAdapter(adapter);

        // Botón Isla Simulacro
        findViewById(R.id.btnIslaSimulacro).setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, IslaSimulacroActivity.class);
            startActivity(i);
        });

        // Barra de navegación inferior
        setupBottomNav(binding.bottomNav);

        // Cargar por defecto "Islas"
        if (savedInstanceState == null) {
            show(new IslasFragment());
            binding.bottomNav.setSelectedItemId(R.id.nav_islas);
        }
    }

    private void setupBottomNav(BottomNavigationView nav) {
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment f;
            if (id == R.id.nav_islas) {
                f = new IslasFragment();
            } else if (id == R.id.nav_progreso) {
                f = new ProgresoFragment();
            } else if (id == R.id.nav_logros) {
                f = new RankingLogrosFragment(); // reemplaza a Activity
            } else if (id == R.id.nav_retos) {
                f = new ProgresoFragment(); // usa el que tengas o tu fragment de retos
            } else if (id == R.id.nav_perfil) {
                f = new com.example.zavira_movil.ui.ranking.perfil.PerfilFragment();
            } else return false;

            show(f);
            return true;
        });
    }

    private void show(Fragment f) {
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragmentContainer, f)
                .commit();
    }
}
