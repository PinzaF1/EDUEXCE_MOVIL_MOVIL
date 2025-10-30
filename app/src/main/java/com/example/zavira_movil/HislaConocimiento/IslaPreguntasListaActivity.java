package com.example.zavira_movil.HislaConocimiento;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Muestra todas las preguntas y al final envía /movil/isla/simulacro/cerrar
 * SIN campos de tiempo. Solo {id_pregunta, respuesta}.
 */
public class IslaPreguntasListaActivity extends AppCompatActivity {

    private RecyclerView rv;
    private Button btnEnviar;

    private String modalidad;
    private String payloadJson;
    private int idSesion = 0;

    private final ArrayList<PregItem> items = new ArrayList<>();
    private PregListaAdapter adapter;

    // Ajusta la obtención real de tu token si lo manejas en otro lado.
    private String getToken() {
        // Ejemplo: return "Bearer " + MisPrefs.getToken(this);
        return "Bearer TU_TOKEN_AQUI";
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_isla_preguntas_lista);

        rv = findViewById(R.id.rvPreguntas);
        btnEnviar = findViewById(R.id.btnEnviar);

        modalidad   = getIntent().getStringExtra("modalidad");
        payloadJson = getIntent().getStringExtra("payload");

        // 1) Parsear payload del iniciar
        try {
            IslaSimulacroResponse data = GsonHolder.gson().fromJson(payloadJson, IslaSimulacroResponse.class);

            String idSesionStr = (data != null && data.getSesion() != null) ? data.getSesion().getIdSesion() : null;
            try { idSesion = Integer.parseInt(String.valueOf(idSesionStr)); } catch (Exception ignore) {}

            if (data != null && data.getPreguntas() != null) {
                for (int i = 0; i < data.getPreguntas().size(); i++) {
                    IslaSimulacroResponse.PreguntaDto p = data.getPreguntas().get(i);
                    PregItem it = new PregItem();
                    it.orden = i + 1;
                    it.idPregunta = safeInt(p.getIdPregunta());
                    it.area = p.getArea();
                    it.subtema = p.getSubtema();
                    it.enunciado = p.getEnunciado();
                    it.opciones = p.getOpciones();
                    items.add(it);
                }
            }
        } catch (Exception e) {
            Log.e("IslaLista", "Error parseando payload", e);
            Toast.makeText(this, "No se pudieron leer las preguntas", Toast.LENGTH_LONG).show();
        }

        // 2) Configurar lista
        adapter = new PregListaAdapter(items, (pos, letra) -> {
            // callback opcional si quieres reaccionar a cada click
        });
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // 3) Enviar cierre
        btnEnviar.setOnClickListener(v -> enviarCierre());
    }

    private void enviarCierre() {
        if (idSesion <= 0) {
            Toast.makeText(this, "Falta id_sesion", Toast.LENGTH_LONG).show();
            return;
        }

        // Construir: { id_sesion, respuestas: [ {id_pregunta, respuesta} ] }
        List<IslaCerrarRequest.Resp> resp = new ArrayList<>();
        for (PregItem it : items) {
            String letra = (it.seleccion == null) ? "" : it.seleccion.trim().toUpperCase();
            resp.add(new IslaCerrarRequest.Resp(it.idPregunta, letra));
        }
        IslaCerrarRequest body = new IslaCerrarRequest(idSesion, resp);

        ApiService api = RetrofitClient.getInstance(getApplicationContext()).create(ApiService.class);
        api.cerrarIslaSimulacro(getToken(), body).enqueue(new Callback<IslaCerrarResultadoResponse>() {
            @Override public void onResponse(Call<IslaCerrarResultadoResponse> call, Response<IslaCerrarResultadoResponse> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(IslaPreguntasListaActivity.this,
                            "No se pudo cerrar (" + res.code() + ")", Toast.LENGTH_LONG).show();
                    return;
                }
                String resultadoJson = GsonHolder.gson().toJson(res.body());
                Intent it = new Intent(IslaPreguntasListaActivity.this, IslaResultadoActivity.class);
                it.putExtra("modalidad", modalidad);
                it.putExtra("resultado_json", resultadoJson);
                startActivity(it);
                finish();
            }

            @Override public void onFailure(Call<IslaCerrarResultadoResponse> call, Throwable t) {
                Toast.makeText(IslaPreguntasListaActivity.this,
                        "Error de red: " + (t.getMessage()==null?"desconocido":t.getMessage() ),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private int safeInt(Object o) {
        try { return Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return 0; }
    }
}
