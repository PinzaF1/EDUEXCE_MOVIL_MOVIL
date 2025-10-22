package com.example.zavira_movil.niveleshome;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zavira_movil.QuizQuestionsAdapter;
import com.example.zavira_movil.databinding.ActivityQuizBinding;
import com.example.zavira_movil.niveleshome.ProgressLockManager;
import com.example.zavira_movil.local.UserSession;
import com.example.zavira_movil.model.Question;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Crea sesión, carga máx 10 preguntas, envía y desbloquea/retrocede según puntaje. */
public class QuizActivity extends AppCompatActivity {

    public static final String EXTRA_AREA    = "extra_area";     // área visible UI
    public static final String EXTRA_SUBTEMA = "extra_subtema";  // subtema visible UI
    public static final String EXTRA_NIVEL   = "extra_nivel";    // 1..5

    private ActivityQuizBinding binding;
    private QuizQuestionsAdapter adapter;
    private Integer idSesion;

    private String areaUi, subtemaUi; // usamos UI para ProgressLockManager
    private int nivel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        areaUi    = getIntent().getStringExtra(EXTRA_AREA);
        subtemaUi = getIntent().getStringExtra(EXTRA_SUBTEMA);
        nivel     = getIntent().getIntExtra(EXTRA_NIVEL, 1);

        binding.tvAreaSubtema.setText((areaUi != null ? areaUi : "") + " • " + (subtemaUi != null ? subtemaUi : ""));

        binding.rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuizQuestionsAdapter(new ArrayList<>());
        binding.rvQuestions.setAdapter(adapter);

        binding.btnEnviar.setOnClickListener(v -> enviar());

