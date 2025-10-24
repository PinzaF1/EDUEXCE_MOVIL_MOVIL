package com.example.zavira_movil.HislaConocimiento;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.R;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla de selección de modalidad para la Isla del Conocimiento.
 * - Llama /movil/isla/simulacro con {"modalidad":"facil"|"dificil"}
 * - Si responde 201 con el payload del simulacro, abre IslaPreguntasActivity.
 */
public class IslaSimulacroActivity extends AppCompatActivity {

    private LinearLayout cardFacil, cardDificil;
    private boolean busy = false; // evita taps dobles

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_isla_modality); // debe tener cardFacil y cardDificil

        cardFacil   = findViewById(R.id.cardFacil);
        cardDificil = findViewById(R.id.cardDificil);

        cardFacil.setOnClickListener(v -> iniciar("facil"));
        cardDificil.setOnClickListener(v -> iniciar("dificil"));
    }

    private void iniciar(String modalidad) {
        if (busy) return;
        busy = true;

        Toast.makeText(this, "Iniciando modo " + modalidad + "…", Toast.LENGTH_SHORT).show();

        ApiService api = RetrofitClient.getInstance(getApplicationContext()).create(ApiService.class);
        Call<IslaSimulacroResponse> call = api.iniciarIslaSimulacro(new IslaSimulacroRequest(modalidad));

        call.enqueue(new Callback<IslaSimulacroResponse>() {
            @Override
            public void onResponse(Call<IslaSimulacroResponse> call, Response<IslaSimulacroResponse> response) {
                busy = false;

                if (response.code() == 401) {
                    Toast.makeText(IslaSimulacroActivity.this,
                            "Token requerido. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(IslaSimulacroActivity.this,
                            "No se pudo iniciar el simulacro (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    return;
                }

                IslaSimulacroResponse data = response.body();

                Intent i = new Intent(IslaSimulacroActivity.this, IslaPreguntasActivity.class);
                i.putExtra("modalidad", modalidad);
                i.putExtra("payload", GsonHolder.gson().toJson(data));
                startActivity(i);
            }

            @Override
            public void onFailure(Call<IslaSimulacroResponse> call, Throwable t) {
                busy = false;
                Toast.makeText(IslaSimulacroActivity.this,
                        "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
