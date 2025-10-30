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

public class IslaModalityActivity extends AppCompatActivity {

    private LinearLayout cardFacil, cardDificil;
    private TextView checkFacil, checkDificil;
    private Button btnIniciar;
    private String modalidadSeleccionada = null; // "facil" | "dificil"
    private boolean busy = false;
    private static final String STATE_MODO = "state_modo";

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
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

        bindSoloSeleccion(cardFacil, () -> aplicarSeleccion("facil"));
        bindSoloSeleccion(cardDificil, () -> aplicarSeleccion("dificil"));

        btnIniciar.setOnClickListener(v -> {
            if (modalidadSeleccionada == null || busy) return;
            new AlertDialog.Builder(this)
                    .setTitle("Confirmar modalidad")
                    .setMessage("Vas a iniciar el simulacro en modo " +
                            ("facil".equals(modalidadSeleccionada) ? "Fácil" : "Difícil") + ". ¿Continuar?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Sí, iniciar", (d, w) -> iniciar(modalidadSeleccionada))
                    .show();
        });
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putString(STATE_MODO, modalidadSeleccionada);
    }

    private void bindSoloSeleccion(LinearLayout card, Runnable onSelect) {
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> onSelect.run());
        card.setOnLongClickListener(v -> true);
        card.setOnTouchListener((v, e) -> {
            if (e.getAction() == MotionEvent.ACTION_UP) v.performClick();
            return true;
        });
    }

    private void aplicarSeleccion(String modo) {
        modalidadSeleccionada = modo;
        boolean esFacil = "facil".equals(modo);
        boolean esDificil = "dificil".equals(modo);

        cardFacil.setSelected(esFacil);
        cardDificil.setSelected(esDificil);
        checkFacil.setVisibility(esFacil ? View.VISIBLE : View.GONE);
        checkDificil.setVisibility(esDificil ? View.VISIBLE : View.GONE);

        boolean habilitar = (modo != null) && !busy;
        btnIniciar.setEnabled(habilitar);
        btnIniciar.setAlpha(habilitar ? 1f : 0.6f);
        btnIniciar.setText(modo == null ? "Iniciar Simulacro"
                : (esFacil ? "Iniciar Simulacro (Fácil)" : "Iniciar Simulacro (Difícil)"));
    }

    private void iniciar(String modalidad) {
        busy = true; aplicarSeleccion(modalidadSeleccionada);

        ApiService api = RetrofitClient.getInstance(getApplicationContext()).create(ApiService.class);
        api.iniciarIslaSimulacro(new IslaSimulacroRequest(modalidad)).enqueue(new Callback<IslaSimulacroResponse>() {
            @Override public void onResponse(Call<IslaSimulacroResponse> call, Response<IslaSimulacroResponse> res) {
                busy = false; aplicarSeleccion(modalidadSeleccionada);
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(IslaModalityActivity.this, "No se pudo iniciar ("+res.code()+")", Toast.LENGTH_LONG).show();
                    return;
                }
                String payload = GsonHolder.gson().toJson(res.body());
                Intent i = new Intent(IslaModalityActivity.this, IslaPreguntasActivity.class);
                i.putExtra("modalidad", modalidad);
                i.putExtra("payload", payload);
                startActivity(i);
            }
            @Override public void onFailure(Call<IslaSimulacroResponse> call, Throwable t) {
                busy = false; aplicarSeleccion(modalidadSeleccionada);
                Toast.makeText(IslaModalityActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
