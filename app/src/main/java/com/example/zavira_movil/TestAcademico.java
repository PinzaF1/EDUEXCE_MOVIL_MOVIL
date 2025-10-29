package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.Home.HomeActivity;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestAcademico extends AppCompatActivity {

    private RecyclerView rvPreguntas;
    private Button btnEnviar;
    private TextView tvBloque;
    private PreguntasAdapter adapter;

    private ProgressBar progressBloque;
    private int totalPreguntas = 25;

    private final List<PreguntaAcademica> preguntas = new ArrayList<>();
    private String idSesion = null;

    private final List<List<PreguntaAcademica>> bloques = new ArrayList<>();
    private final List<String> nombresBloque = new ArrayList<>();
    private int idxBloque = 0;

    private final Map<String, String> respuestasGlobales = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_academico);

        rvPreguntas   = findViewById(R.id.rvPreguntas);
        btnEnviar     = findViewById(R.id.btnEnviar);
        tvBloque      = findViewById(R.id.tvBloque);
        progressBloque = findViewById(R.id.progressBloque);

        rvPreguntas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PreguntasAdapter(this, new ArrayList<>());
        rvPreguntas.setAdapter(adapter);

        btnEnviar.setOnClickListener(v -> {
            if (bloques.isEmpty()) return;

            adapter.collectSeleccionesTo(respuestasGlobales);
            actualizarProgreso(); //

            boolean esUltimo = (idxBloque == bloques.size() - 1);
            if (esUltimo) enviar();
            else {
                idxBloque++;
                mostrarBloque();
            }
        });

        cargar();
    }

    private void cargar() {
        btnEnviar.setEnabled(false);
        btnEnviar.setText("Cargando...");

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);

        String token = com.example.zavira_movil.local.TokenManager.getToken(this);
        if (token == null || token.isEmpty()) {
            toast("No hay sesión. Inicia sesión primero.");
            btnEnviar.setEnabled(true);
            btnEnviar.setText("Siguiente");
            return;
        }
        String bearer = token.startsWith("Bearer ") ? token : "Bearer " + token;

        api.iniciar(bearer, new LinkedHashMap<>()).enqueue(new Callback<QuizInicialResponse>() {
            @Override
            public void onResponse(Call<QuizInicialResponse> call, Response<QuizInicialResponse> res) {
                btnEnviar.setText("Siguiente");
                btnEnviar.setEnabled(true);

                if (!res.isSuccessful() || res.body() == null || res.body().getPreguntas() == null) {
                    toast("Error: " + res.code());
                    return;
                }

                idSesion = res.body().getIdSesion();
                preguntas.clear();
                preguntas.addAll(res.body().getPreguntas());
                totalPreguntas = preguntas.size();

                progressBloque.setMax(totalPreguntas);

                Map<String, List<PreguntaAcademica>> porArea = new LinkedHashMap<>();
                for (PreguntaAcademica p : preguntas) {
                    porArea.computeIfAbsent(p.getArea(), k -> new ArrayList<>()).add(p);
                }

                bloques.clear();
                nombresBloque.clear();
                for (Map.Entry<String, List<PreguntaAcademica>> e : porArea.entrySet()) {
                    List<PreguntaAcademica> lista = e.getValue();
                    List<PreguntaAcademica> sub = lista.size() > 5 ? lista.subList(0, 5) : lista;
                    bloques.add(new ArrayList<>(sub));
                    nombresBloque.add(e.getKey());
                }

                if (bloques.isEmpty()) {
                    toast("No hay preguntas");
                    return;
                }

                idxBloque = 0;
                mostrarBloque();
                toast("Sesión iniciada");
            }

            @Override
            public void onFailure(Call<QuizInicialResponse> call, Throwable t) {
                btnEnviar.setText("Siguiente");
                btnEnviar.setEnabled(true);
                toast("Error de red: " + t.getMessage());
            }
        });
    }

    private void mostrarBloque() {
        List<PreguntaAcademica> bloque = bloques.get(idxBloque);
        adapter.setPreguntas(bloque, respuestasGlobales);

        tvBloque.setText(nombresBloque.get(idxBloque));

        boolean esUltimo = (idxBloque == bloques.size() - 1);
        btnEnviar.setText(esUltimo ? "Enviar respuestas" : "Siguiente");

        actualizarProgreso(); //
    }

    private void actualizarProgreso() {
        progressBloque.setProgress(respuestasGlobales.size());
    }

    private void enviar() {
        adapter.collectSeleccionesTo(respuestasGlobales);
        actualizarProgreso();

        if (respuestasGlobales.size() < preguntas.size()) {
            toast("Faltan preguntas por responder");
            return;
        }

        List<QuizCerrarRequest.RespuestaItem> items = new ArrayList<>();
        for (PreguntaAcademica p : preguntas) {
            items.add(new QuizCerrarRequest.RespuestaItem(
                    p.getIdPregunta(),
                    respuestasGlobales.get(p.getIdPregunta())
            ));
        }

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);

        String token = com.example.zavira_movil.local.TokenManager.getToken(this);
        String bearer = token.startsWith("Bearer ") ? token : "Bearer " + token;

        api.cerrar(bearer, new QuizCerrarRequest(idSesion, items))
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        showResultado("Completado ", "Respuestas enviadas correctamente");
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        showResultado("Error ", t.getMessage());
                    }
                });
    }

    private void showResultado(String titulo, String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setCancelable(false)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    startActivity(new Intent(TestAcademico.this, HomeActivity.class));
                    finish();
                })
                .show();
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
}
