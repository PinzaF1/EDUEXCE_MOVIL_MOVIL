package com.example.zavira_movil.HislaConocimiento;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.R;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IslaPreguntasActivity extends AppCompatActivity {

    private static final int LIMITE_SEG_DIFICIL = 60;
    private static final String[] ABCD = {"A","B","C","D"};

    private TextView tvIndex, tvTimer, tvProgresoBloque, tvTitulo, tvEnunciado;
    private ProgressBar progressBloque;
    private Button btnOp1, btnOp2, btnOp3, btnOp4, btnResponder;

    private String modalidad; // "facil" | "dificil"
    private IslaSimulacroResponse data;
    private final List<PreguntaUI> preguntas = new ArrayList<>();
    private int idx = 0;
    private long inicioPreguntaElapsed = 0L;
    private CountDownTimer timerActual;

    private static class PreguntaUI {
        int id;
        String area, subtema, enunciado;
        List<String> opciones;
        String elegida; // "A".."D" o null
    }

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_isla_preguntas);

        tvIndex = findViewById(R.id.tvIndex);
        tvTimer = findViewById(R.id.tvTimer);
        tvProgresoBloque = findViewById(R.id.tvProgresoBloque);
        tvTitulo = findViewById(R.id.tvTitulo);
        tvEnunciado = findViewById(R.id.tvEnunciado);
        progressBloque = findViewById(R.id.progressBloque);
        btnOp1 = findViewById(R.id.btnOp1);
        btnOp2 = findViewById(R.id.btnOp2);
        btnOp3 = findViewById(R.id.btnOp3);
        btnOp4 = findViewById(R.id.btnOp4);
        btnResponder = findViewById(R.id.btnResponder);

        modalidad = getIntent().getStringExtra("modalidad");
        String payload = getIntent().getStringExtra("payload");
        data = GsonHolder.gson().fromJson(payload, IslaSimulacroResponse.class);

        tvTimer.setVisibility("dificil".equalsIgnoreCase(modalidad) ? View.VISIBLE : View.GONE);

        if (data == null || data.getPreguntas() == null || data.getPreguntas().isEmpty()) {
            Toast.makeText(this, "No se recibieron preguntas", Toast.LENGTH_LONG).show();
            finish(); return;
        }

        // Adaptar a UI
        for (IslaSimulacroResponse.PreguntaDto p : data.getPreguntas()) {
            PreguntaUI q = new PreguntaUI();
            try { q.id = Integer.parseInt(String.valueOf(p.getIdPregunta())); } catch (Exception e) { q.id = 0; }
            q.area = p.getArea();
            q.subtema = p.getSubtema();
            q.enunciado = p.getEnunciado();
            q.opciones = p.getOpciones();
            q.elegida = null;
            preguntas.add(q);
        }

        progressBloque.setMax(preguntas.size());

        View.OnClickListener l = v -> {
            if      (v == btnOp1) seleccionar("A");
            else if (v == btnOp2) seleccionar("B");
            else if (v == btnOp3) seleccionar("C");
            else if (v == btnOp4) seleccionar("D");
        };
        btnOp1.setOnClickListener(l);
        btnOp2.setOnClickListener(l);
        btnOp3.setOnClickListener(l);
        btnOp4.setOnClickListener(l);

        btnResponder.setOnClickListener(v -> {
            if (idx < preguntas.size() - 1) {
                idx++; pintarPregunta();
            } else {
                cerrarSimulacro();
            }
        });

        pintarPregunta();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (timerActual != null) timerActual.cancel();
    }

    private void seleccionar(String letra) {
        preguntas.get(idx).elegida = letra;
        marcarSeleccion(letra);
    }

    private void marcarSeleccion(String letra) {
        btnOp1.setSelected("A".equals(letra));
        btnOp2.setSelected("B".equals(letra));
        btnOp3.setSelected("C".equals(letra));
        btnOp4.setSelected("D".equals(letra));
    }

    private void pintarPregunta() {
        if (timerActual != null) timerActual.cancel();

        PreguntaUI q = preguntas.get(idx);
        tvIndex.setText((idx + 1) + " / " + preguntas.size());
        progressBloque.setProgress(idx + 1);
        tvProgresoBloque.setText("Progreso: " + (idx + 1) + " / " + preguntas.size());

        String titulo = (q.area == null ? "Isla" : q.area)
                + ((q.subtema == null || q.subtema.isEmpty()) ? "" : " • " + q.subtema);
        tvTitulo.setText(titulo);
        tvEnunciado.setText(q.enunciado);

        btnOp1.setText(safeOpt(0, q.opciones));
        btnOp2.setText(safeOpt(1, q.opciones));
        btnOp3.setText(safeOpt(2, q.opciones));
        btnOp4.setText(safeOpt(3, q.opciones));
        marcarSeleccion(q.elegida);

        btnResponder.setText(idx == preguntas.size() - 1 ? "Finalizar" : "Responder");
        inicioPreguntaElapsed = SystemClock.elapsedRealtime();

        if ("dificil".equalsIgnoreCase(modalidad)) {
            tvTimer.setText("00:60");
            timerActual = new CountDownTimer(LIMITE_SEG_DIFICIL * 1000L, 1000L) {
                @Override public void onTick(long ms) {
                    int s = (int) Math.ceil(ms / 1000.0);
                    tvTimer.setText(String.format(Locale.getDefault(), "00:%02d", s));
                }
                @Override public void onFinish() {
                    if (preguntas.get(idx).elegida == null) {
                        preguntas.get(idx).elegida = ""; // nula
                    }
                    if (idx < preguntas.size() - 1) {
                        idx++; pintarPregunta();
                    } else {
                        cerrarSimulacro();
                    }
                }
            }.start();
        }
    }

    private String safeOpt(int i, List<String> ops) {
        if (ops == null || ops.size() <= i) return ABCD[i] + ".";
        String t = ops.get(i) == null ? "" : ops.get(i).trim();
        // No dupliques si ya viene "A. ...":
        if (t.matches("^\\s*[A-Da-d][\\.)]\\s+.*")) return t;
        return ABCD[i] + ". " + t;
    }

    private void cerrarSimulacro() {
        int idSesion = 0;
        try { idSesion = Integer.parseInt(String.valueOf(data.getSesion().getIdSesion())); } catch (Exception ignore) {}

        if (idSesion <= 0) {
            Toast.makeText(this, "Falta id_sesion", Toast.LENGTH_LONG).show();
            return;
        }

        List<IslaCerrarRequest.Resp> resps = new ArrayList<>();
        for (PreguntaUI q : preguntas) {
            String letra = q.elegida == null ? "" : q.elegida.trim().toUpperCase();
            resps.add(new IslaCerrarRequest.Resp(q.id, letra));
        }
        IslaCerrarRequest body = new IslaCerrarRequest(idSesion, resps);

        ApiService api = RetrofitClient.getInstance(getApplicationContext()).create(ApiService.class);
        api.cerrarIslaSimulacro(body).enqueue(new Callback<IslaCerrarResultadoResponse>() {
            @Override public void onResponse(Call<IslaCerrarResultadoResponse> call, Response<IslaCerrarResultadoResponse> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(IslaPreguntasActivity.this, "No se pudo cerrar ("+res.code()+")", Toast.LENGTH_LONG).show();
                    return;
                }
                String resultadoJson = GsonHolder.gson().toJson(res.body());
                Intent it = new Intent(IslaPreguntasActivity.this, IslaResultadoActivity.class);
                it.putExtra("modalidad", modalidad);
                it.putExtra("resultado_json", resultadoJson);
                startActivity(it);
                finish();
            }
            @Override public void onFailure(Call<IslaCerrarResultadoResponse> call, Throwable t) {
                Log.e("IslaCerrar", "Error de red", t);
                Toast.makeText(IslaPreguntasActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
