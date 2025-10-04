package com.example.zavira_movil.Home;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.IslaCerrarRequest;
import com.example.zavira_movil.model.IslaCerrarResponse;
import com.example.zavira_movil.model.Question;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IslaPreguntasActivity extends AppCompatActivity {

    private TextView tvPregunta, tvTimer;
    private RadioGroup rgOpciones;
    private Button btnSiguiente;

    private List<Question> preguntas;
    private int index = 0;
    private int idSesion;
    private String modalidad;

    private List<IslaCerrarRequest.Respuesta> respuestas = new ArrayList<>();
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_isla_preguntas);

        tvPregunta = findViewById(R.id.tvPregunta);
        tvTimer = findViewById(R.id.tvTimer);
        rgOpciones = findViewById(R.id.rgOpciones);
        btnSiguiente = findViewById(R.id.btnSiguiente);

        preguntas = (List<Question>) getIntent().getSerializableExtra("preguntas");
        idSesion = getIntent().getIntExtra("idSesion", -1);
        modalidad = getIntent().getStringExtra("modalidad");

        mostrarPregunta();

        btnSiguiente.setOnClickListener(v -> {
            guardarRespuesta();
            if (index < preguntas.size()) {
                mostrarPregunta();
            } else {
                enviarRespuestas();
            }
        });
    }

    private void mostrarPregunta() {
        rgOpciones.clearCheck();
        Question q = preguntas.get(index);
        tvPregunta.setText((index + 1) + " de " + preguntas.size() + "\n" + q.enunciado);

        // Mostrar las opciones dinámicamente
        for (int i = 0; i < rgOpciones.getChildCount(); i++) {
            RadioButton rb = (RadioButton) rgOpciones.getChildAt(i);
            if (i < q.opciones.size()) {
                Question.Option opt = q.opciones.get(i);
                rb.setText(opt.key + ") " + opt.text);
                rb.setVisibility(RadioButton.VISIBLE);
            } else {
                rb.setVisibility(RadioButton.GONE);
            }
        }

        index++;

        // Timer solo en modalidad difícil
        if ("dificil".equals(modalidad)) {
            if (timer != null) timer.cancel();
            timer = new CountDownTimer(60000, 1000) {
                public void onTick(long ms) {
                    tvTimer.setText("Tiempo: " + (ms / 1000) + "s");
                }

                public void onFinish() {
                    guardarRespuesta();
                    if (index < preguntas.size()) {
                        mostrarPregunta();
                    } else {
                        enviarRespuestas();
                    }
                }
            }.start();
        } else {
            tvTimer.setText("Sin límite de tiempo");
        }
    }

    private void guardarRespuesta() {
        int checkedId = rgOpciones.getCheckedRadioButtonId();
        String opcion = "";
        if (checkedId != -1) {
            int pos = rgOpciones.indexOfChild(findViewById(checkedId));
            if (pos >= 0 && pos < preguntas.get(index - 1).opciones.size()) {
                opcion = preguntas.get(index - 1).opciones.get(pos).key;
            }
        }
        respuestas.add(new IslaCerrarRequest.Respuesta(index, opcion, 0));
    }

    private void enviarRespuestas() {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        IslaCerrarRequest req = new IslaCerrarRequest(idSesion, respuestas);

        api.cerrarIslaSimulacro(req).enqueue(new Callback<IslaCerrarResponse>() {
            @Override
            public void onResponse(Call<IslaCerrarResponse> call, Response<IslaCerrarResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Intent i = new Intent(IslaPreguntasActivity.this, IslaResultadoActivity.class);
                    i.putExtra("idSesion", idSesion);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(IslaPreguntasActivity.this, "Error al cerrar simulacro", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<IslaCerrarResponse> call, Throwable t) {
                Toast.makeText(IslaPreguntasActivity.this, "Fallo: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
