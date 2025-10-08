package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

    private final List<PreguntasKolb> preguntas = new ArrayList<>();
    private final android.util.SparseIntArray respuestas = new android.util.SparseIntArray();

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_test);

        container = findViewById(R.id.questionsContainer);
        btnEnviar  = findViewById(R.id.btnEnviar);

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
            @Override public void onResponse(Call<List<PreguntasKolb>> c, Response<List<PreguntasKolb>> r) {
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

            @Override public void onFailure(Call<List<PreguntasKolb>> c, Throwable t) {
                btnEnviar.setText("Enviar respuestas");
                toast("Error al cargar: " + t.getMessage());
            }
        });
    }

    private void render() {
        container.removeAllViews();

        for (int i = 0; i < preguntas.size(); i++) {
            PreguntasKolb p = preguntas.get(i);

            TextView titulo = new TextView(this);
            titulo.setText(p.getTitulo() != null ? p.getTitulo() : p.getTipo_pregunta());
            titulo.setTextSize(15);

            TextView enunciado = new TextView(this);
            enunciado.setText(p.getPregunta());
            enunciado.setTextSize(14);

            RadioGroup group = new RadioGroup(this);
            group.setOrientation(RadioGroup.VERTICAL);
            for (int v = 1; v <= 4; v++) {
                RadioButton rb = new RadioButton(this);
                rb.setText(String.valueOf(v));
                rb.setTag(v);
                group.addView(rb);
            }

            final int idx = i;
            group.setOnCheckedChangeListener((gr, id) -> {
                RadioButton rb = gr.findViewById(id);
                if (rb != null) respuestas.put(idx, (int) rb.getTag());
            });

            container.addView(titulo);
            container.addView(enunciado);
            container.addView(group);
        }
    }

    private void enviar() {
        int total = preguntas.size(); // normalmente 36
        for (int i = 0; i < total; i++) {
            if (respuestas.get(i, 0) == 0) {
                toast("Responde todas antes de enviar");
                return;
            }
        }

        // Armamos { respuestas: [{ id_item, valor }, ...] }
        List<KolbRequest.Item> items = new ArrayList<>(total);
        for (int i = 0; i < total; i++) {
            PreguntasKolb p = preguntas.get(i);
            int idPregunta = p.getId_pregunta_estilo_aprendizajes(); // <-- asegúrate que tu modelo tenga este getter
            int valorElegido = respuestas.get(i);                    // 1..4
            items.add(new KolbRequest.Item(idPregunta, valorElegido));
        }

        btnEnviar.setEnabled(false);
        btnEnviar.setText("Enviando...");

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        KolbRequest body = new KolbRequest(items);

        api.guardarRespuestas(body).enqueue(new Callback<KolbResponse>() {
            @Override public void onResponse(Call<KolbResponse> c, Response<KolbResponse> r) {
                btnEnviar.setEnabled(true);
                btnEnviar.setText("Enviar respuestas");

                if (!r.isSuccessful() || r.body() == null) {
                    toast("Error " + r.code());
                    return;
                }

                KolbResponse k = r.body();
                // Lanza tu pantalla de resultados
                Intent i = new Intent(TestActivity.this, ResultActivity.class);
                i.putExtra("estilo",          k.getEstiloDominante());
                i.putExtra("caracteristicas", k.getCaracteristicas());
                i.putExtra("recomendaciones", k.getRecomendaciones());
                startActivity(i);
            }

            @Override public void onFailure(Call<KolbResponse> c, Throwable t) {
                btnEnviar.setEnabled(true);
                btnEnviar.setText("Enviar respuestas");
                toast("Fallo: " + t.getMessage());
            }
        });
    }

    // Por si quieres navegar a Home luego
    private void goToHome() {
        Intent i = new Intent(this, HomeActivity.class);
        startActivity(i);
        finish();
    }
}
