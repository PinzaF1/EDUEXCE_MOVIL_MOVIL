// app/src/main/java/com/example/zavira_movil/retos1vs1/FragmentQuiz.java
package com.example.zavira_movil.retos1vs1;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.zavira_movil.model.Estudiante;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.local.TokenManager;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentQuiz extends Fragment {

    private AceptarRetoResponse data;
    private String idReto;
    private int idSesion;

    private int index = 0;
    private final Map<Integer, String> marcadas = new HashMap<>();
    private final Map<Integer, Long> tiemposPorPregunta = new HashMap<>(); // tiempo en ms por pregunta
    private long tiempoInicioPreguntaActual;

    private TextView tvIndex, tvPregunta, tvTimer, txtRight;
    private Button btnNext;
    private ProgressBar progressTimer;
    private OptionAdapter optionsAdapter;
    private String oponenteNombre;
    private ImageView ivFotoUsuario, ivFotoOponente;
    private ApiService api;

    private long startMillis;
    private int tiempoTotalSeg;
    private android.os.CountDownTimer timerPregunta; // Timer de 30 segundos por pregunta
    private ToneGenerator toneGenerator; // Generador de tonos para sonidos de tensión
    private Handler soundHandler; // Handler para controlar los sonidos
    private Runnable soundRunnable; // Runnable para reproducir sonidos periódicamente
    private boolean isLast10Seconds = false; // Flag para saber si estamos en los últimos 10 segundos
    private android.content.BroadcastReceiver fotoActualizadaReceiver; // Receiver para actualizar foto

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        
        // Asegurar que el topBar de HomeActivity esté oculto
        ocultarTopBar();

        // IDs EXACTOS de tu XML
        tvIndex          = v.findViewById(R.id.txtIndex);
        tvPregunta       = v.findViewById(R.id.txtPregunta);
        btnNext          = v.findViewById(R.id.btnNext);
        tvTimer          = v.findViewById(R.id.tvTimer);
        progressTimer    = v.findViewById(R.id.progressTimer);
        txtRight         = v.findViewById(R.id.txtRight);
        ivFotoUsuario    = v.findViewById(R.id.ivFotoUsuario);
        ivFotoOponente   = v.findViewById(R.id.ivFotoOponente);
        
        api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        
        // Inicializar timer y barra de progreso
        if (tvTimer != null) {
            tvTimer.setText("30");
            tvTimer.setTextColor(0xFF4CAF50); // Verde inicial
            tvTimer.setVisibility(View.VISIBLE);
        }
        if (progressTimer != null) {
            progressTimer.setMax(30);
            progressTimer.setProgress(30);
            progressTimer.getProgressDrawable().setColorFilter(0xFFFF9800, android.graphics.PorterDuff.Mode.SRC_IN); // Naranja inicial
        }

        // Removido: "Tú" y "Oponente" ya no se muestran

        RecyclerView rv = v.findViewById(R.id.optionsList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(layoutManager);
        rv.setHasFixedSize(false);
        rv.setNestedScrollingEnabled(false); // Deshabilitar scroll del RecyclerView para que el ScrollView principal maneje todo

        optionsAdapter = new OptionAdapter(key -> {
            marcadas.put(index, key);          // key = "A","B","C","D" según posición
            if (btnNext != null) btnNext.setEnabled(true);
            // Mostrar puntos cuando se selecciona una respuesta
            // Puntos ya no se muestran en el header
        });
        rv.setAdapter(optionsAdapter);

        Bundle args = getArguments();
        if (args == null) { requireActivity().onBackPressed(); return; }

        String aceptarJson = args.getString("aceptarJson");
        idSesion = args.getInt("idSesion", -1);
        idReto   = args.getString("idReto", null);
        
        // Obtener nombre del oponente de los argumentos
        oponenteNombre = args.getString("opName", "Oponente");
        
        // Cargar foto del usuario actual
        cargarFotoUsuario();
        
        // Registrar receiver para actualizar foto cuando se cambia
        registrarReceiverFoto();

        if (TextUtils.isEmpty(aceptarJson) || idSesion <= 0 || TextUtils.isEmpty(idReto)) {
            requireActivity().onBackPressed();
            return;
        }

        data = new Gson().fromJson(aceptarJson, AceptarRetoResponse.class);
        
        // Cargar foto del oponente después de parsear el JSON
        cargarFotoOponente();
        
        // Actualizar nombre del oponente si viene en la respuesta y formatearlo
        if (data != null && data.oponente != null && data.oponente.nombre != null) {
            oponenteNombre = data.oponente.nombre;
        }
        
        // Formatear nombre: solo primer nombre y primer apellido
        String nombreFormateado = formatearNombreOponente(oponenteNombre);
        if (txtRight != null) {
            txtRight.setText(nombreFormateado);
        }
        
        render();

        startMillis = System.currentTimeMillis();
        tiempoInicioPreguntaActual = System.currentTimeMillis(); // iniciar timer de primera pregunta
        inicializarSonido(); // Inicializar generador de sonidos
        iniciarTimerPregunta(); // Iniciar timer de 30 segundos

        if (btnNext != null) {
            btnNext.setOnClickListener(v12 -> {
                if (!marcadas.containsKey(index)) {
                    Toast.makeText(getContext(), "Selecciona una opción", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Cancelar timer de la pregunta actual
                cancelarTimerPregunta();
                
                // Guardar tiempo de la pregunta actual
                long tiempoEmpleado = System.currentTimeMillis() - tiempoInicioPreguntaActual;
                tiemposPorPregunta.put(index, tiempoEmpleado);
                
                if (index < data.preguntas.size() - 1) {
                    index++;
                    tiempoInicioPreguntaActual = System.currentTimeMillis(); // reiniciar timer
                    detenerSonido(); // Detener sonidos antes de avanzar
                    iniciarTimerPregunta(); // Reiniciar timer de 30 segundos
                    render();
                } else {
                    enviarRonda();
                }
            });
        }
    }

    private void render() {
        if (data == null || data.preguntas == null || data.preguntas.isEmpty()) return;

        AceptarRetoResponse.Pregunta p = data.preguntas.get(index);

        if (tvIndex != null) {
            tvIndex.setText("Pregunta " + (index + 1) + "/" + data.preguntas.size());
        }
        if (tvPregunta != null) {
            tvPregunta.setText(p.enunciado != null ? p.enunciado : "Pregunta sin texto");
        }

        // --- Construir SIEMPRE List<String> para tu OptionAdapter ---
        List<String> opTexts = buildOptionTexts(p);

        String sel = marcadas.get(index);            // "A", "B", ...
        optionsAdapter.submit(opTexts, sel);         // <-- ahora coincide con tu adapter

        if (btnNext != null) {
            btnNext.setText(index == data.preguntas.size() - 1 ? "Finalizar" : "Siguiente");
            btnNext.setEnabled(sel != null);
            // Asegurar que el botón use el color correcto (naranja-rojo)
            btnNext.setBackgroundResource(R.drawable.bg_button_siguiente_selector);
            btnNext.setBackgroundTintList(null);
        }
    }
    
    private void cargarFotoUsuario() {
        if (ivFotoUsuario == null) return;
        
        // Primero intentar cargar desde archivo local (más rápido y actualizado)
        Integer userId = TokenManager.getUserId(requireContext());
        if (userId != null) {
            String prefsKey = "foto_path_" + userId;
            android.content.SharedPreferences sp = requireContext().getSharedPreferences("perfil_prefs", android.content.Context.MODE_PRIVATE);
            String localPath = sp.getString(prefsKey, null);
            
            if (localPath != null) {
                java.io.File localFile = new java.io.File(localPath);
                if (localFile.exists()) {
                    Glide.with(requireContext())
                            .load(localFile)
                            .placeholder(R.drawable.usuario)
                            .error(R.drawable.usuario)
                            .circleCrop()
                            .into(ivFotoUsuario);
                    return;
                }
            }
        }
        
        // Si no hay archivo local, obtener desde el perfil
        api.getPerfilEstudiante().enqueue(new retrofit2.Callback<Estudiante>() {
            @Override
            public void onResponse(retrofit2.Call<Estudiante> call, retrofit2.Response<Estudiante> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Estudiante perfil = response.body();
                    String fotoUrl = perfil.getFotoUrl();
                    cargarFotoEnImageView(ivFotoUsuario, fotoUrl);
                } else {
                    // Si falla, usar icono por defecto
                    ivFotoUsuario.setImageResource(R.drawable.usuario);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Estudiante> call, Throwable t) {
                // Si falla, usar icono por defecto
                ivFotoUsuario.setImageResource(R.drawable.usuario);
            }
        });
    }
    
    private void cargarFotoOponente() {
        if (ivFotoOponente == null || data == null) return;
        
        // Obtener foto del oponente desde AceptarRetoResponse
        if (data.oponente != null && data.oponente.foto_url != null && !data.oponente.foto_url.trim().isEmpty()) {
            cargarFotoEnImageView(ivFotoOponente, data.oponente.foto_url);
        } else {
            // Si no tiene foto, usar icono por defecto
            ivFotoOponente.setImageResource(R.drawable.usuario);
        }
    }
    
    private void cargarFotoEnImageView(ImageView imageView, String fotoUrl) {
        if (imageView == null) return;
        
        if (fotoUrl != null && !fotoUrl.trim().isEmpty() && !"null".equalsIgnoreCase(fotoUrl.trim())) {
            Glide.with(requireContext())
                    .load(fotoUrl.trim())
                    .placeholder(R.drawable.usuario)
                    .error(R.drawable.usuario)
                    .circleCrop()
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.usuario);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Asegurar que el topBar esté oculto
        ocultarTopBar();
        // Recargar foto del usuario por si se actualizó
        cargarFotoUsuario();
    }
    
    private void registrarReceiverFoto() {
        if (fotoActualizadaReceiver == null) {
            fotoActualizadaReceiver = new android.content.BroadcastReceiver() {
                @Override
                public void onReceive(android.content.Context context, android.content.Intent intent) {
                    if (intent != null && "com.example.zavira_movil.FOTO_ACTUALIZADA".equals(intent.getAction())) {
                        // Recargar foto del usuario cuando se actualiza
                        if (getActivity() != null && ivFotoUsuario != null) {
                            getActivity().runOnUiThread(() -> {
                                // Limpiar cache de Glide para forzar recarga
                                Glide.with(requireContext()).clear(ivFotoUsuario);
                                // Recargar foto
                                cargarFotoUsuario();
                            });
                        }
                    }
                }
            };
            try {
                android.content.IntentFilter filter = new android.content.IntentFilter("com.example.zavira_movil.FOTO_ACTUALIZADA");
                if (getActivity() != null) {
                    getActivity().registerReceiver(fotoActualizadaReceiver, filter);
                }
            } catch (Exception e) {
                // Ignorar errores de registro
            }
        }
    }
    
    private void ocultarTopBar() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                View topBar = getActivity().findViewById(R.id.topBar);
                if (topBar != null) {
                    topBar.setVisibility(View.GONE);
                }
            });
        }
    }
    
    private void restaurarTopBar() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                View topBar = getActivity().findViewById(R.id.topBar);
                if (topBar != null) {
                    topBar.setVisibility(View.VISIBLE);
                }
            });
        }
    }
    
    /**
     * Formatea el nombre completo para mostrar solo el primer nombre y primer apellido
     * Ejemplo: "Karen ana Castro Moreno" -> "Karen Castro"
     */
    private String formatearNombreOponente(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return "Oponente";
        }
        
        String nombre = nombreCompleto.trim();
        String[] partes = nombre.split("\\s+");
        
        if (partes.length == 0) {
            return "Oponente";
        } else if (partes.length == 1) {
            // Solo tiene un nombre, devolverlo capitalizado
            return capitalizar(partes[0]);
        } else {
            // Tiene al menos dos palabras: primer nombre + primer apellido
            String primerNombre = capitalizar(partes[0]);
            String primerApellido = capitalizar(partes[1]);
            return primerNombre + " " + primerApellido;
        }
    }
    
    /**
     * Capitaliza la primera letra de una palabra
     */
    private String capitalizar(String palabra) {
        if (palabra == null || palabra.isEmpty()) {
            return palabra;
        }
        return palabra.substring(0, 1).toUpperCase() + 
               (palabra.length() > 1 ? palabra.substring(1).toLowerCase() : "");
    }

    /** Convierte p.opciones (strings u objetos) en List<String> de textos visibles */
    private List<String> buildOptionTexts(AceptarRetoResponse.Pregunta p) {
        List<String> list = new ArrayList<>();
        if (p == null || p.opciones == null) return list;

        // p.opciones puede ser List<AceptarRetoResponse.Opcion> (con .text/.key) o ya ser strings si usaste el TypeAdapter
        List<?> raw = (List<?>) (Object) p.opciones; // cast ancho por si la deserialización varía
        for (Object any : raw) {
            if (any instanceof AceptarRetoResponse.Opcion) {
                AceptarRetoResponse.Opcion o = (AceptarRetoResponse.Opcion) any;
                String text = (o.text != null) ? o.text : (o.key != null ? o.key : "");
                list.add(text);
            } else if (any instanceof String) {
                list.add((String) any);
            } else {
                list.add(String.valueOf(any));
            }
        }
        return list;
    }

    private String flagKey() {
        Integer my = null; try { my = TokenManager.getUserId(requireContext()); } catch (Exception ignored) {}
        return "ronda_" + idReto + "_" + (my==null?"0":String.valueOf(my));
    }

    private void marcarEntregada() {
        if (!isAdded() || TextUtils.isEmpty(idReto)) return;
        requireContext().getSharedPreferences("retos1v1", android.content.Context.MODE_PRIVATE)
                .edit().putBoolean(flagKey(), true).apply();
    }

    private void enviarRonda() {
        cancelarTimerPregunta(); // Cancelar timer antes de enviar
        tiempoTotalSeg = (int) ((System.currentTimeMillis() - startMillis) / 1000L);

        // El backend espera la opción marcada por clave (A/B/C/D...), que guarda tu OptionAdapter
        // Si no hay respuesta (vacía), se envía como "" (valor 0)
        List<RondaRequest.Item> items = new ArrayList<>();
        for (int i = 0; i < data.preguntas.size(); i++) {
            String key = marcadas.get(i);
            // Si key es null o vacío, significa que no respondió (valor 0)
            if (key == null) key = "";
            
            // Obtener tiempo en segundos (con decimales)
            Long tiempoMs = tiemposPorPregunta.get(i);
            Double tiempoSeg = (tiempoMs != null) ? (tiempoMs / 1000.0) : 30.0; // Si no hay tiempo, usar 30 segundos (timeout)
            
            items.add(new RondaRequest.Item(i + 1, key, tiempoSeg));
        }

        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        RondaRequest payload = new RondaRequest(idSesion, items);
        payload.tiempoTotalSeg = tiempoTotalSeg;

        api.responderRonda(payload).enqueue(new Callback<RondaResponse>() {
            @Override public void onResponse(Call<RondaResponse> call, Response<RondaResponse> resp) {
                marcarEntregada();
                consultarEstado();
            }
            @Override public void onFailure(Call<RondaResponse> call, Throwable t) {
                marcarEntregada();
                consultarEstado();
            }
        });
    }

    private void iniciarTimerPregunta() {
        cancelarTimerPregunta(); // Cancelar timer anterior si existe
        detenerSonido(); // Detener sonidos anteriores
        isLast10Seconds = false; // Resetear flag de últimos 10 segundos
        
        // Reiniciar timer y barra de progreso
        if (tvTimer != null) {
            tvTimer.setText("30");
            tvTimer.setTextColor(0xFF4CAF50); // Verde inicial (más de 20)
        }
        if (progressTimer != null) {
            progressTimer.setProgress(30);
            progressTimer.getProgressDrawable().setColorFilter(0xFFFF9800, android.graphics.PorterDuff.Mode.SRC_IN); // Naranja inicial
        }
        
        // Timer de 30 segundos por pregunta
        timerPregunta = new android.os.CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!isAdded()) {
                    cancel();
                    return;
                }
                
                long segundos = millisUntilFinished / 1000;
                
                // Actualizar TextView del timer - mostrar solo segundos
                if (tvTimer != null) {
                    tvTimer.setText(String.valueOf(segundos));
                    tvTimer.setVisibility(View.VISIBLE);
                    
                    // Cambiar color: más de 20 segundos = verde, 10 segundos o menos = rojo
                    if (segundos > 20) {
                        tvTimer.setTextColor(0xFF4CAF50); // Verde
                    } else if (segundos <= 10) {
                        tvTimer.setTextColor(0xFFFF0000); // Rojo
                    } else {
                        // Entre 11 y 20: mantener verde
                        tvTimer.setTextColor(0xFF4CAF50); // Verde
                    }
                    
                    // Ajustar tamaño del texto según segundos restantes
                    if (segundos <= 10) {
                        tvTimer.setTextSize(24f); // Más grande cuando está en rojo
                    } else {
                        tvTimer.setTextSize(20f); // Más pequeño cuando está en verde
                    }
                }
                
                // Actualizar barra de progreso
                if (progressTimer != null) {
                    int progress = (int) segundos;
                    progressTimer.setProgress(progress);
                    
                    // Cambiar color de la barra: más de 20 segundos = naranja, 10 segundos o menos = rojo
                    if (segundos > 20) {
                        progressTimer.getProgressDrawable().setColorFilter(0xFFFF9800, android.graphics.PorterDuff.Mode.SRC_IN); // Naranja
                    } else if (segundos <= 10) {
                        progressTimer.getProgressDrawable().setColorFilter(0xFFFF0000, android.graphics.PorterDuff.Mode.SRC_IN); // Rojo
                    } else {
                        // Entre 11 y 20: mantener naranja
                        progressTimer.getProgressDrawable().setColorFilter(0xFFFF9800, android.graphics.PorterDuff.Mode.SRC_IN); // Naranja
                    }
                }
                
                // Detectar cuando quedan 10 segundos o menos para aumentar intensidad del sonido
                if (segundos <= 10 && !isLast10Seconds) {
                    isLast10Seconds = true;
                    detenerSonido(); // Detener sonido anterior
                    iniciarSonidoTension(); // Iniciar sonido más intenso
                }
            }
            
            @Override
            public void onFinish() {
                if (!isAdded()) return;
                
                detenerSonido(); // Detener sonidos al finalizar
                
                // Si no respondió en 30 segundos, marcar como no respondida (valor 0) y avanzar
                // No hay respuesta seleccionada, marcar como vacía (0)
                marcadas.put(index, ""); // Respuesta vacía = 0 (siempre marcar, incluso si ya existe)
                tiemposPorPregunta.put(index, 30000L); // 30 segundos completos
                
                // Avanzar automáticamente a la siguiente pregunta o finalizar
                if (index < data.preguntas.size() - 1) {
                    index++;
                    tiempoInicioPreguntaActual = System.currentTimeMillis();
                    iniciarTimerPregunta(); // Reiniciar timer para la siguiente pregunta
                    render();
                } else {
                    enviarRonda();
                }
            }
        };
        timerPregunta.start();
        
        // Iniciar sonido de tensión desde el principio
        iniciarSonidoTension();
    }
    
    /**
     * Inicializa el generador de tonos para los sonidos interactivos
     */
    private void inicializarSonido() {
        try {
            // Volumen más alto (80) para sonido más audible e interactivo
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 80);
            soundHandler = new Handler();
        } catch (Exception e) {
            // Si falla la inicialización, continuar sin sonidos
            toneGenerator = null;
            soundHandler = null;
        }
    }
    
    /**
     * Inicia el sonido interactivo durante el countdown
     * Sonido más emocionante e interactivo para mantener a los estudiantes atentos
     * Si estamos en los últimos 10 segundos, el sonido será más intenso y frecuente
     */
    private void iniciarSonidoTension() {
        if (toneGenerator == null || soundHandler == null) {
            // Si no se inicializó, intentar inicializar de nuevo
            inicializarSonido();
            if (toneGenerator == null || soundHandler == null) return;
        }
        
        detenerSonido(); // Asegurarse de que no hay otro sonido corriendo
        
        soundRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isAdded() || timerPregunta == null) {
                    detenerSonido();
                    return;
                }
                
                try {
                    // Sonido interactivo y emocionante: usar tonos más dinámicos
                    // En los últimos 10 segundos, usar tono de alerta más intenso
                    int tipoTono;
                    int duracion;
                    
                    if (isLast10Seconds) {
                        // Últimos 10 segundos: sonido más intenso y urgente
                        tipoTono = ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD; // Tono de alerta más intenso
                        duracion = 150; // Duración más larga para más impacto
                    } else {
                        // Primeros 20 segundos: sonido de tic-tac más audible
                        tipoTono = ToneGenerator.TONE_DTMF_0; // Tono DTMF audible
                        duracion = 120; // Duración media
                    }
                    
                    toneGenerator.startTone(tipoTono, duracion);
                } catch (Exception e) {
                    // Si falla, intentar con un tono más simple
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
                // Intervalo más corto para sonido más interactivo
                long intervalo = isLast10Seconds ? 400L : 800L; // 400ms si quedan ≤10s, 800ms si quedan >10s
                
                // Programar siguiente sonido
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
    
    private void cancelarTimerPregunta() {
        if (timerPregunta != null) {
            timerPregunta.cancel();
            timerPregunta = null;
        }
        detenerSonido(); // Detener sonidos al cancelar el timer
    }

    @Override
    public void onDestroyView() {
        cancelarTimerPregunta();
        detenerSonido();
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
        // Desregistrar receiver cuando el fragment se destruye
        if (fotoActualizadaReceiver != null && getActivity() != null) {
            try {
                getActivity().unregisterReceiver(fotoActualizadaReceiver);
            } catch (Exception e) {
                // Ignorar si ya está desregistrado
            }
        }
        // Restaurar topBar al salir
        restaurarTopBar();
        super.onDestroyView();
    }
    

    
    private void consultarEstado() {
        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        api.estadoReto(idReto).enqueue(new Callback<EstadoRetoResponse>() {
            @Override public void onResponse(Call<EstadoRetoResponse> call, Response<EstadoRetoResponse> resp) {
                if (!isAdded()) return;
                if (resp.isSuccessful() && resp.body() != null) {
                    EstadoRetoResponse e = resp.body();

                    Bundle b = new Bundle();
                    b.putString("estadoJson", new Gson().toJson(e));
                    b.putInt("totalPreguntas",
                            (data != null && data.preguntas != null) ? data.preguntas.size() : 25);
                    b.putString("idReto", idReto); // Agregar idReto para polling
                    b.putInt("idSesion", idSesion);
                    b.putInt("tiempoTotalSeg", tiempoTotalSeg);

                    FragmentResultadoReto f = new FragmentResultadoReto();
                    f.setArguments(b);

                    FragmentManager fm = requireActivity().getSupportFragmentManager();
                    int containerId = R.id.fragmentContainer;
                    View root = requireActivity().findViewById(containerId);
                    if (root == null) containerId = android.R.id.content;

                    fm.beginTransaction()
                            .replace(containerId, f)
                            .addToBackStack("resultadoReto")
                            .commit();
                } else {
                    requireActivity().onBackPressed();
                }
            }
            @Override public void onFailure(Call<EstadoRetoResponse> call, Throwable t) {
                if (!isAdded()) return;
                requireActivity().onBackPressed();
            }
        });
    }
}
