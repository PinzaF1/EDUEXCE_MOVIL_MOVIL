package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.databinding.ActivityInfoTestBinding;
import com.example.zavira_movil.model.PreguntasKolb;

public class InfoTestActivity extends AppCompatActivity {

    private ActivityInfoTestBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityInfoTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 🩷 Animación suave desde arriba al cargar la pantalla
        try {
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_down_soft);
            if (anim != null && binding.layoutPrincipal != null) {
                binding.layoutPrincipal.startAnimation(anim);
            }
        } catch (Exception e) {
            android.util.Log.e("InfoTestActivity", "Error al cargar animación", e);
        }

        // Botón para iniciar el test
        if (binding.btnStartTest != null) {
            android.util.Log.d("InfoTestActivity", "Botón encontrado, configurando listener");
            binding.btnStartTest.setOnClickListener(v -> {
                android.util.Log.d("InfoTestActivity", "Botón clickeado, iniciando TestActivity");
                try {
                    Intent intent = new Intent(InfoTestActivity.this, TestActivity.class);
                    android.util.Log.d("InfoTestActivity", "Intent creado: " + intent.toString());
                    startActivity(intent);
                    android.util.Log.d("InfoTestActivity", "Activity iniciada, finalizando InfoTestActivity");
                    finish();
                } catch (Exception e) {
                    android.util.Log.e("InfoTestActivity", "Error al iniciar test", e);
                    e.printStackTrace();
                    android.widget.Toast.makeText(InfoTestActivity.this, "Error al iniciar el test: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                }
            });
        } else {
            android.util.Log.e("InfoTestActivity", "ERROR: btnStartTest es null");
        }
    }
}
