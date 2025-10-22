package com.example.zavira_movil.niveleshome;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zavira_movil.databinding.ActivitySimulacroBinding;
import com.example.zavira_movil.local.UserSession;
import com.example.zavira_movil.model.Question;
import com.example.zavira_movil.model.SimulacroRequest;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Simulacro completo: 25 preguntas, envía respuestas y muestra puntaje. */
public class SimulacroActivity extends AppCompatActivity {

    private ActivitySimulacroBinding binding;
    private QuizQuestionsAdapter adapter;
    private Integer idSesion;
    private int intentosFallidos = 0;

    private String area;                // mapeada a API
    private List<String> subtemas;      // normalizados

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySimulacroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuizQuestionsAdapter(new ArrayList<>());
        binding.rvQuestions.setAdapter(adapter);

        // Recibe desde UI
        String areaIn = getIntent().getStringExtra("area");
        ArrayList<String> subsIn = getIntent().getStringArrayListExtra("subtemas");
        if (areaIn == null || areaIn.trim().isEmpty()) areaIn = "Sociales y ciudadanas";
        if (subsIn == null) subsIn = new ArrayList<>();

        // ÚNICO mapeo UI->API (área/subtema)
        this.area = MapeadorArea.toApiArea(areaIn);
        this.subtemas = new ArrayList<>();
        for (String s : subsIn) this.subtemas.add(MapeadorArea.normalizeSubtema(s));

        binding.btnEnviar.setOnClickListener(v -> enviar());

        crearSimulacro();
    }

    private void setLoading(boolean b) {
        binding.progress.setVisibility(b ? View.VISIBLE : View.GONE);
        binding.btnEnviar.setEnabled(!b);
    }

    private static Integer toIntOrNull(String s) {
        try { return (s == null) ? null : Integer.parseInt(s); }
        catch (Exception e) { return null; }
    }

    private void crearSimulacro() {
        setLoading(true);

        SimulacroRequest req = new SimulacroRequest(area, subtemas);

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.crearSimulacro(req).enqueue(new Callback<SimulacroResponse>() {
            @Override
            public void onResponse(Call<SimulacroResponse> call, Response<SimulacroResponse> response) {
                setLoading(false);

                if (!response.isSuccessful()) {
                    Toast.makeText(SimulacroActivity.this,
                            "⚠️ Error al crear simulacro. Código: " + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                SimulacroResponse sim = response.body();
                if (sim == null) {
                    Toast.makeText(SimulacroActivity.this,
                            "Servidor respondió " + response.code() + " sin cuerpo JSON.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (sim.sesion != null) {
                    idSesion = toIntOrNull(sim.sesion.idSesion);
                }

                List<ApiQuestion> apiQs = sim.preguntas;
                if (apiQs == null || apiQs.isEmpty()) {
                    Toast.makeText(SimulacroActivity.this,
                            "No hay preguntas disponibles",
                            Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                ArrayList<Question> preguntas = ApiQuestionMapper.toAppList(apiQs);
                if (preguntas.size() > 25) preguntas = new ArrayList<>(preguntas.subList(0, 25));

                adapter = new QuizQuestionsAdapter(preguntas);
                binding.rvQuestions.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<SimulacroResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(SimulacroActivity.this,
                        "❌ Error de red: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void enviar() {
        if (idSesion == null) {
            Toast.makeText(this,
                    "⚠️ No hay sesión activa. Vuelve a generar el simulacro.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        List<String> marcadas = adapter.getMarcadas();
        List<CerrarRequest.Respuesta> rs = new ArrayList<>();

        for (int i = 0; i < marcadas.size(); i++) {
            if (marcadas.get(i) == null) {
                Toast.makeText(this,
                        "Responde todas las preguntas antes de enviar.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            rs.add(new CerrarRequest.Respuesta(i + 1, marcadas.get(i)));
        }

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.cerrarSimulacro(new CerrarRequest(idSesion, rs)).enqueue(new Callback<CerrarResponse>() {
            @Override
            public void onResponse(Call<CerrarResponse> call, Response<CerrarResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(SimulacroActivity.this,
                            "Error al cerrar simulacro (HTTP " + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                CerrarResponse r = response.body();
                Toast.makeText(SimulacroActivity.this,
                        "Correctas: " + r.correctas + " | Puntaje: " + r.puntaje + "%",
                        Toast.LENGTH_LONG).show();

                if (Boolean.TRUE.equals(r.aprueba)) {
                    Toast.makeText(SimulacroActivity.this,
                            "✅ ¡Simulacro aprobado!",
                            Toast.LENGTH_LONG).show();
                } else {
                    intentosFallidos++;
                    if (intentosFallidos >= 3) {
                        String userId = String.valueOf(UserSession.getInstance().getIdUsuario());
                        ProgressLockManager.retrocederNivel(SimulacroActivity.this, userId, area);

                        intentosFallidos = 0;
                        int nivelActual = ProgressLockManager.getUnlockedLevel(
                                SimulacroActivity.this, userId, area
                        );
                        Toast.makeText(SimulacroActivity.this,
                                "⚠️ Retrocedes un nivel. Nivel actual: " + nivelActual,
                                Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<CerrarResponse> call, Throwable t) {
                Toast.makeText(SimulacroActivity.this,
                        "Fallo al cerrar simulacro: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
