package com.example.zavira_movil.HislaConocimiento;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.R;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Muestra una pregunta a la vez y al final llama /cerrar.
 * No crea nuevas activities ni adapters.
 */
public class IslaPreguntasActivity extends AppCompatActivity {

    // ---- UI del layout existente ----
    private TextView tvIndex, tvTimer, tvProgresoBloque, tvTitulo, tvEnunciado;
    private ProgressBar progressBloque;
    private Button btnOp1, btnOp2, btnOp3, btnOp4, btnResponder;

    // ---- Datos de flujo ----
    private String modalidadSeleccionada;
    private String payloadJson;
    private String idSesion;
    private final List<PreguntaUI> preguntas = new ArrayList<>();
    private int idx = 0;
    private long inicioPreguntaElapsed = 0L;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_isla_preguntas);

        // Vincular vistas
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

        // Extras
        Intent it = getIntent();
        if (it != null) {
            modalidadSeleccionada = it.getStringExtra("modalidad");
            payloadJson = it.getStringExtra("payload");
        }

        // Parsear payload
        try {
            JsonObject root = new JsonParser().parse(payloadJson).getAsJsonObject();
            JsonObject sesion = root.has("sesion") && root.get("sesion").isJsonObject()
                    ? root.getAsJsonObject("sesion") : null;
            if (sesion != null && sesion.has("idSesion")) {
                idSesion = safeString(sesion.get("idSesion"));
            }

            JsonArray arr = root.has("preguntas") && root.get("preguntas").isJsonArray()
                    ? root.getAsJsonArray("preguntas") : new JsonArray();

            preguntas.clear();
            for (int i = 0; i < arr.size(); i++) {
                JsonObject p = arr.get(i).getAsJsonObject();
                PreguntaUI q = new PreguntaUI();
                q.id = p.has("id_pregunta") ? safeString(p.get("id_pregunta")) : "";
                q.area = p.has("area") ? safeString(p.get("area")) : "";
                q.subtema = p.has("subtema") ? safeString(p.get("subtema")) : "";
                q.enunciado = p.has("enunciado") ? safeString(p.get("enunciado")) : "";
                q.opciones = new ArrayList<>();
                if (p.has("opciones") && p.get("opciones").isJsonArray()) {
                    JsonArray ops = p.getAsJsonArray("opciones");
                    for (int k = 0; k < ops.size(); k++) q.opciones.add(safeString(ops.get(k)));
                }
                while (q.opciones.size() < 4) q.opciones.add("");
                preguntas.add(q);
            }
            progressBloque.setMax(preguntas.size());
            Log.i("IslaPreg", "Preguntas recibidas: " + preguntas.size());
        } catch (Exception e) {
            Log.e("IslaPreg", "Error parseando payload", e);
            Toast.makeText(this, "No se pudo leer preguntas", Toast.LENGTH_LONG).show();
        }

        // Listeners de selección
        btnOp1.setOnClickListener(v -> seleccionar("A"));
        btnOp2.setOnClickListener(v -> seleccionar("B"));
        btnOp3.setOnClickListener(v -> seleccionar("C"));
        btnOp4.setOnClickListener(v -> seleccionar("D"));

        // Responder / avanzar
        btnResponder.setOnClickListener(v -> {
            if (preguntas.isEmpty()) return;
            PreguntaUI actual = preguntas.get(idx);
            if (actual.opcionElegida == null || actual.opcionElegida.isEmpty()) {
                Toast.makeText(this, "Selecciona una opción", Toast.LENGTH_SHORT).show();
                return;
            }
            int seg = (int) Math.max(0, (SystemClock.elapsedRealtime() - inicioPreguntaElapsed) / 1000L);
            actual.tiempoSeg = seg;

            if (idx < preguntas.size() - 1) {
                idx++;
                pintarPreguntaActual();
            } else {
                cerrarSimulacro(); // última
            }
        });

        if (!preguntas.isEmpty()) {
            pintarPreguntaActual();
        }
    }

    // ======= UI helpers =======

    private void pintarPreguntaActual() {
        PreguntaUI q = preguntas.get(idx);

        tvIndex.setText((idx + 1) + " / " + preguntas.size());
        progressBloque.setProgress(idx + 1);
        tvProgresoBloque.setText("Progreso: " + (idx + 1) + " / " + preguntas.size());

        String titulo = (q.area.isEmpty() ? "Isla" : q.area) + (q.subtema.isEmpty() ? "" : " • " + q.subtema);
        tvTitulo.setText(titulo);
        tvEnunciado.setText(q.enunciado);

        btnOp1.setText(q.opciones.get(0));
        btnOp2.setText(q.opciones.get(1));
        btnOp3.setText(q.opciones.get(2));
        btnOp4.setText(q.opciones.get(3));

        marcarSeleccion(q.opcionElegida);
        btnResponder.setText(idx == preguntas.size() - 1 ? "Finalizar" : "Responder");

        inicioPreguntaElapsed = SystemClock.elapsedRealtime();
    }

    private void seleccionar(String letra) {
        PreguntaUI q = preguntas.get(idx);
        q.opcionElegida = letra;
        marcarSeleccion(letra);
    }

    private void marcarSeleccion(String letra) {
        btnOp1.setSelected("A".equals(letra));
        btnOp2.setSelected("B".equals(letra));
        btnOp3.setSelected("C".equals(letra));
        btnOp4.setSelected("D".equals(letra));
    }

    // ======= Cerrar simulacro (array de arrays para el backend) =======

    private void cerrarSimulacro() {
        if (idSesion == null || idSesion.trim().isEmpty()) {
            Toast.makeText(this, "Falta idSesion para cerrar", Toast.LENGTH_LONG).show();
            return;
        }

        ApiService api = RetrofitClient.getInstance(getApplicationContext()).create(ApiService.class);

        JsonObject body = new JsonObject();
        body.addProperty("id_sesion", idSesion);

        // timestamp ISO (respondida_at), el backend ignora el tiempo por pregunta aquí
        String isoNow = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
                .format(new Date());

        JsonArray respuestasJA = new JsonArray();
        for (int i = 0; i < preguntas.size(); i++) {
            PreguntaUI p = preguntas.get(i);
            String opcion = (p.opcionElegida == null) ? "" : p.opcionElegida;

            // Tupla: [$1 alternativa_elegida, $2 es_correcta=null, $3 respondida_at, $4 id_detalle(orden)]
            JsonArray tupla = new JsonArray();
            tupla.add(opcion);           // $1
            tupla.add((String) null);    // $2 -> lo calcula el backend
            tupla.add(isoNow);           // $3
            tupla.add(i + 1);            // $4 -> usamos orden 1..N

            respuestasJA.add(tupla);
        }
        body.add("respuestas", respuestasJA);

        Log.i("IslaCerrar", "Payload cerrar (arrays): " + body);

        api.cerrarIslaSimulacro(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> res) {
                if (!res.isSuccessful()) {
                    String msg = "No se pudo cerrar el simulacro (HTTP " + res.code() + ")";
                    try {
                        if (res.errorBody() != null) {
                            String raw = res.errorBody().string();
                            Log.e("IslaCerrar", "HTTP " + res.code() + " -> " + raw);
                            msg = raw;
                        }
                    } catch (Exception ignore) {}
                    Toast.makeText(IslaPreguntasActivity.this, msg, Toast.LENGTH_LONG).show();
                    return;
                }

                // OK -> ir a resultados (si tienes esa pantalla)
                try {
                    Intent it = new Intent(IslaPreguntasActivity.this, IslaResultadoActivity.class);
                    it.putExtra("modalidad", modalidadSeleccionada);
                    it.putExtra("payload", payloadJson);
                    startActivity(it);
                    finish();
                } catch (Exception e) {
                    Log.w("IslaCerrar", "Cerró OK pero no pude abrir resultados", e);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("IslaCerrar", "Fallo de red al cerrar", t);
                Toast.makeText(IslaPreguntasActivity.this,
                        "Error de red al cerrar: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ======= Utils =======
    private static String safeString(JsonElement e) {
        if (e == null || e.isJsonNull()) return "";
        try { return e.getAsString(); } catch (Exception ex) { return String.valueOf(e); }
    }

    private static class PreguntaUI {
        String id = "";
        String area = "";
        String subtema = "";
        String enunciado = "";
        List<String> opciones = new ArrayList<>();
        String opcionElegida = "";
        int tiempoSeg = 0;
    }
}
