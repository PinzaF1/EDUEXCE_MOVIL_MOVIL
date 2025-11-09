package com.example.zavira_movil.retos1vs1;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.zavira_movil.R;
import com.example.zavira_movil.local.TokenManager;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.gson.Gson;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.ResponseBody;

public class FragmentLoadingSalaReto extends Fragment {

    public FragmentLoadingSalaReto() { super(R.layout.fragment_loading_sala_reto); }

    public static FragmentLoadingSalaReto newInstance(String idReto, String area, String opName, boolean esCreador) {
        Bundle b = new Bundle();
        b.putString("idReto", idReto);
        b.putString("area", area);
        b.putString("opName", opName);
        b.putBoolean("esCreador", esCreador);
        FragmentLoadingSalaReto f = new FragmentLoadingSalaReto();
        f.setArguments(b);
        return f;
    }

    private String idReto, area, opName;
    private boolean esCreador;

    private TextView tvArea, tvOponente, tvMensajeEspera;
    private Button btnEntrar, btnAbandonar;

    private Handler handler;
    private boolean launching = false;
    private static final long POLL_MS = 100L; // Polling ultra rápido: 100ms para respuesta inmediata
    private static final long TIMEOUT_MS = 4 * 60 * 1000L; // 4 minutos
    private long tiempoInicioEspera;
    
    // Sonido delicado para espera de reto
    private ToneGenerator toneGeneratorEspera;
    private Handler soundHandlerEspera;
    private Runnable soundRunnableEspera;

    private String spName() { return "retos1v1"; }
    private Integer myId() { try { return TokenManager.getUserId(requireContext()); } catch (Exception e) { return null; } }
    private String keyAceptadoPorCreador() { return "aceptado_creador_" + (idReto==null?"":idReto); }
    private boolean creadorYaLlamoAceptar() {
        if (!isAdded()) return false;
        return requireContext().getSharedPreferences(spName(), Context.MODE_PRIVATE)
                .getBoolean(keyAceptadoPorCreador(), false);
    }
    private void marcarCreadorLlamoAceptar() {
        if (!isAdded()) return;
        requireContext().getSharedPreferences(spName(), Context.MODE_PRIVATE)
                .edit().putBoolean(keyAceptadoPorCreador(), true).apply();
    }

