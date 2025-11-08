package com.example.zavira_movil.HislaConocimiento;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.zavira_movil.R;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class IslaResultadoActivity extends AppCompatActivity {

    private TextView tvTituloPct, tvSubtitulo, tvPorcentaje, tvTiempoTotal;
    private LinearLayout contAreas;
    private Button btnFinalizar, btnRepetir;
    private ImageView ivTrophyGif;

    private String modalidad;
    private IslaCerrarResultadoResponse cerrar;
    private long tiempoTotalSegundos = 0;
    private MediaPlayer mediaPlayer;
    private boolean sonidoReproducido = false;

    @Override protected void onDestroy() {
        super.onDestroy();
        detenerSonido();
    }
    
    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_isla_resultado);

        tvTituloPct  = findViewById(R.id.tvTituloPct);
        tvSubtitulo  = findViewById(R.id.tvSubtitulo);
        tvPorcentaje = findViewById(R.id.tvPorcentaje);
        contAreas    = findViewById(R.id.contAreas);
        btnFinalizar = findViewById(R.id.btnFinalizar);
        btnRepetir   = findViewById(R.id.btnRepetir);
        ivTrophyGif  = findViewById(R.id.ivTrophyGif);
        
        // Buscar o crear TextView para tiempo total (modalidad fácil)
        tvTiempoTotal = findViewById(android.R.id.text1); // Usar un ID temporal
        if (tvTiempoTotal == null && contAreas != null && contAreas.getParent() instanceof ViewGroup) {
            // Crear TextView para tiempo total si no existe
            ViewGroup parent = (ViewGroup) contAreas.getParent();
            tvTiempoTotal = new TextView(this);
            tvTiempoTotal.setTextSize(16);
            tvTiempoTotal.setTextColor(0xFF2563EB);
            tvTiempoTotal.setPadding(dp(16), dp(16), dp(16), dp(8));
            tvTiempoTotal.setGravity(Gravity.CENTER);
            tvTiempoTotal.setTypeface(null, android.graphics.Typeface.BOLD);
            parent.addView(tvTiempoTotal, parent.indexOfChild(contAreas));
        }

        modalidad = getIntent().getStringExtra("modalidad");
        String area = getIntent().getStringExtra("area");
        String resultadoJson = getIntent().getStringExtra("resultado_json");
        
        // Obtener tiempo total para modalidad fácil
        tiempoTotalSegundos = getIntent().getLongExtra("tiempo_total_segundos", 0);
        
        if (resultadoJson != null && !resultadoJson.trim().isEmpty()) {
            try { cerrar = GsonHolder.gson().fromJson(resultadoJson, IslaCerrarResultadoResponse.class); }
            catch (Exception ignore) {}
        }

        // Mostrar el área si está disponible
        if (area != null && !area.trim().isEmpty()) {
            TextView tvArea = findViewById(R.id.tvArea);
            if (tvArea == null) {
                // Crear TextView para el área si no existe
                LinearLayout contAreas = findViewById(R.id.contAreas);
                if (contAreas != null && contAreas.getParent() instanceof ViewGroup) {
                    ViewGroup parent = (ViewGroup) contAreas.getParent();
                    tvArea = new TextView(this);
                    tvArea.setText("Área: " + area);
                    tvArea.setTextSize(16);
                    tvArea.setTextColor(0xFF000000);
                    tvArea.setPadding(dp(16), dp(8), dp(16), dp(8));
                    parent.addView(tvArea, parent.indexOfChild(contAreas));
                }
            } else {
                tvArea.setText("Área: " + area);
            }
        }

        pintarCabecera(); 
        pintarTiempoTotal(); // Mostrar tiempo total para modalidad fácil
        pintarAreas();
        
        // Verificar si ganó (20+ correctas) y mostrar trofeo/sonido
        verificarGanador();

        btnFinalizar.setOnClickListener(v -> {
            Intent i = new Intent(IslaResultadoActivity.this, IslaModalityActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        });
        
        if (btnRepetir != null) {
            btnRepetir.setOnClickListener(v -> {
                Intent i = new Intent(IslaResultadoActivity.this, IslaSimulacroActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
            });
        }
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
    
    /**
     * Muestra el tiempo total empleado para modalidad fácil
     */
    private void pintarTiempoTotal() {
        if (tvTiempoTotal == null) return;
        
        // Solo mostrar para modalidad fácil
        if ("facil".equalsIgnoreCase(modalidad) && tiempoTotalSegundos > 0) {
            long minutos = tiempoTotalSegundos / 60;
            long segundos = tiempoTotalSegundos % 60;
            String tiempoFormateado = String.format(Locale.getDefault(), 
                "⏱ Tiempo total: %d min %02d seg", minutos, segundos);
            tvTiempoTotal.setText(tiempoFormateado);
            tvTiempoTotal.setVisibility(View.VISIBLE);
        } else {
            tvTiempoTotal.setVisibility(View.GONE);
        }
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
        
        // Aplicar color según el área
        int areaColor = obtenerColorArea(nombre);
        bar.getProgressDrawable().setColorFilter(areaColor, android.graphics.PorterDuff.Mode.SRC_IN);

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
    
    private int obtenerColorArea(String area) {
        if (area == null) return 0xFFB6B9C2;
        String a = area.toLowerCase().trim();
        
        if (a.contains("matem")) return 0xFFE53935;
        if (a.contains("lengua") || a.contains("lectura") || a.contains("espa") || a.contains("critica")) 
            return 0xFF2196F3;
        if (a.contains("social") || a.contains("ciudad")) 
            return 0xFF9C27B0;
        if (a.contains("cien") || a.contains("biolo") || a.contains("fis") || a.contains("quim")) 
            return 0xFF4CAF50;
        if (a.contains("ingl")) 
            return 0xFF9C27B0;
        
        return 0xFFB6B9C2;
    }
    
    /**
     * Verifica si el usuario ganó (20+ correctas de 25) y muestra trofeo/sonido
     */
    private void verificarGanador() {
        if (cerrar == null) return;
        
        // correctas es int (primitivo), no puede ser null
        int correctas = cerrar.correctas;
        boolean gano = correctas >= 20; // 20 de 25 correctas = ganador
        
        if (gano) {
            // Ganador: mostrar trofeo y reproducir sonido de victoria
            mostrarTrofeo(true);
            reproducirSonidoVictoria();
        } else {
            // Perdedor: reproducir sonido de derrota
            mostrarTrofeo(false);
            reproducirSonidoDerrota();
        }
    }
    
    /**
     * Muestra u oculta el trofeo según si ganó
     */
    private void mostrarTrofeo(boolean mostrar) {
        if (ivTrophyGif == null) return;
        
        if (mostrar) {
            ivTrophyGif.setVisibility(View.VISIBLE);
            
            try {
                // Intentar cargar GIF animado desde raw (si existe)
                int gifResourceId = getResources().getIdentifier("trophy_cup", "raw", getPackageName());
                if (gifResourceId != 0) {
                    Glide.with(this)
                        .asGif()
                        .load(gifResourceId)
                        .placeholder(R.drawable.trofeo)
                        .error(R.drawable.trofeo)
                        .listener(new RequestListener<com.bumptech.glide.load.resource.gif.GifDrawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<com.bumptech.glide.load.resource.gif.GifDrawable> target, boolean isFirstResource) {
                                iniciarAnimaciones(ivTrophyGif);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(com.bumptech.glide.load.resource.gif.GifDrawable resource, Object model, Target<com.bumptech.glide.load.resource.gif.GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                                iniciarAnimaciones(ivTrophyGif);
                                return false;
                            }
                        })
                        .into(ivTrophyGif);
                } else {
                    // Cargar imagen PNG del trofeo
                    ivTrophyGif.setImageResource(R.drawable.trofeo);
                    iniciarAnimaciones(ivTrophyGif);
                }
            } catch (Exception e) {
                android.util.Log.e("IslaResultado", "Error cargando trofeo", e);
                ivTrophyGif.setImageResource(R.drawable.trofeo);
                iniciarAnimaciones(ivTrophyGif);
            }
        } else {
            ivTrophyGif.setVisibility(View.GONE);
            ivTrophyGif.clearAnimation();
        }
    }
    
    /**
     * Inicia las animaciones del trofeo
     */
    private void iniciarAnimaciones(ImageView imageView) {
        if (imageView == null) return;
        
        imageView.post(() -> {
            if (imageView == null) return;
            
            try {
                Animation appearAnim = AnimationUtils.loadAnimation(this, R.anim.trophy_appear);
                Animation bounceAnim = AnimationUtils.loadAnimation(this, R.anim.trophy_bounce);
                
                if (appearAnim != null && bounceAnim != null) {
                    imageView.clearAnimation();
                    
                    Animation.AnimationListener appearListener = new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}
                        
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if (imageView != null && bounceAnim != null) {
                                imageView.startAnimation(bounceAnim);
                            }
                        }
                        
                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    };
                    
                    appearAnim.setAnimationListener(appearListener);
                    imageView.startAnimation(appearAnim);
                }
            } catch (Exception e) {
                android.util.Log.e("IslaResultado", "Error iniciando animaciones", e);
            }
        });
    }
    
    /**
     * Reproduce el sonido de victoria
     */
    private void reproducirSonidoVictoria() {
        if (sonidoReproducido) return;
        
        try {
            detenerSonido();
            sonidoReproducido = true;
            
            int resourceId = getResources().getIdentifier("ganador", "raw", getPackageName());
            
            if (resourceId != 0) {
                mediaPlayer = MediaPlayer.create(this, resourceId);
                if (mediaPlayer != null) {
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setVolume(1.0f, 1.0f);
                    mediaPlayer.setOnCompletionListener(mp -> {
                        if (mediaPlayer != null) {
                            mediaPlayer.release();
                            mediaPlayer = null;
                        }
                    });
                    mediaPlayer.start();
                }
            }
        } catch (Exception e) {
            android.util.Log.e("IslaResultado", "Error reproduciendo sonido victoria", e);
        }
    }
    
    /**
     * Reproduce el sonido de derrota
     */
    private void reproducirSonidoDerrota() {
        if (sonidoReproducido) return;
        
        try {
            detenerSonido();
            sonidoReproducido = true;
            
            int resourceId = getResources().getIdentifier("perdedor", "raw", getPackageName());
            
            if (resourceId != 0) {
                mediaPlayer = MediaPlayer.create(this, resourceId);
                if (mediaPlayer != null) {
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setVolume(1.0f, 1.0f);
                    mediaPlayer.setOnCompletionListener(mp -> {
                        if (mediaPlayer != null) {
                            mediaPlayer.release();
                            mediaPlayer = null;
                        }
                    });
                    mediaPlayer.start();
                }
            }
        } catch (Exception e) {
            android.util.Log.e("IslaResultado", "Error reproduciendo sonido derrota", e);
        }
    }
    
    /**
     * Detiene el sonido
     */
    private void detenerSonido() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                android.util.Log.e("IslaResultado", "Error deteniendo sonido", e);
            }
        }
    }
}
