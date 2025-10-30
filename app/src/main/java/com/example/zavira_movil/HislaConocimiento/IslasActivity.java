package com.example.zavira_movil.HislaConocimiento;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.R;

public class IslasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_islas);

        LinearLayout btnFacil   = findViewById(R.id.btnFacil);
        LinearLayout btnDificil = findViewById(R.id.btnDificil);

        View.OnClickListener go = v -> {
            String modalidad = (v.getId() == R.id.btnFacil) ? "facil" : "dificil";
            startActivity(new Intent(this, IslaSimulacroActivity.class)
                    .putExtra("modalidad", modalidad));
        };

        btnFacil.setOnClickListener(go);
        btnDificil.setOnClickListener(go);
    }
}
