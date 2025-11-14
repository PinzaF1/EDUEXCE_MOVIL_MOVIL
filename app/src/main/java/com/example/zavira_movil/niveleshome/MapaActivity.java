package com.example.zavira_movil.niveleshome;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zavira_movil.R;
import com.example.zavira_movil.databinding.ActivityMapaBinding;
import com.example.zavira_movil.model.Subject;
import com.example.zavira_movil.model.Level;
import com.example.zavira_movil.local.TokenManager;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.example.zavira_movil.HislaConocimiento.IslaSimulacroRequest;
import com.example.zavira_movil.HislaConocimiento.IslaSimulacroResponse;
import com.example.zavira_movil.HislaConocimiento.IslaPreguntasActivity;
import com.example.zavira_movil.HislaConocimiento.GsonHolder;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapaActivity extends AppCompatActivity {

    private ActivityMapaBinding binding;
    private Subject subject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // IMPORTANTE: Configurar la ventana ANTES de habilitar EdgeToEdge
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        
        // Configurar EdgeToEdge para que el contenido se dibuje detrás de la status bar (transparente)
        // La barra de navegación será blanca, así que el contenido NO debe dibujarse detrás de ella
        EdgeToEdge.enable(this);
        
        binding = ActivityMapaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtener el subject desde el Intent
        subject = (Subject) getIntent().getSerializableExtra("subject");
        if (subject == null) {
            finish();
            return;
        }

        // CRÍTICO: Asegurar que el subject tenga los niveles cargados desde DemoData
        // Si el subject no tiene niveles o la lista está vacía, cargarlos desde DemoData
        if (subject.levels == null || subject.levels.isEmpty()) {
            android.util.Log.d("MapaActivity", "Subject no tiene niveles, cargando desde DemoData...");
            cargarNivelesDesdeDemoData();
        } else {
            android.util.Log.d("MapaActivity", "Subject ya tiene " + subject.levels.size() + " niveles cargados");
        }

        // Configurar barras del sistema
        // Status bar: transparente para efecto de blur
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        // Ocultar barra de navegación del sistema
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        
        // Configurar según la versión de Android
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            // Android 12+ (API 31+): Status bar con blur, ocultar barra de navegación
            androidx.core.view.WindowInsetsControllerCompat windowInsetsController =
                androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            if (windowInsetsController != null) {
                // Status bar: texto claro (blanco) para visibilidad sobre el blur
                windowInsetsController.setAppearanceLightStatusBars(false);
                // Ocultar barra de navegación del sistema
                windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.navigationBars());
                windowInsetsController.setSystemBarsBehavior(
                    androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Android 11 (API 30): Status bar transparente, ocultar barra de navegación
            androidx.core.view.WindowInsetsControllerCompat windowInsetsController =
                androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            if (windowInsetsController != null) {
                windowInsetsController.setAppearanceLightStatusBars(false);
                // Ocultar barra de navegación del sistema
                windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.navigationBars());
                windowInsetsController.setSystemBarsBehavior(
                    androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Android 6.0+ (API 23+): Flags para status bar transparente y ocultar barra de navegación
            final int flags = (android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                              android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                              android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                              android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                              android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) &
                              ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
            
            // Listener para mantener la barra oculta cuando el usuario interactúa
            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
                if ((visibility & android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                    // Si la barra se muestra, ocultarla de nuevo
                    getWindow().getDecorView().setSystemUiVisibility(flags);
                }
            });
        }
        
        // Aplicar configuración después de que el layout esté listo
        getWindow().getDecorView().post(() -> {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
            
            // Asegurar que la barra de navegación esté oculta
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                androidx.core.view.WindowInsetsControllerCompat windowInsetsController =
                    androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
                if (windowInsetsController != null) {
                    windowInsetsController.setAppearanceLightStatusBars(false);
                    windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.navigationBars());
                    windowInsetsController.setSystemBarsBehavior(
                        androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            }
        });

        // Ajustar padding del header para insets del sistema (header flotante sobre el mapa)
        View headerMapa = findViewById(R.id.headerMapa);
        if (headerMapa != null) {
            ViewCompat.setOnApplyWindowInsetsListener(headerMapa, (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingStart(), systemBars.top, v.getPaddingEnd(), v.getPaddingBottom());
                return insets;
            });
            
            // Aplicar color de blur según la materia
            int blurColor = getBlurColorForSubject(subject.title);
            if (blurColor != 0) {
                // Crear un drawable con el color semitransparente para efecto blur
                android.graphics.drawable.GradientDrawable blurDrawable = new android.graphics.drawable.GradientDrawable();
                blurDrawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                // Usar el color con 50% de opacidad (80 = 128 en hex, que es ~50%)
                blurDrawable.setColor(blurColor);
                headerMapa.setBackground(blurDrawable);
            }
        }

        // Configurar el título
        TextView tvTitle = findViewById(R.id.tvMapaTitle);
        if (tvTitle != null) {
            tvTitle.setText(subject.title);
        }

        // Configurar la imagen del mapa según la materia - Ocupa toda la pantalla
        ImageView ivMapa = findViewById(R.id.ivMapa);
        if (ivMapa != null) {
        // El mapa se extiende detrás de la status bar (transparente)
        // La barra de navegación está oculta, así que el mapa puede ocupar toda la pantalla
        ViewCompat.setOnApplyWindowInsetsListener(ivMapa, (v, insets) -> {
            // El mapa ocupa toda la pantalla, sin padding
            return insets;
        });
            
            int mapaResId = getMapaResource(subject.title);
            android.util.Log.d("MapaActivity", "=== CONFIGURANDO MAPA ===");
            android.util.Log.d("MapaActivity", "Subject title: " + subject.title);
            android.util.Log.d("MapaActivity", "Mapa Resource ID: " + mapaResId);
            
            if (mapaResId != 0) {
                try {
                    ivMapa.setImageResource(mapaResId);
                    // Asegurar que el mapa ocupe toda la pantalla con centerCrop
                    ivMapa.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    android.util.Log.d("MapaActivity", "✓ Mapa cargado exitosamente: " + getResources().getResourceName(mapaResId));
                } catch (Exception e) {
                    android.util.Log.e("MapaActivity", "ERROR al cargar el recurso del mapa", e);
                    android.widget.Toast.makeText(this, "Error al cargar el mapa", android.widget.Toast.LENGTH_SHORT).show();
                }
            } else {
                android.util.Log.e("MapaActivity", "ERROR: No se encontró recurso de mapa para: " + subject.title);
                android.widget.Toast.makeText(this, "Mapa no disponible para: " + subject.title, android.widget.Toast.LENGTH_SHORT).show();
            }
            
            // CRÍTICO: El mapa NO debe interceptar toques para que los hotspots funcionen
            // Los hotspots están sobre el mapa y deben capturar todos los clics
            ivMapa.setClickable(false);
            ivMapa.setFocusable(false);
            ivMapa.setFocusableInTouchMode(false);
            ivMapa.setLongClickable(false);
            
            // Asegurar que el mapa esté por debajo en el z-order
            ivMapa.setElevation(1f);
            
            android.util.Log.d("MapaActivity", "ImageView del mapa configurado - NO intercepta toques");
        } else {
            android.util.Log.e("MapaActivity", "ERROR: ImageView ivMapa es null");
        }

        // Botón de regreso
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Configurar hotspots de niveles DESPUÉS de que las vistas estén infladas
        binding.getRoot().post(() -> {
            android.util.Log.d("MapaActivity", "=== CONFIGURANDO HOTSPOTS DE NIVELES ===");
            setupLevelsHotspots();
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Mantener configuración: status bar transparente, barra de navegación oculta
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            androidx.core.view.WindowInsetsControllerCompat windowInsetsController =
                androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            if (windowInsetsController != null) {
                windowInsetsController.setAppearanceLightStatusBars(false);
                windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.navigationBars());
                windowInsetsController.setSystemBarsBehavior(
                    androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            androidx.core.view.WindowInsetsControllerCompat windowInsetsController =
                androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            if (windowInsetsController != null) {
                windowInsetsController.setAppearanceLightStatusBars(false);
                windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.navigationBars());
                windowInsetsController.setSystemBarsBehavior(
                    androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Ocultar barra de navegación del sistema
            final int flags = (android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                              android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                              android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                              android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                              android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) &
                              ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }

        // IMPORTANTE: Reconfigurar hotspots en onResume para actualizar el estado de desbloqueo
        // Esto asegura que si el usuario completó un nivel, los siguientes se habiliten correctamente
        if (subject != null && subject.levels != null && !subject.levels.isEmpty()) {
            binding.getRoot().post(() -> {
                android.util.Log.d("MapaActivity", "=== ACTUALIZANDO HOTSPOTS EN onResume ===");
                setupLevelsHotspots();
            });
        }
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        
        // Mantener configuración cuando la ventana recibe foco
        if (hasFocus) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                androidx.core.view.WindowInsetsControllerCompat windowInsetsController =
                    androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
                if (windowInsetsController != null) {
                    windowInsetsController.setAppearanceLightStatusBars(false);
                    windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.navigationBars());
                    windowInsetsController.setSystemBarsBehavior(
                        androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                androidx.core.view.WindowInsetsControllerCompat windowInsetsController =
                    androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
                if (windowInsetsController != null) {
                    windowInsetsController.setAppearanceLightStatusBars(false);
                    windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.navigationBars());
                    windowInsetsController.setSystemBarsBehavior(
                        androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                final int flags = (android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                                  android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                                  android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                                  android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                  android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) &
                                  ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                getWindow().getDecorView().setSystemUiVisibility(flags);
            }
        }
    }

    /**
     * Obtiene el color de blur para el header según la materia
     * Retorna el color con opacidad para efecto blur (glassmorphism)
     */
    private int getBlurColorForSubject(String title) {
        if (title == null) {
            return 0;
        }
        String t = title.toLowerCase().trim();
        
        // Colores más transparentes (50% opacidad = 80) para efecto blur más sutil
        if (t.contains("matem")) {
            // Matemáticas: Rojo semitransparente
            return 0x80EF4444; // #80EF4444 (50% opacidad)
        } else if (t.contains("lectura") || t.contains("critica")) {
            // Lectura: Azul semitransparente
            return 0x802563EB; // #802563EB (50% opacidad)
        } else if (t.contains("social") || t.contains("ciudad")) {
            // Sociales: Naranja semitransparente
            return 0x80F97316; // #80F97316 (50% opacidad)
        } else if (t.contains("cien") || t.contains("natural")) {
            // Ciencias: Verde semitransparente
            return 0x8022C55E; // #8022C55E (50% opacidad)
        } else if (t.contains("ingl")) {
            // Inglés: Morado semitransparente (mismo color que SubjectAdapter: #8E24AA)
            return 0x808E24AA; // #808E24AA (50% opacidad, morado)
        } else if (t.contains("conocimiento")) {
            // Conocimiento: Usar un color diferente (púrpura/violeta oscuro)
            return 0x806D28D9; // #806D28D9 (50% opacidad, morado oscuro/violeta)
        }
        
        // Color por defecto: blanco semitransparente
        return 0x80FFFFFF;
    }
    
    /**
     * Crea un drawable circular verde semi-transparente para los hotspots desbloqueados
     */
    private android.graphics.drawable.Drawable createCircularGreenDrawable() {
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        drawable.setColor(0x6600FF00); // Verde semi-transparente (40% opacidad)
        return drawable;
    }
    
    /**
     * Crea un drawable circular gris oscuro semi-transparente para los hotspots bloqueados
     */
    private android.graphics.drawable.Drawable createCircularGrayDrawable() {
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        drawable.setColor(0x66444444); // Gris oscuro semi-transparente (40% opacidad)
        return drawable;
    }
    
    /**
     * Crea un drawable circular rojo semi-transparente para el hotspot DIFÍCIL de Conocimiento
     */
    private android.graphics.drawable.Drawable createCircularRedDrawable() {
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        drawable.setColor(0x66EF4444); // Rojo semi-transparente (40% opacidad)
        return drawable;
    }
    
    /**
     * Obtiene el recurso del mapa según el título de la materia
     */
    private int getMapaResource(String title) {
        if (title == null) {
            android.util.Log.e("MapaActivity", "getMapaResource: title is null");
            return 0;
        }
        String t = title.toLowerCase().trim();
        android.util.Log.d("MapaActivity", "getMapaResource: buscando mapa para título: '" + t + "'");

        if (t.contains("matem")) {
            android.util.Log.d("MapaActivity", "Mapeando a: Matemáticas");
            return R.drawable.mapamatematicas;
        } else if (t.contains("lectura") || t.contains("critica")) {
            android.util.Log.d("MapaActivity", "Mapeando a: Lectura");
            return R.drawable.mapalectura;
        } else if (t.contains("social") || t.contains("ciudad")) {
            android.util.Log.d("MapaActivity", "Mapeando a: Sociales");
            return R.drawable.mapasociales;
        } else if (t.contains("cien") || t.contains("natural")) {
            android.util.Log.d("MapaActivity", "Mapeando a: Ciencias (mapaciencias)");
            return R.drawable.mapaciencias;
        } else if (t.contains("ingl")) {
            android.util.Log.d("MapaActivity", "Mapeando a: Inglés");
            return R.drawable.mapaingles;
        } else if (t.contains("conocimiento")) {
            android.util.Log.d("MapaActivity", "Mapeando a: Conocimiento");
            return R.drawable.mapaconocimiento;
        }

        android.util.Log.w("MapaActivity", "No se encontró coincidencia para: '" + t + "'");
        return 0;
    }

    /**
     * Carga los niveles desde DemoData si el subject no los tiene
     */
    private void cargarNivelesDesdeDemoData() {
        List<Subject> subjects = DemoData.getSubjects();
        for (Subject s : subjects) {
            if (s.title != null && s.title.equalsIgnoreCase(subject.title)) {
                // Encontrar el subject correspondiente en DemoData
                subject.levels = s.levels;
                android.util.Log.d("MapaActivity", "✓ Niveles cargados desde DemoData: " + (subject.levels != null ? subject.levels.size() : 0) + " niveles");
                return;
            }
        }
        android.util.Log.w("MapaActivity", "No se encontró subject en DemoData para: " + subject.title);
    }

    /**
     * Clase auxiliar para almacenar posiciones de hotspots por materia y nivel
     */
    private static class HotspotPosition {
        float horizontalBias;
        float verticalBias;
        int width;
        int height;

        HotspotPosition(float hBias, float vBias, int w, int h) {
            this.horizontalBias = hBias;
            this.verticalBias = vBias;
            this.width = w;
            this.height = h;
        }
    }

    /**
     * Obtiene las posiciones de los hotspots para las materias y examen final

     */
    private HotspotPosition[] getHotspotPositionsCiencias() {

        return new HotspotPosition[]{
            new HotspotPosition(0.78f, 0.73f, 40, 40), // Nivel 1: Casa (abajo derecha)
            new HotspotPosition(0.22f, 0.69f, 40, 40), // Nivel 2: Casa (abajo izquierda)
            new HotspotPosition(0.70f, 0.51f, 40, 40), // Nivel 3: Casa (centro-derecha)
            new HotspotPosition(0.36f, 0.42f, 40, 40), // Nivel 4: Casa (centro-izquierda)
            new HotspotPosition(0.76f, 0.28f, 40, 40), // Nivel 5: Casa (arriba derecha)
            new HotspotPosition(0.68f, 0.11f, 50, 50)  // Examen Final: Edificio (centro superior)
        };
    }

    private HotspotPosition[] getHotspotPositionsMatematicas() {
        android.util.Log.d("MapaActivity", "=== OBTENIENDO POSICIONES DE MATEMÁTICAS ===");
        
        final float COLUMNA_DERECHA = 0.92f;
        return new HotspotPosition[]{
                new HotspotPosition(0.89f, 0.72f, 40, 40), // Nivel 1: Casa (abajo derecha)
                new HotspotPosition(0.14f, 0.52f, 40, 40), // Nivel 2: Casa (abajo izquierda)
                new HotspotPosition(0.90f, 0.48f, 40, 40), // Nivel 3: Casa (centro-derecha)
                new HotspotPosition(0.72f, 0.32f, 40, 40), // Nivel 4: Casa (centro-izquierda)
                new HotspotPosition(0.15f, 0.25f, 40, 40), // Nivel 5: Casa (arriba derecha)
                new HotspotPosition(0.49f, 0.14f, 60, 60)  // Examen Final: Edificio (centro superior)
        };
    }

    private HotspotPosition[] getHotspotPositionsIngles() {
        
        return new HotspotPosition[]{
                new HotspotPosition(0.92f, 0.91f, 40, 40), // Nivel 1: Casa (abajo derecha)
                new HotspotPosition(0.99f, 0.68f, 40, 40), // Nivel 2: Casa (abajo izquierda)
                new HotspotPosition(0.10f, 0.62f, 40, 40), // Nivel 3: Casa (centro-derecha)
                new HotspotPosition(0.28f, 0.45f, 40, 40), // Nivel 4: Casa (centro-izquierda)
                new HotspotPosition(0.88f, 0.36f, 40, 40), // Nivel 5: Casa (arriba derecha)
                new HotspotPosition(0.79f, 0.13f, 60, 60)  // Examen Final: Edificio (centro superior)
        };
    }

    private HotspotPosition[] getHotspotPositionsLectura() {

        return new HotspotPosition[]{
                new HotspotPosition(0.14f, 0.76f, 40, 40), // Nivel 1: Casa (abajo derecha)
                new HotspotPosition(0.82f, 0.69f, 40, 40), // Nivel 2: Casa (abajo izquierda)
                new HotspotPosition(0.26f, 0.66f, 40, 40), // Nivel 3: Casa (centro-derecha)
                new HotspotPosition(0.38f, 0.43f, 40, 40), // Nivel 4: Casa (centro-izquierda)
                new HotspotPosition(0.91f, 0.34f, 40, 40), // Nivel 5: Casa (arriba derecha)
                new HotspotPosition(0.93f, 0.13f, 60, 60)  // Examen Final: Edificio (centro superior)
        };
    }

    private HotspotPosition[] getHotspotPositionsSociales() {
        
        return new HotspotPosition[]{
                new HotspotPosition(0.91f, 0.80f, 50, 50), // Nivel 1: Casa (abajo derecha)
                new HotspotPosition(0.05f, 0.74f, 50, 50), // Nivel 2: Casa (abajo izquierda)
                new HotspotPosition(0.93f, 0.58f, 50, 50), // Nivel 3: Casa (centro-derecha)
                new HotspotPosition(0.19f, 0.48f, 50, 50), // Nivel 4: Casa (centro-izquierda)
                new HotspotPosition(0.82f, 0.37f, 50, 50), // Nivel 5: Casa (arriba derecha)
                new HotspotPosition(0.50f, 0.25f, 75, 75)  // Examen Final: Edificio (centro superior)
        };
    }
    
    private HotspotPosition[] getHotspotPositionsConocimiento() {
        return new HotspotPosition[]{
                new HotspotPosition(0.46f, 0.84f, 80, 35), // FÁCIL: Parte inferior del mapa
                new HotspotPosition(0.29f, 0.68f, 90, 30)  // DIFÍCIL: Parte superior del mapa
        };
    }

    private HotspotPosition[] getHotspotPositionsForSubject(String subjectTitle) {
        if (subjectTitle == null) {
            subjectTitle = "";
        }
        String t = subjectTitle.toLowerCase().trim();

        // CIENCIAS NATURALES - Sistema propio y específico
        if (t.contains("cien") || t.contains("natural")) {
            android.util.Log.d("MapaActivity", "Usando posiciones de Ciencias");
            return getHotspotPositionsCiencias();
        }
        // MATEMÁTICAS - Sistema completamente nuevo e independiente
        else if (t.contains("matem")) {
            android.util.Log.d("MapaActivity", "Usando posiciones de Matemáticas");
            return getHotspotPositionsMatematicas();
        }
        // INGLÉS - Sistema completamente nuevo e independiente (mapa nevado)
        else if (t.contains("ingl")) {
            android.util.Log.d("MapaActivity", "Usando posiciones de Inglés");
            return getHotspotPositionsIngles();
        }
        // LECTURA - Sistema específico para Lectura
        else if (t.contains("lectura") || t.contains("critica")) {
            android.util.Log.d("MapaActivity", "Usando posiciones de Lectura");
            return getHotspotPositionsLectura();
        }
        // SOCIALES - Sistema específico para Sociales
        else if (t.contains("social") || t.contains("ciudad")) {
            android.util.Log.d("MapaActivity", "Usando posiciones de Sociales");
            return getHotspotPositionsSociales();
        }
        // Para otras materias, usar Ciencias temporalmente hasta crear sus propios sistemas
        else {
            android.util.Log.d("MapaActivity", "Materia no configurada ('" + subjectTitle + "'), usando posiciones de Ciencias por defecto");
            return getHotspotPositionsCiencias();
        }
    }

    /**
     * Configura los click listeners para los hotspots de niveles
     * Cada hotspot abre QuizActivity directamente con el nivel correspondiente
     * Para Conocimiento, muestra hotspots de ribbon (FÁCIL y DIFÍCIL) en lugar de niveles
     */
    private void setupLevelsHotspots() {
        // Verificar si es Conocimiento - caso especial
        String title = subject.title != null ? subject.title.toLowerCase().trim() : "";
        if (title.contains("conocimiento")) {
            android.util.Log.d("MapaActivity", "=== CONFIGURANDO HOTSPOTS DE CONOCIMIENTO (FÁCIL/DIFÍCIL) ===");
            setupConocimientoHotspots();
            return;
        }

        // Verificar que el subject tenga niveles
        if (subject.levels == null || subject.levels.isEmpty()) {
            android.util.Log.e("MapaActivity", "ERROR: Subject no tiene niveles para configurar hotspots");
            return;
        }

        // Obtener userId desde TokenManager
        int userIdInt = TokenManager.getUserId(this);
        if (userIdInt <= 0) {
            android.util.Log.e("MapaActivity", "ERROR: userId inválido (" + userIdInt + ")");
            return;
        }
        String userId = String.valueOf(userIdInt);

        android.util.Log.d("MapaActivity", "=== Configurando hotspots para " + subject.levels.size() + " niveles ===");
        android.util.Log.d("MapaActivity", "UserId: " + userId);
        android.util.Log.d("MapaActivity", "Área: " + subject.title);

        // Obtener posiciones específicas para esta materia
        HotspotPosition[] positions = getHotspotPositionsForSubject(subject.title);

        // Ocultar hotspots de ribbon de Conocimiento si no es Conocimiento
        View hotspotConocimientoFacil = findViewById(R.id.hotspotConocimientoFacil);
        View hotspotConocimientoDificil = findViewById(R.id.hotspotConocimientoDificil);
        if (hotspotConocimientoFacil != null) {
            hotspotConocimientoFacil.setVisibility(View.GONE);
        }
        if (hotspotConocimientoDificil != null) {
            hotspotConocimientoDificil.setVisibility(View.GONE);
        }

        // Configurar cada hotspot de nivel (1-5)
        int[] hotspotIds = {
            R.id.hotspotLevel1,
            R.id.hotspotLevel2,
            R.id.hotspotLevel3,
            R.id.hotspotLevel4,
            R.id.hotspotLevel5
        };

        for (int i = 0; i < hotspotIds.length && i < subject.levels.size(); i++) {
            final int levelIndex = i;
            final int nivelNumero = i + 1; // nivel 1..5
            final Level level = subject.levels.get(i);

            View hotspot = findViewById(hotspotIds[i]);
            if (hotspot == null) {
                android.util.Log.e("MapaActivity", "ERROR: Hotspot para nivel " + nivelNumero + " no encontrado");
                continue;
            }

            // Verificar si el nivel está desbloqueado
            boolean unlocked = ProgressLockManager.isLevelUnlocked(this, userId, subject.title, nivelNumero);
            android.util.Log.d("MapaActivity", "Nivel " + nivelNumero + " - Desbloqueado: " + unlocked);

            // Obtener el subtema del nivel (primer subtopic) - CRÍTICO: hacerlo final para usar en lambda
            final String subtema;
            if (level.subtopics != null && !level.subtopics.isEmpty()) {
                subtema = level.subtopics.get(0).title;
            } else {
                subtema = "";
            }

            // CRÍTICO: Hacer una referencia final del subject para usar en lambda
            final Subject finalSubject = subject;
            
            // CRÍTICO: Para TODOS los niveles, hacer una copia final de todas las variables necesarias
            final int finalNivelNumero = nivelNumero;
            final String finalUserId = userId;

            // PRIMERO: Asegurar que el hotspot esté habilitado ANTES de aplicar posiciones
            hotspot.setClickable(true);
            hotspot.setEnabled(true);
            hotspot.setFocusable(true);
            hotspot.setFocusableInTouchMode(true);
            hotspot.setVisibility(View.VISIBLE);

            // Aplicar posiciones dinámicas según la materia
            if (i < positions.length) {
                final HotspotPosition pos = positions[i];
                android.util.Log.d("MapaActivity", "=== CONFIGURANDO NIVEL " + nivelNumero + " ===");
                android.util.Log.d("MapaActivity", "Posición - H:" + pos.horizontalBias + ", V:" + pos.verticalBias + ", Tamaño: " + pos.width + "x" + pos.height);
                
                // Aplicar posición después de que el layout esté listo
                hotspot.post(() -> {
                    try {
                        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params = 
                            (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) hotspot.getLayoutParams();
                        
                        if (params != null) {
                            // CRÍTICO: Asegurar que el hotspot tenga constraints a Start Y End para que horizontalBias funcione
                            // Si no tiene constraint a End, agregarlo
                            if (params.endToEnd == androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET) {
                                params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                                android.util.Log.d("MapaActivity", "Nivel " + finalNivelNumero + " - Agregando constraint a End");
                            }
                            
                            // Si no tiene constraint a Start, agregarlo
                            if (params.startToStart == androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET) {
                                params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                                android.util.Log.d("MapaActivity", "Nivel " + finalNivelNumero + " - Agregando constraint a Start");
                            }
                            
                            // Aplicar posición (bias horizontal y vertical)
                            params.horizontalBias = pos.horizontalBias;
                            params.verticalBias = pos.verticalBias;
                            
                            // Aplicar tamaño (convertir dp a píxeles)
                            float density = getResources().getDisplayMetrics().density;
                            params.width = (int) (pos.width * density);
                            params.height = (int) (pos.height * density);
                            
                            // Aplicar elevación para estar por encima del mapa
                            hotspot.setElevation(20f * density);

                            // Aplicar cambios
                            hotspot.setLayoutParams(params);
                            hotspot.bringToFront();

                            // Aplicar color según estado de desbloqueo
                            final boolean finalUnlocked = unlocked;
                            if (finalUnlocked) {
                                // Nivel desbloqueado: hotspot invisible/transparente (sin fondo)
                                hotspot.setBackground(null);
                                hotspot.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                                android.util.Log.d("MapaActivity", "✓ Hotspot nivel " + finalNivelNumero + " invisible/transparente (desbloqueado)");
                            } else {
                                // Nivel bloqueado: hotspot visible con fondo gris oscuro
                                hotspot.setBackground(createCircularGrayDrawable());
                                // Aplicar animación de pulso solo a hotspots bloqueados
                                aplicarAnimacionPulso(hotspot);
                                android.util.Log.d("MapaActivity", "✓ Hotspot nivel " + finalNivelNumero + " visible con fondo circular gris (bloqueado)");
                            }

                            // Forzar actualización del layout
                            hotspot.requestLayout();
                            
                            android.util.Log.d("MapaActivity", "✓ Nivel " + finalNivelNumero + 
                                " - H:" + params.horizontalBias + ", V:" + params.verticalBias +
                                " - Start:" + params.startToStart + ", End:" + params.endToEnd);
                        } else {
                            android.util.Log.e("MapaActivity", "ERROR: params es null para nivel " + finalNivelNumero);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("MapaActivity", "Error aplicando posición nivel " + finalNivelNumero, e);
                        e.printStackTrace();
                    }
                    
                    // Configurar listeners DESPUÉS de aplicar posición
                    configurarClickListenersHotspot(hotspot, finalNivelNumero, finalSubject, finalUserId, subtema, unlocked);
                });
            } else {
                // Si no hay posición definida, configurar listeners directamente y aplicar color
                final boolean finalUnlocked = unlocked;
                if (finalUnlocked) {
                    // Nivel desbloqueado: hotspot invisible/transparente (sin fondo)
                    hotspot.setBackground(null);
                    hotspot.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                } else {
                    // Nivel bloqueado: hotspot visible con fondo gris oscuro
                    hotspot.setBackground(createCircularGrayDrawable());
                    // Aplicar animación de pulso solo a hotspots bloqueados
                    aplicarAnimacionPulso(hotspot);
                }
                configurarClickListenersHotspot(hotspot, finalNivelNumero, finalSubject, finalUserId, subtema, unlocked);
            }

            android.util.Log.d("MapaActivity", "✓ Hotspot de nivel " + nivelNumero + " inicializado");
        }

        // ==================== CONFIGURAR HOTSPOT DE EXAMEN FINAL ====================
        View hotspotExamenFinal = findViewById(R.id.hotspotExamenFinal);
        if (hotspotExamenFinal != null) {
            // PRIMERO: Habilitar el hotspot
            hotspotExamenFinal.setClickable(true);
            hotspotExamenFinal.setEnabled(true);
            hotspotExamenFinal.setFocusable(true);
            hotspotExamenFinal.setFocusableInTouchMode(true);
            hotspotExamenFinal.setVisibility(View.VISIBLE);
            
            // Verificar si el examen final está desbloqueado (nivel >= 6)
            int unlockedLevel = ProgressLockManager.getUnlockedLevel(this, userId, subject.title);
            boolean examenFinalUnlocked = unlockedLevel >= 6;
            android.util.Log.d("MapaActivity", "=== CONFIGURANDO EXAMEN FINAL ===");
            android.util.Log.d("MapaActivity", "Nivel desbloqueado: " + unlockedLevel + ", Examen Final desbloqueado: " + examenFinalUnlocked);

            // CRÍTICO: Hacer una referencia final del subject para usar en lambda
            final Subject finalSubjectExamen = subject;
            final String finalUserIdExamen = userId;
            final boolean finalExamenFinalUnlocked = examenFinalUnlocked;
            
            // Crear o reutilizar TextView para mostrar "Examen Final"
            android.view.ViewGroup hotspotsContainer = findViewById(R.id.hotspotsLevelsContainer);
            TextView tvExamenFinal = hotspotsContainer != null ? 
                hotspotsContainer.findViewWithTag("examenFinalText") : null;
            
            if (tvExamenFinal == null && hotspotsContainer != null) {
                // Crear TextView solo si no existe
                tvExamenFinal = new TextView(this);
                tvExamenFinal.setTag("examenFinalText");
                tvExamenFinal.setText("Examen Final");
                tvExamenFinal.setTextColor(android.graphics.Color.WHITE);
                tvExamenFinal.setTextSize(16); // Tamaño más grande
                tvExamenFinal.setTypeface(null, android.graphics.Typeface.BOLD);
                
                // Centrar el texto horizontal y verticalmente
                tvExamenFinal.setGravity(android.view.Gravity.CENTER);
                tvExamenFinal.setTextAlignment(android.view.View.TEXT_ALIGNMENT_CENTER);
                
                // Asegurar que el TextView no tenga padding que pueda afectar el centrado
                tvExamenFinal.setPadding(0, 0, 0, 0);
                
                // Asegurar que el texto se dibuje en el centro
                tvExamenFinal.setIncludeFontPadding(false);
                
                hotspotsContainer.addView(tvExamenFinal);
            }
            
            final TextView finalTvExamenFinal = tvExamenFinal;
            
            // Asegurar que el TextView existente también tenga las propiedades de centrado
            if (finalTvExamenFinal != null) {
                finalTvExamenFinal.setGravity(android.view.Gravity.CENTER);
                finalTvExamenFinal.setTextAlignment(android.view.View.TEXT_ALIGNMENT_CENTER);
                finalTvExamenFinal.setIncludeFontPadding(false);
            }
            
            // Aplicar posición del examen final según la materia
            if (positions.length > 5) {
                final HotspotPosition posExamenFinal = positions[5];
                android.util.Log.d("MapaActivity", "Aplicando posición Examen Final - H:" + posExamenFinal.horizontalBias + 
                    ", V:" + posExamenFinal.verticalBias);
                
                hotspotExamenFinal.post(() -> {
                    try {
                        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams paramsExamen = 
                            (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) hotspotExamenFinal.getLayoutParams();
                        
                        if (paramsExamen != null) {
                            // Aplicar bias (posición)
                            paramsExamen.horizontalBias = posExamenFinal.horizontalBias;
                            paramsExamen.verticalBias = posExamenFinal.verticalBias;
                            
                            // Elevación alta para estar por encima
                            float density = getResources().getDisplayMetrics().density;
                            hotspotExamenFinal.setElevation(20f * density);
                            
                            hotspotExamenFinal.setLayoutParams(paramsExamen);
                            hotspotExamenFinal.bringToFront();
                            
                            // Aplicar color según estado de desbloqueo
                            if (finalExamenFinalUnlocked) {
                                // Examen Final desbloqueado: hotspot invisible/transparente (sin fondo)
                                hotspotExamenFinal.setBackground(null);
                                hotspotExamenFinal.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                                // También ocultar el TextView del examen final si está desbloqueado
                                if (finalTvExamenFinal != null) {
                                    finalTvExamenFinal.setVisibility(View.GONE);
                                }
                                android.util.Log.d("MapaActivity", "✓ Hotspot Examen Final invisible/transparente (desbloqueado)");
                            } else {
                                // Examen Final bloqueado: hotspot visible con fondo gris oscuro
                                hotspotExamenFinal.setBackground(createCircularGrayDrawable());
                                // Aplicar animación de pulso solo a hotspots bloqueados
                                aplicarAnimacionPulso(hotspotExamenFinal);
                                // Mostrar el TextView del examen final si está bloqueado
                                if (finalTvExamenFinal != null) {
                                    finalTvExamenFinal.setVisibility(View.VISIBLE);
                                }
                                android.util.Log.d("MapaActivity", "✓ Hotspot Examen Final visible con fondo circular gris (bloqueado)");
                            }
                            
                            // Vincular el TextView "Examen Final" directamente al hotspot DESPUÉS de que el hotspot esté posicionado
                            // Usar post para asegurar que el layout se haya medido completamente
                            hotspotExamenFinal.post(() -> {
                                vincularTextViewAlHotspot(finalTvExamenFinal, hotspotExamenFinal);
                            });
                            
                            android.util.Log.d("MapaActivity", "✓ Posición aplicada para Examen Final");
                            
                            // Configurar listeners DESPUÉS de aplicar posición
                            configurarClickListenersExamenFinal(hotspotExamenFinal, finalSubjectExamen, finalUserIdExamen, finalExamenFinalUnlocked);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("MapaActivity", "Error al aplicar posición para Examen Final", e);
                        // Aún así, aplicar color y vincular TextView al hotspot
                        if (finalExamenFinalUnlocked) {
                            // Examen Final desbloqueado: hotspot invisible/transparente (sin fondo)
                            hotspotExamenFinal.setBackground(null);
                            hotspotExamenFinal.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                            if (finalTvExamenFinal != null) {
                                finalTvExamenFinal.setVisibility(View.GONE);
                            }
                        } else {
                            // Examen Final bloqueado: hotspot visible con fondo gris oscuro
                            hotspotExamenFinal.setBackground(createCircularGrayDrawable());
                            // Aplicar animación de pulso solo a hotspots bloqueados
                            aplicarAnimacionPulso(hotspotExamenFinal);
                            if (finalTvExamenFinal != null) {
                                finalTvExamenFinal.setVisibility(View.VISIBLE);
                            }
                        }
                        
                        // Vincular TextView al hotspot en caso de error
                        vincularTextViewAlHotspot(finalTvExamenFinal, hotspotExamenFinal);
                        
                        configurarClickListenersExamenFinal(hotspotExamenFinal, finalSubjectExamen, finalUserIdExamen, finalExamenFinalUnlocked);
                    }
                });
            } else {
                // Aplicar color y vincular TextView al hotspot directamente si no hay posición
                if (finalExamenFinalUnlocked) {
                    // Examen Final desbloqueado: hotspot invisible/transparente (sin fondo)
                    hotspotExamenFinal.setBackground(null);
                    hotspotExamenFinal.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                    if (finalTvExamenFinal != null) {
                        finalTvExamenFinal.setVisibility(View.GONE);
                    }
                } else {
                    // Examen Final bloqueado: hotspot visible con fondo gris oscuro
                    hotspotExamenFinal.setBackground(createCircularGrayDrawable());
                    // Aplicar animación de pulso solo a hotspots bloqueados
                    aplicarAnimacionPulso(hotspotExamenFinal);
                    if (finalTvExamenFinal != null) {
                        finalTvExamenFinal.setVisibility(View.VISIBLE);
                    }
                }
                
                // Vincular TextView al hotspot
                hotspotExamenFinal.post(() -> {
                    vincularTextViewAlHotspot(finalTvExamenFinal, hotspotExamenFinal);
                });
                
                configurarClickListenersExamenFinal(hotspotExamenFinal, finalSubjectExamen, finalUserIdExamen, finalExamenFinalUnlocked);
            }
        } else {
            android.util.Log.e("MapaActivity", "ERROR: Hotspot de Examen Final no encontrado");
        }
    }
    
    /**
     * Aplica animación de pulso (crecer y volver) a un hotspot
     */
    private void aplicarAnimacionPulso(View hotspot) {
        if (hotspot != null) {
            android.view.animation.Animation anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.pulse_animation);
            if (anim != null) {
                hotspot.startAnimation(anim);
            }
        }
    }
    
    private void setupConocimientoHotspots() {
        // Ocultar todos los hotspots de niveles normales
        int[] nivelHotspotIds = {
            R.id.hotspotLevel1,
            R.id.hotspotLevel2,
            R.id.hotspotLevel3,
            R.id.hotspotLevel4,
            R.id.hotspotLevel5,
            R.id.hotspotExamenFinal
        };
        
        for (int id : nivelHotspotIds) {
            View hotspot = findViewById(id);
            if (hotspot != null) {
                hotspot.setVisibility(View.GONE);
            }
        }
        
        // Obtener posiciones de hotspots para Conocimiento
        HotspotPosition[] positions = getHotspotPositionsConocimiento();
        
        // Configurar hotspot FÁCIL
        View hotspotFacil = findViewById(R.id.hotspotConocimientoFacil);
        if (hotspotFacil != null && positions.length > 0) {
            final HotspotPosition posFacil = positions[0];
            hotspotFacil.setClickable(true);
            hotspotFacil.setEnabled(true);
            hotspotFacil.setFocusable(true);
            hotspotFacil.setFocusableInTouchMode(true);
            hotspotFacil.setVisibility(View.VISIBLE);
            
            hotspotFacil.post(() -> {
                try {
                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params = 
                        (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) hotspotFacil.getLayoutParams();
                    
                    if (params != null) {
                        if (params.endToEnd == androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET) {
                            params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                        }
                        if (params.startToStart == androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET) {
                            params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                        }
                        
                        params.horizontalBias = posFacil.horizontalBias;
                        params.verticalBias = posFacil.verticalBias;
                        
                        float density = getResources().getDisplayMetrics().density;
                        params.width = (int) (posFacil.width * density);
                        params.height = (int) (posFacil.height * density);
                        
                        hotspotFacil.setElevation(20f * density);
                        hotspotFacil.setLayoutParams(params);
                        hotspotFacil.bringToFront();
                        hotspotFacil.setBackground(createCircularGreenDrawable());
                        hotspotFacil.requestLayout();
                        // Aplicar animación de pulso
                        aplicarAnimacionPulso(hotspotFacil);
                    }
                } catch (Exception e) {
                    android.util.Log.e("MapaActivity", "Error aplicando posición hotspot FÁCIL", e);
                }
                
                hotspotFacil.setOnClickListener(v -> {
                    android.util.Log.d("MapaActivity", "=== CLICK EN HOTSPOT FÁCIL ===");
                    // Iniciar directamente el simulacro en modo FÁCIL
                    iniciarSimulacroConocimiento("facil");
                });
            });
        }
        
        // Configurar hotspot DIFÍCIL
        View hotspotDificil = findViewById(R.id.hotspotConocimientoDificil);
        if (hotspotDificil != null && positions.length > 1) {
            final HotspotPosition posDificil = positions[1];
            hotspotDificil.setClickable(true);
            hotspotDificil.setEnabled(true);
            hotspotDificil.setFocusable(true);
            hotspotDificil.setFocusableInTouchMode(true);
            hotspotDificil.setVisibility(View.VISIBLE);
            
            hotspotDificil.post(() -> {
                try {
                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params = 
                        (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) hotspotDificil.getLayoutParams();
                    
                    if (params != null) {
                        if (params.endToEnd == androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET) {
                            params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                        }
                        if (params.startToStart == androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET) {
                            params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                        }
                        
                        params.horizontalBias = posDificil.horizontalBias;
                        params.verticalBias = posDificil.verticalBias;
                        
                        float density = getResources().getDisplayMetrics().density;
                        params.width = (int) (posDificil.width * density);
                        params.height = (int) (posDificil.height * density);
                        
                        hotspotDificil.setElevation(20f * density);
                        hotspotDificil.setLayoutParams(params);
                        hotspotDificil.bringToFront();
                        hotspotDificil.setBackground(createCircularRedDrawable());
                        hotspotDificil.requestLayout();
                        // Aplicar animación de pulso
                        aplicarAnimacionPulso(hotspotDificil);
                    }
                } catch (Exception e) {
                    android.util.Log.e("MapaActivity", "Error aplicando posición hotspot DIFÍCIL", e);
                }
                
                hotspotDificil.setOnClickListener(v -> {
                    android.util.Log.d("MapaActivity", "=== CLICK EN HOTSPOT DIFÍCIL ===");
                    // Iniciar directamente el simulacro en modo DIFÍCIL
                    iniciarSimulacroConocimiento("dificil");
                });
            });
        }
    }

    private void vincularTextViewAlHotspot(TextView tvExamenFinal, View hotspotExamenFinal) {
        if (tvExamenFinal == null || hotspotExamenFinal == null || tvExamenFinal.getParent() == null) {
            return;
        }
        
        try {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams tvParams = 
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) tvExamenFinal.getLayoutParams();
            
            if (tvParams == null) {
                tvParams = new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT);
            }
            
            // CRÍTICO: Vincular el TextView directamente al hotspot (no al parent)
            // Esto hará que el TextView esté centrado DENTRO del hotspot
            tvParams.startToStart = hotspotExamenFinal.getId();
            tvParams.endToEnd = hotspotExamenFinal.getId();
            tvParams.topToTop = hotspotExamenFinal.getId();
            tvParams.bottomToBottom = hotspotExamenFinal.getId();
            
            // Centrar horizontal y verticalmente dentro del hotspot
            tvParams.horizontalBias = 0.5f; // Centrado horizontal
            tvParams.verticalBias = 0.5f;   // Centrado vertical
            
            // Sin márgenes para que quede perfectamente centrado
            tvParams.setMargins(0, 0, 0, 0);
            
            // Asegurar que el texto esté centrado dentro del TextView
            tvExamenFinal.setGravity(android.view.Gravity.CENTER);
            tvExamenFinal.setTextAlignment(android.view.View.TEXT_ALIGNMENT_CENTER);
            
            // Aplicar layout params
            tvExamenFinal.setLayoutParams(tvParams);
            
            // Elevación más alta para estar por encima del hotspot
            float density = getResources().getDisplayMetrics().density;
            tvExamenFinal.setElevation(25f * density);
            tvExamenFinal.bringToFront();
            
            // Forzar actualización del layout
            tvExamenFinal.requestLayout();
            hotspotExamenFinal.requestLayout();
            
            android.util.Log.d("MapaActivity", "✓ TextView 'Examen Final' vinculado y centrado dentro del hotspot");
        } catch (Exception e) {
            android.util.Log.e("MapaActivity", "Error al vincular TextView al hotspot", e);
        }
    }

    /**
     * Configura los click listeners para el examen final
     */
    private void configurarClickListenersExamenFinal(View hotspotExamenFinal, Subject subject, String userId, boolean isUnlocked) {
        android.util.Log.d("MapaActivity", "=== CONFIGURANDO CLICK LISTENERS PARA EXAMEN FINAL ===");
        
        // Asegurar propiedades
        hotspotExamenFinal.setClickable(true);
        hotspotExamenFinal.setEnabled(true);
        hotspotExamenFinal.setFocusable(true);
        hotspotExamenFinal.setFocusableInTouchMode(true);
        hotspotExamenFinal.setVisibility(View.VISIBLE);
        hotspotExamenFinal.bringToFront();
        
        // Configurar OnClickListener
        hotspotExamenFinal.setOnClickListener(v -> {
            android.util.Log.d("MapaActivity", "=== CLICK EN EXAMEN FINAL ===");
            
            // Verificar nuevamente si está desbloqueado
            int currentUnlockedLevel = ProgressLockManager.getUnlockedLevel(this, userId, subject.title);
            boolean currentIsUnlocked = currentUnlockedLevel >= 6;
            android.util.Log.d("MapaActivity", "Verificación en tiempo real - Nivel desbloqueado: " + currentUnlockedLevel + ", Examen Final desbloqueado: " + currentIsUnlocked);
            
            if (!currentIsUnlocked) {
                android.util.Log.d("MapaActivity", "Examen Final está bloqueado");
                android.widget.Toast.makeText(this, 
                    "Examen Final bloqueado. Completa el nivel 5 primero.", 
                    android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            // Abrir SimulacroActivity para el examen final
            try {
                // Construir la lista de subtemas desde los niveles
                java.util.ArrayList<String> subtemas = new java.util.ArrayList<>();
                if (subject.levels != null) {
                    for (Level lvl : subject.levels) {
                        if (lvl.subtopics != null && !lvl.subtopics.isEmpty()) {
                            String st = lvl.subtopics.get(0).title;
                            if (st != null && !st.trim().isEmpty()) {
                                subtemas.add(st);
                            }
                        }
                    }
                }

                Intent intent = new Intent(this, SimulacroActivity.class);
                intent.putExtra("area", subject.title);
                intent.putStringArrayListExtra("subtemas", subtemas);
                android.util.Log.d("MapaActivity", "✓ Abriendo SimulacroActivity - Área: " + subject.title + ", Subtemas: " + subtemas.size());
                startActivity(intent);
            } catch (Exception e) {
                android.util.Log.e("MapaActivity", "ERROR al abrir SimulacroActivity", e);
                android.widget.Toast.makeText(this, "Error al abrir examen final: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        
        // Configurar OnTouchListener como respaldo
        hotspotExamenFinal.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            if (action == android.view.MotionEvent.ACTION_DOWN) {
                android.util.Log.d("MapaActivity", "=== TOUCH DOWN en Examen Final ===");
            } else if (action == android.view.MotionEvent.ACTION_UP) {
                android.util.Log.d("MapaActivity", "=== TOUCH UP en Examen Final ===");
                // También manejar desde touch como respaldo
                v.performClick();
                return true;
            }
            return false;
        });
        
        android.util.Log.d("MapaActivity", "✓ Click listeners configurados para Examen Final");
    }


    private void configurarClickListenersHotspot(View hotspot, int nivelNumero, Subject subject, String userId, String subtema, boolean isUnlocked) {
        android.util.Log.d("MapaActivity", "=== CONFIGURANDO LISTENERS PARA NIVEL " + nivelNumero + " ===");
        android.util.Log.d("MapaActivity", "Área: " + subject.title + ", UserId: " + userId);
        
        // Asegurar propiedades básicas del hotspot
        hotspot.setClickable(true);
        hotspot.setEnabled(true);
        hotspot.setFocusable(true);
        hotspot.setFocusableInTouchMode(true);
        hotspot.setVisibility(View.VISIBLE);
        
        // Traer al frente
        hotspot.bringToFront();
        
        // CRÍTICO: Hacer copias finales de todas las variables para usar en lambdas
        final Subject finalSubject = subject;
        final int finalNivelNumero = nivelNumero;
        final String finalUserId = userId;
        final String finalSubtema = subtema;
        
        // Configurar OnClickListener principal
        // IMPORTANTE: La verificación de desbloqueo se hace DENTRO de manejarClickNivel
        hotspot.setOnClickListener(v -> {
            android.util.Log.d("MapaActivity", "=== CLICK DETECTADO en nivel " + finalNivelNumero + " ===");
            android.util.Log.d("MapaActivity", "Área: " + finalSubject.title);
            manejarClickNivel(finalNivelNumero, finalSubject, finalUserId, finalSubtema);
        });
        
        // Configurar OnTouchListener como respaldo (especialmente útil para niveles problemáticos)
        hotspot.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                android.util.Log.d("MapaActivity", "=== TOUCH UP DETECTADO en nivel " + finalNivelNumero + " ===");
                android.util.Log.d("MapaActivity", "Área: " + finalSubject.title);
                manejarClickNivel(finalNivelNumero, finalSubject, finalUserId, finalSubtema);
                return true;
            }
            return false;
        });
        
        // Aplicar color según estado de desbloqueo
        if (isUnlocked) {
            // Nivel desbloqueado: hotspot invisible/transparente (sin fondo)
            hotspot.setBackground(null);
            hotspot.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            android.util.Log.d("MapaActivity", "✓ Listeners configurados para nivel " + nivelNumero + " (desbloqueado - transparente)");
        } else {
            // Nivel bloqueado: hotspot visible con fondo gris oscuro
            hotspot.setBackground(createCircularGrayDrawable());
            android.util.Log.d("MapaActivity", "✓ Listeners configurados para nivel " + nivelNumero + " (bloqueado - gris)");
        }
    }
    private volatile boolean procesandoClick = false; // Prevenir múltiples llamadas simultáneas
    
    private void manejarClickNivel(int nivelNumero, Subject subject, String userId, String subtema) {
        // Prevenir múltiples llamadas simultáneas
        if (procesandoClick) {
            android.util.Log.w("MapaActivity", "Ya hay un click en proceso, ignorando este");
            return;
        }
        
        procesandoClick = true;
        android.util.Log.d("MapaActivity", "========================================");
        android.util.Log.d("MapaActivity", "=== manejarClickNivel llamado ===");
        android.util.Log.d("MapaActivity", "Nivel: " + nivelNumero);
        android.util.Log.d("MapaActivity", "Área: '" + subject.title + "'");
        android.util.Log.d("MapaActivity", "UserId: '" + userId + "'");
        android.util.Log.d("MapaActivity", "========================================");
        
        // CRÍTICO: Validar que userId y subject.title no sean nulos o vacíos
        if (userId == null || userId.trim().isEmpty()) {
            android.util.Log.e("MapaActivity", "ERROR: userId es nulo o vacío");
            android.widget.Toast.makeText(this, "Error: Usuario no identificado", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (subject == null || subject.title == null || subject.title.trim().isEmpty()) {
            android.util.Log.e("MapaActivity", "ERROR: subject o subject.title es nulo o vacío");
            android.widget.Toast.makeText(this, "Error: Área no identificada", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        // CRÍTICO: Verificar el nivel desbloqueado actual para esta área (igual que Ciencias)
        // IMPORTANTE: Usar el título exacto del subject tal como viene de DemoData
        String areaTitle = subject.title.trim();
        android.util.Log.d("MapaActivity", "Verificando desbloqueo para área: '" + areaTitle + "'");
        
        int unlockedLevel = ProgressLockManager.getUnlockedLevel(this, userId, areaTitle);
        android.util.Log.d("MapaActivity", "Nivel máximo desbloqueado para área '" + areaTitle + "': " + unlockedLevel);
        
        // Verificar si este nivel específico está desbloqueado (igual que en Ciencias)
        boolean isUnlocked = ProgressLockManager.isLevelUnlocked(this, userId, areaTitle, nivelNumero);
        android.util.Log.d("MapaActivity", "Verificación - Nivel " + nivelNumero + " desbloqueado: " + isUnlocked);
        android.util.Log.d("MapaActivity", "Comparación: nivelNumero (" + nivelNumero + ") <= unlockedLevel (" + unlockedLevel + ") = " + isUnlocked);
        
        // CRÍTICO: Si NO está desbloqueado, mostrar mensaje y NO abrir (igual que Ciencias)
        // ESTA ES LA VERIFICACIÓN QUE DEBE BLOQUEAR EL ACCESO
        if (!isUnlocked) {
            android.util.Log.w("MapaActivity", "⚠⚠⚠ Nivel " + nivelNumero + " está BLOQUEADO - ACCESO DENEGADO ⚠⚠⚠");
            android.util.Log.w("MapaActivity", "unlockedLevel = " + unlockedLevel + ", nivelNumero = " + nivelNumero);
            android.util.Log.w("MapaActivity", "isUnlocked = " + isUnlocked);
            android.widget.Toast.makeText(this, 
                "Nivel " + nivelNumero + " bloqueado. Completa el nivel anterior primero.", 
                android.widget.Toast.LENGTH_LONG).show();
            // CRÍTICO: Retornar INMEDIATAMENTE sin abrir QuizActivity
            android.util.Log.w("MapaActivity", "RETORNANDO - NO se abrirá QuizActivity");
            procesandoClick = false; // Liberar el flag antes de retornar
            return; // IMPORTANTE: Salir sin abrir QuizActivity - ESTO DEBE FUNCIONAR
        }
        
        // Verificación adicional: Si llegamos aquí, el nivel DEBE estar desbloqueado
        android.util.Log.d("MapaActivity", "✓ Verificación pasada - Nivel " + nivelNumero + " está desbloqueado");

        // Solo abrir si está desbloqueado (igual que Ciencias)
        android.util.Log.d("MapaActivity", "✓✓✓ Nivel " + nivelNumero + " está desbloqueado - Abriendo QuizActivity ✓✓✓");
        try {
            Intent intent = new Intent(this, QuizActivity.class);
            intent.putExtra(QuizActivity.EXTRA_AREA, subject.title);
            intent.putExtra(QuizActivity.EXTRA_SUBTEMA, subtema);
            intent.putExtra(QuizActivity.EXTRA_NIVEL, nivelNumero);
            android.util.Log.d("MapaActivity", "✓ Abriendo QuizActivity - Área: " + subject.title + ", Subtema: " + subtema + ", Nivel: " + nivelNumero);
            startActivity(intent);
        } catch (Exception e) {
            android.util.Log.e("MapaActivity", "ERROR al abrir QuizActivity", e);
            android.widget.Toast.makeText(this, "Error al abrir nivel: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        } finally {
            // Liberar el flag después de procesar
            procesandoClick = false;
        }
    }
    
    /**
     * Inicia directamente el simulacro de la Isla del Conocimiento con la modalidad especificada
     * @param modalidad "facil" o "dificil"
     */
    private void iniciarSimulacroConocimiento(String modalidad) {
        android.util.Log.d("MapaActivity", "=== INICIANDO SIMULACRO CONOCIMIENTO ===");
        android.util.Log.d("MapaActivity", "Modalidad: " + modalidad);
        
        // Mostrar mensaje de carga
        android.widget.Toast.makeText(this, "Iniciando simulacro en modo " + 
            ("facil".equals(modalidad) ? "Fácil" : "Difícil") + "...", 
            android.widget.Toast.LENGTH_SHORT).show();
        
        // Llamar a la API para iniciar el simulacro
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.iniciarIslaSimulacro(new IslaSimulacroRequest(modalidad)).enqueue(new Callback<IslaSimulacroResponse>() {
            @Override
            public void onResponse(Call<IslaSimulacroResponse> call, Response<IslaSimulacroResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    android.util.Log.e("MapaActivity", "Error al iniciar simulacro: " + response.code());
                    android.widget.Toast.makeText(MapaActivity.this, 
                        "No se pudo iniciar el simulacro (" + response.code() + ")", 
                        android.widget.Toast.LENGTH_LONG).show();
                    return;
                }
                
                // Convertir respuesta a JSON y abrir la actividad de preguntas
                String payload = GsonHolder.gson().toJson(response.body());
                Intent intent = new Intent(MapaActivity.this, IslaPreguntasActivity.class);
                intent.putExtra("modalidad", modalidad);
                intent.putExtra("payload", payload);
                android.util.Log.d("MapaActivity", "Abriendo IslaPreguntasActivity con modalidad: " + modalidad);
                startActivity(intent);
            }
            
            @Override
            public void onFailure(Call<IslaSimulacroResponse> call, Throwable t) {
                android.util.Log.e("MapaActivity", "Error de red al iniciar simulacro", t);
                android.widget.Toast.makeText(MapaActivity.this, 
                    "Error de red: " + (t.getMessage() != null ? t.getMessage() : "desconocido"), 
                    android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }
}

