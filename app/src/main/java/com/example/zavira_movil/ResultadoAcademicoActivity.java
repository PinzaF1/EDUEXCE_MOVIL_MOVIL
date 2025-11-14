package com.example.zavira_movil;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.zavira_movil.Home.HomeActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResultadoAcademicoActivity extends AppCompatActivity {

    private TextView tvPuntajeGeneral;
    private TextView tvPuntajeGeneralIcfes;
    private TextView tvTotalCorrectas;
    private TextView tvTotalIncorrectas;
    private TextView tvTotalPreguntas;
    private LinearLayout containerPuntajesPorArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultado_academico);

        tvPuntajeGeneral = findViewById(R.id.tvPuntajeGeneral);
        tvPuntajeGeneralIcfes = findViewById(R.id.tvPuntajeGeneralIcfes);
        tvTotalCorrectas = findViewById(R.id.tvTotalCorrectas);
        tvTotalIncorrectas = findViewById(R.id.tvTotalIncorrectas);
        tvTotalPreguntas = findViewById(R.id.tvTotalPreguntas);
        containerPuntajesPorArea = findViewById(R.id.containerPuntajesPorArea);

        findViewById(R.id.btnIrHome).setOnClickListener(v -> {
            Intent i = new Intent(ResultadoAcademicoActivity.this, HomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });

        cargarResultados();
    }

    private void cargarResultados() {
        // Obtener valores del Intent
        int puntajeGeneral = getIntent().getIntExtra("puntaje_general", -1);
        int puntajeGeneralIcfes = getIntent().getIntExtra("puntaje_general_icfes", -1);
        int totalCorrectas = getIntent().getIntExtra("total_correctas", -1);
        int totalPreguntas = getIntent().getIntExtra("total_preguntas", -1);

        // Debug: verificar valores recibidos
        android.util.Log.d("ResultadoAcademico", "=== VALORES RECIBIDOS DEL INTENT ===");
        android.util.Log.d("ResultadoAcademico", "total_correctas: " + totalCorrectas);
        android.util.Log.d("ResultadoAcademico", "total_preguntas: " + totalPreguntas);
        android.util.Log.d("ResultadoAcademico", "puntaje_general: " + puntajeGeneral);
        android.util.Log.d("ResultadoAcademico", "puntaje_general_icfes: " + puntajeGeneralIcfes);

        // Si los valores son -1 (no fueron pasados), usar 0 como fallback
        if (totalCorrectas == -1) totalCorrectas = 0;
        if (totalPreguntas == -1) totalPreguntas = 0;
        if (puntajeGeneral == -1) puntajeGeneral = 0;
        if (puntajeGeneralIcfes == -1) puntajeGeneralIcfes = 0;

        String puntajesPorAreaJson = getIntent().getStringExtra("puntajes_por_area");
        String puntajesIcfesPorAreaJson = getIntent().getStringExtra("puntajes_icfes_por_area");
        String correctasPorAreaJson = getIntent().getStringExtra("correctas_por_area");
        String totalesPorAreaJson = getIntent().getStringExtra("totales_por_area");

        // Calcular porcentaje real
        int porcentajeReal = puntajeGeneral > 0 ? puntajeGeneral : (totalPreguntas > 0 ? (totalCorrectas * 100 / totalPreguntas) : 0);
        
        // Calcular incorrectas
        int totalIncorrectas = totalPreguntas - totalCorrectas;
        
        // Mostrar valores
        tvPuntajeGeneral.setText(porcentajeReal + "%");
        tvPuntajeGeneralIcfes.setText(String.valueOf(puntajeGeneralIcfes));
        
        // Mostrar correctas e incorrectas
        tvTotalCorrectas.setText(totalCorrectas + " correctas");
        if (tvTotalIncorrectas != null) {
            tvTotalIncorrectas.setText(totalIncorrectas + " incorrectas");
        }
        tvTotalPreguntas.setText(totalPreguntas + " preguntas totales");
        
        android.util.Log.d("ResultadoAcademico", "=== VALORES MOSTRADOS ===");
        android.util.Log.d("ResultadoAcademico", "Porcentaje: " + porcentajeReal + "%");
        android.util.Log.d("ResultadoAcademico", "Correctas: " + totalCorrectas);
        android.util.Log.d("ResultadoAcademico", "Incorrectas: " + totalIncorrectas);
        android.util.Log.d("ResultadoAcademico", "Total: " + totalPreguntas);

        // Parsear y mostrar puntajes por área
        Map<String, Integer> puntajesPorArea = new LinkedHashMap<>();
        Map<String, Integer> puntajesIcfesPorArea = new LinkedHashMap<>();
        Map<String, Integer> correctasPorArea = new LinkedHashMap<>();
        Map<String, Integer> totalesPorArea = new LinkedHashMap<>();
        
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Integer>>(){}.getType();
        
        if (puntajesPorAreaJson != null && puntajesIcfesPorAreaJson != null) {
            try {
                puntajesPorArea = gson.fromJson(puntajesPorAreaJson, type);
                puntajesIcfesPorArea = gson.fromJson(puntajesIcfesPorAreaJson, type);
            } catch (Exception e) {
                android.util.Log.e("ResultadoAcademico", "Error al parsear JSON de áreas", e);
            }
        }
        
        // Parsear correctas y totales por área
        if (correctasPorAreaJson != null && totalesPorAreaJson != null) {
            try {
                correctasPorArea = gson.fromJson(correctasPorAreaJson, type);
                totalesPorArea = gson.fromJson(totalesPorAreaJson, type);
                android.util.Log.d("ResultadoAcademico", "Correctas por área: " + correctasPorArea);
                android.util.Log.d("ResultadoAcademico", "Totales por área: " + totalesPorArea);
            } catch (Exception e) {
                android.util.Log.e("ResultadoAcademico", "Error al parsear correctas/totales por área", e);
            }
        }
        
        // Si no hay correctas por área, calcular desde el porcentaje y el total por defecto
        if (correctasPorArea == null || correctasPorArea.isEmpty()) {
            android.util.Log.w("ResultadoAcademico", "No hay correctas por área, calculando desde porcentajes");
            int preguntasPorArea = totalPreguntas / 5;
            String[] areas = {"Matematicas", "Lenguaje", "Ciencias", "sociales", "Ingles"};
            for (String area : areas) {
                Integer porcentaje = puntajesPorArea.get(area);
                if (porcentaje == null) porcentaje = 0;
                int correctasCalculadas = Math.round((porcentaje * preguntasPorArea) / 100);
                correctasPorArea.put(area, correctasCalculadas);
                totalesPorArea.put(area, preguntasPorArea);
            }
            android.util.Log.d("ResultadoAcademico", "Correctas calculadas desde porcentajes: " + correctasPorArea);
        }
        
        // Si no hay puntajes por área o están vacíos, calcular desde totalCorrectas y totalPreguntas
        // Esto es un fallback, pero idealmente deberían venir del backend
        if (puntajesPorArea == null || puntajesPorArea.isEmpty()) {
            android.util.Log.w("ResultadoAcademico", "No hay puntajes por área, calculando desde totales");
            // Si no hay datos por área, distribuir el porcentaje general equitativamente
            int porcentajeGeneral = porcentajeReal;
            String[] areas = {"Matematicas", "Lenguaje", "Ciencias", "sociales", "Ingles"};
            for (String area : areas) {
                puntajesPorArea.put(area, porcentajeGeneral);
                int icfes = calcularIcfes(porcentajeGeneral);
                puntajesIcfesPorArea.put(area, icfes);
                android.util.Log.d("ResultadoAcademico", "Área " + area + ": porcentaje=" + porcentajeGeneral + ", ICFES=" + icfes);
            }
        } else {
            // Asegurar que todos los ICFES estén calculados correctamente
            for (Map.Entry<String, Integer> entry : puntajesPorArea.entrySet()) {
                String area = entry.getKey();
                Integer porcentaje = entry.getValue();
                if (porcentaje == null) porcentaje = 0;
                
                Integer icfes = puntajesIcfesPorArea.get(area);
                if (icfes == null || (icfes == 0 && porcentaje > 0)) {
                    // Recalcular ICFES si falta o está mal calculado
                    icfes = calcularIcfes(porcentaje);
                    puntajesIcfesPorArea.put(area, icfes);
                    android.util.Log.d("ResultadoAcademico", "ICFES recalculado para " + area + ": porcentaje=" + porcentaje + ", ICFES=" + icfes);
                }
            }
            
            // Asegurar que todas las áreas estén presentes (incluso si tienen 0)
            String[] todasLasAreas = {"Matematicas", "Lenguaje", "Ciencias", "sociales", "Ingles"};
            for (String area : todasLasAreas) {
                if (!puntajesPorArea.containsKey(area)) {
                    puntajesPorArea.put(area, 0);
                    puntajesIcfesPorArea.put(area, 0);
                }
            }
        }
        
        // Asegurar que el ICFES general esté calculado
        if (puntajeGeneralIcfes == 0 && porcentajeReal > 0) {
            puntajeGeneralIcfes = calcularIcfes(porcentajeReal);
            android.util.Log.d("ResultadoAcademico", "ICFES general calculado: " + puntajeGeneralIcfes + " desde porcentaje: " + porcentajeReal);
        }
        
        // Actualizar el ICFES general en la UI
        tvPuntajeGeneralIcfes.setText(String.valueOf(puntajeGeneralIcfes));
        
        // SIEMPRE mostrar todas las áreas, incluso si todas fueron incorrectas
        mostrarPuntajesPorArea(puntajesPorArea, puntajesIcfesPorArea, correctasPorArea, totalesPorArea, totalPreguntas / 5);
    }

    private void mostrarPuntajesPorArea(Map<String, Integer> puntajesPorArea, Map<String, Integer> puntajesIcfesPorArea, 
                                       Map<String, Integer> correctasPorArea, Map<String, Integer> totalesPorArea, int preguntasPorArea) {
        containerPuntajesPorArea.removeAllViews();

        // Orden de áreas con sus claves de backend
        Map<String, String> areasMap = new LinkedHashMap<>();
        areasMap.put("Matematicas", "Matemáticas");
        areasMap.put("Lenguaje", "Lenguaje");
        areasMap.put("Ciencias", "Ciencias Naturales");
        areasMap.put("sociales", "Sociales");
        areasMap.put("Ingles", "Inglés");

        // SIEMPRE mostrar todas las áreas, incluso si todas fueron incorrectas
        for (Map.Entry<String, String> entry : areasMap.entrySet()) {
            String clave = entry.getKey();
            String nombreArea = entry.getValue();

            Integer puntaje = puntajesPorArea != null ? puntajesPorArea.get(clave) : null;
            Integer puntajeIcfes = puntajesIcfesPorArea != null ? puntajesIcfesPorArea.get(clave) : null;
            Integer correctas = correctasPorArea != null ? correctasPorArea.get(clave) : null;
            Integer total = totalesPorArea != null ? totalesPorArea.get(clave) : null;

            // Si no hay datos, usar valores por defecto
            if (correctas == null) correctas = 0;
            if (total == null) total = preguntasPorArea; // Usar el total por defecto si no hay datos

            // RECALCULAR porcentaje desde correctas y total REALES (no usar el del backend si está mal)
            int porcentajeReal = (total > 0) ? Math.round((correctas * 100) / total) : 0;
            
            // RECALCULAR ICFES desde el porcentaje real
            int icfesReal = calcularIcfes(porcentajeReal);
            
            // Usar los valores recalculados en lugar de los del backend si hay discrepancia
            if (puntaje != null && puntaje != porcentajeReal) {
                android.util.Log.w("ResultadoAcademico", "Área " + clave + ": porcentaje del backend (" + puntaje + "%) no coincide con cálculo real (" + porcentajeReal + "%). Usando cálculo real.");
                puntaje = porcentajeReal;
            } else if (puntaje == null) {
                puntaje = porcentajeReal;
            }
            
            if (puntajeIcfes != null && puntajeIcfes != icfesReal) {
                android.util.Log.w("ResultadoAcademico", "Área " + clave + ": ICFES del backend (" + puntajeIcfes + ") no coincide con cálculo real (" + icfesReal + "). Usando cálculo real.");
                puntajeIcfes = icfesReal;
            } else if (puntajeIcfes == null) {
                puntajeIcfes = icfesReal;
            }

            android.util.Log.d("ResultadoAcademico", "Área " + clave + ": " + correctas + "/" + total + " = " + porcentajeReal + "% (ICFES: " + icfesReal + ")");

            // Crear tarjeta para cada área con correctas y totales reales
            View card = crearTarjetaArea(nombreArea, puntaje, puntajeIcfes, correctas, total, clave);
            containerPuntajesPorArea.addView(card);
        }
    }

    private View crearTarjetaArea(String nombreArea, int puntaje, int puntajeIcfes, int correctas, int totalPreguntas, String claveArea) {
        // Obtener color y icono por área
        int areaColor = obtenerColorArea(claveArea);
        int iconRes = obtenerIconoArea(claveArea);
        
        // Crear CardView
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 12);
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
        card.setRadius(18f);
        card.setCardElevation(2f);
        card.setStrokeWidth(2);
        card.setStrokeColor(areaColor); // Borde con color del área

        // Contenedor interno
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(16, 16, 16, 16);

        // Header con nombre del área e icono
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        // Icono del área
        ImageView ivIconoArea = new ImageView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                dp(28), dp(28)
        );
        iconParams.setMargins(0, 0, 10, 0);
        ivIconoArea.setLayoutParams(iconParams);
        ivIconoArea.setImageResource(iconRes);
        ivIconoArea.setBackgroundColor(obtenerColorFondoArea(areaColor));
        ivIconoArea.setPadding(dp(4), dp(4), dp(4), dp(4));
        ivIconoArea.setScaleType(ImageView.ScaleType.FIT_CENTER);

        TextView tvNombreArea = new TextView(this);
        LinearLayout.LayoutParams nombreParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        );
        tvNombreArea.setLayoutParams(nombreParams);
        tvNombreArea.setText(nombreArea);
        tvNombreArea.setTypeface(null, Typeface.BOLD);
        tvNombreArea.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        tvNombreArea.setTextSize(15);

        header.addView(ivIconoArea);
        header.addView(tvNombreArea);

        // Contenedor de puntajes con barra de progreso
        LinearLayout puntajesContainer = new LinearLayout(this);
        puntajesContainer.setOrientation(LinearLayout.VERTICAL);
        puntajesContainer.setPadding(dp(12), dp(12), dp(12), dp(12));
        puntajesContainer.setBackgroundResource(R.drawable.acad_item_soft);
        LinearLayout.LayoutParams puntajesParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        puntajesParams.setMargins(0, dp(12), 0, 0);
        puntajesContainer.setLayoutParams(puntajesParams);

        // Barra de progreso
        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(8)
        );
        progressParams.setMargins(0, 0, 0, dp(8));
        progressBar.setLayoutParams(progressParams);
        progressBar.setMax(100);
        progressBar.setProgress(puntaje);
        progressBar.setProgressTintList(ColorStateList.valueOf(areaColor));
        progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(0xFFE6EAF2));

        // Puntaje porcentaje
        TextView tvPuntaje = new TextView(this);
        tvPuntaje.setText(puntaje + "%");
        tvPuntaje.setTextColor(areaColor);
        tvPuntaje.setTextSize(16);
        tvPuntaje.setTypeface(null, Typeface.BOLD);

        // Puntaje ICFES
        TextView tvPuntajeIcfes = new TextView(this);
        tvPuntajeIcfes.setText("ICFES: " + puntajeIcfes);
        tvPuntajeIcfes.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tvPuntajeIcfes.setTextSize(12);
        LinearLayout.LayoutParams icfesParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        icfesParams.setMargins(0, dp(4), 0, 0);
        tvPuntajeIcfes.setLayoutParams(icfesParams);

        // Correctas de total (datos reales)
        TextView tvCorrectasArea = new TextView(this);
        tvCorrectasArea.setText(correctas + " de " + totalPreguntas + " correctas");
        tvCorrectasArea.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tvCorrectasArea.setTextSize(12);
        LinearLayout.LayoutParams correctasParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        correctasParams.setMargins(0, dp(4), 0, 0);
        tvCorrectasArea.setLayoutParams(correctasParams);

        puntajesContainer.addView(progressBar);
        puntajesContainer.addView(tvPuntaje);
        puntajesContainer.addView(tvPuntajeIcfes);
        puntajesContainer.addView(tvCorrectasArea);

        container.addView(header);
        container.addView(puntajesContainer);
        card.addView(container);

        return card;
    }
    
    // Calcular ICFES desde porcentaje (0-100) -> (0-500)
    private int calcularIcfes(int porcentaje) {
        if (porcentaje < 0) porcentaje = 0;
        if (porcentaje > 100) porcentaje = 100;
        return Math.round((porcentaje * 500) / 100);
    }
    
    private int obtenerColorArea(String claveArea) {
        if (claveArea == null) return 0xFFB6B9C2;
        String a = claveArea.toLowerCase().trim();
        
        // Isla del Conocimiento / Todas las áreas - Amarillo
        if (a.contains("conocimiento") || a.contains("isla") || 
            (a.contains("todas") && (a.contains("area") || a.contains("área")))) {
            return 0xFFFFC107; // Amarillo
        }
        
        switch (claveArea) {
            case "Matematicas": return 0xFFE53935; // Rojo
            case "Lenguaje": return 0xFF1E88E5; // Azul
            case "Ciencias": return 0xFF43A047; // Verde
            case "sociales": return 0xFFFB8C00; // Naranja
            case "Ingles": return 0xFF8E24AA; // Morado
            default: return 0xFFB6B9C2; // Gris
        }
    }
    
    private int obtenerIconoArea(String claveArea) {
        if (claveArea == null) return R.drawable.lectu;
        switch (claveArea) {
            case "Matematicas": return R.drawable.calculator;
            case "Lenguaje": return R.drawable.lectu;
            case "Ciencias": return R.drawable.naturales;
            case "sociales": return R.drawable.sociale;
            case "Ingles": return R.drawable.english;
            default: return R.drawable.lectu;
        }
    }
    
    private int obtenerColorFondoArea(int colorArea) {
        // Color de fondo suave basado en el color del área
        return (colorArea & 0x00FFFFFF) | 0x15000000; // Agregar alpha 15
    }
    
    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}

