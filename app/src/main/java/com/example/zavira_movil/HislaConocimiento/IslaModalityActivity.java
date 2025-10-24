package com.example.zavira_movil.HislaConocimiento;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.R;

public class IslaModalityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_isla_modality);

        LinearLayout cardFacil   = findViewById(R.id.cardFacil);
        LinearLayout cardDificil = findViewById(R.id.cardDificil);

        View.OnClickListener go = v -> {
            String modalidad = (v.getId() == R.id.cardFacil) ? "facil" : "dificil";
            Intent i = new Intent(this, IslaSimulacroActivity.class);
            i.putExtra("modalidad", modalidad);
            startActivity(i);
        };
        cardFacil.setOnClickListener(go);
        cardDificil.setOnClickListener(go);
    }
}
