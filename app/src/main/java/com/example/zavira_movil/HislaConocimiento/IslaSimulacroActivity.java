package com.example.zavira_movil.HislaConocimiento;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.R;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla de selección de modalidad para la Isla del Conocimiento.
 * - Las tarjetas (Fácil / Difícil) SOLO seleccionan.
 * - El POST al backend se hace únicamente al pulsar el botón "Iniciar".
 */
public class IslaSimulacroActivity extends AppCompatActivity {

    private LinearLayout cardFacil, cardDificil;
    private TextView checkFacil, checkDificil;
    private Button btnIniciar;

    private String modalidadSeleccionada = null; // "facil" | "dificil"
    private boolean busy = false;

    private static final String STATE_MODO = "state_modo";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_isla_modality);

        cardFacil   = findViewById(R.id.cardFacil);
        cardDificil = findViewById(R.id.cardDificil);
        checkFacil  = findViewById(R.id.checkFacil);
        checkDificil= findViewById(R.id.checkDificil);
        btnIniciar  = findViewById(R.id.btnIniciar);

        if (savedInstanceState != null) {
            modalidadSeleccionada = savedInstanceState.getString(STATE_MODO);
        }
        aplicarSeleccion(modalidadSeleccionada);

        // Las tarjetas SOLO seleccionan y consumen el touch
        bindSoloSeleccion(cardFacil, () -> aplicarSeleccion("facil"));
        bindSoloSeleccion(cardDificil, () -> aplicarSeleccion("dificil"));

        // El botón inicia el simulacro
        btnIniciar.setOnClickListener(v -> {
            if (modalidadSeleccionada == null || busy) return;

            new AlertDialog.Builder(this)
                    .setTitle("Confirmar")
                    .setMessage("Iniciar simulacro en modo " +
                            ("facil".equals(modalidadSeleccionada) ? "Fácil" : "Difícil") + "?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Sí, iniciar", (d, w) -> iniciar(modalidadSeleccionada))
                    .show();
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_MODO, modalidadSeleccionada);
    }

    private void bindSoloSeleccion(LinearLayout card, Runnable onSelect) {
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> onSelect.run());
        card.setOnLongClickListener(v -> true); // consume long click
        card.setOnTouchListener((v, e) -> {
            if (e.getAction() == MotionEvent.ACTION_UP) v.performClick();
            return true; // SIEMPRE consumimos el touch
        });
    }

    private void aplicarSeleccion(@Nullable String modo) {
        modalidadSeleccionada = modo;

        boolean esFacil = "facil".equals(modo);
        boolean esDificil = "dificil".equals(modo);

        cardFacil.setSelected(esFacil);
        cardDificil.setSelected(esDificil);

        checkFacil.setVisibility(esFacil ? View.VISIBLE : View.GONE);
        checkDificil.setVisibility(esDificil ? View.VISIBLE : View.GONE);

        boolean habilitar = modo != null && !busy;
        btnIniciar.setEnabled(habilitar);
        btnIniciar.setAlpha(habilitar ? 1f : 0.6f);
        btnIniciar.setText(modo == null ? "Iniciar Simulacro"
                : (esFacil ? "Iniciar Simulacro (Fácil)" : "Iniciar Simulacro (Difícil)"));
    }

    private void iniciar(String modalidad) {
        busy = true;
        aplicarSeleccion(modalidadSeleccionada); // refresca estado del botón
        Toast.makeText(this, "Iniciando modo " + modalidad + "…", Toast.LENGTH_SHORT).show();

        ApiService api = RetrofitClient.getInstance(getApplicationContext()).create(ApiService.class);
        Call<IslaSimulacroResponse> call = api.iniciarIslaSimulacro(new IslaSimulacroRequest(modalidad));

        call.enqueue(new Callback<IslaSimulacroResponse>() {
            @Override
            public void onResponse(Call<IslaSimulacroResponse> call, Response<IslaSimulacroResponse> response) {
                busy = false;
                aplicarSeleccion(modalidadSeleccionada);

                // Token vencido/no presente
                if (response.code() == 401) {
                    Toast.makeText(IslaSimulacroActivity.this,
                            "Token requerido. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show();
                    return;
                }

                // Detecta 404 por NGROK offline (ngrok agrega el header 'ngrok-error-code')
                String ngrokCode = response.headers().get("ngrok-error-code");
                if (response.code() == 404 && ngrokCode != null) {
                    Toast.makeText(IslaSimulacroActivity.this,
                            "Servidor ngrok OFFLINE. Inicia el túnel o actualiza la BASE_URL.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(IslaSimulacroActivity.this,
                            "No se pudo iniciar el simulacro (HTTP " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                IslaSimulacroResponse data = response.body();

                // Navega a tu pantalla de preguntas de la Isla
                Intent i = new Intent(IslaSimulacroActivity.this, IslaPreguntasActivity.class);
                i.putExtra("modalidad", modalidad);
                // Usa tu GsonHolder del paquete HislaConocimiento
                i.putExtra("payload", GsonHolder.gson().toJson(data));
                startActivity(i);
            }

            @Override
            public void onFailure(Call<IslaSimulacroResponse> call, Throwable t) {
                busy = false;
                aplicarSeleccion(modalidadSeleccionada);
                Toast.makeText(IslaSimulacroActivity.this,
                        "Error de red: " + (t.getMessage() == null ? "desconocido" : t.getMessage()),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