        crearParadaYMostrar();
    }

    private void setLoading(boolean b) {
        binding.progress.setVisibility(b ? View.VISIBLE : View.GONE);
        binding.btnEnviar.setEnabled(!b);
    }

    private static Integer toIntOrNull(String s) {
        try { return (s == null) ? null : Integer.parseInt(s); } catch (Exception e) { return null; }
    }

    /** Crea sesión y pinta preguntas (máximo 10). */
    private void crearParadaYMostrar() {
        setLoading(true);

        final String areaApi    = MapeadorArea.toApiArea(areaUi);
        final String subtemaApi = MapeadorArea.normalizeSubtema(subtemaUi);

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        ParadaRequest req = new ParadaRequest(
                areaApi != null ? areaApi : "",
                subtemaApi != null ? subtemaApi : "",
                Math.max(1, Math.min(5, nivel)),
                true,
                1
        );

        api.crearParada(req).enqueue(new Callback<ParadaResponse>() {
            @Override public void onResponse(Call<ParadaResponse> call, Response<ParadaResponse> resp) {
                setLoading(false);

                if (!resp.isSuccessful()) {
                    Toast.makeText(QuizActivity.this,
                            "No se pudo crear la sesión (HTTP " + resp.code() + ")",
                            Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                ParadaResponse pr = resp.body();
                if (pr == null) {
                    Toast.makeText(QuizActivity.this,
                            "Servidor respondió " + resp.code() + " sin cuerpo JSON.",
                            Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                if (pr.sesion != null) idSesion = toIntOrNull(pr.sesion.idSesion);

                ArrayList<ApiQuestion> apiQs = new ArrayList<>();
                if (pr.preguntas != null) apiQs.addAll(pr.preguntas);
                if (pr.preguntasPorSubtema != null) apiQs.addAll(pr.preguntasPorSubtema);
                if (pr.sesion != null) {
                    if (pr.sesion.preguntas != null) apiQs.addAll(pr.sesion.preguntas);
                    if (pr.sesion.preguntasPorSubtema != null) apiQs.addAll(pr.sesion.preguntasPorSubtema);
                }

                ArrayList<Question> preguntas = ApiQuestionMapper.toAppList(apiQs);
                if (preguntas.size() > 10) preguntas = new ArrayList<>(preguntas.subList(0, 10));
                if (preguntas.isEmpty()) {
                    Toast.makeText(QuizActivity.this, "No hay preguntas para este subtema.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                adapter = new QuizQuestionsAdapter(preguntas);
                binding.rvQuestions.setAdapter(adapter);
            }

            @Override public void onFailure(Call<ParadaResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(QuizActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    /** Envía respuestas: intenta NUEVO y si falla con “cannot extract elements from an object”, reintenta LEGACY. */
    private void enviar() {
        if (adapter.getItemCount() == 0) {
            Toast.makeText(this, "No hay preguntas.", Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> marcadas = adapter.getMarcadas();
        List<CerrarRequest.Respuesta> rs = new ArrayList<>();
        for (int i = 0; i < marcadas.size(); i++) {
            String k = marcadas.get(i);
            if (k == null) {
                Toast.makeText(this, "Responde todas las preguntas.", Toast.LENGTH_SHORT).show();
                return;
            }
            rs.add(new CerrarRequest.Respuesta(i + 1, k));
        }
        if (idSesion == null) {
            Toast.makeText(this, "No hay sesión activa.", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);

        // Primer intento: formato NUEVO
        api.cerrarSesion(new CerrarRequest(idSesion, rs)).enqueue(new Callback<CerrarResponse>() {
            @Override public void onResponse(Call<CerrarResponse> call, Response<CerrarResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    onCierreOk(response.body());
                    return;
                }

                // ¿Mensaje típico del server legacy?
                String err = readErr(response.errorBody());
                boolean esLegacy = response.code() == 400 &&
                        err != null && err.contains("cannot extract elements from an object");

                if (esLegacy) {
                    // Reintento: LEGACY
                    Map<String, Object> compat = new HashMap<>();
                    compat.put("id_sesion", idSesion);
                    List<List<Object>> legacyRs = new ArrayList<>();
                    for (CerrarRequest.Respuesta r : rs) {
                        List<Object> par = new ArrayList<>();
                        par.add(r.opcion);
                        par.add(r.orden);
                        legacyRs.add(par);
                    }
                    compat.put("respuestas", legacyRs);

                    api.cerrarSesionCompat(compat).enqueue(new Callback<CerrarResponse>() {
                        @Override public void onResponse(Call<CerrarResponse> call2, Response<CerrarResponse> resp2) {
                            setLoading(false);
                            if (resp2.isSuccessful() && resp2.body() != null) {
                                onCierreOk(resp2.body());
                            } else {
                                Toast.makeText(QuizActivity.this,
                                        "No se pudo cerrar (compat) HTTP " + resp2.code(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override public void onFailure(Call<CerrarResponse> call2, Throwable t) {
                            setLoading(false);
                            Toast.makeText(QuizActivity.this, "Fallo compat: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    setLoading(false);
                    Toast.makeText(QuizActivity.this,
                            "No se pudo cerrar la sesión (HTTP " + response.code() + ").",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override public void onFailure(Call<CerrarResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(QuizActivity.this, "Fallo al cerrar: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void onCierreOk(CerrarResponse r) {
        setLoading(false);
        Integer puntaje = r.puntaje;
        Toast.makeText(this,
                "Correctas: " + r.correctas + " | Puntaje: " + (puntaje != null ? puntaje : 0) + "%", Toast.LENGTH_LONG).show();

        String userId = String.valueOf(UserSession.getInstance().getIdUsuario());

        if (Boolean.TRUE.equals(r.aprueba)) {
            // Si aprueba nivel 5, desbloquea Examen Final; si no, avanza normal
            if (nivel >= 5 && (puntaje != null && puntaje >= 80)) {
                ProgressLockManager.unlockFinalExam(this, userId, areaUi);
            } else {
                ProgressLockManager.unlockNext(this, userId, areaUi, nivel);
            }
            setResult(RESULT_OK);
        } else {
            // Retroceso de nivel si <80% y nivel>1
            if (puntaje == null || puntaje < 80) {
                ProgressLockManager.retrocederPorFallo(this, userId, areaUi, nivel);
            }
        }
        finish();
    }

    private static String readErr(ResponseBody eb) {
        if (eb == null) return null;
        try { return eb.string(); } catch (IOException ignored) { return null; }
    }
}
