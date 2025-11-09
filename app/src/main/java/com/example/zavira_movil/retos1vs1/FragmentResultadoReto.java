package com.example.zavira_movil.retos1vs1;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.load.DataSource;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.Glide;
import com.example.zavira_movil.R;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentResultadoReto extends Fragment {
    
    private Handler handler;
    private static final long POLL_MS = 500L; // Polling cada 0.5 segundos para respuesta más rápida
    private String idReto;
    private boolean ambosTerminaron = false;
    private boolean sonidoReproducido = false; // Para evitar reproducir el sonido múltiples veces
    private MediaPlayer mediaPlayer;

    public FragmentResultadoReto() { super(R.layout.fragment_result_reto); }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        
        // Asegurar que el topBar de HomeActivity esté oculto
        ocultarTopBar();

        ImageView ivTrophyGif = v.findViewById(R.id.ivTrophyGif);
        TextView tvEmoji = v.findViewById(R.id.tvEmoji);
        TextView tvTitle = v.findViewById(R.id.tvTitle);
        TextView tvSubtitle = v.findViewById(R.id.tvSubtitle);
        TextView tvYouName = v.findViewById(R.id.tvYouName);
        TextView tvYouDetail = v.findViewById(R.id.tvYouDetail);
        TextView tvYouPoints = v.findViewById(R.id.tvYouPoints);
        TextView tvOppName = v.findViewById(R.id.tvOppName);
        TextView tvOppDetail = v.findViewById(R.id.tvOppDetail);
        TextView tvOppPoints = v.findViewById(R.id.tvOppPoints);
        Button btnVolver   = v.findViewById(R.id.btnVolver);
        btnVolver.setText("Aceptar");
        // Asegurar que el botón use el color naranja-rojo correcto
        btnVolver.setBackgroundResource(R.drawable.bg_button_siguiente_selector);
        btnVolver.setBackgroundTintList(null);
        
        // Los sonidos se inicializarán cuando sea necesario

        String json = getArguments()!=null ? getArguments().getString("estadoJson") : null;
        int totalPreg = getArguments()!=null ? getArguments().getInt("totalPreguntas", 25) : 25;
        idReto = getArguments()!=null ? getArguments().getString("idReto") : null;
        
        if (json == null) { getParentFragmentManager().popBackStack(); return; }

        handler = new Handler();
        actualizarUI(json, totalPreg);

        // Si ambos no terminaron, iniciar polling inmediato para actualizar cuando el oponente termine
        // Usar un pequeño delay para evitar llamadas inmediatas múltiples
        if (!ambosTerminaron && idReto != null && !idReto.isEmpty()) {
            // Iniciar polling inmediatamente (sin delay inicial)
            handler.postDelayed(this::pollEstado, 100); // Pequeño delay inicial (100ms) para evitar sobrecarga
        }

        btnVolver.setOnClickListener(view -> {
            // 🔔 Y también al cerrar
            actualizarMarcadorEnBanner(null);
            volverARetos();
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Asegurar que el topBar esté oculto
        ocultarTopBar();
    }
    
    @Override
    public void onDestroyView() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        detenerSonido();
        // Restaurar topBar al salir
        restaurarTopBar();
        super.onDestroyView();
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
    
    private void detenerSonido() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                android.util.Log.e("FragmentResultadoReto", "Error deteniendo sonido", e);
            }
        }
    }
    
    private void reproducirSonidoVictoria() {
        if (sonidoReproducido || !isAdded()) return;
        
        try {
            // Detener cualquier sonido previo
            detenerSonido();
            
            sonidoReproducido = true;
            android.util.Log.d("FragmentResultadoReto", "Reproduciendo sonido de VICTORIA (aplausos)");
            
            // Intentar cargar archivo de audio desde res/raw/
            // Nombre: ganador.mp3, ganador.wav, ganador.ogg, etc.
            int resourceId = getResources().getIdentifier("ganador", "raw", requireContext().getPackageName());
            
            if (resourceId != 0) {
                // Archivo de audio encontrado - reproducir con MediaPlayer
                mediaPlayer = MediaPlayer.create(requireContext(), resourceId);
                if (mediaPlayer != null) {
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setVolume(1.0f, 1.0f); // Volumen máximo
                    mediaPlayer.setOnCompletionListener(mp -> {
                        if (mediaPlayer != null) {
                            mediaPlayer.release();
                            mediaPlayer = null;
                        }
                    });
                    mediaPlayer.start();
                    android.util.Log.d("FragmentResultadoReto", "Archivo 'ganador' reproducido correctamente");
                } else {
                    android.util.Log.w("FragmentResultadoReto", "No se pudo crear MediaPlayer para ganador");
                    // Fallback a tonos si no hay archivo
                    reproducirSonidoVictoriaFallback();
                }
            } else {
                android.util.Log.w("FragmentResultadoReto", "No se encontró archivo 'ganador' en res/raw/");
                // Fallback a tonos si no hay archivo
                reproducirSonidoVictoriaFallback();
            }
            
        } catch (Exception e) {
            android.util.Log.e("FragmentResultadoReto", "Error reproduciendo sonido victoria", e);
            sonidoReproducido = false;
            // Intentar fallback
            reproducirSonidoVictoriaFallback();
        }
    }
    
    // Método fallback mejorado: simula aplausos con múltiples tonos rápidos y rítmicos
    private void reproducirSonidoVictoriaFallback() {
        try {
            android.media.ToneGenerator toneGen1 = new android.media.ToneGenerator(AudioManager.STREAM_MUSIC, 100);
            android.media.ToneGenerator toneGen2 = new android.media.ToneGenerator(AudioManager.STREAM_MUSIC, 90);
            android.media.ToneGenerator toneGen3 = new android.media.ToneGenerator(AudioManager.STREAM_MUSIC, 85);
            Handler h = new Handler(requireContext().getMainLooper());
            
            // Simular aplausos: múltiples "claps" rápidos y superpuestos
            // Primera ráfaga de aplausos (0-600ms)
            reproducirAplausoSimulado(h, toneGen1, 0, android.media.ToneGenerator.TONE_DTMF_1, 50);
            reproducirAplausoSimulado(h, toneGen2, 50, android.media.ToneGenerator.TONE_DTMF_2, 50);
            reproducirAplausoSimulado(h, toneGen3, 100, android.media.ToneGenerator.TONE_DTMF_3, 50);
            reproducirAplausoSimulado(h, toneGen1, 150, android.media.ToneGenerator.TONE_DTMF_1, 50);
            reproducirAplausoSimulado(h, toneGen2, 200, android.media.ToneGenerator.TONE_DTMF_2, 50);
            
            // Segunda ráfaga más intensa (250-850ms)
            reproducirAplausoSimulado(h, toneGen3, 250, android.media.ToneGenerator.TONE_DTMF_4, 60);
            reproducirAplausoSimulado(h, toneGen1, 300, android.media.ToneGenerator.TONE_DTMF_5, 60);
            reproducirAplausoSimulado(h, toneGen2, 350, android.media.ToneGenerator.TONE_DTMF_6, 60);
            reproducirAplausoSimulado(h, toneGen3, 400, android.media.ToneGenerator.TONE_DTMF_7, 60);
            reproducirAplausoSimulado(h, toneGen1, 450, android.media.ToneGenerator.TONE_DTMF_8, 60);
            
            // Tercera ráfaga final (500-1100ms)
            reproducirAplausoSimulado(h, toneGen2, 500, android.media.ToneGenerator.TONE_DTMF_9, 70);
            reproducirAplausoSimulado(h, toneGen3, 550, android.media.ToneGenerator.TONE_DTMF_0, 70);
            reproducirAplausoSimulado(h, toneGen1, 600, android.media.ToneGenerator.TONE_DTMF_1, 70);
            reproducirAplausoSimulado(h, toneGen2, 650, android.media.ToneGenerator.TONE_DTMF_2, 70);
            reproducirAplausoSimulado(h, toneGen3, 700, android.media.ToneGenerator.TONE_DTMF_3, 70);
            
            // Final con tonos más altos (800-1200ms)
            reproducirAplausoSimulado(h, toneGen1, 800, android.media.ToneGenerator.TONE_DTMF_7, 80);
            reproducirAplausoSimulado(h, toneGen2, 850, android.media.ToneGenerator.TONE_DTMF_8, 80);
            reproducirAplausoSimulado(h, toneGen3, 900, android.media.ToneGenerator.TONE_DTMF_9, 100);
            
            // Liberar recursos después de terminar
            h.postDelayed(() -> {
                try {
                    if (toneGen1 != null) toneGen1.release();
                    if (toneGen2 != null) toneGen2.release();
                    if (toneGen3 != null) toneGen3.release();
                } catch (Exception e) {}
            }, 1200);
            
        } catch (Exception e) {
            android.util.Log.e("FragmentResultadoReto", "Error en fallback de victoria", e);
        }
    }
    
    // Helper para simular un aplauso individual
    private void reproducirAplausoSimulado(Handler h, android.media.ToneGenerator toneGen, long delay, int tone, int duration) {
        h.postDelayed(() -> {
            try {
                if (toneGen != null && isAdded()) {
                    toneGen.startTone(tone, duration);
                }
            } catch (Exception e) {}
        }, delay);
    }
    
    private void reproducirSonidoDerrota() {
        if (sonidoReproducido || !isAdded()) return;
        
        try {
            // Detener cualquier sonido previo
            detenerSonido();
            
            sonidoReproducido = true;
            android.util.Log.d("FragmentResultadoReto", "Reproduciendo sonido de DERROTA (abucheo)");
            
            // Intentar cargar archivo de audio desde res/raw/
            // Nombre: perdedor.mp3, perdedor.wav, perdedor.ogg, etc.
            int resourceId = getResources().getIdentifier("perdedor", "raw", requireContext().getPackageName());
            
            if (resourceId != 0) {
                // Archivo de audio encontrado - reproducir con MediaPlayer
                mediaPlayer = MediaPlayer.create(requireContext(), resourceId);
                if (mediaPlayer != null) {
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setVolume(1.0f, 1.0f); // Volumen máximo
                    mediaPlayer.setOnCompletionListener(mp -> {
                        if (mediaPlayer != null) {
                            mediaPlayer.release();
                            mediaPlayer = null;
                        }
                    });
                    mediaPlayer.start();
                    android.util.Log.d("FragmentResultadoReto", "Archivo 'perdedor' reproducido correctamente");
                } else {
                    android.util.Log.w("FragmentResultadoReto", "No se pudo crear MediaPlayer para perdedor");
                    // Fallback a tonos si no hay archivo
                    reproducirSonidoDerrotaFallback();
                }
            } else {
                android.util.Log.w("FragmentResultadoReto", "No se encontró archivo 'perdedor' en res/raw/");
                // Fallback a tonos si no hay archivo
                reproducirSonidoDerrotaFallback();
            }
            
        } catch (Exception e) {
            android.util.Log.e("FragmentResultadoReto", "Error reproduciendo sonido derrota", e);
            sonidoReproducido = false;
            // Intentar fallback
            reproducirSonidoDerrotaFallback();
        }
    }
    
    // Método fallback mejorado: simula abucheo "nooo" con tonos graves descendentes
    private void reproducirSonidoDerrotaFallback() {
        try {
            android.media.ToneGenerator toneGen = new android.media.ToneGenerator(AudioManager.STREAM_MUSIC, 100);
            Handler h = new Handler(requireContext().getMainLooper());
            
            // Simular "No" inicial - tono medio-grave
            h.postDelayed(() -> {
                try {
                    if (toneGen != null && isAdded()) {
                        toneGen.startTone(android.media.ToneGenerator.TONE_DTMF_5, 180);
                    }
                } catch (Exception e) {}
            }, 0);
            
            // "oo" - descenso gradual a tonos más graves
            h.postDelayed(() -> {
                try {
                    if (toneGen != null && isAdded()) {
                        toneGen.startTone(android.media.ToneGenerator.TONE_DTMF_4, 200);
                    }
                } catch (Exception e) {}
            }, 200);
            
            h.postDelayed(() -> {
                try {
                    if (toneGen != null && isAdded()) {
                        toneGen.startTone(android.media.ToneGenerator.TONE_DTMF_3, 250);
                    }
                } catch (Exception e) {}
            }, 420);
            
            // "ooo" final - tono muy grave y largo
            h.postDelayed(() -> {
                try {
                    if (toneGen != null && isAdded()) {
                        toneGen.startTone(android.media.ToneGenerator.TONE_DTMF_2, 300);
                    }
                } catch (Exception e) {}
            }, 690);
            
            h.postDelayed(() -> {
                try {
                    if (toneGen != null && isAdded()) {
                        toneGen.startTone(android.media.ToneGenerator.TONE_DTMF_1, 350);
                    }
                } catch (Exception e) {}
            }, 1010);
            
            // Final muy grave y sostenido
            h.postDelayed(() -> {
                try {
                    if (toneGen != null && isAdded()) {
                        toneGen.startTone(android.media.ToneGenerator.TONE_DTMF_0, 500);
                    }
                } catch (Exception e) {}
            }, 1380);
            
            // Liberar recurso
            h.postDelayed(() -> {
                try {
                    if (toneGen != null) {
                        toneGen.release();
                    }
                } catch (Exception e) {}
            }, 2000);
            
        } catch (Exception e) {
            android.util.Log.e("FragmentResultadoReto", "Error en fallback de derrota", e);
        }
    }
    
    private void mostrarGifCopa(ImageView imageView, boolean mostrar) {
        if (imageView == null || !isAdded()) return;
        
        if (mostrar) {
            // Mostrar trofeo con animación de aparición
            android.util.Log.d("FragmentResultadoReto", "Mostrando trofeo con animación");
            
            // Hacer visible el ImageView primero
            imageView.setVisibility(View.VISIBLE);
            
            try {
                // Intentar cargar GIF animado desde raw (si existe)
                int gifResourceId = getResources().getIdentifier("trophy_cup", "raw", requireContext().getPackageName());
                if (gifResourceId != 0) {
                    // Cargar GIF animado con Glide
                    Glide.with(requireContext())
                        .asGif()
                        .load(gifResourceId)
                        .placeholder(R.drawable.trofeo)
                        .error(R.drawable.trofeo)
                        .listener(new RequestListener<com.bumptech.glide.load.resource.gif.GifDrawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<com.bumptech.glide.load.resource.gif.GifDrawable> target, boolean isFirstResource) {
                                iniciarAnimaciones(imageView);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(com.bumptech.glide.load.resource.gif.GifDrawable resource, Object model, Target<com.bumptech.glide.load.resource.gif.GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                                iniciarAnimaciones(imageView);
                                return false;
                            }
                        })
                        .into(imageView);
                } else {
                    // Cargar imagen PNG del trofeo (trofeo.png)
                    try {
                        imageView.setImageResource(R.drawable.trofeo);
                        android.util.Log.d("FragmentResultadoReto", "Trofeo cargado desde drawable");
                        // Iniciar animación después de cargar la imagen
                        iniciarAnimaciones(imageView);
                    } catch (Exception e) {
                        android.util.Log.e("FragmentResultadoReto", "Error cargando trofeo.png", e);
                        // Intentar con Glide
                        try {
                            Glide.with(requireContext())
                                .load(R.drawable.trofeo)
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        iniciarAnimaciones(imageView);
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        iniciarAnimaciones(imageView);
                                        return false;
                                    }
                                })
                                .into(imageView);
                        } catch (Exception e2) {
                            android.util.Log.e("FragmentResultadoReto", "Error con Glide", e2);
                            iniciarAnimaciones(imageView);
                        }
                    }
                }
                
            } catch (Exception e) {
                android.util.Log.e("FragmentResultadoReto", "Error general cargando trofeo", e);
                // Último intento: cargar directamente y hacer visible
                try {
                    imageView.setImageResource(R.drawable.trofeo);
                    imageView.setVisibility(View.VISIBLE);
                    iniciarAnimaciones(imageView);
                } catch (Exception e2) {
                    android.util.Log.e("FragmentResultadoReto", "Error final cargando trofeo", e2);
                }
            }
        } else {
            imageView.setVisibility(View.GONE);
            imageView.clearAnimation();
        }
    }
    
    private void iniciarAnimaciones(ImageView imageView) {
        if (imageView == null || !isAdded()) return;
        
        // Usar post para asegurar que la imagen ya está renderizada
        imageView.post(() -> {
            if (!isAdded() || imageView == null) return;
            
            try {
                // Cargar animaciones
                Animation appearAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.trophy_appear);
                Animation bounceAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.trophy_bounce);
                
                if (appearAnim != null && bounceAnim != null) {
                    // Asegurar que no haya animación previa
                    imageView.clearAnimation();
                    
                    // Crear listener para la animación de aparición
                    Animation.AnimationListener appearListener = new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            android.util.Log.d("FragmentResultadoReto", "Animación de aparición iniciada");
                        }
                        
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            // Después de aparecer, hacer bounce infinito
                            if (imageView != null && isAdded() && bounceAnim != null) {
                                imageView.startAnimation(bounceAnim);
                                android.util.Log.d("FragmentResultadoReto", "Animación de bounce iniciada");
                            }
                        }
                        
                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    };
                    
                    appearAnim.setAnimationListener(appearListener);
                    
                    // Iniciar animación de aparición
                    imageView.startAnimation(appearAnim);
                    android.util.Log.d("FragmentResultadoReto", "Animaciones aplicadas al trofeo");
                } else {
                    android.util.Log.w("FragmentResultadoReto", "No se pudieron cargar las animaciones");
                }
            } catch (Exception e) {
                android.util.Log.e("FragmentResultadoReto", "Error aplicando animación", e);
            }
        });
    }
    
    private void pollEstado() {
        if (!isAdded() || TextUtils.isEmpty(idReto) || ambosTerminaron) return;
        
        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        api.estadoReto(idReto).enqueue(new Callback<EstadoRetoResponse>() {
            @Override
            public void onResponse(Call<EstadoRetoResponse> call, Response<EstadoRetoResponse> resp) {
                if (!isAdded()) return;
                
                if (resp.isSuccessful() && resp.body() != null) {
                    EstadoRetoResponse e = resp.body();
                    String estadoJson = new Gson().toJson(e);
                    int totalPreg = getArguments() != null ? getArguments().getInt("totalPreguntas", 25) : 25;
                    
                    // Guardar estado anterior de ambosTerminaron
                    boolean terminaronAntes = ambosTerminaron;
                    
                    // Actualizar UI con el nuevo estado (esto resetea el flag de sonido si ambos terminaron)
                    actualizarUI(estadoJson, totalPreg);
                    
                    // Si ambos terminaron ahora (pero no antes), detener polling inmediatamente
                    if (ambosTerminaron && !terminaronAntes) {
                        // Detener cualquier polling pendiente
                        if (handler != null) {
                            handler.removeCallbacksAndMessages(null);
                        }
                        return;
                    }
                    
                    // Si ambos terminaron, no continuar polling
                    if (ambosTerminaron) {
                        return;
                    }
                }
                
                // Continuar polling si ambos no terminaron (con intervalo más corto)
                if (handler != null && !ambosTerminaron) {
                    handler.postDelayed(FragmentResultadoReto.this::pollEstado, POLL_MS);
                }
            }
            
            @Override
            public void onFailure(Call<EstadoRetoResponse> call, Throwable t) {
                if (!isAdded() || ambosTerminaron) return;
                
                // En caso de error, reintentar después de un tiempo más corto
                if (handler != null && !ambosTerminaron) {
                    handler.postDelayed(FragmentResultadoReto.this::pollEstado, POLL_MS);
                }
            }
        });
    }
    
    private void actualizarUI(String json, int totalPreg) {
        if (json == null || !isAdded()) return;
        
        View v = getView();
        if (v == null) return;
        
        ImageView ivTrophyGif = v.findViewById(R.id.ivTrophyGif);
        TextView tvEmoji = v.findViewById(R.id.tvEmoji);
        TextView tvTitle = v.findViewById(R.id.tvTitle);
        TextView tvSubtitle = v.findViewById(R.id.tvSubtitle);
        TextView tvYouName = v.findViewById(R.id.tvYouName);
        TextView tvYouDetail = v.findViewById(R.id.tvYouDetail);
        TextView tvYouPoints = v.findViewById(R.id.tvYouPoints);
        TextView tvOppName = v.findViewById(R.id.tvOppName);
        TextView tvOppDetail = v.findViewById(R.id.tvOppDetail);
        TextView tvOppPoints = v.findViewById(R.id.tvOppPoints);
        
        if (tvEmoji == null || tvTitle == null || tvSubtitle == null || 
            tvYouName == null || tvYouDetail == null || tvYouPoints == null ||
            tvOppName == null || tvOppDetail == null || tvOppPoints == null || ivTrophyGif == null) {
            return;
        }
        
        EstadoRetoResponse e = new Gson().fromJson(json, EstadoRetoResponse.class);

        Integer myId = null;
        try { myId = com.example.zavira_movil.local.TokenManager.getUserId(requireContext()); } catch (Exception ignored) {}

        EstadoRetoResponse.Jugador yo = null, rival = null;
        if (e.jugadores != null) {
            for (EstadoRetoResponse.Jugador j : e.jugadores) {
                if (myId != null && j.id_usuario != null && j.id_usuario.equals(myId)) yo = j;
                else if (rival == null) rival = j;
            }
        }
        if (yo == null && e.jugadores != null && !e.jugadores.isEmpty()) {
            yo = e.jugadores.get(0);
            if (e.jugadores.size() > 1) rival = e.jugadores.get(1);
        }

        int youCorrect = yo != null && yo.correctas != null ? yo.correctas : 0;
        int youTime    = yo != null && yo.tiempo_total_seg != null ? yo.tiempo_total_seg : 0;
        int oppCorrect = rival != null && rival.correctas != null ? rival.correctas : -1; // -1 si no terminó
        int oppTime    = rival != null && rival.tiempo_total_seg != null ? rival.tiempo_total_seg : 0;

        int youWrong = Math.max(0, totalPreg - youCorrect);
        int oppWrong = oppCorrect >= 0 ? Math.max(0, totalPreg - oppCorrect) : 0;

        // Calcular puntaje ICFES: 300 + (porcentaje * 2)
        // Porcentaje = (correctas / totalPreg) * 100
        int youPorcentaje = totalPreg > 0 ? Math.round((youCorrect * 100.0f) / totalPreg) : 0;
        int youPts = 300 + (youPorcentaje * 2); // Fórmula ICFES: 300-500
        
        int oppPorcentaje = (oppCorrect >= 0 && totalPreg > 0) ? Math.round((oppCorrect * 100.0f) / totalPreg) : 0;
        int oppPts = oppCorrect >= 0 ? (300 + (oppPorcentaje * 2)) : 0; // Fórmula ICFES: 300-500

        // Verificar si ambos terminaron (correctas >= 0 indica que terminó)
        boolean yoTermine = yo != null && yo.correctas != null && yo.correctas >= 0;
        boolean rivalTermino = rival != null && rival.correctas != null && rival.correctas >= 0;
        boolean terminaronAntes = ambosTerminaron;
        ambosTerminaron = yoTermine && rivalTermino;
        
        // Resetear flag de sonido solo la primera vez que ambos terminan
        if (ambosTerminaron && !terminaronAntes) {
            sonidoReproducido = false;
            // MediaPlayer se inicializa automáticamente cuando se reproduce el sonido
        }

        String titulo;
        String emoji;
        
        if (ambosTerminaron) {
            // Ambos terminaron: determinar ganador y mostrar resultado final
            Integer ganador = e.ganador;
            if (ganador == null && myId != null) {
                // Calcular ganador: más correctas gana, si empate, menos tiempo gana
                if (youCorrect > oppCorrect) ganador = myId;
                else if (oppCorrect > youCorrect) ganador = rival.id_usuario;
                else if (youTime < oppTime) ganador = myId; // Empate en correctas, menos tiempo gana
                else if (oppTime < youTime) ganador = rival.id_usuario;
                else ganador = 0; // Empate total
            }
            
            if (ganador != null && myId != null) {
                if (ganador.equals(myId)) {
                    titulo = "¡Victoria!";
                    emoji = "😄";
                    // Ocultar el emoji cuando hay trofeo
                    tvEmoji.setVisibility(View.GONE);
                    // Mostrar trofeo con animación solo al ganador
                    mostrarGifCopa(ivTrophyGif, true);
                    // Reproducir sonido de celebración inmediatamente
                    reproducirSonidoVictoria();
                } else if (ganador == 0) {
                    titulo = "Empate";
                    emoji = "😐";
                    mostrarGifCopa(ivTrophyGif, false);
                    tvEmoji.setVisibility(View.VISIBLE);
                } else {
                    titulo = "Derrota";
                    emoji = "😞";
                    mostrarGifCopa(ivTrophyGif, false);
                    tvEmoji.setVisibility(View.VISIBLE);
                    // Reproducir sonido de derrota inmediatamente
                    reproducirSonidoDerrota();
                }
            } else {
                titulo = "Resultados Finales";
                emoji = "📊";
                mostrarGifCopa(ivTrophyGif, false);
                tvEmoji.setVisibility(View.VISIBLE);
            }
        } else {
            // Solo uno terminó: mostrar "Esperando oponente..." sin victoria/derrota
            titulo = "Esperando oponente...";
            emoji = "⏳";
            mostrarGifCopa(ivTrophyGif, false);
            tvEmoji.setVisibility(View.VISIBLE);
        }

        tvTitle.setText(titulo);
        tvEmoji.setText(emoji);
        tvSubtitle.setText(""); // Quitar "Reto #" - es información interna
        tvYouName.setText("Tú");
        tvOppName.setText("Oponente");
        
        // Mostrar resultados reales del jugador con tiempo formateado
        String tiempoYo = formatoTiempo(youTime);
        tvYouDetail.setText(youCorrect + "/" + totalPreg + " correctas • " + youWrong + " incorrectas • " + tiempoYo);
        
        // Si el oponente no terminó, mostrar "Esperando..." en lugar de resultados
        if (ambosTerminaron) {
            String tiempoOpo = formatoTiempo(oppTime);
            tvOppDetail.setText(oppCorrect + "/" + totalPreg + " correctas • " + oppWrong + " incorrectas • " + tiempoOpo);
        } else {
            tvOppDetail.setText("Esperando oponente...");
        }
        
        // Establecer puntajes y colores según quién ganó
        tvYouPoints.setText(String.valueOf(youPts));
        if (ambosTerminaron) {
            tvOppPoints.setText(String.valueOf(oppPts));
            
            // Determinar ganador para colorear los puntajes
            Integer ganador = e.ganador;
            if (ganador == null && myId != null) {
                // Calcular ganador si no viene en la respuesta
                if (youCorrect > oppCorrect) ganador = myId;
                else if (oppCorrect > youCorrect) ganador = rival.id_usuario;
                else if (youTime < oppTime) ganador = myId;
                else if (oppTime < youTime) ganador = rival.id_usuario;
                else ganador = 0; // Empate
            }
            
            // Colorear puntajes: verde para ganador, rojo para perdedor
            if (ganador != null && myId != null) {
                if (ganador.equals(myId)) {
                    // Yo gané: mi puntaje verde, oponente rojo
                    tvYouPoints.setTextColor(0xFF22C55E); // Verde
                    tvOppPoints.setTextColor(0xFFEF4444); // Rojo
                } else if (ganador == 0) {
                    // Empate: ambos en color neutro (púrpura)
                    tvYouPoints.setTextColor(0xFF957DAD); // Púrpura
                    tvOppPoints.setTextColor(0xFF957DAD); // Púrpura
                } else {
                    // Oponente ganó: mi puntaje rojo, oponente verde
                    tvYouPoints.setTextColor(0xFFEF4444); // Rojo
                    tvOppPoints.setTextColor(0xFF22C55E); // Verde
                }
            } else {
                // Color por defecto si no se puede determinar
                tvYouPoints.setTextColor(0xFF957DAD); // Púrpura
                tvOppPoints.setTextColor(0xFF957DAD); // Púrpura
            }
        } else {
            tvOppPoints.setText("---");
            // Color por defecto mientras espera
            tvYouPoints.setTextColor(0xFF957DAD); // Púrpura
            tvOppPoints.setTextColor(0xFF957DAD); // Púrpura
        }

        // 🔔 Refresca el banner apenas abren resultados
        actualizarMarcadorEnBanner(null);
    }

    private void actualizarMarcadorEnBanner(@Nullable Integer idSesion) {
        if (!isAdded()) return;

        final TextView tvVictorias = findMetricViewSafely(R.id.tvVictorias);
        final TextView tvDerrotas  = findMetricViewSafely(R.id.tvDerrotas);
        if (tvVictorias == null && tvDerrotas == null) return;

        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        Call<MarcadorResponse> call = (idSesion != null)
                ? api.marcadorPorSesion(idSesion)
                : api.marcador();

        call.enqueue(new Callback<MarcadorResponse>() {
            @Override public void onResponse(Call<MarcadorResponse> c, Response<MarcadorResponse> r) {
                if (!isAdded() || r.body() == null || !r.isSuccessful()) return;
                if (tvVictorias != null) tvVictorias.setText(String.valueOf(r.body().victorias));
                if (tvDerrotas  != null) tvDerrotas.setText(String.valueOf(r.body().derrotas));
            }
            @Override public void onFailure(Call<MarcadorResponse> c, Throwable t) { }
        });
    }

    @Nullable
    private TextView findMetricViewSafely(int id) {
        Fragment parent = getParentFragment();
        if (parent != null && parent.getView() != null) {
            View v = parent.getView().findViewById(id);
            if (v instanceof TextView) return (TextView) v;
        }
        if (getActivity() != null) {
            View v = getActivity().findViewById(id);
            if (v instanceof TextView) return (TextView) v;
        }
        return null;
    }

    private void volverARetos() {
        if (!isAdded()) return;

        Fragment parent = getParentFragment();
        if (parent != null) {
            FragmentManager childFm = parent.getChildFragmentManager();
            childFm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            View parentView = parent.getView();
            if (parentView != null) {
                int overlayId = idByName("container");
                if (overlayId != 0) {
                    View overlay = parentView.findViewById(overlayId);
                    if (overlay != null) overlay.setVisibility(View.GONE);
                }
            }
        }

        int rootId = idByName("fragmentContainer");
        if (rootId == 0) rootId = idByName("main_container");
        if (rootId == 0) rootId = android.R.id.content;

        FragmentManager fm = requireActivity().getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction()
                .replace(rootId, new com.example.zavira_movil.ui.ranking.progreso.RetosFragment())
                .commit();
    }

    private int idByName(String name) {
        try {
            return getResources().getIdentifier(name, "id", requireContext().getPackageName());
        } catch (Exception ignored) { return 0; }
    }
    
    // Formatear tiempo en minutos y segundos
    private String formatoTiempo(int segundos) {
        int minutos = segundos / 60;
        int segs = segundos % 60;
        if (minutos > 0) {
            return minutos + "m " + segs + "s";
        } else {
            return segs + "s";
        }
    }
}
