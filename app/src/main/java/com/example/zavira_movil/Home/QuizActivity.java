package com.example.zavira_movil.Home;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zavira_movil.databinding.ActivityQuizBinding;
import com.example.zavira_movil.local.ProgressLockManager;
import com.example.zavira_movil.local.UserSession;
import com.example.zavira_movil.model.CerrarRequest;
import com.example.zavira_movil.model.CerrarResponse;
import com.example.zavira_movil.model.ParadaRequest;
import com.example.zavira_movil.model.ParadaResponse;
import com.example.zavira_movil.model.Question;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Crea sesión, carga máx 10 preguntas, envía y desbloquea siguiente nivel si aprueba. */
public class QuizActivity extends AppCompatActivity {

    public static final String EXTRA_AREA    = "extra_area";     // área visible
    public static final String EXTRA_SUBTEMA = "extra_subtema";  // subtema visible
    public static final String EXTRA_NIVEL   = "extra_nivel";    // 1..5

    private ActivityQuizBinding binding;
    private QuizQuestionsAdapter adapter;
    private Integer idSesion;

    private String areaUi, subtemaUi;
    private int nivel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        areaUi    = getIntent().getStringExtra(EXTRA_AREA);
        subtemaUi = getIntent().getStringExtra(EXTRA_SUBTEMA);
        nivel     = getIntent().getIntExtra(EXTRA_NIVEL, 1);

        binding.tvAreaSubtema.setText(
                (areaUi != null ? areaUi : "") + " • " + (subtemaUi != null ? subtemaUi : "")
        );

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

    /** Crea sesión y pinta preguntas aquí mismo (máximo 10). */
    private void crearParadaYMostrar() {
        setLoading(true);

        final String areaApi    = AreaMapper.toApiArea(areaUi);
        final String subtemaApi = AreaMapper.normalizeSubtema(subtemaUi);

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
                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(QuizActivity.this,
                            "No se pudo crear la sesión (HTTP " + resp.code() + ")",
                            Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                ParadaResponse pr = resp.body();
                idSesion = (pr.sesion != null) ? pr.sesion.id_sesion : null;

                ArrayList<Question> preguntas = new ArrayList<>();
                if (pr.preguntas != null) preguntas.addAll(pr.preguntas);

                if (preguntas.size() > 10) preguntas = new ArrayList<>(preguntas.subList(0, 10));
                if (preguntas.isEmpty()) {
                    Toast.makeText(QuizActivity.this,
                            "No hay preguntas para este subtema.",
                            Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                adapter = new QuizQuestionsAdapter(preguntas);
                binding.rvQuestions.setAdapter(adapter);
            }

            @Override public void onFailure(Call<ParadaResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(QuizActivity.this,
                        "Error de red: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    /** Envía respuestas a /sesion/cerrar y desbloquea siguiente nivel si aprueba. */
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
        api.cerrarSesion(new CerrarRequest(idSesion, rs)).enqueue(new Callback<CerrarResponse>() {
            @Override public void onResponse(Call<CerrarResponse> call, Response<CerrarResponse> response) {
                setLoading(false);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(QuizActivity.this,
                            "No se pudo cerrar la sesión.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                CerrarResponse r = response.body();
                Integer puntaje = r.puntaje;
                Toast.makeText(QuizActivity.this,
                        "Puntaje: " + (puntaje != null ? puntaje : 0) + "%",
                        Toast.LENGTH_LONG).show();

                if (Boolean.TRUE.equals(r.aprueba)) {
                    String userId = String.valueOf(UserSession.getInstance().getIdUsuario());
                    ProgressLockManager.unlockNext(QuizActivity.this, userId, areaUi, nivel);
                    setResult(RESULT_OK);
                }
                finish();
            }

            @Override public void onFailure(Call<CerrarResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(QuizActivity.this,
                        "Fallo al cerrar: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