    private String keyEntregada() {
        Integer my = myId();
        return "ronda_" + (idReto==null?"":idReto) + "_" + (my==null?"0":String.valueOf(my));
    }
    private void clearEntregadaFlag() {
        if (!isAdded() || TextUtils.isEmpty(idReto)) return;
        requireContext().getSharedPreferences(spName(), Context.MODE_PRIVATE)
                .edit().putBoolean(keyEntregada(), false).apply();
    }
    private boolean yaEntregue() {
        if (!isAdded() || TextUtils.isEmpty(idReto)) return false;
        return requireContext().getSharedPreferences(spName(), Context.MODE_PRIVATE)
                .getBoolean(keyEntregada(), false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        
        // Asegurar que el topBar de HomeActivity esté oculto
        ocultarTopBar();

        tvArea          = v.findViewById(R.id.tvArea);
        tvOponente      = v.findViewById(R.id.tvOponente);
        btnEntrar       = v.findViewById(R.id.btnEntrar);
        btnAbandonar    = v.findViewById(R.id.btnAbandonar);
        tvMensajeEspera = v.findViewById(R.id.tvMensajeEspera);

        Bundle args = getArguments();
        if (args != null) {
            idReto    = args.getString("idReto");
            area      = args.getString("area", "");
            opName    = args.getString("opName", "");
            esCreador = args.getBoolean("esCreador", false);
        }

        tvArea.setText(area);
        tvOponente.setText(!TextUtils.isEmpty(opName) ? opName : "Oponente");

        clearEntregadaFlag();
        handler = new Handler();
        tiempoInicioEspera = System.currentTimeMillis();
        
        // Inicializar sonido delicado para espera
        inicializarSonidoEspera();
        iniciarSonidoEspera();

        if (esCreador) {
            // RETADOR: Esperar a que el oponente acepte el reto
            // NO mostrar botón "Entrar", solo mensaje de espera y botón abandonar
            btnEntrar.setVisibility(View.GONE);
            tvMensajeEspera.setVisibility(View.VISIBLE);
            tvMensajeEspera.setText("Debes esperar que tu oponente te acepte");
            btnAbandonar.setVisibility(View.VISIBLE);
            // Asegurar que el botón tenga fondo blanco sin tint
            btnAbandonar.setBackgroundTintList(null);
            btnAbandonar.setBackgroundResource(R.drawable.btn_lobby_white);
            btnAbandonar.setTextColor(0xFFFF7A39); // Naranja
            btnAbandonar.setOnClickListener(v1 -> abandonarReto());
            // IMPORTANTE: NO llamar a aceptarReto aquí, solo hacer polling del estado
            // Iniciar polling inmediatamente para detectar aceptación más rápido
            pollEstado(); // Llamar directamente sin delay para máxima velocidad
            // Iniciar timeout de 4 minutos
            handler.postDelayed(this::verificarTimeout, TIMEOUT_MS);
        } else {
            // RETADO: El oponente ya aceptó en FragmentRetosRecibidos, y las sesiones ya fueron creadas para ambos
            // El estado debería ser 'en_curso' inmediatamente, así que verificar inmediatamente
            btnEntrar.setVisibility(View.GONE);
            tvMensajeEspera.setVisibility(View.VISIBLE);
            tvMensajeEspera.setText("Cargando...");
            btnAbandonar.setVisibility(View.GONE);
            // Verificar el estado INMEDIATAMENTE sin delay para comenzar el quiz lo más rápido posible
            pollEstado(); // Llamar directamente sin delay
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Asegurar que el topBar esté oculto
        ocultarTopBar();
    }
    
    @Override 
    public void onDestroyView() {
        detenerSonidoEspera();
        if (handler != null) { handler.removeCallbacksAndMessages(null); handler = null; }
        // Restaurar topBar al salir
        restaurarTopBar();
        super.onDestroyView();
    }

    private void reintentar(Runnable r) { 
        // Polling más rápido para mejor interacción
        if (handler != null) handler.postDelayed(r, POLL_MS); // Usar POLL_MS directamente sin mínimo
    }

    private void pollEstado() {
        if (!isAdded() || launching || TextUtils.isEmpty(idReto)) return;

        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        api.estadoReto(idReto).enqueue(new Callback<EstadoRetoResponse>() {
            @Override public void onResponse(Call<EstadoRetoResponse> call, Response<EstadoRetoResponse> resp) {
                if (!isAdded() || launching) return;

                if (resp.isSuccessful() && resp.body() != null) {
                    EstadoRetoResponse e = resp.body();
                    String st = e.estado != null ? e.estado.toLowerCase() : "";

                    // Verificar si el reto fue abandonado o cancelado
                    if ("cancelado".equals(st) || "abandonado".equals(st)) {
                        detenerSonidoEspera(); // Detener sonido al cancelar/abandonar
                        Toast.makeText(requireContext(), "El reto fue cancelado o abandonado.", Toast.LENGTH_LONG).show();
                        volverACrearReto();
                        return;
                    }
                    
                    // IMPORTANTE: Si el estado es "pendiente", SOLO esperar (NO hacer nada más)
                    if ("pendiente".equals(st)) {
                        // El reto está pendiente - el oponente aún no ha aceptado (si es creador)
                        // o el creador aún no ha aceptado (si es oponente)
                        // NO hacer nada, solo continuar esperando con polling rápido
                        reintentar(FragmentLoadingSalaReto.this::pollEstado);
                        return;
                    }
                    
                    // SOLO cuando el estado es "en_curso" (oponente aceptó y se crearon sesiones para ambos), comenzar el quiz
                    if ("en_curso".equals(st)) {
                        // Detener sonido de espera cuando se acepta el reto
                        detenerSonidoEspera();
                        // Cuando el estado es 'en_curso', ambos ya tienen sesión (creadas automáticamente cuando el oponente aceptó)
                        // Detener polling inmediatamente y obtener sesiones
                        if (handler != null) {
                            handler.removeCallbacksAndMessages(null);
                        }
                        // Obtener sesiones y comenzar el quiz INMEDIATAMENTE (sin delay)
                        // Ejecutar directamente sin esperar
                        if (!isAdded() || launching) return;
                        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
                        api.aceptarRetoConBody(idReto, new HashMap<>()).enqueue(new Callback<AceptarRetoResponse>() {
                            @Override public void onResponse(Call<AceptarRetoResponse> call, Response<AceptarRetoResponse> resp) {
                                if (!isAdded() || launching) return;
                                if (resp.isSuccessful() && resp.body() != null) {
                                    AceptarRetoResponse body = resp.body();
                                    if (tieneSesion(body) && body.preguntas != null && !body.preguntas.isEmpty()) {
                                        lanzarQuiz(body);
                                    } else {
                                        reintentar(FragmentLoadingSalaReto.this::pollEstado);
                                    }
                                } else {
                                    reintentar(FragmentLoadingSalaReto.this::pollEstado);
                                }
                            }
                            @Override public void onFailure(Call<AceptarRetoResponse> call, Throwable t) {
                                if (!isAdded() || launching) return;
                                reintentar(FragmentLoadingSalaReto.this::pollEstado);
                            }
                        });
                        return;
                    }

                    if ("finalizado".equals(st)) {
                        if (yaEntregue()) { irAResultado(e); return; }
                        if (esCreador) {
                            if (!creadorYaLlamoAceptar()) fetchSesionesSoloConAceptarUnaVez(true);
                        } else {
                            // RETADO: Si el reto está finalizado, obtener sesión y ver resultado
                            fetchSesionesSoloConAceptarUnaVez(false);
                        }
                        return;
                    }
                }
                reintentar(FragmentLoadingSalaReto.this::pollEstado);
            }
            @Override public void onFailure(Call<EstadoRetoResponse> call, Throwable t) {
                if (!isAdded() || launching) return;
                reintentar(FragmentLoadingSalaReto.this::pollEstado);
            }
        });
    }

    // Método eliminado: aceptarYOEntrar() ya no se usa
    // El oponente acepta en FragmentRetosRecibidos, no necesita aceptar otra vez aquí
    
    private void abandonarReto() {
        if (!isAdded() || launching || TextUtils.isEmpty(idReto)) return;
        
        // Detener sonido de espera al abandonar
        detenerSonidoEspera();
        
        // Confirmar abandono
        new android.app.AlertDialog.Builder(requireContext())
            .setTitle("Abandonar Reto")
            .setMessage("¿Estás seguro de que quieres abandonar este reto?")
            .setPositiveButton("Sí, abandonar", (dialog, which) -> {
                // Llamar al backend para abandonar el reto
                ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
                api.abandonarReto(idReto).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> resp) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Reto abandonado", Toast.LENGTH_SHORT).show();
                        volverACrearReto();
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Error al abandonar: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    private void verificarTimeout() {
        if (!isAdded() || launching || !esCreador) return;
        
        // Verificar si han pasado 4 minutos
        long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicioEspera;
        if (tiempoTranscurrido >= TIMEOUT_MS) {
            // Timeout alcanzado: notificar y redirigir a crear nuevo reto
            Toast.makeText(requireContext(), "El oponente no aceptó el reto en 4 minutos. El reto se ha cancelado.", Toast.LENGTH_LONG).show();
            volverACrearReto();
        }
    }
    
    private void volverACrearReto() {
        if (!isAdded()) return;
        // Volver a la pantalla de crear reto
        requireActivity().onBackPressed();
        
        // Refrescar la lista de oponentes después de volver (para mostrar disponibilidad actualizada)
        // CRÍTICO: Ejecutar inmediatamente sin delay para mejor UX
        if (!isAdded()) return;
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        // Buscar FragmentReto en el back stack o en el fragmentContainer
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
        if (fragment == null) {
            // Buscar en el back stack
            int count = fm.getBackStackEntryCount();
            if (count > 0) {
                String name = fm.getBackStackEntryAt(count - 1).getName();
                if ("salaCreador".equals(name) || "salaRetado".equals(name)) {
                    // El fragment anterior debería ser FragmentReto
                }
            }
        }
        if (fragment instanceof FragmentReto) {
            ((FragmentReto) fragment).cargarOponentes();
        }
    }

    private void fetchSesionesSoloConAceptarUnaVez(boolean conJitter) {
        if (!isAdded() || launching) return;

        // CRÍTICO: Eliminar delays innecesarios - ejecutar inmediatamente para mejor UX
        Runnable work = () -> {
            if (!isAdded() || launching) return;

            ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
            
            // Cuando el estado es 'en_curso', ambos ya tienen sesión (creadas cuando el oponente aceptó)
            // Solo necesitamos obtener las sesiones llamando a aceptarReto (que devuelve las sesiones y preguntas)
            api.aceptarRetoConBody(idReto, new HashMap<>()).enqueue(new Callback<AceptarRetoResponse>() {
                @Override public void onResponse(Call<AceptarRetoResponse> call, Response<AceptarRetoResponse> resp) {
                    if (!isAdded() || launching) return;
                    
                    if (resp.isSuccessful() && resp.body() != null) {
                        AceptarRetoResponse body = resp.body();
                        // Verificar que tenga sesiones y preguntas
                        if (tieneSesion(body) && body.preguntas != null && !body.preguntas.isEmpty()) {
                            // Lanzar el quiz inmediatamente
                            lanzarQuiz(body);
                        } else {
                            // Si no tiene sesión o preguntas, continuar esperando
                            reintentar(FragmentLoadingSalaReto.this::pollEstado);
                        }
                    } else {
                        // Si el backend rechaza, continuar esperando
                        reintentar(FragmentLoadingSalaReto.this::pollEstado);
                    }
                }
                @Override public void onFailure(Call<AceptarRetoResponse> call, Throwable t) {
                    if (!isAdded() || launching) return;
                    // En caso de error de red, continuar esperando
                    reintentar(FragmentLoadingSalaReto.this::pollEstado);
                }
            });
        };

        // CRÍTICO: Ejecutar inmediatamente sin delays para mejor UX
        // Eliminar jitter/delays que hacen que el usuario espere innecesariamente
        work.run();
    }

    private boolean tieneSesion(AceptarRetoResponse a) {
        if (a == null || a.sesiones == null || a.sesiones.isEmpty()) return false;
        Integer my = myId();
        int id = a.findSesionIdForUser(my);
        return id > 0;
    }

    private void lanzarQuiz(AceptarRetoResponse aceptar) {
        if (!isAdded() || aceptar == null) return;
        
        // Detener sonido de espera al lanzar el quiz
        detenerSonidoEspera();

        // VALIDACIÓN CRÍTICA: Solo lanzar el quiz si el estado es 'en_curso' y tiene preguntas
        if (aceptar.reto == null || !"en_curso".equalsIgnoreCase(aceptar.reto.estado)) {
            // El estado no es 'en_curso', no comenzar el quiz
            reintentar(FragmentLoadingSalaReto.this::pollEstado);
            return;
        }
        
        // Verificar que tenga preguntas
        if (aceptar.preguntas == null || aceptar.preguntas.isEmpty()) {
            // No hay preguntas, continuar esperando
            reintentar(FragmentLoadingSalaReto.this::pollEstado);
            return;
        }

        int idSesion = aceptar.findSesionIdForUser(myId());
        if (idSesion <= 0) { reintentar(FragmentLoadingSalaReto.this::pollEstado); return; }

        launching = true;

        Bundle args = new Bundle();
        args.putString("aceptarJson", new Gson().toJson(aceptar));
        args.putInt("idSesion", idSesion);
        args.putString("idReto", aceptar.reto != null ? String.valueOf(aceptar.reto.id_reto) : idReto);
        // Obtener nombre del oponente desde aceptar.oponente si está disponible
        String nombreOponente = opName;
        if (aceptar.oponente != null && aceptar.oponente.nombre != null) {
            nombreOponente = aceptar.oponente.nombre;
        }
        args.putString("opName", nombreOponente); // Pasar nombre del oponente

        FragmentQuiz f = new FragmentQuiz();
        f.setArguments(args);

        FragmentManager fm = requireActivity().getSupportFragmentManager();
        fm.beginTransaction()
                .replace(resolveContainerId(), f)
                .addToBackStack("quiz")
                .commit();
    }

    private void irAResultado(EstadoRetoResponse e) {
        if (!isAdded()) return;

        Bundle b = new Bundle();
        b.putString("estadoJson", new Gson().toJson(e));
        b.putInt("totalPreguntas", 25);
        b.putString("idReto", idReto); // Agregar idReto para polling
        // Si conoces la sesión aquí, puedes pasarla:
        // b.putInt("idSesion", <miIdSesion>);

        FragmentResultadoReto f = new FragmentResultadoReto();
        f.setArguments(b);

        FragmentManager fm = requireActivity().getSupportFragmentManager();
        fm.beginTransaction()
                .replace(resolveContainerId(), f)
                .addToBackStack("resultadoReto")
                .commit();
    }

    private int resolveContainerId() {
        View parent = getParentFragment() != null ? getParentFragment().getView() : null;
        View overlay = parent != null ? parent.findViewById(R.id.container) : null;
        if (overlay != null) { overlay.setVisibility(View.VISIBLE); return R.id.container; }
        View root = requireActivity().findViewById(R.id.fragmentContainer);
        return (root != null) ? R.id.fragmentContainer : android.R.id.content;
    }
    
    /**
     * Inicializa el generador de tonos para el sonido delicado de espera
     */
    private void inicializarSonidoEspera() {
        try {
            // Volumen medio (60) para sonido audible pero delicado
            toneGeneratorEspera = new ToneGenerator(AudioManager.STREAM_MUSIC, 60);
            soundHandlerEspera = new Handler();
        } catch (Exception e) {
            // Si falla la inicialización, continuar sin sonidos
            toneGeneratorEspera = null;
            soundHandlerEspera = null;
        }
    }
    
    /**
     * Inicia el sonido delicado de espera (sonido para retar)
     * Se reproduce periódicamente mientras se espera al oponente
     */
    private void iniciarSonidoEspera() {
        if (toneGeneratorEspera == null || soundHandlerEspera == null) {
            // Si no se inicializó, intentar inicializar de nuevo
            inicializarSonidoEspera();
            if (toneGeneratorEspera == null || soundHandlerEspera == null) return;
        }
        
        detenerSonidoEspera(); // Asegurarse de que no hay otro sonido corriendo
        
        soundRunnableEspera = new Runnable() {
            @Override
            public void run() {
                if (!isAdded() || launching) {
                    detenerSonidoEspera();
                    return;
                }
                
                try {
                    // Sonido delicado y suave para retar: usar tono DTMF suave
                    // TONE_DTMF_2 es un tono delicado y agradable
                    toneGeneratorEspera.startTone(ToneGenerator.TONE_DTMF_2, 200); // Duración media
                } catch (Exception e) {
                    // Si falla, intentar con un tono más simple
                    try {
                        toneGeneratorEspera.startTone(ToneGenerator.TONE_DTMF_0, 150);
                    } catch (Exception e2) {
                        // Ignorar errores de sonido
                    }
                }
                
                // Reproducir cada 2.5 segundos (sonido delicado y no intrusivo)
                if (soundHandlerEspera != null) {
                    soundHandlerEspera.postDelayed(this, 2500L);
                }
            }
        };
        
        soundHandlerEspera.post(soundRunnableEspera);
    }
    
    /**
     * Detiene el sonido de espera
     */
    private void detenerSonidoEspera() {
        if (soundHandlerEspera != null && soundRunnableEspera != null) {
            soundHandlerEspera.removeCallbacks(soundRunnableEspera);
            soundRunnableEspera = null;
        }
        if (toneGeneratorEspera != null) {
            toneGeneratorEspera.release();
            toneGeneratorEspera = null;
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
}