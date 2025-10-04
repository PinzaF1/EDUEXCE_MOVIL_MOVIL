package com.example.zavira_movil.Home;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zavira_movil.databinding.ActivitySimulacroBinding;
import com.example.zavira_movil.local.ProgressLockManager;
import com.example.zavira_movil.local.UserSession;  // ✅ Import agregado
import com.example.zavira_movil.model.CerrarRequest;
import com.example.zavira_movil.model.CerrarResponse;
import com.example.zavira_movil.model.Question;
import com.example.zavira_movil.model.SimulacroRequest;
import com.example.zavira_movil.model.SimulacroResponse;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla para ejecutar un simulacro completo:
 * - Crear simulacro con 25 preguntas
 * - Responder preguntas y enviar respuestas
 * - Recibir resultados de puntaje y correctas
 */
public class SimulacroActivity extends AppCompatActivity {

    private ActivitySimulacroBinding binding;
    private QuizQuestionsAdapter adapter;
    private Integer idSesion;
    private int intentosFallidos = 0;

    // Por defecto, área y subtemas
    private String area = "Sociales";
    private List<String> subtemas = Arrays.asList(
            "Geografía", "Historia", "Economía", "Ciudadanía", "Pensamiento social"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySimulacroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar RecyclerView
        binding.rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuizQuestionsAdapter(new ArrayList<>());
        binding.rvQuestions.setAdapter(adapter);

        binding.btnEnviar.setOnClickListener(v -> enviar());

        crearSimulacro();
    }

    private void setLoading(boolean b) {
        binding.progress.setVisibility(b ? View.VISIBLE : View.GONE);
        binding.btnEnviar.setEnabled(!b);
    }

    private void crearSimulacro() {
        setLoading(true);

        SimulacroRequest req = new SimulacroRequest(area, subtemas);

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.crearSimulacro(req).enqueue(new Callback<SimulacroResponse>() {
            @Override
            public void onResponse(Call<SimulacroResponse> call, Response<SimulacroResponse> response) {
                setLoading(false);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(SimulacroActivity.this,
                            "⚠️ Error al crear simulacro. Código: " + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                SimulacroResponse sim = response.body();
                idSesion = (sim.sesion != null) ? sim.sesion.idSesion : null;

                List<Question> preguntas = sim.preguntas;
                if (preguntas == null || preguntas.isEmpty()) {
                    Toast.makeText(SimulacroActivity.this,
                            "No hay preguntas disponibles",
                            Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                if (preguntas.size() > 25) {
                    preguntas = preguntas.subList(0, 25);
                }

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
                            "Error al cerrar simulacro",
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
                        // ✅ Ahora se pasa userId también
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
