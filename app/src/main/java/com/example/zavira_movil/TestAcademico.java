package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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
    private Button btnEnviar, btnPrevBloque, btnNextBloque;
    private TextView tvBloque;
    private PreguntasAdapter adapter;

    // de backend
    private final List<PreguntaAcademica> preguntas = new ArrayList<>();
    private String idSesion = null;

    // navegación por bloques
    private final List<List<PreguntaAcademica>> bloques = new ArrayList<>();
    private final List<String> nombresBloque = new ArrayList<>(); // área de cada bloque
    private int idxBloque = 0;

    // respuestas globales (se conservan al cambiar de bloque)
    private final Map<String, String> respuestasGlobales = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_academico);

        rvPreguntas   = findViewById(R.id.rvPreguntas);
        btnEnviar     = findViewById(R.id.btnEnviar);
        btnPrevBloque = findViewById(R.id.btnPrevBloque);
        btnNextBloque = findViewById(R.id.btnNextBloque);
        tvBloque      = findViewById(R.id.tvBloque);

        rvPreguntas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PreguntasAdapter(this, new ArrayList<>());
        rvPreguntas.setAdapter(adapter);

        btnEnviar.setOnClickListener(v -> enviar());
        btnPrevBloque.setOnClickListener(v -> cambiarBloque(-1));
        btnNextBloque.setOnClickListener(v -> cambiarBloque(1));

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
            btnEnviar.setText("Enviar respuestas");
            return;
        }
        String bearer = token.startsWith("Bearer ") ? token : "Bearer " + token;

        api.iniciar(bearer, new LinkedHashMap<>()).enqueue(new Callback<QuizInicialResponse>() {
            @Override
            public void onResponse(Call<QuizInicialResponse> call, Response<QuizInicialResponse> res) {
                btnEnviar.setText("Enviar respuestas");
                btnEnviar.setEnabled(true);

                if (!res.isSuccessful() || res.body() == null || res.body().getPreguntas() == null) {
                    String err = "";
                    try { err = res.errorBody() != null ? res.errorBody().string() : ""; } catch (Exception ignored) {}
                    toast("Fallo " + res.code() + " al cargar · " + err);
                    return;
                }

                idSesion = res.body().getIdSesion();
                preguntas.clear();
                preguntas.addAll(res.body().getPreguntas()); // 25

                // agrupar por área y crear bloques de 5
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
                toast("Sesión " + idSesion + " · " + preguntas.size() + " preguntas (5 por área) ");
            }

            @Override
            public void onFailure(Call<QuizInicialResponse> call, Throwable t) {
                btnEnviar.setText("Enviar respuestas");
                btnEnviar.setEnabled(true);
                toast("Error de red: " + t.getLocalizedMessage());
            }
        });
    }

    private void mostrarBloque() {
        List<PreguntaAcademica> bloque = bloques.get(idxBloque);
        adapter.setPreguntas(bloque, respuestasGlobales);

        String nombre = nombresBloque.get(idxBloque);
        tvBloque.setText(nombre + " - Bloque " + (idxBloque + 1) + "/" + bloques.size());

        btnPrevBloque.setEnabled(idxBloque > 0);
        btnNextBloque.setEnabled(idxBloque < bloques.size() - 1);
    }

    private void cambiarBloque(int delta) {
        adapter.collectSeleccionesTo(respuestasGlobales);
        idxBloque += delta;
        if (idxBloque < 0) idxBloque = 0;
        if (idxBloque > bloques.size() - 1) idxBloque = bloques.size() - 1;
        mostrarBloque();
    }

    private void enviar() {
        if (idSesion == null || bloques.isEmpty()) {
            toast("Inicia el quiz primero");
            return;
        }

        // guarda selecciones del bloque actual
        adapter.collectSeleccionesTo(respuestasGlobales);

        // validar que estén respondidas todas
        int total = preguntas.size();
        if (respuestasGlobales.size() < total) {
            toast("Responde todas las preguntas (" + respuestasGlobales.size() + "/" + total + ")");
            return;
        }

        // construir payload en el mismo orden que 'preguntas'
        List<QuizCerrarRequest.RespuestaItem> items = new ArrayList<>();
        for (PreguntaAcademica p : preguntas) {
            String sel = respuestasGlobales.get(p.getIdPregunta());
            items.add(new QuizCerrarRequest.RespuestaItem(p.getIdPregunta(), sel));
        }

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);

        String token = com.example.zavira_movil.local.TokenManager.getToken(this);
        if (token == null || token.isEmpty()) {
            toast("No hay sesión. Inicia sesión primero.");
            return;
        }
        String bearer = token.startsWith("Bearer ") ? token : "Bearer " + token;

        api.cerrar(bearer, new QuizCerrarRequest(idSesion, items))
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> res) {
                        if (!res.isSuccessful()) {
                            String err = "";
                            try { err = res.errorBody() != null ? res.errorBody().string() : ""; } catch (Exception ignored) {}
                            showResultado("No se pudo enviar (" + res.code() + ")", err);
                            return;
                        }

                        String raw = "";
                        try { raw = res.body() != null ? res.body().string() : ""; } catch (Exception ignored) {}

                        if (raw == null || raw.trim().isEmpty()) {
                            showResultado("Resultado", "Respuestas enviadas ");
                            return;
                        }

                        // === AQUÍ PARSEO TU FORMATO DE RESPUESTA ===
                        try {
                            JSONObject obj = new JSONObject(raw);

                            int puntajeGeneral = obj.optInt("puntaje_general", -1);

                            // detalle: lista de preguntas con es_correcta
                            int aciertos = -1;
                            int totalDet = total;
                            JSONArray detalle = obj.optJSONArray("detalle");
                            if (detalle != null) {
                                int ok = 0;
                                for (int i = 0; i < detalle.length(); i++) {
                                    JSONObject d = detalle.optJSONObject(i);
                                    if (d != null && d.optBoolean("es_correcta", false)) ok++;
                                }
                                aciertos = ok;
                                totalDet = detalle.length();
                            }

                            // porcentaje (preferimos puntaje_general si viene)
                            double porcentaje = puntajeGeneral >= 0
                                    ? puntajeGeneral
                                    : (aciertos >= 0 && totalDet > 0 ? (aciertos * 100.0 / totalDet) : -1);

                            // puntaje por área
                            JSONObject porArea = obj.optJSONObject("puntajes_por_area");

                            // Mensaje bonito
                            StringBuilder sb = new StringBuilder("¡Respuestas enviadas!\n\n");
                            if (aciertos >= 0) {
                                sb.append("Aciertos: ").append(aciertos).append("/").append(totalDet);
                                if (porcentaje >= 0) {
                                    sb.append(" (").append(Math.round(porcentaje)).append("%)");
                                }
                                sb.append("\n");
                            }
                            if (puntajeGeneral >= 0) {
                                sb.append("Puntaje general: ").append(puntajeGeneral).append("%\n");
                            }
                            if (porArea != null) {
                                sb.append("\nPor área:\n");
                                // orden preferido
                                String[] orden = {"Matemáticas","Lenguaje","Ciencias","Sociales","Inglés"};
                                boolean alguno = false;
                                for (String k : orden) {
                                    if (porArea.has(k)) {
                                        sb.append("• ").append(k).append(": ")
                                                .append(porArea.optInt(k)).append("%\n");
                                        alguno = true;
                                    }
                                }
                                // cualquier otra área no prevista
                                if (!alguno) {
                                    // listar todo lo que venga
                                    for (java.util.Iterator<String> it = porArea.keys(); it.hasNext();) {
                                        String k = it.next();
                                        sb.append("• ").append(k).append(": ")
                                                .append(porArea.optInt(k)).append("%\n");
                                    }
                                }
                            }

                            showResultado("Resultado", sb.toString().trim());
                        } catch (Exception e) {
                            // si no era ese formato, muestro texto tal cual
                            showResultado("Resultado", raw);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        showResultado("Error de red", t.getMessage());
                    }
                });
    }

    private void showResultado(String titulo, String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje == null ? "" : mensaje)
                .setCancelable(false)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    dialog.dismiss();
                    // 🔹 Ir al HomeActivity
                    startActivity(new Intent(TestAcademico.this, HomeActivity.class));
                    finish(); // cierra la pantalla actual
                })
                .show();
    }


    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }
}
