package com.example.zavira_movil.niveleshome;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zavira_movil.QuizQuestionsAdapter;
import com.example.zavira_movil.R;
import com.example.zavira_movil.databinding.ActivityQuizBinding;
import com.example.zavira_movil.local.UserSession;
import com.example.zavira_movil.model.Question;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Crea sesión, carga máx 10 preguntas, envía y desbloquea/retrocede según puntaje. */
public class QuizActivity extends AppCompatActivity {

    public static final String EXTRA_AREA    = "extra_area";     // área visible UI
    public static final String EXTRA_SUBTEMA = "extra_subtema";  // subtema visible UI
    public static final String EXTRA_NIVEL   = "extra_nivel";    // 1..5

    private ActivityQuizBinding binding;
    private QuizQuestionsAdapter adapter;
    private Integer idSesion;

    private String areaUi, subtemaUi; // usamos UI para ProgressLockManager
    private int nivel;
    private int currentQuestionIndex = 0; // Índice de la pregunta actual
    private List<Question> allQuestions = new ArrayList<>(); // Todas las preguntas
    private List<String> todasLasRespuestas = new ArrayList<>(); // Respuestas guardadas mientras avanza

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Asegurar fondo blanco y sin bordes
        getWindow().setBackgroundDrawableResource(android.R.color.white);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }

        areaUi    = getIntent().getStringExtra(EXTRA_AREA);
        subtemaUi = getIntent().getStringExtra(EXTRA_SUBTEMA);
        nivel     = getIntent().getIntExtra(EXTRA_NIVEL, 1);

        // Configurar header
        binding.tvAreaSubtema.setText("Pregunta 1 de 5 • " + (areaUi != null ? areaUi : ""));

        binding.rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuizQuestionsAdapter(new ArrayList<>(), areaUi);
        binding.rvQuestions.setAdapter(adapter);

        // Ocultar ProgressBar completamente desde el inicio - NO mostrar pantalla de carga
        if (binding.progress != null) {
            binding.progress.setVisibility(View.GONE);
        }

        // Configurar botón con color del área (igual que la barra de progreso)
        int areaColor = obtenerColorArea(areaUi);
        binding.btnEnviar.setBackgroundResource(R.drawable.bg_button_area_color);
        binding.btnEnviar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(areaColor));
        binding.btnEnviar.setTextColor(Color.WHITE);
        binding.btnEnviar.setText("Siguiente Pregunta");
        binding.btnEnviar.setElevation(4f);
        
        binding.btnEnviar.setOnClickListener(v -> siguientePregunta());

        crearParadaYMostrar();
    }

    private void setLoading(boolean b) {
        // NO mostrar pantalla de carga - mantener invisible siempre para mejor UX
        if (binding.progress != null) {
            binding.progress.setVisibility(View.GONE);
        }
        // El botón se mantiene habilitado para mejor experiencia
        binding.btnEnviar.setEnabled(true);
    }

    private static Integer toIntOrNull(String s) {
        try { return (s == null) ? null : Integer.parseInt(s); } catch (Exception e) { return null; }
    }

    /** Crea sesión y pinta preguntas (máximo 10). */
    private void crearParadaYMostrar() {
        setLoading(true);

        final String areaApi    = MapeadorArea.toApiArea(areaUi);
        final String subtemaApi = MapeadorArea.normalizeSubtema(subtemaUi);

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        ParadaRequest req = new ParadaRequest(
                areaApi != null ? areaApi : "",
                subtemaApi != null ? subtemaApi : "",
                Math.max(1, Math.min(5, nivel)),
                true,
                1
        );

        api.crearParada(req).enqueue(new Callback<ParadaResponse>() {
            @Override public void onResponse(Call<ParadaResponse> call, Response<ParadaResponse> resp) {
                setLoading(false);

                if (!resp.isSuccessful()) {
                    Toast.makeText(QuizActivity.this,
                            "No se pudo crear la sesión (HTTP " + resp.code() + ")",
                            Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                ParadaResponse pr = resp.body();
                if (pr == null) {
                    Toast.makeText(QuizActivity.this,
                            "Servidor respondió " + resp.code() + " sin cuerpo JSON.",
                            Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                if (pr.sesion != null) idSesion = toIntOrNull(pr.sesion.idSesion);

                ArrayList<ApiQuestion> apiQs = new ArrayList<>();
                if (pr.preguntas != null) apiQs.addAll(pr.preguntas);
                if (pr.preguntasPorSubtema != null) apiQs.addAll(pr.preguntasPorSubtema);
                if (pr.sesion != null) {
                    if (pr.sesion.preguntas != null) apiQs.addAll(pr.sesion.preguntas);
                    if (pr.sesion.preguntasPorSubtema != null) apiQs.addAll(pr.sesion.preguntasPorSubtema);
                }

                ArrayList<Question> preguntas = ApiQuestionMapper.toAppList(apiQs);
                if (preguntas.size() > 10) preguntas = new ArrayList<>(preguntas.subList(0, 10));
                if (preguntas.isEmpty()) {
                    Toast.makeText(QuizActivity.this, "No hay preguntas para este subtema.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                // Guardar todas las preguntas
                allQuestions = preguntas;
                currentQuestionIndex = 0;
                todasLasRespuestas = new ArrayList<>();
                // Inicializar lista de respuestas con nulls
                for (int i = 0; i < preguntas.size(); i++) {
                    todasLasRespuestas.add(null);
                }
                
                // Mostrar la primera pregunta
                mostrarPreguntaActual();
            }

            @Override public void onFailure(Call<ParadaResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(QuizActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    /** Muestra la pregunta actual */
    private void mostrarPreguntaActual() {
        if (allQuestions.isEmpty() || currentQuestionIndex >= allQuestions.size()) {
            Toast.makeText(this, "No hay preguntas.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Crear lista con solo la pregunta actual para el adapter
        List<Question> preguntaActual = new ArrayList<>();
        preguntaActual.add(allQuestions.get(currentQuestionIndex));
        
        // Obtener la respuesta guardada para esta pregunta (si existe)
        String respuestaGuardada = todasLasRespuestas.get(currentQuestionIndex);
        
        // Crear adapter con la pregunta actual, respuesta guardada y número de pregunta
        adapter = new QuizQuestionsAdapter(preguntaActual, areaUi, respuestaGuardada, currentQuestionIndex + 1);
        binding.rvQuestions.setAdapter(adapter);
        
        // Actualizar header
        binding.tvAreaSubtema.setText("Pregunta " + (currentQuestionIndex + 1) + " de " + allQuestions.size() + " • " + (areaUi != null ? areaUi : ""));
        
        // Actualizar texto y color del botón (usar color del área)
        int areaColor = obtenerColorArea(areaUi);
        binding.btnEnviar.setBackgroundResource(R.drawable.bg_button_area_color);
        binding.btnEnviar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(areaColor));
        if (currentQuestionIndex == allQuestions.size() - 1) {
            binding.btnEnviar.setText("Finalizar");
        } else {
            binding.btnEnviar.setText("Siguiente Pregunta");
        }
    }
    
    /** Avanza a la siguiente pregunta o envía todas las respuestas si es la última */
    private void siguientePregunta() {
        if (adapter.getItemCount() == 0) {
            Toast.makeText(this, "No hay preguntas.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Verificar que la pregunta actual tenga respuesta
        List<String> marcadas = adapter.getMarcadas();
        if (marcadas.isEmpty() || marcadas.get(0) == null) {
            Toast.makeText(this, "Por favor selecciona una respuesta.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Guardar la respuesta de la pregunta actual
        String respuestaActual = marcadas.get(0);
        todasLasRespuestas.set(currentQuestionIndex, respuestaActual);
        
        // Si es la última pregunta, enviar todas las respuestas
        if (currentQuestionIndex == allQuestions.size() - 1) {
            enviarTodasLasRespuestas();
        } else {
            // Avanzar a la siguiente pregunta
            currentQuestionIndex++;
            mostrarPreguntaActual();
        }
    }
    
    /** Envía todas las respuestas: intenta NUEVO y si falla con "cannot extract elements from an object", reintenta LEGACY. */
    private void enviarTodasLasRespuestas() {
        if (allQuestions.isEmpty()) {
            Toast.makeText(this, "No hay preguntas.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Asegurar que la última respuesta está guardada
        List<String> marcadasActual = adapter.getMarcadas();
        if (!marcadasActual.isEmpty() && marcadasActual.get(0) != null) {
            todasLasRespuestas.set(currentQuestionIndex, marcadasActual.get(0));
        }
        
        // Verificar que todas las preguntas tengan respuesta
        for (int i = 0; i < todasLasRespuestas.size(); i++) {
            if (todasLasRespuestas.get(i) == null) {
                Toast.makeText(this, "Por favor responde todas las preguntas.", Toast.LENGTH_SHORT).show();
                // Volver a la pregunta sin respuesta
                currentQuestionIndex = i;
                mostrarPreguntaActual();
                return;
            }
        }
        
        if (idSesion == null) {
            Toast.makeText(this, "No hay sesión activa.", Toast.LENGTH_LONG).show();
            return;
        }

        // Construir lista de respuestas para enviar
        List<CerrarRequest.Respuesta> rs = new ArrayList<>();
        for (int i = 0; i < todasLasRespuestas.size(); i++) {
            String respuesta = todasLasRespuestas.get(i);
            if (respuesta != null) {
                rs.add(new CerrarRequest.Respuesta(i + 1, respuesta));
            }
        }

        setLoading(true);
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);

        // Primer intento: formato NUEVO
        api.cerrarSesion(new CerrarRequest(idSesion, rs)).enqueue(new Callback<CerrarResponse>() {
            @Override public void onResponse(Call<CerrarResponse> call, Response<CerrarResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    onCierreOk(response.body());
                    return;
                }

                // ¿Mensaje típico del server legacy?
                String err = readErr(response.errorBody());
                boolean esLegacy = response.code() == 400 &&
                        err != null && err.contains("cannot extract elements from an object");

                if (esLegacy) {
                    // Reintento: LEGACY
                    Map<String, Object> compat = new HashMap<>();
                    compat.put("id_sesion", idSesion);
                    List<List<Object>> legacyRs = new ArrayList<>();
                    for (CerrarRequest.Respuesta r : rs) {
                        List<Object> par = new ArrayList<>();
                        par.add(r.opcion);
                        par.add(r.orden);
                        legacyRs.add(par);
                    }
                    compat.put("respuestas", legacyRs);

                    api.cerrarSesionCompat(compat).enqueue(new Callback<CerrarResponse>() {
                        @Override public void onResponse(Call<CerrarResponse> call2, Response<CerrarResponse> resp2) {
                            setLoading(false);
                            if (resp2.isSuccessful() && resp2.body() != null) {
                                onCierreOk(resp2.body());
                            } else {
                                Toast.makeText(QuizActivity.this,
                                        "No se pudo cerrar (compat) HTTP " + resp2.code(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override public void onFailure(Call<CerrarResponse> call2, Throwable t) {
                            setLoading(false);
                            Toast.makeText(QuizActivity.this, "Fallo compat: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    setLoading(false);
                    Toast.makeText(QuizActivity.this,
                            "No se pudo cerrar la sesión (HTTP " + response.code() + ").",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override public void onFailure(Call<CerrarResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(QuizActivity.this, "Fallo al cerrar: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void onCierreOk(CerrarResponse r) {
        setLoading(false);
        Integer puntaje = r.puntaje;
        int correctas = r.correctas != null ? r.correctas : 0;
        int totalPreguntas = allQuestions.size();

        // CRÍTICO: Usar TokenManager como fuente única de verdad
        int userIdInt = com.example.zavira_movil.local.TokenManager.getUserId(this);
        if (userIdInt <= 0) {
            android.util.Log.e("QuizActivity", "ERROR: userId inválido al cerrar sesión");
            finish();
            return;
        }
        String userId = String.valueOf(userIdInt);
        
        // Nivel 1: Pasa con 4 o 5 correctas (sin límite de intentos)
        if (nivel == 1) {
            if (correctas >= 4) {
                // Pasa al nivel 2
                ProgressLockManager.unlockNextAndSync(this, userId, areaUi, nivel);
                // Reiniciar vidas para el nivel 2
                LivesManager.resetLivesForNextLevelAndSync(this, userId, areaUi, 2);
                
                // Mostrar diálogo explicativo del sistema de vidas (solo la primera vez)
                mostrarDialogoExplicacionVidas(areaUi);
                return;
            } else {
                // No pasa, pero puede seguir intentando (sin límite)
                Toast.makeText(this,
                        "Correctas: " + correctas + " | Puntaje: " + (puntaje != null ? puntaje : 0) + "%\n" +
                        "Necesitas 4 o 5 correctas para pasar al siguiente nivel.",
                        Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }

        // Niveles 2+: Lógica con vidas
        if (Boolean.TRUE.equals(r.aprueba)) {
            // Si aprueba nivel 5, desbloquea Examen Final; si no, avanza normal
            if (nivel >= 5 && (puntaje != null && puntaje >= 80)) {
                ProgressLockManager.unlockFinalExamAndSync(this, userId, areaUi);
                // Inicializar vidas para el examen final (nivel 6)
                LivesManager.resetLivesAndSync(this, userId, areaUi, 6);
                // Mostrar diálogo explicativo del examen final
                mostrarDialogoExplicacionExamenFinal(areaUi);
            } else {
                ProgressLockManager.unlockNextAndSync(this, userId, areaUi, nivel);
                // Reiniciar vidas para el siguiente nivel
                LivesManager.resetLivesForNextLevelAndSync(this, userId, areaUi, nivel + 1);
                mostrarDialogoExito("¡Felicitaciones! Pasaste al Nivel " + (nivel + 1), areaUi);
            }
            return;
        } else {
            // No pasó - manejar vidas
            // CRÍTICO: Solo consumir 1 vida por intento, no más
            // IMPORTANTE: Si las vidas no están inicializadas, inicializarlas primero
            int vidasRestantes = LivesManager.getLives(this, userId, areaUi, nivel);
            android.util.Log.d("QuizActivity", "Antes de consumir vida - vidasRestantes: " + vidasRestantes + ", nivel: " + nivel);
            
            if (vidasRestantes == -1) {
                // Si no están inicializadas, inicializar con MAX_LIVES
                LivesManager.resetLives(this, userId, areaUi, nivel);
                vidasRestantes = LivesManager.getLives(this, userId, areaUi, nivel);
                android.util.Log.d("QuizActivity", "Vidas inicializadas: " + vidasRestantes);
            }
            
            // CRÍTICO: Solo consumir 1 vida - el backend calculará las vidas correctamente al cerrar la sesión
            // NO sincronizar vidas aquí porque el backend ya las calculará correctamente
            boolean tieneVidas = LivesManager.consumeLife(this, userId, areaUi, nivel);
            int nuevasVidas = LivesManager.getLives(this, userId, areaUi, nivel);
            android.util.Log.d("QuizActivity", "Después de consumir vida - tieneVidas: " + tieneVidas + ", nuevasVidas: " + nuevasVidas);
            
            // Sincronizar vidas con backend DESPUÉS de consumir (solo para informar, el backend calculará correctamente)
            // Solo sincronizar si el nivel es mayor a 1 (nivel 1 no tiene vidas)
            if (nivel > 1) {
                com.example.zavira_movil.sincronizacion.ProgresoSincronizador.getInstance()
                    .actualizarVidasEnBackend(this, userId, areaUi, nivel, nuevasVidas);
            }
            
            if (tieneVidas) {
                // Todavía tiene vidas - mostrar diálogo
                mostrarDialogoVidas(correctas, totalPreguntas, nuevasVidas, false);
            } else {
                // CRÍTICO: Se acabaron las vidas - retroceder INMEDIATAMENTE y bloquear el nivel
                android.util.Log.d("QuizActivity", "Vidas agotadas en nivel " + nivel + " - retrocediendo INMEDIATAMENTE");
                
                // Retroceder INMEDIATAMENTE al nivel anterior (esto bloquea el nivel actual)
                ProgressLockManager.retrocederPorFalloAndSync(this, userId, areaUi, nivel);
                
                // Obtener el nivel retrocedido (debe ser nivel - 1)
                int nivelRetrocedido = ProgressLockManager.getUnlockedLevel(this, userId, areaUi);
                android.util.Log.d("QuizActivity", "Nivel retrocedido a: " + nivelRetrocedido + " (desde nivel " + nivel + ")");
                
                // Reiniciar vidas para el nivel retrocedido (3 vidas nuevas)
                LivesManager.resetLivesAndSync(this, userId, areaUi, nivelRetrocedido);
                
                // Verificar que el nivel se bloqueó correctamente
                int nivelVerificado = ProgressLockManager.getUnlockedLevel(this, userId, areaUi);
                if (nivelVerificado != nivelRetrocedido) {
                    android.util.Log.e("QuizActivity", "ERROR: El nivel no se bloqueó correctamente. Esperado: " + nivelRetrocedido + ", Obtenido: " + nivelVerificado);
                } else {
                    android.util.Log.d("QuizActivity", "✓ Nivel bloqueado correctamente. Nivel actual desbloqueado: " + nivelVerificado);
                }
                
                mostrarDialogoVidas(correctas, totalPreguntas, 0, true);
            }
        }
        
        // CRÍTICO: Sincronizar desde el backend DESPUÉS de manejar vidas para asegurar consistencia
        // El backend ya calculó las vidas correctamente al cerrar la sesión
        // IMPORTANTE: Sincronizar inmediatamente para que el retroceso se refleje correctamente
        if (userIdInt > 0) {
            // Sincronizar inmediatamente para que el retroceso se refleje correctamente
            com.example.zavira_movil.sincronizacion.ProgresoSincronizador.getInstance()
                .sincronizarDesdeBackend(QuizActivity.this, String.valueOf(userIdInt));
        }
    }
    
    private void mostrarDialogoVidas(int correctas, int totalPreguntas, int vidasRestantes, boolean sinVidas) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_vidas_nivel, null);
        
        // Obtener color del área
        int areaColor = obtenerColorArea(areaUi);
        
        // Configurar color de la tarjeta del diálogo
        com.google.android.material.card.MaterialCardView cardDialog = dialogView.findViewById(R.id.cardDialog);
        if (cardDialog != null) {
            // Usar un color muy claro del área como fondo de la tarjeta
            int colorClaro = Color.argb(20, Color.red(areaColor), Color.green(areaColor), Color.blue(areaColor));
            cardDialog.setCardBackgroundColor(colorClaro);
            cardDialog.setStrokeColor(areaColor);
            cardDialog.setStrokeWidth(2);
        }
        
        // Configurar elementos del diálogo
        ImageView ivIcono = dialogView.findViewById(R.id.ivIconoVida);
        TextView tvTitulo = dialogView.findViewById(R.id.tvTitulo);
        TextView tvSubtitulo = dialogView.findViewById(R.id.tvSubtitulo);
        LinearLayout llCorazones = dialogView.findViewById(R.id.llCorazones);
        TextView tvVidasRestantes = dialogView.findViewById(R.id.tvVidasRestantes);
        TextView tvRegeneracion = dialogView.findViewById(R.id.tvRegeneracion);
        TextView tvMensajeFinal = dialogView.findViewById(R.id.tvMensajeFinal);
        MaterialButton btnUsarVida = dialogView.findViewById(R.id.btnUsarVida);
        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        
        // Configurar icono y título según el ejemplo
        if (sinVidas) {
            ivIcono.setImageResource(android.R.drawable.ic_menu_revert);
            ivIcono.setColorFilter(Color.parseColor("#E53935"));
            tvTitulo.setText("Sin Vidas Disponibles");
            tvTitulo.setTextColor(Color.parseColor("#1F2937")); // Título oscuro para mejor legibilidad
        } else {
            ivIcono.setImageResource(android.R.drawable.ic_menu_revert);
            ivIcono.setColorFilter(areaColor);
            tvTitulo.setText("Necesitas Practicar Más");
            tvTitulo.setTextColor(Color.parseColor("#1F2937")); // Título oscuro para mejor legibilidad
        }
        
        // Configurar subtítulo
        tvSubtitulo.setText("Obtuviste " + correctas + " de " + totalPreguntas + " respuestas correctas");
        
        // Configurar corazones - diseño mejorado según ejemplo (más pequeños)
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
                // Corazón lleno del color del área
                ivCorazon.setImageResource(R.drawable.ic_heart_filled);
                ivCorazon.setColorFilter(areaColor, android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                // Corazón vacío
                ivCorazon.setImageResource(R.drawable.ic_heart_empty);
                ivCorazon.setColorFilter(Color.parseColor("#CCCCCC"), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            llCorazones.addView(ivCorazon);
        }
        
        // Configurar vidas restantes
        if (sinVidas) {
            tvVidasRestantes.setVisibility(View.GONE);
            tvRegeneracion.setVisibility(View.VISIBLE);
            tvRegeneracion.setTextColor(areaColor); // Color del área para el mensaje de regeneración
            tvMensajeFinal.setVisibility(View.VISIBLE);
            tvMensajeFinal.setTextColor(areaColor); // Color del área para el mensaje final
            btnUsarVida.setVisibility(View.GONE);
        } else {
            tvVidasRestantes.setText("Te quedan " + vidasRestantes + " vidas");
            tvVidasRestantes.setTextColor(areaColor); // Color del área para vidas restantes
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
        
        // Configurar ventana del diálogo: overlay oscuro para fondo y transparente para el diálogo
        if (dialog.getWindow() != null) {
            // Fondo oscuro semi-transparente para el overlay
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_overlay_oscuro);
            // Asegurar que el diálogo tenga el tamaño correcto
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
        
        btnUsarVida.setOnClickListener(v -> {
            dialog.dismiss();
            // Reiniciar el nivel para volver a intentar
            setResult(RESULT_OK); // Notificar que hubo cambios para actualizar la UI
            finish();
        });
        
        btnCancelar.setOnClickListener(v -> {
            dialog.dismiss();
            setResult(RESULT_OK); // Notificar que hubo cambios para actualizar la UI
            finish();
        });
        
        dialog.show();
    }
    
    private void mostrarDialogoExplicacionVidas(String area) {
        // Verificar si ya vio el tutorial
        int userIdInt = com.example.zavira_movil.local.TokenManager.getUserId(this);
        if (userIdInt <= 0) {
            android.util.Log.e("QuizActivity", "ERROR: userId inválido en mostrarDialogoExplicacionVidas");
            return;
        }
        int userId = userIdInt;
        String prefsKey = "vidas_tutorial_visto_" + userId + "_" + area;
        boolean yaVisto = getSharedPreferences("vidas_tutorial", MODE_PRIVATE).getBoolean(prefsKey, false);
        
        if (yaVisto) {
            // Si ya vio el tutorial, solo mostrar toast y cerrar
            Toast.makeText(this, "¡Felicitaciones! Pasaste al Nivel 2", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
            return;
        }
        
        // Mostrar diálogo explicativo
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_explicacion_vidas, null);
        int areaColor = obtenerColorArea(area);
        
        // Configurar color de la tarjeta del diálogo - diseño más sutil
        com.google.android.material.card.MaterialCardView cardDialog = dialogView.findViewById(R.id.cardDialog);
        if (cardDialog != null) {
            // Fondo blanco con borde sutil del color del área
            cardDialog.setCardBackgroundColor(Color.WHITE);
            cardDialog.setStrokeColor(areaColor);
            cardDialog.setStrokeWidth(3);
        }
        
        ImageView ivIcono = dialogView.findViewById(R.id.ivIconoVida);
        TextView tvTitulo = dialogView.findViewById(R.id.tvTitulo);
        TextView tvMensaje = dialogView.findViewById(R.id.tvMensaje);
        LinearLayout llCorazones = dialogView.findViewById(R.id.llCorazones);
        MaterialButton btnEntendido = dialogView.findViewById(R.id.btnEntendido);
        
        // Configurar icono con color del área (ya está dentro del contenedor circular)
        if (ivIcono != null) {
            ivIcono.setImageResource(R.drawable.ic_heart_filled);
            ivIcono.setColorFilter(areaColor, android.graphics.PorterDuff.Mode.SRC_IN);
        }
        
        // Configurar corazones (3 llenos) - tamaño más pequeño y elegante
        llCorazones.removeAllViews();
        for (int i = 0; i < 3; i++) {
            ImageView ivCorazon = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dp(28), dp(28)
            );
            params.setMargins(dp(4), 0, dp(4), 0);
            ivCorazon.setLayoutParams(params);
            ivCorazon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            ivCorazon.setImageResource(R.drawable.ic_heart_filled);
            ivCorazon.setColorFilter(areaColor, android.graphics.PorterDuff.Mode.SRC_IN);
            llCorazones.addView(ivCorazon);
        }
        
        // Configurar botón con color del área
        btnEntendido.setBackgroundTintList(android.content.res.ColorStateList.valueOf(areaColor));
        
        // Crear y mostrar diálogo
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();
        
        // Configurar ventana del diálogo: overlay oscuro para fondo y transparente para el diálogo
        if (dialog.getWindow() != null) {
            // Fondo oscuro semi-transparente para el overlay
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_overlay_oscuro);
            // Asegurar que el diálogo tenga el tamaño correcto
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
        
        btnEntendido.setOnClickListener(v -> {
            // Marcar como visto
            getSharedPreferences("vidas_tutorial", MODE_PRIVATE)
                    .edit()
                    .putBoolean(prefsKey, true)
                    .apply();
            
            dialog.dismiss();
            Toast.makeText(this, "¡Felicitaciones! Pasaste al Nivel 2", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
        });
        
        dialog.show();
    }
    
    private void mostrarDialogoExito(String mensaje, String area) {
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
        
        // Configurar ventana del diálogo: overlay oscuro para fondo y transparente para el diálogo
        if (dialog.getWindow() != null) {
            // Fondo oscuro semi-transparente para el overlay
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_overlay_oscuro);
            // Asegurar que el diálogo tenga el tamaño correcto
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
        
        btnContinuar.setOnClickListener(v -> {
            dialog.dismiss();
            setResult(RESULT_OK);
            finish();
        });
        
        dialog.show();
    }
    
    private void mostrarDialogoExplicacionExamenFinal(String area) {
        // Verificar si ya vio el tutorial
        int userIdInt = com.example.zavira_movil.local.TokenManager.getUserId(this);
        if (userIdInt <= 0) {
            android.util.Log.e("QuizActivity", "ERROR: userId inválido en mostrarDialogoExplicacionExamenFinal");
            return;
        }
        int userId = userIdInt;
        String prefsKey = "examen_final_tutorial_visto_" + userId + "_" + area;
        boolean yaVisto = getSharedPreferences("examen_final_tutorial", MODE_PRIVATE).getBoolean(prefsKey, false);
        
        if (yaVisto) {
            // Si ya vio el tutorial, solo mostrar toast y cerrar
            Toast.makeText(this, "¡Felicitaciones! Desbloqueaste el Examen Final", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
            return;
        }
        
        // Mostrar diálogo explicativo
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_explicacion_vidas, null);
        int areaColor = obtenerColorArea(area);
        
        // Configurar color de la tarjeta del diálogo
        com.google.android.material.card.MaterialCardView cardDialog = dialogView.findViewById(R.id.cardDialog);
        if (cardDialog != null) {
            cardDialog.setCardBackgroundColor(Color.WHITE);
            cardDialog.setStrokeColor(areaColor);
            cardDialog.setStrokeWidth(3);
        }
        
        ImageView ivIcono = dialogView.findViewById(R.id.ivIconoVida);
        TextView tvTitulo = dialogView.findViewById(R.id.tvTitulo);
        TextView tvMensaje = dialogView.findViewById(R.id.tvMensaje);
        LinearLayout llCorazones = dialogView.findViewById(R.id.llCorazones);
        MaterialButton btnEntendido = dialogView.findViewById(R.id.btnEntendido);
        
        if (ivIcono != null) {
            ivIcono.setImageResource(android.R.drawable.ic_menu_info_details);
            ivIcono.setColorFilter(areaColor);
        }
        
        tvTitulo.setText("¡Examen Final Desbloqueado!");
        tvTitulo.setTextColor(Color.parseColor("#1F2937"));
        
        tvMensaje.setText("¡Felicitaciones! Has desbloqueado el Examen Final de " + area + ".\n\n" +
                         "El Examen Final consiste en 25 preguntas de esta área.\n\n" +
                         "Para aprobar, necesitas responder correctamente 20 de las 25 preguntas.\n\n" +
                         "Tendrás 3 intentos (vidas) para aprobar el examen. Si pierdes los 3 intentos, " +
                         "podrás intentarlo nuevamente después de un tiempo.\n\n" +
                         "Si apruebas, obtendrás una insignia por tu excelente desempeño.");
        
        // Configurar corazones (vidas)
        llCorazones.removeAllViews();
        for (int i = 0; i < 3; i++) {
            ImageView ivCorazon = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dp(28), dp(28)
            );
            params.setMargins(dp(4), 0, dp(4), 0);
            ivCorazon.setLayoutParams(params);
            ivCorazon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            ivCorazon.setImageResource(R.drawable.ic_heart_filled);
            ivCorazon.setColorFilter(areaColor, android.graphics.PorterDuff.Mode.SRC_IN);
            llCorazones.addView(ivCorazon);
        }
        
        btnEntendido.setBackgroundTintList(android.content.res.ColorStateList.valueOf(areaColor));
        btnEntendido.setOnClickListener(v -> {
            // Marcar tutorial como visto
            getSharedPreferences("examen_final_tutorial", MODE_PRIVATE)
                    .edit()
                    .putBoolean(prefsKey, true)
                    .apply();
            
            Toast.makeText(this, "¡Felicitaciones! Desbloqueaste el Examen Final", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
        });
        
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
    
    private int obtenerColorAreaSoft(String area) {
        if (area == null) return Color.parseColor("#BA68C8");
        String a = area.toLowerCase().trim();
        
        if (a.contains("matem")) return ContextCompat.getColor(this, R.color.area_matematicas_soft);
        if (a.contains("lengua") || a.contains("lectura") || a.contains("espa") || a.contains("critica")) 
            return ContextCompat.getColor(this, R.color.area_lenguaje_soft);
        if (a.contains("social") || a.contains("ciudad")) 
            return ContextCompat.getColor(this, R.color.area_sociales_soft);
        if (a.contains("cien") || a.contains("biolo") || a.contains("fis") || a.contains("quim")) 
            return ContextCompat.getColor(this, R.color.area_ciencias_soft);
        if (a.contains("ingl")) 
            return ContextCompat.getColor(this, R.color.area_ingles_soft);
        
        return Color.parseColor("#BA68C8");
    }
    
    private int dp(int px) {
        return (int) (px * getResources().getDisplayMetrics().density);
    }

    private static String readErr(ResponseBody eb) {
        if (eb == null) return null;
        try { return eb.string(); } catch (IOException ignored) { return null; }
    }
}
