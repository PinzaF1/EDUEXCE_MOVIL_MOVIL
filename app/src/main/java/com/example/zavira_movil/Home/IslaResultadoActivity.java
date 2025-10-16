package com.example.zavira_movil.Home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.IslaResumenResponse;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IslaResultadoActivity extends AppCompatActivity {

    private TextView tvGlobal, tvDetalle;
    private Button btnRepetir, btnFinalizar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_isla_resultado);

        tvGlobal = findViewById(R.id.tvGlobal);
        tvDetalle = findViewById(R.id.tvDetalle);
        btnRepetir = findViewById(R.id.btnRepetir);
        btnFinalizar = findViewById(R.id.btnFinalizar);

        int idSesion = getIntent().getIntExtra("idSesion", -1);
        cargarResumen(idSesion);

        btnRepetir.setOnClickListener(v -> {
            Intent i = new Intent(this, IslaSimulacroActivity.class);
            startActivity(i);
            finish();
        });

        btnFinalizar.setOnClickListener(v -> {
            // ✅ ahora compila porque importamos HomeActivity del paquete correcto
            Intent i = new Intent(this, HomeActivity.class);
            startActivity(i);
            finish();
        });
    }

    private void cargarResumen(int idSesion) {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.getIslaResumen(idSesion).enqueue(new Callback<IslaResumenResponse>() {
            @Override
            public void onResponse(Call<IslaResumenResponse> call, Response<IslaResumenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    IslaResumenResponse r = response.body();
                    tvGlobal.setText("Puntaje Global: " + r.puntajeGeneral + "%");

                    StringBuilder detalle = new StringBuilder();
                    for (String area : r.puntajesPorArea.keySet()) {
                        detalle.append(area)
                                .append(": ")
                                .append(r.puntajesPorArea.get(area))
                                .append("%\n");
                    }
                    tvDetalle.setText(detalle.toString());
                }
            }

            @Override
            public void onFailure(Call<IslaResumenResponse> call, Throwable t) {
                tvGlobal.setText("Error: " + t.getMessage());
            }
        });
    }
}
