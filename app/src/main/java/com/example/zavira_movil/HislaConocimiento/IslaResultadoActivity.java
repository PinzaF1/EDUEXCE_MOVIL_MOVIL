package com.example.zavira_movil.HislaConocimiento;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.R;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class IslaResultadoActivity extends AppCompatActivity {

    private TextView tvTituloPct, tvSubtitulo, tvPorcentaje;
    private LinearLayout contAreas;
    private Button btnFinalizar;

    private String modalidad;
    private IslaCerrarResultadoResponse cerrar;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_isla_resultado);

        tvTituloPct  = findViewById(R.id.tvTituloPct);
        tvSubtitulo  = findViewById(R.id.tvSubtitulo);
        tvPorcentaje = findViewById(R.id.tvPorcentaje);
        contAreas    = findViewById(R.id.contAreas);
        btnFinalizar = findViewById(R.id.btnFinalizar);

        modalidad = getIntent().getStringExtra("modalidad");
        String resultadoJson = getIntent().getStringExtra("resultado_json");
        if (resultadoJson != null && !resultadoJson.trim().isEmpty()) {
            try { cerrar = GsonHolder.gson().fromJson(resultadoJson, IslaCerrarResultadoResponse.class); }
            catch (Exception ignore) {}
        }

        pintarCabecera(); pintarAreas();

        btnFinalizar.setOnClickListener(v -> {
            Intent i = new Intent(IslaResultadoActivity.this, IslaModalityActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        });
    }

    private void pintarCabecera() {
        tvTituloPct.setText("¡Simulacro Completado!");
        String modTxt = (modalidad == null || modalidad.isEmpty()) ? "" :
                " • Modalidad " + modalidad.substring(0,1).toUpperCase(Locale.getDefault()) + modalidad.substring(1);
        tvSubtitulo.setText("Puntuación Global" + modTxt);

        int pct = 0;
        if (cerrar != null) {
            if (cerrar.global != null && cerrar.global.porcentaje != null) {
                pct = cerrar.global.porcentaje;
            } else if (cerrar.puntajePorcentaje != null) {
                pct = cerrar.puntajePorcentaje;
            }
        }
        tvPorcentaje.setText(String.format(Locale.getDefault(), "%d%%", pct));
    }

    private void pintarAreas() {
        contAreas.removeAllViews();
        if (cerrar == null || cerrar.resumenAreas == null || cerrar.resumenAreas.isEmpty()) {
            agregarFilaPlaceholder("No hay datos por área"); return;
        }
        Map<String, IslaCerrarResultadoResponse.Area> mapa = new LinkedHashMap<>(cerrar.resumenAreas);
        for (Map.Entry<String, IslaCerrarResultadoResponse.Area> e : mapa.entrySet()) {
            String area = e.getKey();
            IslaCerrarResultadoResponse.Area a = e.getValue();

            int total = safe(a.total), buenas = safe(a.correctas);
            int pct = total > 0 ? (int) Math.round(buenas * 100.0 / total) : 0;
            agregarFilaArea(area, buenas, total, pct);
        }
    }

    private void agregarFilaPlaceholder(String msg) {
        TextView tv = new TextView(this);
        tv.setText(msg); tv.setTextSize(14); tv.setTextColor(0xFF666666);
        tv.setPadding(dp(2), dp(4), dp(2), dp(8));
        contAreas.addView(tv);
    }

    private void agregarFilaArea(String nombre, int buenas, int total, int pct) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.setPadding(0, dp(8), 0, dp(8));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView tvArea = new TextView(this);
        tvArea.setText(nombre); tvArea.setTextSize(16); tvArea.setTextColor(0xFF000000);
        tvArea.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvPct = new TextView(this);
        tvPct.setText(String.format(Locale.getDefault(), "%d%%", pct));
        tvPct.setTextSize(14); tvPct.setTextColor(0xFF000000);
        tvPct.setGravity(Gravity.END);

        top.addView(tvArea); top.addView(tvPct);

        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams lpBar = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(8));
        lpBar.topMargin = dp(6);
        bar.setLayoutParams(lpBar); bar.setMax(100); bar.setProgress(pct);

        TextView tvSub = new TextView(this);
        tvSub.setText(String.format(Locale.getDefault(), "%d de %d correctas", buenas, total));
        tvSub.setTextSize(12); tvSub.setTextColor(0xFF777777);
        tvSub.setPadding(0, dp(4), 0, 0);

        View sep = new View(this);
        LinearLayout.LayoutParams lpSep = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
        lpSep.topMargin = dp(10);
        sep.setLayoutParams(lpSep); sep.setBackgroundColor(0x11000000);

        root.addView(top); root.addView(bar); root.addView(tvSub);
        contAreas.addView(root); contAreas.addView(sep);
    }

    private int dp(int v) {
        float d = getResources().getDisplayMetrics().density;
        return (int) (v * d + 0.5f);
    }

    private int safe(Integer i) { return i == null ? 0 : i; }
}
