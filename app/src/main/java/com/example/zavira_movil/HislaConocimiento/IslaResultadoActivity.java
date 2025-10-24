package com.example.zavira_movil.HislaConocimiento;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class IslaResultadoActivity extends AppCompatActivity {

    private TextView tvModo, tvGlobal;
    private LinearLayout contAreas;

    private IslaSimulacroResponse data;
    private List<IslaCerrarRequest.Respuesta> respuestas;
    private String modalidad;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_isla_resultado);

        tvModo = findViewById(R.id.tvModo);
        tvGlobal = findViewById(R.id.tvGlobal);
        contAreas = findViewById(R.id.contAreas);

        modalidad = getIntent().getStringExtra("modalidad");
        String payload = getIntent().getStringExtra("payload");
        String respuestasJson = getIntent().getStringExtra("respuestas_json");

        data = GsonHolder.gson().fromJson(payload, IslaSimulacroResponse.class);
        IslaCerrarRequest.Respuesta[] arr =
                GsonHolder.gson().fromJson(respuestasJson, IslaCerrarRequest.Respuesta[].class);
        respuestas = Arrays.asList(arr);

        pintarResumen();
    }

    private void pintarResumen() {
        int total = (data != null && data.getPreguntas() != null)
                ? data.getPreguntas().size() : 0;

        // ---- Conteo global (completitud) ----
        int respondidas = 0;
        long tiempoTotalSeg = 0;
        if (respuestas != null) {
            for (IslaCerrarRequest.Respuesta r : respuestas) {
                if (r == null) continue;

                // usar getters del POJO
                String op = r.getOpcion() == null ? "" : r.getOpcion().trim();
                if (!op.isEmpty()) respondidas++;

                Integer t = r.getTiempo();  // puede ser null en modo fácil
                tiempoTotalSeg += (t != null ? t : 0);
            }
        }
        int omitidas = Math.max(0, total - respondidas);
        double pct = total > 0 ? (respondidas * 100.0 / total) : 0.0;
        long prom = total > 0 ? (tiempoTotalSeg / total) : 0;

        tvModo.setText(String.format(Locale.getDefault(),
                "Modo: %s • Tiempo total: %s • Promedio por pregunta: %s",
                capitalize(modalidad), formatSeg(tiempoTotalSeg), formatSeg(prom)));

        tvGlobal.setText(String.format(Locale.getDefault(),
                "%d respondidas / %d total  (%.1f%%)  •  %d omitidas",
                respondidas, total, pct, omitidas));

        // ---- Resultados por área (completitud por área) ----
        contAreas.removeAllViews();
        if (data != null && data.getPreguntas() != null) {

            // Total por área
            Map<String, Integer> totalArea = new HashMap<>();
            for (IslaSimulacroResponse.PreguntaDto p : data.getPreguntas()) {
                if (p == null) continue;
                String area = safe(p.getArea());
                totalArea.put(area, totalArea.getOrDefault(area, 0) + 1);
            }

            // Respondidas por área usando el "orden" si tu Respuesta lo tiene
            Map<String, Integer> respArea = new HashMap<>();
            if (respuestas != null) {
                for (IslaCerrarRequest.Respuesta r : respuestas) {
                    if (r == null) continue;

                    // preferimos el orden (1-based). Si tu POJO no lo tiene, pon 0 y se ignora.
                    Integer orden = r.getOrden(); // <-- getter; si no existe en tu POJO, añade uno
                    int idx = (orden != null ? orden - 1 : -1);
                    if (idx < 0 || data.getPreguntas() == null || idx >= data.getPreguntas().size())
                        continue;

                    IslaSimulacroResponse.PreguntaDto p = data.getPreguntas().get(idx);
                    if (p == null) continue;

                    String area = safe(p.getArea());
                    boolean contesto = r.getOpcion() != null && !r.getOpcion().trim().isEmpty();
                    if (contesto) {
                        respArea.put(area, respArea.getOrDefault(area, 0) + 1);
                    }
                }
            }

            // Pintar filas
            for (Map.Entry<String, Integer> e : totalArea.entrySet()) {
                String area = e.getKey();
                int totA = e.getValue();
                int respA = respArea.getOrDefault(area, 0);
                double pctA = totA > 0 ? (respA * 100.0 / totA) : 0.0;

                TextView tv = new TextView(this);
                tv.setText(String.format(Locale.getDefault(),
                        "%s — %d/%d (%.1f%%)", area, respA, totA, pctA));
                tv.setTextSize(16f);
                tv.setTextColor(0xFF000000);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.topMargin = dp(6);
                tv.setLayoutParams(lp);

                contAreas.addView(tv);
            }
        }
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "Área" : s.trim();
    }

    private String formatSeg(long s) {
        long mm = s / 60;
        long ss = s % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", mm, ss);
    }

    private int dp(int v) {
        float d = getResources().getDisplayMetrics().density;
        return (int) (v * d + 0.5f);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase(Locale.getDefault()) + s.substring(1);
    }
}
