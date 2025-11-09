package com.example.zavira_movil;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.Home.HomeActivity;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestAcademico extends AppCompatActivity {

    private RecyclerView rvPreguntas;
    private Button btnEnviar;
    private TextView tvBloque;
    private PreguntasAdapter adapter;

    private ProgressBar progressBloque;
    private int totalPreguntas = 25;

    private final List<PreguntaAcademica> preguntas = new ArrayList<>();
    private String idSesion = null;

    private final List<List<PreguntaAcademica>> bloques = new ArrayList<>();
    private final List<String> nombresBloque = new ArrayList<>();
    private int idxBloque = 0;

    private final Map<String, String> respuestasGlobales = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_academico);

        rvPreguntas   = findViewById(R.id.rvPreguntas);
        btnEnviar     = findViewById(R.id.btnEnviar);
        tvBloque      = findViewById(R.id.tvBloque);
        progressBloque = findViewById(R.id.progressBloque);

        rvPreguntas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PreguntasAdapter(this, new ArrayList<>());
        rvPreguntas.setAdapter(adapter);

        btnEnviar.setOnClickListener(v -> {
            if (bloques.isEmpty()) return;

            adapter.collectSeleccionesTo(respuestasGlobales);
            
            // Validar que todas las preguntas del bloque actual estén respondidas
            if (!validarBloqueActual()) {
                return;
            }
            
            actualizarProgreso(); // Actualizar progreso normal

            boolean esUltimo = (idxBloque == bloques.size() - 1);
            if (esUltimo) enviar();
            else {
                idxBloque++;
                mostrarBloque();
            }
        });

        cargar();
    }

    private void cargar() {
        btnEnviar.setEnabled(false);
        btnEnviar.setText("Cargando...");

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);

        String token = com.example.zavira_movil.local.TokenManager.getToken(this);
        if (token == null || token.isEmpty()) {
            toast("No hay sesión. Inicia sesión primero.");
            btnEnviar.setEnabled(true);
            btnEnviar.setText("Siguiente");
            return;
        }
        String bearer = token.startsWith("Bearer ") ? token : "Bearer " + token;

        api.iniciar(bearer, new LinkedHashMap<>()).enqueue(new Callback<QuizInicialResponse>() {
            @Override
            public void onResponse(Call<QuizInicialResponse> call, Response<QuizInicialResponse> res) {
                btnEnviar.setText("Siguiente");
                btnEnviar.setEnabled(true);

                if (!res.isSuccessful() || res.body() == null || res.body().getPreguntas() == null) {
                    toast("Error: " + res.code());
                    return;
                }

                idSesion = res.body().getIdSesion();
                preguntas.clear();
                preguntas.addAll(res.body().getPreguntas());
                totalPreguntas = preguntas.size();

                progressBloque.setMax(totalPreguntas);

                // Orden esperado de áreas
                String[] areasOrdenadas = {"Matematicas", "Lenguaje", "Ciencias", "sociales", "Ingles"};

                Map<String, List<PreguntaAcademica>> porArea = new LinkedHashMap<>();
                for (PreguntaAcademica p : preguntas) {
                    String area = p.getArea();
                    // Normalizar nombres de área
                    if (area == null) area = "Desconocida";
                    String areaNormalizada = normalizarArea(area);
                    porArea.computeIfAbsent(areaNormalizada, k -> new ArrayList<>()).add(p);
                }

                bloques.clear();
                nombresBloque.clear();
                
                // Agrupar exactamente 5 preguntas por área en el orden correcto
                for (String areaEsperada : areasOrdenadas) {
                    List<PreguntaAcademica> lista = porArea.get(areaEsperada);
                    if (lista == null || lista.isEmpty()) continue;
                    
                    // Tomar exactamente 5 preguntas por área
                    // Si hay más de 5, solo tomamos las primeras 5
                    // Si hay menos de 5, tomamos todas
                    List<PreguntaAcademica> sub = new ArrayList<>();
                    int maxPreguntas = Math.min(5, lista.size());
                    for (int i = 0; i < maxPreguntas; i++) {
                        sub.add(lista.get(i));
                    }
                    
                    bloques.add(sub);
                    nombresBloque.add(obtenerNombreArea(areaEsperada));
                }
                
                // Si hay áreas que no están en el orden esperado, agregarlas también
                for (Map.Entry<String, List<PreguntaAcademica>> e : porArea.entrySet()) {
                    String area = e.getKey();
                    boolean yaAgregada = false;
                    for (String areaEsperada : areasOrdenadas) {
                        if (area.equals(areaEsperada)) {
                            yaAgregada = true;
                            break;
                        }
                    }
                    if (!yaAgregada && !e.getValue().isEmpty()) {
                        List<PreguntaAcademica> sub = e.getValue().size() >= 5 ? 
                            new ArrayList<>(e.getValue().subList(0, 5)) : new ArrayList<>(e.getValue());
                        bloques.add(sub);
                        nombresBloque.add(obtenerNombreArea(area));
                    }
                }

                if (bloques.isEmpty()) {
                    toast("No hay preguntas");
                    return;
                }

                idxBloque = 0;
                progressBloque.setProgress(0); // Resetear progreso al inicio
                mostrarBloque();
                toast("Sesión iniciada");
            }

            @Override
            public void onFailure(Call<QuizInicialResponse> call, Throwable t) {
                btnEnviar.setText("Siguiente");
                btnEnviar.setEnabled(true);
                toast("Error de red: " + t.getMessage());
            }
        });
    }

    private void mostrarBloque() {
        List<PreguntaAcademica> bloque = bloques.get(idxBloque);
        adapter.setPreguntas(bloque, respuestasGlobales);

        tvBloque.setText(nombresBloque.get(idxBloque));

        boolean esUltimo = (idxBloque == bloques.size() - 1);
        btnEnviar.setText(esUltimo ? "Enviar respuestas" : "Siguiente");

        // Usar animación lenta cuando se carga un nuevo bloque
        actualizarProgreso(true);
        
        // Hacer scroll al inicio del RecyclerView al cargar un nuevo bloque
        if (rvPreguntas != null) {
            rvPreguntas.post(() -> {
                RecyclerView.LayoutManager lm = rvPreguntas.getLayoutManager();
                if (lm != null && lm instanceof LinearLayoutManager) {
                    ((LinearLayoutManager) lm).scrollToPositionWithOffset(0, 0);
                }
            });
        }
    }

    private void actualizarProgreso() {
        actualizarProgreso(false);
    }
    
    private void actualizarProgreso(boolean animacionLenta) {
        int respondidas = respuestasGlobales.size();
        int porcentaje = totalPreguntas > 0 ? (int) ((respondidas / (float) totalPreguntas) * 100) : 0;

        if (animacionLenta && respondidas > 1) {
            // Si hay varias respuestas ya respondidas, animar pregunta por pregunta
            animarProgresoPreguntaPorPregunta(respondidas, totalPreguntas, porcentaje);
        } else {
            // Actualización normal cuando el usuario responde una pregunta nueva
            ObjectAnimator anim = ObjectAnimator.ofInt(progressBloque, "progress", progressBloque.getProgress(), respondidas);
            anim.setDuration(800);
            anim.start();
        }
    }
    
    private void animarProgresoPreguntaPorPregunta(int respondidas, int total, int porcentajeFinal) {
        // Resetear la barra a 0 primero
        progressBloque.setProgress(0);
        
        // Si no hay respuestas, solo mostrar 0
        if (respondidas == 0) {
            return;
        }
        
        // Calcular el progreso por pregunta
        int progresoPorPregunta = total > 0 ? (total * 100 / total) / total : 0;
        int delay = 0;
        
        // Animar pregunta por pregunta con delay entre cada una
        for (int i = 1; i <= respondidas; i++) {
            final int preguntaNum = i;
            final int nuevoProgreso = i;
            
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                ObjectAnimator anim = ObjectAnimator.ofInt(progressBloque, "progress", progressBloque.getProgress(), nuevoProgreso);
                anim.setDuration(600);
                anim.start();
            }, delay);
            
            delay += 400; // Delay de 400ms entre cada pregunta
        }
        
        // Al final, asegurar que esté en el progreso final exacto
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            ObjectAnimator animFinal = ObjectAnimator.ofInt(progressBloque, "progress", progressBloque.getProgress(), respondidas);
            animFinal.setDuration(300);
            animFinal.start();
        }, delay);
    }

    private String normalizarArea(String area) {
        if (area == null) return "Desconocida";
        String areaLower = area.toLowerCase().trim();
        if (areaLower.startsWith("mate")) return "Matematicas";
        if (areaLower.startsWith("leng") || areaLower.startsWith("lect")) return "Lenguaje";
        if (areaLower.startsWith("cien")) return "Ciencias";
        if (areaLower.startsWith("soci")) return "sociales";
        if (areaLower.startsWith("ing")) return "Ingles";
        return area;
    }
    
    private String obtenerNombreArea(String area) {
        if (area == null) return "Desconocida";
        if (area.equals("Matematicas")) return "Matemáticas";
        if (area.equals("Lenguaje")) return "Lenguaje";
        if (area.equals("Ciencias")) return "Ciencias Naturales";
        if (area.equals("sociales")) return "Sociales";
        if (area.equals("Ingles")) return "Inglés";
        return area;
    }
    
    private boolean validarBloqueActual() {
        List<PreguntaAcademica> bloque = bloques.get(idxBloque);
        List<Integer> preguntasFaltantes = new ArrayList<>();
        
        for (int i = 0; i < bloque.size(); i++) {
            PreguntaAcademica p = bloque.get(i);
            if (!respuestasGlobales.containsKey(p.getIdPregunta()) || 
                respuestasGlobales.get(p.getIdPregunta()) == null || 
                respuestasGlobales.get(p.getIdPregunta()).isEmpty()) {
                preguntasFaltantes.add(i);
            }
        }
        
        if (!preguntasFaltantes.isEmpty()) {
            // Marcar preguntas faltantes y hacer scroll
            adapter.marcarPreguntasFaltantes(preguntasFaltantes);
            
            // Hacer scroll a la primera pregunta faltante
            if (!preguntasFaltantes.isEmpty() && rvPreguntas != null) {
                int primeraFaltante = preguntasFaltantes.get(0);
                rvPreguntas.post(() -> {
                    RecyclerView.LayoutManager lm = rvPreguntas.getLayoutManager();
                    if (lm != null) {
                        // Hacer scroll suave a la posición
                        if (lm instanceof LinearLayoutManager) {
                            ((LinearLayoutManager) lm).scrollToPositionWithOffset(primeraFaltante, 100);
                        } else {
                            lm.scrollToPosition(primeraFaltante);
                        }
                    }
                });
            }
            
            toast("Debes responder todas las preguntas de " + nombresBloque.get(idxBloque) + " para continuar");
            return false;
        }
        
        return true;
    }

    private void enviar() {
        adapter.collectSeleccionesTo(respuestasGlobales);
        actualizarProgreso();

        if (respuestasGlobales.size() < preguntas.size()) {
            toast("Faltan preguntas por responder");
            return;
        }

        List<QuizCerrarRequest.RespuestaItem> items = new ArrayList<>();
        for (PreguntaAcademica p : preguntas) {
            String respuesta = respuestasGlobales.get(p.getIdPregunta());
            if (respuesta == null || respuesta.isEmpty()) {
                respuesta = ""; // Asegurar que no sea null
            }
            items.add(new QuizCerrarRequest.RespuestaItem(
                    p.getIdPregunta(),
                    respuesta
            ));
        }
        
        // Debug: verificar qué se está enviando
        android.util.Log.d("TestAcademico", "=== ENVIANDO RESPUESTAS ===");
        android.util.Log.d("TestAcademico", "idSesion: " + idSesion);
        android.util.Log.d("TestAcademico", "Total items: " + items.size());
        android.util.Log.d("TestAcademico", "Total respuestasGlobales: " + respuestasGlobales.size());

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);

        String token = com.example.zavira_movil.local.TokenManager.getToken(this);
        String bearer = token.startsWith("Bearer ") ? token : "Bearer " + token;

        btnEnviar.setEnabled(false);
        btnEnviar.setText("Enviando...");

        api.cerrar(bearer, new QuizCerrarRequest(idSesion, items))
                .enqueue(new Callback<QuizCerrarResponse>() {
                    @Override
                    public void onResponse(Call<QuizCerrarResponse> call, Response<QuizCerrarResponse> response) {
                        btnEnviar.setEnabled(true);
                        btnEnviar.setText("Enviar respuestas");
                        
                        if (!response.isSuccessful()) {
                            toast("Error al enviar respuestas: " + response.code());
                            android.util.Log.e("TestAcademico", "Error HTTP: " + response.code());
                            if (response.errorBody() != null) {
                                try {
                                    String errorBody = response.errorBody().string();
                                    android.util.Log.e("TestAcademico", "Error body: " + errorBody);
                                } catch (Exception e) {
                                    android.util.Log.e("TestAcademico", "Error al leer errorBody", e);
                                }
                            }
                            return;
                        }
                        
                        QuizCerrarResponse resultado = response.body();
                        
                        // Validar que resultado no sea null
                        if (resultado == null) {
                            toast("Error: respuesta vacía");
                            android.util.Log.e("TestAcademico", "ERROR: response.body() es null");
                            return;
                        }
                        
                        // Obtener valores con fallback seguro - FORZAR lectura directa
                        int totalCorrectas = 0;
                        int totalPreguntas = 0;
                        int puntajeGeneral = 0;
                        int puntajeGeneralIcfes = 0;
                        
                        // Intentar leer directamente del objeto
                        if (resultado.totalCorrectas != null) {
                            totalCorrectas = resultado.totalCorrectas;
                        } else {
                            android.util.Log.w("TestAcademico", "totalCorrectas es NULL en el objeto");
                        }
                        
                        if (resultado.totalPreguntas != null) {
                            totalPreguntas = resultado.totalPreguntas;
                        } else {
                            android.util.Log.w("TestAcademico", "totalPreguntas es NULL en el objeto");
                        }
                        
                        if (resultado.puntajeGeneral != null) {
                            puntajeGeneral = resultado.puntajeGeneral;
                        }
                        
                        if (resultado.puntajeGeneralIcfes != null) {
                            puntajeGeneralIcfes = resultado.puntajeGeneralIcfes;
                        }
                        
                        // Debug: verificar valores recibidos
                        android.util.Log.d("TestAcademico", "=== RESULTADO RECIBIDO ===");
                        android.util.Log.d("TestAcademico", "totalCorrectas (raw): " + resultado.totalCorrectas);
                        android.util.Log.d("TestAcademico", "totalPreguntas (raw): " + resultado.totalPreguntas);
                        android.util.Log.d("TestAcademico", "totalCorrectas (final): " + totalCorrectas);
                        android.util.Log.d("TestAcademico", "totalPreguntas (final): " + totalPreguntas);
                        android.util.Log.d("TestAcademico", "puntajeGeneral: " + puntajeGeneral);
                        android.util.Log.d("TestAcademico", "puntajeGeneralIcfes: " + puntajeGeneralIcfes);
                        
                        // Si los valores son 0 pero deberían tener datos, intentar calcular desde el detalle
                        if (totalCorrectas == 0 && totalPreguntas == 0 && resultado.detalle != null && resultado.detalle.size() > 0) {
                            int correctasDelDetalle = 0;
                            for (QuizCerrarResponse.DetalleItem item : resultado.detalle) {
                                if (item.esCorrecta != null && item.esCorrecta) {
                                    correctasDelDetalle++;
                                }
                            }
                            totalCorrectas = correctasDelDetalle;
                            totalPreguntas = resultado.detalle.size();
                            
                            // Recalcular puntaje general si es necesario
                            if (puntajeGeneral == 0 && totalPreguntas > 0) {
                                puntajeGeneral = Math.round((totalCorrectas * 100) / totalPreguntas);
                            }
                            
                            // Recalcular ICFES general si es necesario
                            if (puntajeGeneralIcfes == 0 && puntajeGeneral > 0) {
                                puntajeGeneralIcfes = Math.round((puntajeGeneral * 500) / 100);
                            }
                            
                            // Recalcular puntajes por área desde el detalle si no están
                            if (resultado.puntajesPorArea == null || resultado.puntajesPorArea.isEmpty()) {
                                Map<String, Integer> correctasPorArea = new HashMap<>();
                                Map<String, Integer> totalesPorArea = new HashMap<>();
                                
                                for (QuizCerrarResponse.DetalleItem item : resultado.detalle) {
                                    if (item.area != null) {
                                        String area = normalizarArea(item.area);
                                        totalesPorArea.put(area, totalesPorArea.getOrDefault(area, 0) + 1);
                                        if (item.esCorrecta != null && item.esCorrecta) {
                                            correctasPorArea.put(area, correctasPorArea.getOrDefault(area, 0) + 1);
                                        }
                                    }
                                }
                                
                                // Calcular porcentajes e ICFES por área
                                Map<String, Integer> porcentajesPorArea = new HashMap<>();
                                Map<String, Integer> icfesPorArea = new HashMap<>();
                                
                                for (Map.Entry<String, Integer> entry : totalesPorArea.entrySet()) {
                                    String area = entry.getKey();
                                    int total = entry.getValue();
                                    int correctas = correctasPorArea.getOrDefault(area, 0);
                                    int porcentaje = total > 0 ? Math.round((correctas * 100) / total) : 0;
                                    int icfes = Math.round((porcentaje * 500) / 100);
                                    
                                    porcentajesPorArea.put(area, porcentaje);
                                    icfesPorArea.put(area, icfes);
                                }
                                
                                resultado.puntajesPorArea = porcentajesPorArea;
                                resultado.puntajesIcfesPorArea = icfesPorArea;
                                
                                android.util.Log.w("TestAcademico", "Puntajes por área calculados desde detalle");
                            }
                            
                            android.util.Log.w("TestAcademico", "Calculado desde detalle: correctas=" + totalCorrectas + ", total=" + totalPreguntas + ", porcentaje=" + puntajeGeneral + ", ICFES=" + puntajeGeneralIcfes);
                        }
                        
                        // Redirigir a la pantalla de resultados
                        Intent intent = new Intent(TestAcademico.this, ResultadoAcademicoActivity.class);
                        intent.putExtra("puntaje_general", puntajeGeneral);
                        intent.putExtra("puntaje_general_icfes", puntajeGeneralIcfes);
                        intent.putExtra("total_correctas", totalCorrectas);
                        intent.putExtra("total_preguntas", totalPreguntas);
                        
                        android.util.Log.d("TestAcademico", "=== VALORES PASADOS AL INTENT ===");
                        android.util.Log.d("TestAcademico", "total_correctas: " + totalCorrectas);
                        android.util.Log.d("TestAcademico", "total_preguntas: " + totalPreguntas);
                        
                        // Calcular correctas por área desde el detalle
                        Map<String, Integer> correctasPorArea = new HashMap<>();
                        Map<String, Integer> totalesPorArea = new HashMap<>();
                        
                        if (resultado.detalle != null && !resultado.detalle.isEmpty()) {
                            for (QuizCerrarResponse.DetalleItem item : resultado.detalle) {
                                if (item.area != null) {
                                    String area = normalizarArea(item.area);
                                    totalesPorArea.put(area, totalesPorArea.getOrDefault(area, 0) + 1);
                                    if (item.esCorrecta != null && item.esCorrecta) {
                                        correctasPorArea.put(area, correctasPorArea.getOrDefault(area, 0) + 1);
                                    }
                                }
                            }
                        }
                        
                        // Pasar puntajes por área como JSON string usando Gson
                        Gson gson = new Gson();
                        if (resultado.puntajesPorArea != null && !resultado.puntajesPorArea.isEmpty()) {
                            intent.putExtra("puntajes_por_area", gson.toJson(resultado.puntajesPorArea));
                            android.util.Log.d("TestAcademico", "Puntajes por área pasados: " + gson.toJson(resultado.puntajesPorArea));
                        } else {
                            android.util.Log.w("TestAcademico", "No hay puntajes por área en el resultado");
                        }
                        
                        if (resultado.puntajesIcfesPorArea != null && !resultado.puntajesIcfesPorArea.isEmpty()) {
                            intent.putExtra("puntajes_icfes_por_area", gson.toJson(resultado.puntajesIcfesPorArea));
                            android.util.Log.d("TestAcademico", "Puntajes ICFES por área pasados: " + gson.toJson(resultado.puntajesIcfesPorArea));
                        } else {
                            android.util.Log.w("TestAcademico", "No hay puntajes ICFES por área en el resultado");
                        }
                        
                        // Pasar correctas y totales por área
                        if (!correctasPorArea.isEmpty()) {
                            intent.putExtra("correctas_por_area", gson.toJson(correctasPorArea));
                            intent.putExtra("totales_por_area", gson.toJson(totalesPorArea));
                            android.util.Log.d("TestAcademico", "Correctas por área: " + gson.toJson(correctasPorArea));
                            android.util.Log.d("TestAcademico", "Totales por área: " + gson.toJson(totalesPorArea));
                        }
                        
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(Call<QuizCerrarResponse> call, Throwable t) {
                        btnEnviar.setEnabled(true);
                        btnEnviar.setText("Enviar respuestas");
                        toast("Error de red: " + t.getMessage());
                    }
                });
    }


    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
}
