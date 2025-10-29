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
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_down_soft);
        binding.layoutPrincipal.startAnimation(anim);

        // Botón para iniciar el test (sin cambios)
        binding.btnStartTest.setOnClickListener(v -> {
            Intent intent = new Intent(InfoTestActivity.this, TestActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
