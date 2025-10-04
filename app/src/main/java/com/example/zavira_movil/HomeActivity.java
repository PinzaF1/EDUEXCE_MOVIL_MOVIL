package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zavira_movil.Home.IslaSimulacroActivity;
import com.example.zavira_movil.databinding.ActivityHomeBinding;
import com.example.zavira_movil.Home.SubjectAdapter;
import com.example.zavira_movil.model.DemoData;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    private SubjectAdapter adapter;

    // ✅ Nuevo: ActivityResultLauncher
    private final ActivityResultLauncher<Intent> launcher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && adapter != null) {
                    adapter.notifyDataSetChanged(); // 🔄 refresca niveles desbloqueados
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

        // Lista de materias con launcher
        binding.rvSubjects.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubjectAdapter(DemoData.getSubjects(), intent -> launcher.launch(intent));
        binding.rvSubjects.setAdapter(adapter);

        findViewById(R.id.btnIslaSimulacro).setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, IslaSimulacroActivity.class);
            startActivity(i);
        });

    }
}
