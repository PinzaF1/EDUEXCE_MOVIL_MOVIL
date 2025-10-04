package com.example.zavira_movil.Home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.IslaSimulacroRequest;
import com.example.zavira_movil.model.IslaSimulacroResponse;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IslaSimulacroActivity extends AppCompatActivity {

    private RadioButton rbFacil, rbDificil;
    private Button btnIniciar;
    private String modalidad = "facil"; // por defecto

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_isla_simulacro);

        rbFacil = findViewById(R.id.rbFacil);
        rbDificil = findViewById(R.id.rbDificil);
        btnIniciar = findViewById(R.id.btnIniciar);

        rbFacil.setOnClickListener(v -> modalidad = "facil");
        rbDificil.setOnClickListener(v -> modalidad = "dificil");

        btnIniciar.setOnClickListener(v -> iniciarSimulacro());
    }

    private void iniciarSimulacro() {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        IslaSimulacroRequest req = new IslaSimulacroRequest(modalidad);

        api.iniciarIslaSimulacro(req).enqueue(new Callback<IslaSimulacroResponse>() {
            @Override
            public void onResponse(Call<IslaSimulacroResponse> call, Response<IslaSimulacroResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    IslaSimulacroResponse resp = response.body();

                    Intent i = new Intent(IslaSimulacroActivity.this, IslaPreguntasActivity.class);
                    i.putExtra("idSesion", resp.sesion.idSesion);
                    i.putExtra("modalidad", resp.sesion.modalidad);
                    i.putExtra("preguntas", new ArrayList<>(resp.preguntas)); // Parcelable o Serializable
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(IslaSimulacroActivity.this, "Error al iniciar simulacro", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<IslaSimulacroResponse> call, Throwable t) {
                Toast.makeText(IslaSimulacroActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
