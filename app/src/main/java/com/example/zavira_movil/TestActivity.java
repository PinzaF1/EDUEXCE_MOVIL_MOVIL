package com.example.zavira_movil;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.zavira_movil.Home.HomeActivity;
import com.example.zavira_movil.model.KolbRequest;
import com.example.zavira_movil.model.KolbResponse;
import com.example.zavira_movil.model.PreguntasKolb;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestActivity extends AppCompatActivity {

    private LinearLayout container;
    private Button btnEnviar;
    private ProgressBar progressBar;
    private TextView tvProgresoBloque;

    private final List<PreguntasKolb> preguntas = new ArrayList<>();
    private final android.util.SparseIntArray respuestas = new android.util.SparseIntArray();

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_test);

        container = findViewById(R.id.questionsContainer);
        btnEnviar = findViewById(R.id.btnEnviar);
        progressBar = findViewById(R.id.progressBloque);
        tvProgresoBloque = findViewById(R.id.tvProgresoBloque);

        // Barra de progreso vacía
        progressBar.setProgress(0);
        tvProgresoBloque.setText("Progreso: 0 / 36");

        btnEnviar.setOnClickListener(v -> enviar());
        cargar();
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    private void cargar() {
        btnEnviar.setEnabled(false);
        btnEnviar.setText("Cargando...");

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.getPreguntas().enqueue(new Callback<List<PreguntasKolb>>() {
            @Override
            public void onResponse(Call<List<PreguntasKolb>> c, Response<List<PreguntasKolb>> r) {
                btnEnviar.setText("Enviar respuestas");
                if (!r.isSuccessful() || r.body() == null) {
                    toast("No se pudieron cargar las preguntas (" + r.code() + ")");
                    return;
                }
                preguntas.clear();
                preguntas.addAll(r.body());
                render();
                btnEnviar.setEnabled(true);
            }

            @Override
            public void onFailure(Call<List<PreguntasKolb>> c, Throwable t) {
                btnEnviar.setText("Enviar respuestas");
                toast("Error al cargar: " + t.getMessage());
            }
        });
    }

    private void render() {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);

        for (int i = 0; i < preguntas.size(); i++) {
            PreguntasKolb p = preguntas.get(i);

            // CARD por cada pregunta
            CardView card = new CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 16, 0, 16);
            card.setLayoutParams(cardParams);
            card.setRadius(18f);
            card.setCardElevation(6f);
            card.setUseCompatPadding(true);
            card.setCardBackgroundColor(getResources().getColor(android.R.color.white));

            LinearLayout inner = new LinearLayout(this);
            inner.setOrientation(LinearLayout.VERTICAL);
            inner.setPadding(24, 24, 24, 24);

            TextView titulo = new TextView(this);
            titulo.setText(p.getTitulo() != null ? p.getTitulo() : p.getTipo_pregunta());
            titulo.setTextSize(16);
            titulo.setTextColor(getResources().getColor(R.color.primaryyy));
            titulo.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);

            TextView enunciado = new TextView(this);
            enunciado.setText(p.getPregunta());
            enunciado.setTextSize(15);
            enunciado.setTextColor(getResources().getColor(android.R.color.black));
            enunciado.setPadding(0, 8, 0, 16);

            RadioGroup group = new RadioGroup(this);
            group.setOrientation(RadioGroup.VERTICAL);

            for (int v = 1; v <= 4; v++) {
                RadioButton rb = new RadioButton(this);
                rb.setText("Opción " + v);
                rb.setTag(v);
                rb.setTextColor(getResources().getColor(R.color.primaryyy));
                group.addView(rb);
            }

            final int idx = i;
            group.setOnCheckedChangeListener((gr, id) -> {
                RadioButton rb = gr.findViewById(id);
                if (rb != null) {
                    respuestas.put(idx, (int) rb.getTag());
                    actualizarProgreso();
                }
            });

            inner.addView(titulo);
            inner.addView(enunciado);
            inner.addView(group);
            card.addView(inner);
            container.addView(card);

            card.startAnimation(anim);
        }
    }

    private void actualizarProgreso() {
        int respondidas = 0;
        for (int i = 0; i < preguntas.size(); i++) {
            if (respuestas.get(i, 0) != 0) respondidas++;
        }

        int total = 36;
        int porcentaje = (int) ((respondidas / (float) total) * 100);

        ObjectAnimator anim = ObjectAnimator.ofInt(progressBar, "progress", progressBar.getProgress(), porcentaje);
        anim.setDuration(500);
        anim.start();

        tvProgresoBloque.setText("Progreso: " + respondidas + " / " + total);
    }

    private void enviar() {
        int total = preguntas.size();
        for (int i = 0; i < total; i++) {
            if (respuestas.get(i, 0) == 0) {
                toast("Responde todas antes de enviar");
                return;
            }
        }

        List<KolbRequest.Item> items = new ArrayList<>(total);
        for (int i = 0; i < total; i++) {
            PreguntasKolb p = preguntas.get(i);
            int idPregunta = p.getId_pregunta_estilo_aprendizajes();
            int valorElegido = respuestas.get(i);
            items.add(new KolbRequest.Item(idPregunta, valorElegido));
        }

        btnEnviar.setEnabled(false);
        btnEnviar.setText("Enviando...");

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        KolbRequest body = new KolbRequest(items);

        api.guardarRespuestas(body).enqueue(new Callback<KolbResponse>() {
            @Override
            public void onResponse(Call<KolbResponse> c, Response<KolbResponse> r) {
                btnEnviar.setEnabled(true);
                btnEnviar.setText("Enviar respuestas");

                if (!r.isSuccessful() || r.body() == null) {
                    toast("Error " + r.code());
                    return;
                }

                KolbResponse k = r.body();
                Intent i = new Intent(TestActivity.this, ResultActivity.class);
                i.putExtra("estilo", k.getEstiloDominante());
                i.putExtra("caracteristicas", k.getCaracteristicas());
                i.putExtra("recomendaciones", k.getRecomendaciones());
                startActivity(i);
            }

            @Override
            public void onFailure(Call<KolbResponse> c, Throwable t) {
                btnEnviar.setEnabled(true);
                btnEnviar.setText("Enviar respuestas");
                toast("Fallo: " + t.getMessage());
            }
        });
    }

    private void goToHome() {
        Intent i = new Intent(this, HomeActivity.class);
        startActivity(i);
        finish();
    }
}
