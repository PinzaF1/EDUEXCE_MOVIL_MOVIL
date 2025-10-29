package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class InfoAcademico extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_info_academico);

        setupNavigation(R.id.btnStartTest);

        setupNavigation(R.id.btnEnviar);
    }

    private void setupNavigation(int buttonId) {
        Button b = findViewById(buttonId);
        if (b != null) {
            b.setOnClickListener(v -> {
                Intent i = new Intent(InfoAcademico.this, TestAcademico.class);
                startActivity(i);
            });
        }
    }
}
