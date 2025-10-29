package com.example.zavira_movil.niveleshome;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.HislaConocimiento.IslaModalityActivity;

/**
 * Bridge para redirigir el flujo antiguo al nuevo flujo de
 * "Isla del Conocimiento". Con esto eliminamos las dependencias
 * a ApiQuestion/ApiQuestionMapper y los errores de getters.
 */
public class SimulacroActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = new Intent(this, IslaModalityActivity.class);
        // Si te llegan extras (área, subtemas), se reenvían:
        if (getIntent() != null && getIntent().getExtras() != null) {
            i.putExtras(getIntent().getExtras());
        }
        startActivity(i);
        finish();
    }
}
