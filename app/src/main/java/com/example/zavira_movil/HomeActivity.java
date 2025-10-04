package com.example.zavira_movil;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.zavira_movil.databinding.ActivityHomeBinding;
import com.example.zavira_movil.ui.ranking.home.IslasFragment;
import com.example.zavira_movil.ui.progreso.ProgresoFragment;
import com.example.zavira_movil.ui.ranking.RankingLogrosFragment; // nuevo fragment
import com.example.zavira_movil.ui.ranking.perfil.PerfilFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBell.setOnClickListener(v ->
                Toast.makeText(this, "Notificaciones pronto", Toast.LENGTH_SHORT).show());

        // Cargar por defecto "Islas"
        if (savedInstanceState == null) {
            show(new IslasFragment());
            binding.bottomNav.setSelectedItemId(R.id.nav_islas);
        }

        setupBottomNav(binding.bottomNav);
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
