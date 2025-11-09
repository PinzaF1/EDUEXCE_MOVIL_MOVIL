package com.example.zavira_movil.HislaConocimiento;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.zavira_movil.R;
import com.example.zavira_movil.databinding.ActivityQuizBinding;
import com.example.zavira_movil.local.UserSession;
import com.example.zavira_movil.model.OtorgarAreaRequest;
import com.example.zavira_movil.model.OtorgarAreaResponse;
import com.example.zavira_movil.niveleshome.LivesManager;
import com.example.zavira_movil.niveleshome.ProgressLockManager;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IslaPreguntasActivity extends AppCompatActivity {

    private static final int LIMITE_SEG_DIFICIL = 60;
    private static final String[] ABCD = {"A","B","C","D"};

    // UI para Isla del Conocimiento (modalidad fácil/difícil)
    private TextView tvIndex, tvTimer;
    private ProgressBar progressTimer;
    private Button btnResponder;
    private androidx.recyclerview.widget.RecyclerView rvQuestions;
    private com.example.zavira_movil.QuizQuestionsAdapter adapterIsla;
    private List<com.example.zavira_movil.model.Question> allQuestionsIsla = new ArrayList<>();
    
    // UI para Examen Final (pregunta por pregunta, como QuizActivity)
    private ActivityQuizBinding bindingFinal;
    private com.example.zavira_movil.QuizQuestionsAdapter adapterFinal;
    private int currentQuestionIndex = 0; // Índice de la pregunta actual (examen final)
    private List<com.example.zavira_movil.model.Question> allQuestionsFinal = new ArrayList<>(); // Todas las preguntas (examen final)
    private List<String> todasLasRespuestasFinal = new ArrayList<>(); // Respuestas guardadas (examen final)

    private String modalidad; // "facil" | "dificil" | "estandar"
    private IslaSimulacroResponse data;
    private final List<PreguntaUI> preguntas = new ArrayList<>();
    private int idx = 0;
    private long inicioPreguntaElapsed = 0L;
    private long inicioSimulacroElapsed = 0L; // Tiempo de inicio del simulacro completo (para modalidad fácil)
    private CountDownTimer timerActual;
    private String areaUi; // Área del examen para el sistema de vidas
    private static final int EXAMEN_FINAL_LEVEL = 6; // Nivel especial para el examen final
    private boolean esExamenFinal = false; // Flag para identificar si es examen final
    private Integer idSesionFinal = null; // ID de sesión para examen final
    
    // Sonido de tiempo (para modalidad difícil)
    private ToneGenerator toneGenerator;
    private Handler soundHandler;
    private Runnable soundRunnable;
    private boolean isLast10Seconds = false;

    private static class PreguntaUI {
        int id;
        String area, subtema, enunciado;
        List<String> opciones;
        String elegida; // "A".."D" o null
    }

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Inicializar tiempo de inicio del simulacro (para modalidad fácil)
        inicioSimulacroElapsed = SystemClock.elapsedRealtime();

        modalidad = getIntent().getStringExtra("modalidad");
        String payload = getIntent().getStringExtra("payload");
        data = GsonHolder.gson().fromJson(payload, IslaSimulacroResponse.class);
        
        // Verificar si es examen final
        esExamenFinal = getIntent().getBooleanExtra("es_examen_final", false);

        // Obtener área del examen (desde sesión o primera pregunta)
        if (data != null && data.getSesion() != null && data.getSesion().getArea() != null) {
            areaUi = data.getSesion().getArea();
        } else {
            // Fallback: obtener desde el intent si viene
            areaUi = getIntent().getStringExtra("area");
        }

        if (data == null || data.getPreguntas() == null || data.getPreguntas().isEmpty()) {
            Toast.makeText(this, "No se recibieron preguntas", Toast.LENGTH_LONG).show();
            finish(); return;
        }

        // Si es examen final, usar layout y lógica de QuizActivity (pregunta por pregunta)
        if (esExamenFinal) {
            inicializarExamenFinal();
        } else {
            inicializarIslaConocimiento();
        }
    }
    
    /** Inicializa el examen final con pregunta por pregunta (como QuizActivity) */
    private void inicializarExamenFinal() {
        // Usar layout de QuizActivity
        bindingFinal = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(bindingFinal.getRoot());
        
        // Asegurar fondo blanco y sin bordes
        getWindow().setBackgroundDrawableResource(android.R.color.white);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
        
        // Obtener ID de sesión
        if (data != null && data.getSesion() != null && data.getSesion().getIdSesion() != null) {
            try {
                idSesionFinal = Integer.parseInt(String.valueOf(data.getSesion().getIdSesion()));
            } catch (Exception e) {
                idSesionFinal = null;
            }
        }
        
        // Normalizar área (capitalizar primera letra)
        if (areaUi != null && !areaUi.isEmpty()) {
            areaUi = areaUi.substring(0, 1).toUpperCase() + areaUi.substring(1).toLowerCase();
            // Normalizar nombres de áreas
            if (areaUi.toLowerCase().contains("matematic")) areaUi = "Matemáticas";
            else if (areaUi.toLowerCase().contains("lengua") || areaUi.toLowerCase().contains("lectura")) areaUi = "Lenguaje";
            else if (areaUi.toLowerCase().contains("cien")) areaUi = "Ciencias";
            else if (areaUi.toLowerCase().contains("social")) areaUi = "Sociales";
            else if (areaUi.toLowerCase().contains("ingl")) areaUi = "Inglés";
        }
        
        // Convertir PreguntaUI a Question para usar QuizQuestionsAdapter
        allQuestionsFinal = new ArrayList<>();
        for (IslaSimulacroResponse.PreguntaDto p : data.getPreguntas()) {
            com.example.zavira_movil.model.Question q = new com.example.zavira_movil.model.Question();
            try { 
                q.id_pregunta = String.valueOf(p.getIdPregunta()); 
            } catch (Exception e) { 
                q.id_pregunta = "0"; 
            }
            q.area = areaUi != null ? areaUi : p.getArea();
            q.subtema = p.getSubtema() != null ? p.getSubtema() : "todos los subtemas";
            q.enunciado = p.getEnunciado() != null ? p.getEnunciado() : "";
            
            // Convertir opciones (List<String> a List<Option>)
            q.opciones = new ArrayList<>();
            if (p.getOpciones() != null) {
                String[] letras = {"A", "B", "C", "D"};
                for (int i = 0; i < p.getOpciones().size() && i < 4; i++) {
                    String opcionTexto = p.getOpciones().get(i);
                    // Remover prefijo "A. ", "B. ", etc. si existe
                    if (opcionTexto != null && opcionTexto.matches("^[A-Da-d][\\.)]\\s+.*")) {
                        opcionTexto = opcionTexto.substring(2).trim();
                    }
                    q.addOption(letras[i], opcionTexto != null ? opcionTexto : "");
                }
            }
            
            allQuestionsFinal.add(q);
        }
        
        // Inicializar lista de respuestas
        todasLasRespuestasFinal = new ArrayList<>();
        for (int i = 0; i < allQuestionsFinal.size(); i++) {
            todasLasRespuestasFinal.add(null);
        }
        
        // Configurar RecyclerView
        bindingFinal.rvQuestions.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        bindingFinal.rvQuestions.setNestedScrollingEnabled(false);
        
        // Configurar header
        bindingFinal.tvAreaSubtema.setText("Pregunta 1 de " + allQuestionsFinal.size() + " • " + (areaUi != null ? areaUi : ""));
        
        // Configurar botón con color del área
        int areaColor = obtenerColorArea(areaUi);
        bindingFinal.btnEnviar.setBackgroundResource(R.drawable.bg_button_area_color);
        bindingFinal.btnEnviar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(areaColor));
        bindingFinal.btnEnviar.setTextColor(Color.WHITE);
        bindingFinal.btnEnviar.setText("Siguiente Pregunta");
        bindingFinal.btnEnviar.setElevation(4f);
        
        bindingFinal.btnEnviar.setOnClickListener(v -> siguientePreguntaFinal());
        
        // Mostrar la primera pregunta
        currentQuestionIndex = 0;
        mostrarPreguntaActualFinal();
    }
    
    /** Inicializa Isla del Conocimiento (modalidad fácil/difícil) con el layout original */
    private void inicializarIslaConocimiento() {
        setContentView(R.layout.activity_isla_preguntas);

        tvIndex = findViewById(R.id.tvIndex);
        tvTimer = findViewById(R.id.tvTimer);
        progressTimer = findViewById(R.id.progressTimer);
        rvQuestions = findViewById(R.id.rvQuestions);
        btnResponder = findViewById(R.id.btnResponder);

        // Timer y barra de progreso solo para modalidad difícil
        boolean esModalidadDificil = "dificil".equalsIgnoreCase(modalidad);
        tvTimer.setVisibility(esModalidadDificil ? View.VISIBLE : View.GONE);
        if (progressTimer != null) {
            progressTimer.setVisibility(esModalidadDificil ? View.VISIBLE : View.GONE);
        }

        // Convertir preguntas a Question y PreguntaUI
        for (IslaSimulacroResponse.PreguntaDto p : data.getPreguntas()) {
            com.example.zavira_movil.model.Question q = new com.example.zavira_movil.model.Question();
            try { 
                q.id_pregunta = String.valueOf(p.getIdPregunta()); 
            } catch (Exception e) { 
                q.id_pregunta = "0"; 
            }
            q.area = p.getArea() != null ? p.getArea() : "";
            q.subtema = p.getSubtema() != null ? p.getSubtema() : "";
            q.enunciado = p.getEnunciado() != null ? p.getEnunciado() : "";
            
            // Convertir opciones (List<String> a List<Option>)
            q.opciones = new ArrayList<>();
            if (p.getOpciones() != null) {
                String[] letras = {"A", "B", "C", "D"};
                for (int i = 0; i < p.getOpciones().size() && i < 4; i++) {
                    String opcionTexto = p.getOpciones().get(i);
                    // Remover prefijo "A. ", "B. ", etc. si existe
                    if (opcionTexto != null && opcionTexto.matches("^[A-Da-d][\\.)]\\s+.*")) {
                        opcionTexto = opcionTexto.substring(2).trim();
                    }
                    q.addOption(letras[i], opcionTexto != null ? opcionTexto : "");
                }
            }
            
            allQuestionsIsla.add(q);
            
            // Inicializar PreguntaUI para cerrarSimulacro
            PreguntaUI preguntaUI = new PreguntaUI();
            try { 
                preguntaUI.id = Integer.parseInt(q.id_pregunta); 
            } catch (Exception e) { 
                preguntaUI.id = 0; 
            }
            preguntaUI.area = q.area;
            preguntaUI.subtema = q.subtema;
            preguntaUI.enunciado = q.enunciado;
            preguntaUI.opciones = new ArrayList<>();
            for (com.example.zavira_movil.model.Question.Option op : q.opciones) {
                preguntaUI.opciones.add(op.text);
            }
            preguntaUI.elegida = null;
            preguntas.add(preguntaUI);
            
            // Si no tenemos área, obtenerla de la primera pregunta
            if (areaUi == null && q.area != null && !q.area.isEmpty()) {
                areaUi = q.area;
            }
        }
        
        // Si aún no tenemos área, usar un valor por defecto
        if (areaUi == null) {
            areaUi = "Examen";
        }

        // Configurar RecyclerView
        rvQuestions.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        rvQuestions.setNestedScrollingEnabled(false);
        
        // Actualizar header
        actualizarHeader();

        // Configurar botón con color amarillo
        btnResponder.setBackgroundResource(R.drawable.bg_button_area_color);
        btnResponder.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F59E0B")));
        btnResponder.setTextColor(Color.WHITE);

        btnResponder.setOnClickListener(v -> {
            // Validar que haya una opción seleccionada
            List<String> marcadas = adapterIsla.getMarcadas();
            if (marcadas == null || marcadas.isEmpty() || marcadas.get(0) == null || marcadas.get(0).isEmpty()) {
                Toast.makeText(this, "Por favor selecciona una opción antes de continuar", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Guardar respuesta
            PreguntaUI q = new PreguntaUI();
            q.id = Integer.parseInt(allQuestionsIsla.get(idx).id_pregunta);
            q.area = allQuestionsIsla.get(idx).area;
            q.subtema = allQuestionsIsla.get(idx).subtema;
            q.enunciado = allQuestionsIsla.get(idx).enunciado;
            q.opciones = new ArrayList<>();
            for (com.example.zavira_movil.model.Question.Option op : allQuestionsIsla.get(idx).opciones) {
                q.opciones.add(op.text);
            }
            q.elegida = marcadas.get(0);
            if (idx < preguntas.size()) {
                preguntas.set(idx, q);
            } else {
                preguntas.add(q);
            }
            
            if (idx < allQuestionsIsla.size() - 1) {
                idx++;
                mostrarPreguntaActual();
            } else {
                cerrarSimulacro();
            }
        });

        mostrarPreguntaActual();
    }
    
    private void mostrarPreguntaActual() {
        if (idx >= allQuestionsIsla.size()) {
            cerrarSimulacro();
            return;
        }
        
        // Crear lista con solo la pregunta actual
        List<com.example.zavira_movil.model.Question> preguntaActual = new ArrayList<>();
        preguntaActual.add(allQuestionsIsla.get(idx));
        
        // Obtener respuesta guardada si existe
        String respuestaGuardada = null;
        if (idx < preguntas.size() && preguntas.get(idx).elegida != null) {
            respuestaGuardada = preguntas.get(idx).elegida;
        }
        
        // Obtener el área real de la pregunta actual
        String areaPregunta = allQuestionsIsla.get(idx).area != null ? allQuestionsIsla.get(idx).area : "Isla";
        
        // Crear nuevo adapter con la pregunta actual, el área real y forzar color amarillo
        adapterIsla = new com.example.zavira_movil.QuizQuestionsAdapter(preguntaActual, areaPregunta, respuestaGuardada, idx + 1, true);
        rvQuestions.setAdapter(adapterIsla);
        
        actualizarHeader();
        
        // Configurar botón
        btnResponder.setText(idx == allQuestionsIsla.size() - 1 ? "Finalizar" : "Responder");
        
        // Iniciar timer si es modalidad difícil
        if ("dificil".equalsIgnoreCase(modalidad)) {
            iniciarTimer();
        }
    }
    
    private void actualizarHeader() {
        if (idx < allQuestionsIsla.size()) {
            String area = allQuestionsIsla.get(idx).area != null ? allQuestionsIsla.get(idx).area : "";
            tvIndex.setText("Pregunta " + (idx + 1) + " de " + allQuestionsIsla.size() + " • " + area);
        }
    }
    
    private void iniciarTimer() {
        if (timerActual != null) timerActual.cancel();
        detenerSonido(); // Detener sonido previo
        
        // Inicializar sonido
        inicializarSonido();
        
        tvTimer.setText("00:60");
        isLast10Seconds = false; // Reset flag
        if (progressTimer != null) {
            progressTimer.setMax(LIMITE_SEG_DIFICIL);
            progressTimer.setProgress(LIMITE_SEG_DIFICIL);
            progressTimer.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F59E0B")));
        }
        
        // Iniciar sonido de tensión
        iniciarSonidoTension();
        
        timerActual = new CountDownTimer(LIMITE_SEG_DIFICIL * 1000L, 100L) {
            @Override public void onTick(long ms) {
                int s = (int) Math.ceil(ms / 1000.0);
                tvTimer.setText(String.format(Locale.getDefault(), "00:%02d", s));
                
                // Actualizar flag para los últimos 10 segundos
                boolean ahoraEsUltimos10 = s <= 10;
                if (ahoraEsUltimos10 != isLast10Seconds) {
                    isLast10Seconds = ahoraEsUltimos10;
                    // Reiniciar sonido con nueva intensidad
                    detenerSonido();
                    iniciarSonidoTension();
                }
                
                if (progressTimer != null) {
                    int progreso = (int) Math.ceil(ms / 1000.0);
                    progressTimer.setProgress(progreso);
                    
                    if (progreso <= 10) {
                        progressTimer.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336")));
                        tvTimer.setTextColor(Color.parseColor("#F44336"));
                    } else {
                        progressTimer.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F59E0B")));
                        tvTimer.setTextColor(Color.parseColor("#F59E0B"));
                    }
                }
            }
            @Override public void onFinish() {
                detenerSonido(); // Detener sonido al terminar
                // Avanzar automáticamente cuando se acaba el tiempo
                if (idx < allQuestionsIsla.size() - 1) {
                    idx++;
                    mostrarPreguntaActual();
                } else {
                    cerrarSimulacro();
                }
            }
        }.start();
    }
    
    /**
     * Inicializa el generador de tonos para los sonidos interactivos
     */
    private void inicializarSonido() {
        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 80);
            soundHandler = new Handler();
        } catch (Exception e) {
            toneGenerator = null;
            soundHandler = null;
        }
    }
    
    /**
     * Inicia el sonido interactivo durante el countdown (60 segundos)
     * Sonido más emocionante e interactivo para mantener a los estudiantes atentos
     * Si estamos en los últimos 10 segundos, el sonido será más intenso y frecuente
     */
    private void iniciarSonidoTension() {
        if (toneGenerator == null || soundHandler == null) {
            inicializarSonido();
            if (toneGenerator == null || soundHandler == null) return;
        }
        
        detenerSonido(); // Asegurarse de que no hay otro sonido corriendo
        
        soundRunnable = new Runnable() {
            @Override
            public void run() {
                if (timerActual == null) {
                    detenerSonido();
                    return;
                }
                
                try {
                    int tipoTono;
                    int duracion;
                    
                    if (isLast10Seconds) {
                        // Últimos 10 segundos: sonido más intenso y urgente
                        tipoTono = ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD;
                        duracion = 150;
                    } else {
                        // Primeros 50 segundos: sonido de tic-tac más audible
                        tipoTono = ToneGenerator.TONE_DTMF_0;
                        duracion = 120;
                    }
                    
                    toneGenerator.startTone(tipoTono, duracion);
                } catch (Exception e) {
                    try {
                        int fallbackTone = isLast10Seconds 
                            ? ToneGenerator.TONE_DTMF_1 
                            : ToneGenerator.TONE_DTMF_0;
                        toneGenerator.startTone(fallbackTone, 100);
                    } catch (Exception e2) {
                        // Ignorar errores de sonido
                    }
                }
                
                // Calcular intervalo dinámicamente: más frecuente en los últimos 10 segundos
                long intervalo = isLast10Seconds ? 400L : 800L;
                
                if (soundHandler != null) {
                    soundHandler.postDelayed(this, intervalo);
                }
            }
        };
        
        soundHandler.post(soundRunnable);
    }
    
    /**
     * Detiene el sonido de tensión
     */
    private void detenerSonido() {
        if (soundHandler != null && soundRunnable != null) {
            soundHandler.removeCallbacks(soundRunnable);
            soundRunnable = null;
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (timerActual != null) timerActual.cancel();
        detenerSonido();
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }
    
    // ============ MÉTODOS PARA EXAMEN FINAL (pregunta por pregunta) ============
    
    /** Muestra la pregunta actual del examen final */
    private void mostrarPreguntaActualFinal() {
        if (allQuestionsFinal.isEmpty() || currentQuestionIndex >= allQuestionsFinal.size()) {
            Toast.makeText(this, "No hay preguntas.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Crear lista con solo la pregunta actual para el adapter
        List<com.example.zavira_movil.model.Question> preguntaActual = new ArrayList<>();
        preguntaActual.add(allQuestionsFinal.get(currentQuestionIndex));
        
        // Obtener la respuesta guardada para esta pregunta (si existe)
        String respuestaGuardada = todasLasRespuestasFinal.get(currentQuestionIndex);
        
        // Crear adapter con la pregunta actual, respuesta guardada y número de pregunta
        adapterFinal = new com.example.zavira_movil.QuizQuestionsAdapter(preguntaActual, areaUi, respuestaGuardada, currentQuestionIndex + 1);
        bindingFinal.rvQuestions.setAdapter(adapterFinal);
        
        // Actualizar header
        bindingFinal.tvAreaSubtema.setText("Pregunta " + (currentQuestionIndex + 1) + " de " + allQuestionsFinal.size() + " • " + (areaUi != null ? areaUi : ""));
        
        // Actualizar texto y color del botón (usar color del área)
        int areaColor = obtenerColorArea(areaUi);
        bindingFinal.btnEnviar.setBackgroundResource(R.drawable.bg_button_area_color);
        bindingFinal.btnEnviar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(areaColor));
        if (currentQuestionIndex == allQuestionsFinal.size() - 1) {
            bindingFinal.btnEnviar.setText("Finalizar");
        } else {
            bindingFinal.btnEnviar.setText("Siguiente Pregunta");
        }
    }
    
    /** Avanza a la siguiente pregunta del examen final o envía todas las respuestas si es la última */
    private void siguientePreguntaFinal() {
        if (adapterFinal == null || adapterFinal.getItemCount() == 0) {
            Toast.makeText(this, "No hay preguntas.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Verificar que la pregunta actual tenga respuesta
        List<String> marcadas = adapterFinal.getMarcadas();
        if (marcadas.isEmpty() || marcadas.get(0) == null) {
            Toast.makeText(this, "Por favor selecciona una respuesta.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Guardar la respuesta de la pregunta actual
        String respuestaActual = marcadas.get(0);
        todasLasRespuestasFinal.set(currentQuestionIndex, respuestaActual);
        
        // Si es la última pregunta, enviar todas las respuestas
        if (currentQuestionIndex == allQuestionsFinal.size() - 1) {
            cerrarSimulacroFinal();
        } else {
            // Avanzar a la siguiente pregunta
            currentQuestionIndex++;
            mostrarPreguntaActualFinal();
        }
    }
    
    /** Cierra el examen final enviando todas las respuestas */
    private void cerrarSimulacroFinal() {
        if (allQuestionsFinal.isEmpty()) {
            Toast.makeText(this, "No hay preguntas.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Asegurar que la última respuesta está guardada
        if (adapterFinal != null) {
            List<String> marcadasActual = adapterFinal.getMarcadas();
            if (!marcadasActual.isEmpty() && marcadasActual.get(0) != null) {
                todasLasRespuestasFinal.set(currentQuestionIndex, marcadasActual.get(0));
            }
        }
        
        // Verificar que todas las preguntas tengan respuesta
        for (int i = 0; i < todasLasRespuestasFinal.size(); i++) {
            if (todasLasRespuestasFinal.get(i) == null) {
                Toast.makeText(this, "Por favor responde todas las preguntas.", Toast.LENGTH_SHORT).show();
                // Volver a la pregunta sin respuesta
                currentQuestionIndex = i;
                mostrarPreguntaActualFinal();
                return;
            }
        }
        
        if (idSesionFinal == null || idSesionFinal <= 0) {
            Toast.makeText(this, "No hay sesión activa.", Toast.LENGTH_LONG).show();
            return;
        }

        // Construir lista de respuestas para enviar
        List<com.example.zavira_movil.niveleshome.CerrarRequest.Respuesta> resps = new ArrayList<>();
        for (int i = 0; i < todasLasRespuestasFinal.size(); i++) {
            String respuesta = todasLasRespuestasFinal.get(i);
            if (respuesta != null) {
                resps.add(new com.example.zavira_movil.niveleshome.CerrarRequest.Respuesta(i + 1, respuesta));
            }
        }
        
        ApiService api = RetrofitClient.getInstance(getApplicationContext()).create(ApiService.class);
        com.example.zavira_movil.niveleshome.CerrarRequest body = 
            new com.example.zavira_movil.niveleshome.CerrarRequest(idSesionFinal, resps);

        api.cerrarSimulacro(body).enqueue(new Callback<com.example.zavira_movil.niveleshome.CerrarResponse>() {
            @Override public void onResponse(
                Call<com.example.zavira_movil.niveleshome.CerrarResponse> call, 
                Response<com.example.zavira_movil.niveleshome.CerrarResponse> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(IslaPreguntasActivity.this, "No se pudo cerrar ("+res.code()+")", Toast.LENGTH_LONG).show();
                    return;
                }
                
                // Convertir CerrarResponse a IslaCerrarResultadoResponse para usar el mismo método
                com.example.zavira_movil.niveleshome.CerrarResponse cerrarResponse = res.body();
                IslaCerrarResultadoResponse resultado = convertirACerrarResultado(cerrarResponse);
                
                // Calcular tiempo total (no se usa para examen final pero se pasa para compatibilidad)
                long tiempoTotalSegundos = 0;
                long tiempoTotalMs = SystemClock.elapsedRealtime() - inicioSimulacroElapsed;
                tiempoTotalSegundos = tiempoTotalMs / 1000;
                
                onExamenCerrado(resultado, tiempoTotalSegundos);
            }
            @Override public void onFailure(Call<com.example.zavira_movil.niveleshome.CerrarResponse> call, Throwable t) {
                Log.e("SimulacroCerrar", "Error de red", t);
                Toast.makeText(IslaPreguntasActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private String safeOpt(int i, List<String> ops) {
        if (ops == null || ops.size() <= i) return ABCD[i] + ".";
        String t = ops.get(i) == null ? "" : ops.get(i).trim();
        // No dupliques si ya viene "A. ...":
        if (t.matches("^\\s*[A-Da-d][\\.)]\\s+.*")) return t;
        return ABCD[i] + ". " + t;
    }

    private void cerrarSimulacro() {
        // Este método solo se usa para Isla del Conocimiento (no para examen final)
        // El examen final usa cerrarSimulacroFinal()
        if (esExamenFinal) {
            // No debería llegar aquí, pero por seguridad redirigir
            cerrarSimulacroFinal();
            return;
        }
        
        int idSesion = 0;
        try { idSesion = Integer.parseInt(String.valueOf(data.getSesion().getIdSesion())); } catch (Exception ignore) {}

        if (idSesion <= 0) {
            Toast.makeText(this, "Falta id_sesion", Toast.LENGTH_LONG).show();
            return;
        }

        ApiService api = RetrofitClient.getInstance(getApplicationContext()).create(ApiService.class);
        
        // Isla del Conocimiento: usar endpoint movil/isla/simulacro/cerrar
        List<IslaCerrarRequest.Resp> resps = new ArrayList<>();
        
        // Calcular tiempo total para modalidad fácil (una sola vez)
        long tiempoTotalSegundos = 0;
        if ("facil".equalsIgnoreCase(modalidad)) {
            long tiempoTotalMs = SystemClock.elapsedRealtime() - inicioSimulacroElapsed;
            tiempoTotalSegundos = tiempoTotalMs / 1000;
            Log.d("IslaPreguntas", "Tiempo total modalidad fácil: " + tiempoTotalSegundos + " segundos");
        }
        
        for (PreguntaUI q : preguntas) {
            String letra = q.elegida == null ? "" : q.elegida.trim().toUpperCase();
            // Para modalidad fácil, incluir tiempo empleado (tiempo total / número de preguntas)
            // Para modalidad difícil, el tiempo ya se maneja por pregunta (60 segundos máximo)
            long tiempoEmpleadoSeg = 0;
            if ("facil".equalsIgnoreCase(modalidad) && tiempoTotalSegundos > 0) {
                // Distribuir el tiempo total entre todas las preguntas
                tiempoEmpleadoSeg = tiempoTotalSegundos / preguntas.size();
            }
            resps.add(new IslaCerrarRequest.Resp(q.id, letra, tiempoEmpleadoSeg));
        }
        IslaCerrarRequest body = new IslaCerrarRequest(idSesion, resps);
        
        // Guardar tiempoTotalSegundos en una variable final para usar en el callback
        final long tiempoTotalFinal = tiempoTotalSegundos;

        api.cerrarIslaSimulacro(body).enqueue(new Callback<IslaCerrarResultadoResponse>() {
            @Override public void onResponse(Call<IslaCerrarResultadoResponse> call, Response<IslaCerrarResultadoResponse> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(IslaPreguntasActivity.this, "No se pudo cerrar ("+res.code()+")", Toast.LENGTH_LONG).show();
                    return;
                }
                
                // Manejar sistema de vidas antes de mostrar resultados
                IslaCerrarResultadoResponse resultado = res.body();
                
                // Usar el tiempo total calculado anteriormente
                onExamenCerrado(resultado, tiempoTotalFinal);
            }
            @Override public void onFailure(Call<IslaCerrarResultadoResponse> call, Throwable t) {
                Log.e("IslaCerrar", "Error de red", t);
                Toast.makeText(IslaPreguntasActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    /**
     * Convierte CerrarResponse a IslaCerrarResultadoResponse para reutilizar el método onExamenCerrado
     */
    private IslaCerrarResultadoResponse convertirACerrarResultado(
            com.example.zavira_movil.niveleshome.CerrarResponse cerrarResponse) {
        
        IslaCerrarResultadoResponse resultado = new IslaCerrarResultadoResponse();
        resultado.aprueba = cerrarResponse.aprueba != null && cerrarResponse.aprueba;
        resultado.correctas = cerrarResponse.correctas != null ? cerrarResponse.correctas : 0;
        resultado.puntaje = cerrarResponse.getPorcentaje();
        resultado.puntajePorcentaje = cerrarResponse.getPorcentaje();
        
        // Convertir detalleResumen si existe
        if (cerrarResponse.detalleResumen != null && !cerrarResponse.detalleResumen.isEmpty()) {
            resultado.detalleResumen = new ArrayList<>();
            for (com.example.zavira_movil.niveleshome.CerrarResponse.Detalle detalle : cerrarResponse.detalleResumen) {
                IslaCerrarResultadoResponse.Detalle detalleIsla = new IslaCerrarResultadoResponse.Detalle();
                detalleIsla.id_pregunta = detalle.id_pregunta;
                detalleIsla.orden = detalle.orden;
                detalleIsla.correcta = detalle.correcta;
                detalleIsla.marcada = detalle.marcada;
                detalleIsla.es_correcta = detalle.es_correcta != null && detalle.es_correcta;
                resultado.detalleResumen.add(detalleIsla);
            }
        }
        
        return resultado;
    }
    
    private void onExamenCerrado(IslaCerrarResultadoResponse resultado, long tiempoTotalSegundos) {
        int correctas = resultado.correctas;
        
        // CRÍTICO: Simulacro mixto (Isla del Conocimiento) NO usa vidas
        // Solo el examen final (Simulacro) usa vidas
        if (!esExamenFinal) {
            // Simulacro mixto (Isla del Conocimiento): mostrar resultados directamente sin vidas
            int totalPreguntas = preguntas.size();
            String resultadoJson = GsonHolder.gson().toJson(resultado);
            Intent it = new Intent(IslaPreguntasActivity.this, IslaResultadoActivity.class);
            it.putExtra("modalidad", modalidad);
            it.putExtra("resultado_json", resultadoJson);
            it.putExtra("area", areaUi);
            
            // Pasar tiempo total para modalidad fácil
            if ("facil".equalsIgnoreCase(modalidad) && tiempoTotalSegundos > 0) {
                it.putExtra("tiempo_total_segundos", tiempoTotalSegundos);
                Log.d("IslaPreguntas", "Tiempo total enviado a resultados (simulacro mixto): " + tiempoTotalSegundos + " segundos");
            }
            
            startActivity(it);
            finish();
            return;
        }
        
        // Examen final (Simulacro): Aprobar con 20 de 25 correctas
        // El examen final SIEMPRE tiene 25 preguntas
        int totalPreguntas = 25;
        boolean aprueba = correctas >= 20;
        
        // CRÍTICO: Usar TokenManager como fuente única de verdad para userId
        int userIdInt = com.example.zavira_movil.local.TokenManager.getUserId(this);
        if (userIdInt <= 0) {
            Log.e("IslaPreguntas", "ERROR: userId inválido al manejar vidas del examen final");
            Toast.makeText(this, "Error: No se pudo identificar al usuario.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        String userId = String.valueOf(userIdInt);
        
        // CRÍTICO: Normalizar el área al formato canónico del backend antes de guardar vidas
        // El backend espera: "Matematicas", "Lenguaje", "Ciencias", "Sociales", "Ingles"
        String areaCanonica = com.example.zavira_movil.niveleshome.MapeadorArea.toApiArea(areaUi);
        if (areaCanonica == null || areaCanonica.isEmpty()) {
            // Fallback: usar el área UI tal cual
            areaCanonica = areaUi;
        }
        
        Log.d("IslaPreguntas", "Examen final - userId: " + userId + ", areaUi: " + areaUi + ", areaCanonica: " + areaCanonica + ", correctas: " + correctas + "/25, aprueba: " + aprueba);
        
        // Examen final: Lógica con vidas (3 intentos)
        if (aprueba) {
            // Pasó el examen - otorgar insignia y mostrar éxito
            otorgarInsigniaArea(areaUi, resultado, tiempoTotalSegundos);
            return;
        } else {
            // No pasó - manejar vidas
            // IMPORTANTE: consumeLifeAndSync() ya maneja la inicialización si es necesaria
            // NO inicializar manualmente antes para evitar condiciones de carrera
            int vidasAntes = LivesManager.getLives(this, userId, areaCanonica, EXAMEN_FINAL_LEVEL);
            Log.d("IslaPreguntas", "Vidas ANTES de consumir: " + vidasAntes + " (nivel " + EXAMEN_FINAL_LEVEL + ")");
            
            // Consumir UNA vida (esto inicializa a 3 si es necesario y luego consume 1)
            boolean tieneVidas = LivesManager.consumeLifeAndSync(this, userId, areaCanonica, EXAMEN_FINAL_LEVEL);
            
            // Obtener vidas DESPUÉS de consumir
            int nuevasVidas = LivesManager.getLives(this, userId, areaCanonica, EXAMEN_FINAL_LEVEL);
            Log.d("IslaPreguntas", "Vidas DESPUÉS de consumir: " + nuevasVidas + ", tieneVidas: " + tieneVidas);
            
            if (tieneVidas) {
                // Todavía tiene vidas - mostrar diálogo
                mostrarDialogoVidas(correctas, totalPreguntas, nuevasVidas, false);
            } else {
                // Se acabaron las vidas - retroceder al nivel 5 y reiniciar vidas
                Log.d("IslaPreguntas", "Vidas agotadas en examen final (nivel 6), retrocediendo al nivel 5");
                ProgressLockManager.retrocederPorFalloAndSync(this, userId, areaCanonica, EXAMEN_FINAL_LEVEL);
                // Reiniciar vidas para el nivel retrocedido (nivel 5)
                int nivelRetrocedido = ProgressLockManager.getUnlockedLevel(this, userId, areaCanonica);
                Log.d("IslaPreguntas", "Nivel retrocedido a: " + nivelRetrocedido);
                LivesManager.resetLivesAndSync(this, userId, areaCanonica, nivelRetrocedido);
                mostrarDialogoVidas(correctas, totalPreguntas, 0, true);
            }
        }
    }
    
    private void mostrarDialogoVidas(int correctas, int totalPreguntas, int vidasRestantes, boolean sinVidas) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_vidas_nivel, null);
        
        // Obtener color del área
        int areaColor = obtenerColorArea(areaUi);
        
        // Configurar color de la tarjeta del diálogo
        com.google.android.material.card.MaterialCardView cardDialog = dialogView.findViewById(R.id.cardDialog);
        if (cardDialog != null) {
            int colorClaro = Color.argb(20, Color.red(areaColor), Color.green(areaColor), Color.blue(areaColor));
            cardDialog.setCardBackgroundColor(colorClaro);
            cardDialog.setStrokeColor(areaColor);
            cardDialog.setStrokeWidth(2);
        }
        
        ImageView ivIcono = dialogView.findViewById(R.id.ivIconoVida);
        TextView tvTitulo = dialogView.findViewById(R.id.tvTitulo);
        TextView tvSubtitulo = dialogView.findViewById(R.id.tvSubtitulo);
        LinearLayout llCorazones = dialogView.findViewById(R.id.llCorazones);
        TextView tvVidasRestantes = dialogView.findViewById(R.id.tvVidasRestantes);
        TextView tvRegeneracion = dialogView.findViewById(R.id.tvRegeneracion);
        TextView tvMensajeFinal = dialogView.findViewById(R.id.tvMensajeFinal);
        MaterialButton btnUsarVida = dialogView.findViewById(R.id.btnUsarVida);
        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        
        if (ivIcono != null) {
            ivIcono.setImageResource(android.R.drawable.ic_menu_revert);
            ivIcono.setColorFilter(areaColor);
            tvTitulo.setText("Necesitas Practicar Más");
            tvTitulo.setTextColor(Color.parseColor("#1F2937"));
        }
        
        // Configurar subtítulo
        tvSubtitulo.setText("Obtuviste " + correctas + " de " + totalPreguntas + " respuestas correctas");
        
        // Configurar corazones
        llCorazones.removeAllViews();
        for (int i = 0; i < 3; i++) {
            ImageView ivCorazon = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dp(28), dp(28)
            );
            params.setMargins(dp(4), 0, dp(4), 0);
            ivCorazon.setLayoutParams(params);
            ivCorazon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            
            if (i < vidasRestantes) {
                ivCorazon.setImageResource(R.drawable.ic_heart_filled);
                ivCorazon.setColorFilter(areaColor, android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                ivCorazon.setImageResource(R.drawable.ic_heart_empty);
                ivCorazon.setColorFilter(Color.parseColor("#CCCCCC"), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            llCorazones.addView(ivCorazon);
        }
        
        // Configurar vidas restantes
        if (sinVidas) {
            tvVidasRestantes.setVisibility(View.GONE);
            tvRegeneracion.setVisibility(View.VISIBLE);
            tvRegeneracion.setTextColor(areaColor);
            tvMensajeFinal.setVisibility(View.VISIBLE);
            tvMensajeFinal.setTextColor(areaColor);
            btnUsarVida.setVisibility(View.GONE);
        } else {
            tvVidasRestantes.setText("Te quedan " + vidasRestantes + " vidas");
            tvVidasRestantes.setTextColor(areaColor);
            tvRegeneracion.setVisibility(View.GONE);
            tvMensajeFinal.setVisibility(View.GONE);
            btnUsarVida.setText("Usar 1 Vida (" + vidasRestantes + " restantes)");
            btnUsarVida.setBackgroundTintList(android.content.res.ColorStateList.valueOf(areaColor));
            btnUsarVida.setIconResource(android.R.drawable.ic_menu_revert);
        }
        
        // Crear y mostrar diálogo
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_overlay_oscuro);
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
        
        btnUsarVida.setOnClickListener(v -> {
            dialog.dismiss();
            finish(); // Volver para reintentar
        });
        
        btnCancelar.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });
        
        dialog.show();
    }
    
    private void mostrarDialogoExito(String mensaje, String area, IslaCerrarResultadoResponse resultado, long tiempoTotalSegundos) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_exito_nivel, null);
        int areaColor = obtenerColorArea(area);
        
        // Configurar color de la tarjeta del diálogo
        com.google.android.material.card.MaterialCardView cardDialog = dialogView.findViewById(R.id.cardDialog);
        if (cardDialog != null) {
            cardDialog.setCardBackgroundColor(Color.WHITE);
            cardDialog.setStrokeColor(areaColor);
            cardDialog.setStrokeWidth(3);
        }
        
        TextView tvTitulo = dialogView.findViewById(R.id.tvTitulo);
        TextView tvMensaje = dialogView.findViewById(R.id.tvMensaje);
        MaterialButton btnContinuar = dialogView.findViewById(R.id.btnContinuar);
        
        tvMensaje.setText(mensaje);
        
        // Configurar botón con color del área
        btnContinuar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(areaColor));
        
        // Crear y mostrar diálogo
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_overlay_oscuro);
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
        
        btnContinuar.setOnClickListener(v -> {
            dialog.dismiss();
            // Ir a resultados
            String resultadoJson = GsonHolder.gson().toJson(resultado);
            Intent it = new Intent(IslaPreguntasActivity.this, IslaResultadoActivity.class);
            it.putExtra("modalidad", modalidad);
            it.putExtra("resultado_json", resultadoJson);
            it.putExtra("area", areaUi); // Pasar el área a la actividad de resultados
            
            // Pasar tiempo total para modalidad fácil
            if ("facil".equalsIgnoreCase(modalidad) && tiempoTotalSegundos > 0) {
                it.putExtra("tiempo_total_segundos", tiempoTotalSegundos);
                Log.d("IslaPreguntas", "Tiempo total enviado a resultados: " + tiempoTotalSegundos + " segundos");
            }
            
            startActivity(it);
            finish();
        });
        
        dialog.show();
    }
    
    private int obtenerColorArea(String area) {
        if (area == null) return Color.parseColor("#B6B9C2");
        String a = area.toLowerCase().trim();
        
        if (a.contains("matem")) return ContextCompat.getColor(this, R.color.area_matematicas);
        if (a.contains("lengua") || a.contains("lectura") || a.contains("espa") || a.contains("critica")) 
            return ContextCompat.getColor(this, R.color.area_lenguaje);
        if (a.contains("social") || a.contains("ciudad")) 
            return ContextCompat.getColor(this, R.color.area_sociales);
        if (a.contains("cien") || a.contains("biolo") || a.contains("fis") || a.contains("quim")) 
            return ContextCompat.getColor(this, R.color.area_ciencias);
        if (a.contains("ingl")) 
            return ContextCompat.getColor(this, R.color.area_ingles);
        
        return Color.parseColor("#B6B9C2");
    }
    
    private void otorgarInsigniaArea(String area, IslaCerrarResultadoResponse resultado, long tiempoTotalSegundos) {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        OtorgarAreaRequest request = new OtorgarAreaRequest(area);
        
        api.otorgarInsigniaArea(request).enqueue(new Callback<OtorgarAreaResponse>() {
            @Override
            public void onResponse(Call<OtorgarAreaResponse> call, Response<OtorgarAreaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OtorgarAreaResponse resp = response.body();
                    if (resp.isOtorgada()) {
                        // Mostrar diálogo de insignia
                        mostrarDialogoInsignia(area, resultado, tiempoTotalSegundos);
                    } else {
                        // Aunque no se otorgue, mostrar éxito
                        mostrarDialogoExito("¡Felicitaciones! Aprobaste el Examen Final", area, resultado, tiempoTotalSegundos);
                    }
                } else {
                    // En caso de error, mostrar éxito de todas formas
                    mostrarDialogoExito("¡Felicitaciones! Aprobaste el Examen Final", area, resultado, tiempoTotalSegundos);
                }
            }
            
            @Override
            public void onFailure(Call<OtorgarAreaResponse> call, Throwable t) {
                // En caso de error, mostrar éxito de todas formas
                mostrarDialogoExito("¡Felicitaciones! Aprobaste el Examen Final", area, resultado, tiempoTotalSegundos);
            }
        });
    }
    
    private void mostrarDialogoInsignia(String area, IslaCerrarResultadoResponse resultado, long tiempoTotalSegundos) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_insignia_aprobado, null);
        
        int areaColor = obtenerColorArea(area);
        
        // Configurar color de la tarjeta del diálogo
        com.google.android.material.card.MaterialCardView cardDialog = dialogView.findViewById(R.id.cardDialog);
        if (cardDialog != null) {
            cardDialog.setCardBackgroundColor(Color.WHITE);
            cardDialog.setStrokeColor(areaColor);
            cardDialog.setStrokeWidth(3);
        }
        
        ImageView ivIcono = dialogView.findViewById(R.id.ivIconoInsignia);
        TextView tvTitulo = dialogView.findViewById(R.id.tvTitulo);
        TextView tvMensaje = dialogView.findViewById(R.id.tvMensaje);
        MaterialButton btnContinuar = dialogView.findViewById(R.id.btnContinuar);
        
        if (ivIcono != null) {
            // Usar un ícono de medalla o trofeo
            ivIcono.setImageResource(android.R.drawable.star_big_on);
            ivIcono.setColorFilter(areaColor);
        }
        
        tvTitulo.setText("¡Insignia Obtenida!");
        tvTitulo.setTextColor(Color.parseColor("#1F2937"));
        
        tvMensaje.setText("¡Felicitaciones! Has aprobado el Examen Final de " + area + 
                         " con " + resultado.correctas + " de 25 respuestas correctas. " +
                         "Has obtenido una insignia por tu excelente desempeño.");
        
        btnContinuar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(areaColor));
        
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();
        
        btnContinuar.setOnClickListener(v -> {
            dialog.dismiss();
            // Usar el tiempo total que ya viene como parámetro (ya está calculado)
            // Si no viene calculado y es modalidad fácil, recalcular
            long tiempoFinal = tiempoTotalSegundos;
            if (tiempoFinal == 0 && "facil".equalsIgnoreCase(modalidad)) {
                long tiempoTotalMs = SystemClock.elapsedRealtime() - inicioSimulacroElapsed;
                tiempoFinal = tiempoTotalMs / 1000;
            }
            // Continuar con el diálogo de éxito normal
            mostrarDialogoExito("¡Felicitaciones! Aprobaste el Examen Final", area, resultado, tiempoFinal);
        });
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_overlay_oscuro);
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
        
        dialog.show();
    }
    
    private int dp(int px) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(px * density);
    }
}
