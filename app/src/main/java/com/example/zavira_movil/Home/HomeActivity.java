package com.example.zavira_movil.Home;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.example.zavira_movil.Perfil.ProfileActivity;
import com.example.zavira_movil.R;
import com.example.zavira_movil.databinding.ActivityHomeBinding;
import com.example.zavira_movil.model.KolbResultado;
import com.example.zavira_movil.niveleshome.DemoData;

import java.util.List;
import com.example.zavira_movil.progreso.DiagnosticoInicial;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.example.zavira_movil.ui.ranking.RankingLogrosFragment;
import com.example.zavira_movil.ui.ranking.progreso.ProgresoFragment;
import com.example.zavira_movil.ui.ranking.progreso.RetosFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.bumptech.glide.Glide;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private boolean testKolbCompletado = true; // Inicializar como true para evitar bloqueos prematuras
    private boolean diagnosticoInicialCompletado = true; // Inicializar como true para evitar bloqueos prematuras
    private boolean isRetosActive = false; // Track si Retos está activo

    private ActivityResultLauncher<Intent> launcher;

    private static final Fragment BLANK_FRAGMENT = new Fragment();

    private android.content.BroadcastReceiver syncReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Inicializar ActivityResultLauncher
        launcher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        // Ya no necesitamos actualizar adapter porque no hay RecyclerView
                        // Si necesitamos refrescar algo en el futuro, lo haremos aquí
                    }
        );

        // IMPORTANTE: Configurar la ventana para que podamos controlar el color de la status bar
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // ESTABLECER EL COLOR AZUL ANTES de inflar el layout
        final int azulStatusBar = android.graphics.Color.parseColor("#3988FF");
        getWindow().setStatusBarColor(azulStatusBar);
        android.util.Log.d("HomeActivity", "Color azul establecido ANTES de setContentView: #3988FF");

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // NO usar EdgeToEdge.enable() porque está sobrescribiendo el color
        // En su lugar, configurar manualmente los insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            // Aplicar padding solo donde sea necesario, pero mantener el color azul
            // FORZAR el color azul cada vez que se aplican insets
            getWindow().setStatusBarColor(azulStatusBar);
            return insets;
        });
        
        // FORZAR el color azul INMEDIATAMENTE después de setContentView
        getWindow().setStatusBarColor(azulStatusBar);
        int colorDespuesSetContent = getWindow().getStatusBarColor();
        android.util.Log.d("HomeActivity", "Color DESPUÉS de setContentView: " + String.format("#%08X", colorDespuesSetContent) + 
            " (esperado: #3988FF)");
        
        // Usar WindowInsetsController para establecer el color (Android 11+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            androidx.core.view.WindowInsetsControllerCompat windowInsetsController = 
                androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            if (windowInsetsController != null) {
                getWindow().setStatusBarColor(azulStatusBar);
                windowInsetsController.setAppearanceLightStatusBars(false); // Texto blanco sobre fondo azul
            }
        }
        
        // Usar ViewTreeObserver para forzar el color cuando la ventana esté completamente lista
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Forzar el color azul cada vez que el layout cambia
                if (!isRetosActive) {
                    int currentColor = getWindow().getStatusBarColor();
                    if (currentColor != azulStatusBar) {
                        getWindow().setStatusBarColor(azulStatusBar);
                        android.util.Log.d("HomeActivity", "ViewTreeObserver: Color corregido de " + 
                            String.format("#%08X", currentColor) + " a #3988FF");
                    }
                }
            }
        });
        
        // Configurar status bar con color azul (#3988FF)
        setupStatusBarColor(false);
        
        // Usar Handler para forzar el color azul de manera continua y verificar que se mantenga
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        Runnable forceBlueColor = new Runnable() {
            @Override
            public void run() {
                if (!isRetosActive) {
                    int currentColor = getWindow().getStatusBarColor();
                    getWindow().setStatusBarColor(azulStatusBar);
                    
                    // Verificar si el color realmente cambió
                    int newColor = getWindow().getStatusBarColor();
                    if (newColor != azulStatusBar) {
                        android.util.Log.e("HomeActivity", "ERROR: Color no se estableció correctamente. Esperado: #3988FF, Actual: " + 
                            String.format("#%08X", newColor));
                    } else if (currentColor != azulStatusBar) {
                        android.util.Log.d("HomeActivity", "Color corregido: " + String.format("#%08X", currentColor) + " -> " + 
                            String.format("#%08X", azulStatusBar));
                    }
                    
                    // Programar siguiente verificación en 50ms (más frecuente)
                    handler.postDelayed(this, 50);
                }
            }
        };
        handler.postDelayed(forceBlueColor, 50);
        
        // También forzar en múltiples momentos específicos
        getWindow().getDecorView().post(() -> {
            getWindow().setStatusBarColor(azulStatusBar);
            android.util.Log.d("HomeActivity", "Forzando color azul en post: #3988FF");
        });
        
        getWindow().getDecorView().postDelayed(() -> {
            getWindow().setStatusBarColor(azulStatusBar);
            android.util.Log.d("HomeActivity", "Forzando color azul en postDelayed 50ms: #3988FF");
        }, 50);
        
        getWindow().getDecorView().postDelayed(() -> {
            getWindow().setStatusBarColor(azulStatusBar);
            android.util.Log.d("HomeActivity", "Forzando color azul en postDelayed 200ms: #3988FF");
        }, 200);
        
        getWindow().getDecorView().postDelayed(() -> {
            getWindow().setStatusBarColor(azulStatusBar);
            android.util.Log.d("HomeActivity", "Forzando color azul en postDelayed 500ms: #3988FF");
        }, 500);
        
        // La barra de navegación del sistema se mantiene visible (comportamiento por defecto)
        // No se modifica para que muestre la hora, batería, etc. normalmente
        
        // IMPORTANTE: NO modificar el padding del header para que se mantenga fijo
        // El header debe tener un paddingTop fijo (32dp) y no cambiar cuando se cambia de pestaña
        View headerContainer = findViewById(R.id.header_container);
        if (headerContainer != null) {
            // NO aplicar listener de WindowInsets para mantener el header fijo
            // El paddingTop de 32dp en el XML es suficiente
        }
        
        // IMPORTANTE: Configurar el contenedor de fragments
        // Cuando el header está oculto, el fragment debe empezar desde el top (sin padding)
        FrameLayout fragmentContainer = findViewById(R.id.fragmentContainer);
        if (fragmentContainer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(fragmentContainer, (v, insets) -> {
                // Verificar si el header está visible
                View header = findViewById(R.id.header_container);
                // Verificar si el fragment actual es FragmentDetalleSimulacro
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                boolean isDetalleSimulacro = currentFragment instanceof com.example.zavira_movil.detalleprogreso.FragmentDetalleSimulacro;
                
                if (header != null && header.getVisibility() == View.VISIBLE) {
                    // Si el header está visible (Home), el fragment no debe tener padding
                    // porque el header ya maneja los insets
                    v.setPadding(0, 0, 0, 0);
                } else if (isDetalleSimulacro) {
                    // Si es FragmentDetalleSimulacro, mantener el paddingTop para bajar el contenido
                    int paddingTop = dp(40);
                    v.setPadding(0, paddingTop, 0, 0);
                } else {
                    // Si el header está oculto (Retos, Progreso, Logros), el fragment
                    // debe empezar desde el top sin padding, para que el contenido del fragment
                    // maneje sus propios insets
                    v.setPadding(0, 0, 0, 0);
                }
                return insets;
            });
        }
        
        // Hacer el avatar circular y agregar click listener para navegar a Perfil
        ImageView ivAvatar = findViewById(R.id.ivAvatar);
        if (ivAvatar != null) {
            ivAvatar.post(() -> {
                ivAvatar.setClipToOutline(true);
                ivAvatar.setOutlineProvider(new android.view.ViewOutlineProvider() {
                    @Override
                    public void getOutline(android.view.View view, android.graphics.Outline outline) {
                        outline.setOval(0, 0, view.getWidth(), view.getHeight());
                    }
                });
            });
            // Agregar click listener para navegar a ProfileActivity
            ivAvatar.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            });
        }
        
        // Configurar click listener para el ícono de notificaciones
        ImageView ivNotifications = findViewById(R.id.ivNotifications);
        if (ivNotifications != null) {
            ivNotifications.setOnClickListener(v -> {
                Intent intentNotifications = new Intent(HomeActivity.this, NotificationsActivity.class);
                startActivity(intentNotifications);
            });
        }

        // Configurar badge de notificaciones no leídas
        updateNotificationBadge();

        // Registrar receiver para actualizar el badge de notificaciones
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this)
            .registerReceiver(new android.content.BroadcastReceiver() {
                @Override
                public void onReceive(android.content.Context context, android.content.Intent intent) {
                    updateNotificationBadge();
                }
            }, new android.content.IntentFilter("com.example.zavira_movil.UPDATE_NOTIFICATION_BADGE"));

        // Registrar receiver para sincronización (solo si no está registrado)
        if (syncReceiver == null) {
            syncReceiver = new android.content.BroadcastReceiver() {
                @Override
                public void onReceive(android.content.Context context, android.content.Intent intent) {
                    if (intent != null) {
                        if ("com.example.zavira_movil.SYNC_COMPLETED".equals(intent.getAction())) {
                            android.util.Log.d("HomeActivity", "=== Broadcast SYNC_COMPLETED recibido ===");
                            // Ya no hay RecyclerView que actualizar, pero podemos hacer otras acciones si es necesario
                            runOnUiThread(() -> {
                                android.util.Log.d("HomeActivity", "Sincronización completada - islas disponibles");
                            });
                        } else if ("com.example.zavira_movil.FOTO_ACTUALIZADA".equals(intent.getAction())) {
                            // Recargar foto del usuario cuando se actualiza
                            runOnUiThread(() -> {
                                // Limpiar cache de Glide para forzar recarga
                                ImageView ivAvatar = findViewById(R.id.ivAvatar);
                                if (ivAvatar != null) {
                                    Glide.with(HomeActivity.this).clear(ivAvatar);
                                }
                                // Recargar foto
                                cargarNombreUsuario();
                            });
                        }
                    }
                }
            };
            try {
                android.content.IntentFilter filter = new android.content.IntentFilter();
                filter.addAction("com.example.zavira_movil.SYNC_COMPLETED");
                filter.addAction("com.example.zavira_movil.FOTO_ACTUALIZADA");

                // Android 13+ requiere especificar RECEIVER_EXPORTED o RECEIVER_NOT_EXPORTED
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    registerReceiver(syncReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
                } else {
                    registerReceiver(syncReceiver, filter);
                }

                android.util.Log.d("HomeActivity", "BroadcastReceiver registrado para SYNC_COMPLETED y FOTO_ACTUALIZADA");
            } catch (Exception e) {
                android.util.Log.e("HomeActivity", "Error al registrar BroadcastReceiver", e);
            }
        }

        // Cargar nombre del usuario en el header
        cargarNombreUsuario();
        
        // IMPORTANTE: Asumir que ambos tests están completados inicialmente
        // Esto evita que se muestre el mensaje de bloqueo antes de verificar el backend
        testKolbCompletado = true;
        diagnosticoInicialCompletado = true;
        
        // Configurar click listeners para las islas DESPUÉS de que las vistas estén infladas
        // Usar post para asegurar que las vistas estén completamente renderizadas
        binding.getRoot().post(() -> {
            android.util.Log.d("HomeActivity", "=== CONFIGURANDO HOTSPOTS DESPUÉS DE INFLAR VISTAS ===");
            setupIslandsClickListeners();
        });

        // Nota: El efecto parallax fue eliminado porque el header ahora es azul sólido (#44B7CB)
        // Nota: btnBell e ivBell fueron eliminados del layout

        // Bottom navigation - habilitado por defecto, solo se bloquea si la verificación confirma que falta algo
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            
            // Solo verificar si la verificación del backend confirmó que falta algo
            if (!testKolbCompletado) {
                // Redirigir silenciosamente al test de Kolb (sin mostrar mensaje)
                Intent intentKolb = new Intent(HomeActivity.this, com.example.zavira_movil.InfoTestActivity.class);
                intentKolb.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intentKolb);
                finish();
                return false;
            }
            if (!diagnosticoInicialCompletado) {
                // Redirigir silenciosamente al diagnóstico (sin mostrar mensaje)
                Intent intentDiag = new Intent(HomeActivity.this, com.example.zavira_movil.InfoAcademico.class);
                intentDiag.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intentDiag);
                finish();
                return false;
            }
            
            // Si ambos están completos, permitir la acción
            return false;
        });

        // Verificar si se debe mostrar el detalle del simulacro
        Intent intent = getIntent();
        if (intent != null && "show_detalle".equals(intent.getStringExtra("action"))) {
            int idSesion = intent.getIntExtra("id_sesion", 0);
            if (idSesion > 0) {
                // Mostrar fragment de detalle
                Bundle bundle = new Bundle();
                bundle.putInt("id_sesion", idSesion);
                bundle.putString("materia", intent.getStringExtra("materia"));
                bundle.putInt("initial_tab", intent.getIntExtra("initial_tab", 0));
                
                com.example.zavira_movil.detalleprogreso.FragmentDetalleSimulacro fragment = 
                    new com.example.zavira_movil.detalleprogreso.FragmentDetalleSimulacro();
                fragment.setArguments(bundle);
                
                // Navegar a la pestaña de Progreso primero
                binding.bottomNav.setSelectedItemId(R.id.nav_progreso);
                // Mostrar el fragment de detalle
                show(fragment);
                applyTabVisibility(false, false);
            }
        } else {
            // Pestaña por defecto: Islas
            if (savedInstanceState == null) {
                // Asegurar que la imagen de fondo esté visible solo en Home
                ImageView mapBackground = findViewById(R.id.mapBackground);
                if (mapBackground != null) {
                    mapBackground.setVisibility(View.VISIBLE);
                }
                show(BLANK_FRAGMENT);
                applyTabVisibility(true, false);
                binding.bottomNav.setSelectedItemId(R.id.nav_islas);
            }
        }
        
        // CRÍTICO: Asegurar que UserSession esté sincronizado con TokenManager
        try {
            int userId = com.example.zavira_movil.local.TokenManager.getUserId(this);
            if (userId > 0) {
                // Sincronizar UserSession con TokenManager (fuente única de verdad)
                com.example.zavira_movil.local.UserSession.getInstance().setIdUsuario(userId);
                android.util.Log.d("HomeActivity", "UserSession sincronizado con userId: " + userId);
                
                // Sincronizar progreso desde el backend (niveles y vidas) - esto es independiente
                // Esto se hace primero para que los datos estén disponibles
                com.example.zavira_movil.sincronizacion.ProgresoSincronizador.getInstance()
                    .sincronizarDesdeBackend(this, String.valueOf(userId));
            } else {
                android.util.Log.e("HomeActivity", "ERROR: userId inválido (" + userId + ")");
            }
        } catch (Exception e) {
            android.util.Log.e("HomeActivity", "Error al sincronizar progreso", e);
        }
        
        // Verificar si el usuario completó el test de Kolb PRIMERO (no depende de sincronización)
        // Esto debe hacerse siempre desde el backend, no desde datos locales
        // IMPORTANTE: Asumir que está completado inicialmente para evitar bloqueos mientras se verifica
        testKolbCompletado = true;
        verificarTestKolb();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Recargar foto del usuario en el header
        cargarNombreUsuario();
        
        // Actualizar badge de notificaciones
        updateNotificationBadge();

        // La barra de navegación del sistema se mantiene visible (comportamiento por defecto)
        // No se modifica para que muestre la hora, batería, etc. normalmente
        
        // IMPORTANTE: Verificar qué fragment está visible actualmente
        // Si es RetosFragment, establecer color naranja; de lo contrario, azul
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        boolean isRetosVisible = currentFragment instanceof com.example.zavira_movil.ui.ranking.progreso.RetosFragment;
        
        // Actualizar isRetosActive basado en el fragment visible
        isRetosActive = isRetosVisible;
        
        // Restaurar el color correcto de la status bar según el fragment visible
        // La barra de estado del sistema (hora y batería) DEBE ser azul cuando no está en Retos
        int azulStatusBar = android.graphics.Color.parseColor("#3988FF");
        if (!isRetosActive) {
            // Forzar color azul inmediatamente si no está en Retos
            getWindow().setStatusBarColor(azulStatusBar);
        }
        // Hacer esto DESPUÉS de que los fragments hayan tenido oportunidad de ejecutar su onResume
        getWindow().getDecorView().postDelayed(() -> {
            setupStatusBarColor(isRetosActive);
            if (!isRetosActive) {
                // Asegurar que el color azul se mantenga
                getWindow().setStatusBarColor(azulStatusBar);
            }
        }, 50);
        
        // Nota: topBar fue eliminado del layout, se usa header_container ahora
        
        // Sincronizar niveles desde el backend cuando el usuario vuelve a la app
        // Esto asegura que los niveles desbloqueados estén actualizados
        try {
            int userId = com.example.zavira_movil.local.TokenManager.getUserId(this);
            if (userId > 0) {
                // CRÍTICO: Sincronizar UserSession con TokenManager en cada onResume
                com.example.zavira_movil.local.UserSession.getInstance().setIdUsuario(userId);
                
                // Verificar si ha pasado mucho tiempo desde la última sincronización (más de 5 minutos)
                long lastSync = com.example.zavira_movil.sincronizacion.ProgresoSincronizador.getInstance()
                    .getLastSyncTimestamp(this);
                long now = System.currentTimeMillis();
                long timeSinceLastSync = now - lastSync;
                long fiveMinutes = 5 * 60 * 1000; // 5 minutos en milisegundos
                
                // Sincronizar si no hay timestamp de última sincronización o si pasaron más de 5 minutos
                if (lastSync == 0 || timeSinceLastSync > fiveMinutes) {
                    android.util.Log.d("HomeActivity", "Sincronizando niveles desde backend (última sync: " + 
                        (lastSync == 0 ? "nunca" : (timeSinceLastSync / 1000) + " segundos atrás") + ")");
                    com.example.zavira_movil.sincronizacion.ProgresoSincronizador.getInstance()
                        .sincronizarDesdeBackend(this, String.valueOf(userId));
                }
            } else {
                android.util.Log.e("HomeActivity", "ERROR: userId inválido en onResume (" + userId + ")");
            }
        } catch (Exception e) {
            android.util.Log.e("HomeActivity", "Error al sincronizar progreso en onResume", e);
        }
        
        // Verificar ambos tests al volver a la actividad solo si no están completos
        // Esto asegura que las interacciones estén bloqueadas si falta algún test
        if (!testKolbCompletado || !diagnosticoInicialCompletado) {
            // Si falta algún test, verificar desde cero
            bloquearInteracciones();
            verificarTestKolb();
        }
    }
    
    private void verificarTestKolb() {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        android.util.Log.d("HomeActivity", "Verificando test de Kolb desde el backend...");
        api.obtenerResultado().enqueue(new Callback<KolbResultado>() {
            @Override
            public void onResponse(Call<KolbResultado> call, Response<KolbResultado> response) {
                android.util.Log.d("HomeActivity", "Respuesta Kolb - código: " + response.code() + ", exitoso: " + response.isSuccessful());
                
                // Verificar si el test de Kolb está completado
                if (response.isSuccessful() && response.body() != null) {
                    KolbResultado resultado = response.body();
                    String estilo = resultado.getEstilo();
                    android.util.Log.d("HomeActivity", "Test de Kolb - Estilo recibido: " + (estilo != null ? estilo : "null"));
                    
                    // Si tiene estilo (no null y no vacío), está completado
                    if (estilo != null && !estilo.trim().isEmpty()) {
                        android.util.Log.d("HomeActivity", "Test de Kolb completado. Estilo: " + estilo);
                        testKolbCompletado = true;
                        verificarDiagnosticoInicial();
                    } else {
                        // Si no tiene estilo, verificar si es un 404 o un error
                        if (response.code() == 404) {
                            android.util.Log.d("HomeActivity", "Test de Kolb NO completado (404)");
                            testKolbCompletado = false;
                            bloquearInteracciones();
                        } else {
                            // Otro error - asumir completado para evitar bloqueos
                            android.util.Log.w("HomeActivity", "Test de Kolb - respuesta sin estilo pero código " + response.code() + ". Asumiendo completado.");
                            testKolbCompletado = true;
                            verificarDiagnosticoInicial();
                        }
                    }
                } else if (response.code() == 404) {
                    // 404 significa que definitivamente no ha completado el test
                    android.util.Log.d("HomeActivity", "Test de Kolb NO completado (404)");
                    testKolbCompletado = false;
                    bloquearInteracciones();
                } else {
                    // Otro error (500, etc.) - asumir que está completado si el código no es 404
                    // Esto evita bloquear al usuario por errores temporales del servidor
                    android.util.Log.w("HomeActivity", "Error al verificar Kolb: " + response.code() + ". Asumiendo completado para evitar bloqueo.");
                    testKolbCompletado = true; // Asumir completado para evitar bloqueos por errores del servidor
                    verificarDiagnosticoInicial();
                }
            }
            
            @Override
            public void onFailure(Call<KolbResultado> call, Throwable t) {
                // En caso de error de red, NO bloquear - asumir que está completado
                // Esto evita que errores de conexión bloqueen al usuario
                android.util.Log.e("HomeActivity", "Error de red al verificar Kolb", t);
                android.util.Log.w("HomeActivity", "Asumiendo test de Kolb completado por error de red para evitar bloqueo");
                testKolbCompletado = true; // Asumir completado para no bloquear por errores de red
                verificarDiagnosticoInicial();
            }
        });
    }
    
    private void verificarDiagnosticoInicial() {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        android.util.Log.d("HomeActivity", "Verificando diagnóstico inicial desde el backend...");
        api.diagnosticoProgreso().enqueue(new Callback<DiagnosticoInicial>() {
            @Override
            public void onResponse(Call<DiagnosticoInicial> call, Response<DiagnosticoInicial> response) {
                android.util.Log.d("HomeActivity", "Respuesta Diagnóstico - código: " + response.code() + ", exitoso: " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    DiagnosticoInicial diagnostico = response.body();
                    android.util.Log.d("HomeActivity", "Diagnóstico tieneDiagnostico: " + diagnostico.tieneDiagnostico);
                    if (diagnostico.tieneDiagnostico) {
                        // Ya completó el diagnóstico inicial, permitir todas las interacciones
                        android.util.Log.d("HomeActivity", "Diagnóstico inicial completado");
                        diagnosticoInicialCompletado = true;
                        habilitarInteracciones();
                    } else {
                        // No ha completado el diagnóstico inicial, bloquear interacciones
                        android.util.Log.d("HomeActivity", "Diagnóstico inicial NO completado");
                        diagnosticoInicialCompletado = false;
                        bloquearInteracciones();
                    }
                } else if (response.code() == 404) {
                    // 404 significa que definitivamente no ha completado el diagnóstico
                    android.util.Log.d("HomeActivity", "Diagnóstico inicial NO completado (404)");
                    diagnosticoInicialCompletado = false;
                    bloquearInteracciones();
                } else {
                    // Otro error (500, etc.) - asumir que está completado si el código no es 404
                    android.util.Log.w("HomeActivity", "Error al verificar diagnóstico: " + response.code() + ". Asumiendo completado para evitar bloqueo.");
                    diagnosticoInicialCompletado = true; // Asumir completado para evitar bloqueos por errores del servidor
                    habilitarInteracciones();
                }
            }
            
            @Override
            public void onFailure(Call<DiagnosticoInicial> call, Throwable t) {
                // En caso de error de red, no bloquear - asumir que está completado
                // Esto evita que errores de conexión bloqueen al usuario
                android.util.Log.e("HomeActivity", "Error de red al verificar diagnóstico", t);
                android.util.Log.w("HomeActivity", "Asumiendo diagnóstico completado por error de red");
                diagnosticoInicialCompletado = true; // Asumir completado para no bloquear por errores de red
                habilitarInteracciones();
            }
        });
    }
    
    private void bloquearInteracciones() {
        // Verificar nuevamente antes de bloquear - puede que la verificación haya fallado
        // Solo bloquear si realmente no está completado (404 confirmado)
        android.util.Log.d("HomeActivity", "bloquearInteracciones() - testKolbCompletado: " + testKolbCompletado + ", diagnosticoInicialCompletado: " + diagnosticoInicialCompletado);
        
        // Deshabilitar clicks en las islas
        setupIslandsClickListeners();
    }
    
    private void habilitarInteracciones() {
        // SOLO habilitar si AMBOS tests están completos
        if (!testKolbCompletado || !diagnosticoInicialCompletado) {
            // Si falta algún test, mantener bloqueado
            bloquearInteracciones();
            return;
        }
        
        // Restaurar interacciones normales en las islas
        setupIslandsClickListeners();
        
        setupBottomNav(binding.bottomNav);
    }
    
    /**
     * Configura los click listeners para los hotspots invisibles sobre el fondo
     * Cada hotspot abre el mapa completo de su materia correspondiente (MapaActivity)
     * El hotspot del Conocimiento abre el mapa de Conocimiento donde están los hotspots de FÁCIL y DIFÍCIL
     */
    private void setupIslandsClickListeners() {
        // Obtener las materias de DemoData una vez - hacerla final para usar en lambdas
        final List<com.example.zavira_movil.model.Subject> subjects = DemoData.getSubjects();
        
        // Buscar cada materia y hacerla final para usar en lambdas
        com.example.zavira_movil.model.Subject subjectMatematicas = null;
        com.example.zavira_movil.model.Subject subjectLectura = null;
        com.example.zavira_movil.model.Subject subjectSociales = null;
        com.example.zavira_movil.model.Subject subjectCiencias = null;
        com.example.zavira_movil.model.Subject subjectIngles = null;
        
        for (com.example.zavira_movil.model.Subject s : subjects) {
            String title = s.title.toLowerCase();
            android.util.Log.d("HomeActivity", "Buscando materia: " + s.title);
            if (title.contains("matem")) {
                subjectMatematicas = s;
                android.util.Log.d("HomeActivity", "Matemáticas encontrada: " + s.title);
            } else if (title.contains("lectura") || title.contains("critica")) {
                subjectLectura = s;
                android.util.Log.d("HomeActivity", "Lectura encontrada: " + s.title);
            } else if (title.contains("social") || title.contains("ciudad")) {
                subjectSociales = s;
                android.util.Log.d("HomeActivity", "Sociales encontrada: " + s.title);
            } else if (title.contains("cien") || title.contains("natural")) {
                subjectCiencias = s;
                android.util.Log.d("HomeActivity", "Ciencias encontrada: " + s.title);
            } else if (title.contains("ingl")) {
                subjectIngles = s;
                android.util.Log.d("HomeActivity", "Inglés encontrada: " + s.title);
            }
        }
        
        // Verificar que Ciencias se encontró correctamente
        if (subjectCiencias == null) {
            android.util.Log.e("HomeActivity", "ERROR CRÍTICO: No se encontró la materia de Ciencias en DemoData");
        } else {
            android.util.Log.d("HomeActivity", "Ciencias configurada correctamente: " + subjectCiencias.title);
        }
        
        // Hacer las variables finales para usar en lambdas
        final com.example.zavira_movil.model.Subject finalMatematicas = subjectMatematicas;
        final com.example.zavira_movil.model.Subject finalLectura = subjectLectura;
        final com.example.zavira_movil.model.Subject finalSociales = subjectSociales;
        final com.example.zavira_movil.model.Subject finalCiencias = subjectCiencias;
        final com.example.zavira_movil.model.Subject finalIngles = subjectIngles;
        
        // Hotspot Isla del Conocimiento - Abre el mapa de Conocimiento
        // Crear un Subject temporal para Conocimiento
        final com.example.zavira_movil.model.Subject subjectConocimiento = new com.example.zavira_movil.model.Subject();
        subjectConocimiento.title = "Conocimiento";
        subjectConocimiento.iconRes = R.drawable.ic_science_24; // Usar un icono por defecto
        subjectConocimiento.headerDrawableRes = R.drawable.bg_header_science; // Usar un header por defecto
        subjectConocimiento.done = 0;
        subjectConocimiento.total = 5;
        
        View hotspotConocimiento = findViewById(R.id.hotspotConocimiento);
        android.util.Log.d("HomeActivity", "=== CONFIGURANDO HOTSPOT CONOCIMIENTO ===");
        android.util.Log.d("HomeActivity", "hotspotConocimiento: " + (hotspotConocimiento != null ? "ENCONTRADO" : "NULL"));
        
        if (hotspotConocimiento == null) {
            android.util.Log.e("HomeActivity", "ERROR: hotspotConocimiento no encontrado en el layout");
        }
        
        if (hotspotConocimiento != null) {
            android.util.Log.d("HomeActivity", "✓ Configurando click listener para Conocimiento");
            hotspotConocimiento.setOnClickListener(v -> {
                android.util.Log.d("HomeActivity", "=== CLICK EN HOTSPOT CONOCIMIENTO ===");
                
                if (!testKolbCompletado || !diagnosticoInicialCompletado) {
                    android.util.Log.w("HomeActivity", "Tests no completados");
                    verificarYRedirigir();
                    return;
                }
                
                // Mostrar Dialog informativo
                mostrarDialogInfoConocimiento();
            });
            
            // Asegurar que el hotspot esté completamente habilitado
            hotspotConocimiento.setClickable(true);
            hotspotConocimiento.setEnabled(true);
            hotspotConocimiento.setFocusable(true);
            hotspotConocimiento.setVisibility(View.VISIBLE);
            
            // Aplicar animación de pulso
            aplicarAnimacionPulso(hotspotConocimiento);
            
            // Verificar propiedades del hotspot
            android.util.Log.d("HomeActivity", "Hotspot Conocimiento - Clickable: " + hotspotConocimiento.isClickable());
            android.util.Log.d("HomeActivity", "Hotspot Conocimiento - Enabled: " + hotspotConocimiento.isEnabled());
            android.util.Log.d("HomeActivity", "Hotspot Conocimiento - Visible: " + (hotspotConocimiento.getVisibility() == View.VISIBLE));
        } else {
            android.util.Log.e("HomeActivity", "ERROR: No se pudo configurar hotspot de Conocimiento");
        }
        
        // Hotspot Isla de Matemáticas - Abre el mapa completo
        View hotspotMatematicas = findViewById(R.id.hotspotMatematicas);
        if (hotspotMatematicas != null && finalMatematicas != null) {
            hotspotMatematicas.setOnClickListener(v -> {
                if (!testKolbCompletado || !diagnosticoInicialCompletado) {
                    verificarYRedirigir();
                    return;
                }
                Intent intent = new Intent(HomeActivity.this, com.example.zavira_movil.niveleshome.MapaActivity.class);
                intent.putExtra("subject", finalMatematicas);
                startActivity(intent);
            });
            // Aplicar animación de pulso
            aplicarAnimacionPulso(hotspotMatematicas);
        }
        
        // Hotspot Isla de Lectura - Abre el mapa completo
        View hotspotLectura = findViewById(R.id.hotspotLectura);
        if (hotspotLectura != null && finalLectura != null) {
            hotspotLectura.setOnClickListener(v -> {
                if (!testKolbCompletado || !diagnosticoInicialCompletado) {
                    verificarYRedirigir();
                    return;
                }
                Intent intent = new Intent(HomeActivity.this, com.example.zavira_movil.niveleshome.MapaActivity.class);
                intent.putExtra("subject", finalLectura);
                startActivity(intent);
            });
            // Aplicar animación de pulso
            aplicarAnimacionPulso(hotspotLectura);
        }
        
        // Hotspot Isla de Sociales - Abre el mapa completo
        View hotspotSociales = findViewById(R.id.hotspotSociales);
        if (hotspotSociales != null && finalSociales != null) {
            hotspotSociales.setOnClickListener(v -> {
                if (!testKolbCompletado || !diagnosticoInicialCompletado) {
                    verificarYRedirigir();
                    return;
                }
                Intent intent = new Intent(HomeActivity.this, com.example.zavira_movil.niveleshome.MapaActivity.class);
                intent.putExtra("subject", finalSociales);
                startActivity(intent);
            });
            // Aplicar animación de pulso
            aplicarAnimacionPulso(hotspotSociales);
        }
        
        // Hotspot Isla de Ciencias (ISLA VERDE) - Abre el mapa completo de Ciencias
        View hotspotCiencias = findViewById(R.id.hotspotCiencias);
        android.util.Log.d("HomeActivity", "=== CONFIGURANDO HOTSPOT CIENCIAS ===");
        android.util.Log.d("HomeActivity", "hotspotCiencias: " + (hotspotCiencias != null ? "ENCONTRADO" : "NULL"));
        android.util.Log.d("HomeActivity", "finalCiencias: " + (finalCiencias != null ? finalCiencias.title : "NULL"));
        
        if (hotspotCiencias == null) {
            android.util.Log.e("HomeActivity", "ERROR: hotspotCiencias no encontrado en el layout");
        }
        
        if (finalCiencias == null) {
            android.util.Log.e("HomeActivity", "ERROR: finalCiencias es null - materia no encontrada");
        }
        
        if (hotspotCiencias != null && finalCiencias != null) {
            android.util.Log.d("HomeActivity", "✓ Configurando click listener para Ciencias");
            hotspotCiencias.setOnClickListener(v -> {
                android.util.Log.d("HomeActivity", "=== CLICK EN HOTSPOT CIENCIAS ===");
                if (!testKolbCompletado || !diagnosticoInicialCompletado) {
                    android.util.Log.w("HomeActivity", "Tests no completados");
                    verificarYRedirigir();
                    return;
                }
                
                try {
                    Intent intent = new Intent(HomeActivity.this, com.example.zavira_movil.niveleshome.MapaActivity.class);
                    intent.putExtra("subject", finalCiencias);
                    android.util.Log.d("HomeActivity", "Abriendo MapaActivity con: " + finalCiencias.title);
                    startActivity(intent);
                } catch (Exception e) {
                    android.util.Log.e("HomeActivity", "ERROR al abrir MapaActivity", e);
                    android.widget.Toast.makeText(HomeActivity.this, "Error: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                }
            });
            
            // Asegurar que el hotspot esté completamente habilitado
            hotspotCiencias.setClickable(true);
            hotspotCiencias.setEnabled(true);
            hotspotCiencias.setFocusable(true);
            hotspotCiencias.setVisibility(View.VISIBLE);
            
            // Aplicar animación de pulso
            aplicarAnimacionPulso(hotspotCiencias);
            
            // Verificar propiedades del hotspot
            android.util.Log.d("HomeActivity", "Hotspot Ciencias - Clickable: " + hotspotCiencias.isClickable());
            android.util.Log.d("HomeActivity", "Hotspot Ciencias - Enabled: " + hotspotCiencias.isEnabled());
            android.util.Log.d("HomeActivity", "Hotspot Ciencias - Visible: " + (hotspotCiencias.getVisibility() == View.VISIBLE));
        } else {
            android.util.Log.e("HomeActivity", "ERROR: No se pudo configurar hotspot de Ciencias");
        }
        
        // Hotspot Isla de Inglés - Abre el mapa completo
        View hotspotIngles = findViewById(R.id.hotspotIngles);
        if (hotspotIngles != null && finalIngles != null) {
            hotspotIngles.setOnClickListener(v -> {
                if (!testKolbCompletado || !diagnosticoInicialCompletado) {
                    verificarYRedirigir();
                    return;
                }
                Intent intent = new Intent(HomeActivity.this, com.example.zavira_movil.niveleshome.MapaActivity.class);
                intent.putExtra("subject", finalIngles);
                startActivity(intent);
            });
            // Aplicar animación de pulso
            aplicarAnimacionPulso(hotspotIngles);
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
    
    /**
     * Verifica qué test falta y redirige silenciosamente
     */
    private void verificarYRedirigir() {
            if (!testKolbCompletado) {
                Intent intentKolb = new Intent(HomeActivity.this, com.example.zavira_movil.InfoTestActivity.class);
                intentKolb.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intentKolb);
                finish();
        } else if (!diagnosticoInicialCompletado) {
                Intent intentDiag = new Intent(HomeActivity.this, com.example.zavira_movil.InfoAcademico.class);
                intentDiag.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intentDiag);
                finish();
        }
    }

    private void setupBottomNav(BottomNavigationView nav) {
        // Quitar fondo de los items (incluyendo el seleccionado) - usar null para remover completamente
        nav.setItemBackground(null);
        
        // Función recursiva para quitar fondo de todos los views
        java.util.function.Consumer<View> removeBackgroundRecursive = new java.util.function.Consumer<View>() {
            @Override
            public void accept(View view) {
                if (view != null) {
                    view.setBackground(null);
                    if (view instanceof android.view.ViewGroup) {
                        android.view.ViewGroup group = (android.view.ViewGroup) view;
                        for (int i = 0; i < group.getChildCount(); i++) {
                            accept(group.getChildAt(i));
                        }
                    }
                }
            }
        };
        
        // Usar ViewTreeObserver para asegurar que los items estén completamente renderizados
        nav.getViewTreeObserver().addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Quitar fondo de todos los items y sus hijos
                for (int i = 0; i < nav.getChildCount(); i++) {
                    View child = nav.getChildAt(i);
                    removeBackgroundRecursive.accept(child);
                }
                // Remover el listener después de ejecutarse una vez
                nav.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        
        // También ejecutar después de un pequeño delay para asegurar que se aplique
        nav.postDelayed(() -> {
            for (int i = 0; i < nav.getChildCount(); i++) {
                View child = nav.getChildAt(i);
                removeBackgroundRecursive.accept(child);
            }
        }, 100);
        
        // Configurar estilo inicial (fondo blanco, iconos azul claro cuando está seleccionado, azul oscuro cuando no)
        nav.setBackgroundColor(android.graphics.Color.WHITE);
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},  // Seleccionado
                new int[]{} // default (no seleccionado)
        };
        int[] colors = new int[]{
                android.graphics.Color.parseColor("#60A5FA"),  // Azul claro cuando está seleccionado
                android.graphics.Color.parseColor("#2563EB")  // Azul oscuro cuando no está seleccionado
        };
        ColorStateList tint = new ColorStateList(states, colors);
        nav.setItemIconTintList(tint);
        nav.setItemTextColor(tint);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_islas) {
                isRetosActive = false;
                applyTabVisibility(true, false);
                show(BLANK_FRAGMENT);
                return true;
            }

            // RA es una Activity (no Fragment)
            if (id == R.id.nav_ra) {
                // Navegar directamente a Modelo 3D RA
                startActivity(new Intent(this, com.example.zavira_movil.Perfil.Modelo3DRAActivity.class));
                return false; // mantiene la selección anterior
            }

            Fragment f;
            boolean isRetos = false;
            if (id == R.id.nav_progreso) {
                f = new ProgresoFragment();
                isRetosActive = false;
            } else if (id == R.id.nav_logros) {
                f = new RankingLogrosFragment();
                isRetosActive = false;
            } else if (id == R.id.nav_retos) {
                f = new RetosFragment();
                isRetos = true;
                isRetosActive = true;
            } else {
                isRetosActive = false;
                return false;
            }

            applyTabVisibility(false, isRetos);
            show(f);
            return true;
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Desregistrar receiver
        if (syncReceiver != null) {
            try {
                unregisterReceiver(syncReceiver);
            } catch (Exception e) {
                // Ignorar si ya está desregistrado
                android.util.Log.w("HomeActivity", "Error al desregistrar receiver", e);
            }
        }
    }

    private void applyTabVisibility(boolean isIslas, boolean isRetos) {
        // Mostrar/ocultar contenedor de hotspots
        View hotspotsContainer = findViewById(R.id.hotspotsContainer);
        if (hotspotsContainer != null) {
            hotspotsContainer.setVisibility(isIslas ? View.VISIBLE : View.GONE);
            android.util.Log.d("HomeActivity", "applyTabVisibility - hotspotsContainer visibility: " + (isIslas ? "VISIBLE" : "GONE"));
            
            // Si estamos en Home (Islas), reconfigurar los click listeners después de hacer visible
            if (isIslas) {
                hotspotsContainer.post(() -> {
                    android.util.Log.d("HomeActivity", "Reconfigurando click listeners después de hacer visible hotspotsContainer");
                    setupIslandsClickListeners();
                });
            }
        } else {
            android.util.Log.e("HomeActivity", "ERROR: hotspotsContainer no encontrado en applyTabVisibility");
        }
        
        // Mostrar header solo en Home (Islas), ocultarlo completamente en otras pestañas
        View headerContainer = findViewById(R.id.header_container);
        if (headerContainer != null) {
            headerContainer.setVisibility(isIslas ? View.VISIBLE : View.GONE);
        }
        
        // Mostrar imagen de fondo SOLO en Home (Islas)
        ImageView mapBackground = findViewById(R.id.mapBackground);
        if (mapBackground != null) {
            if (isIslas) {
                // En Home: mostrar la imagen de fondo
                mapBackground.setVisibility(View.VISIBLE);
                mapBackground.setAlpha(1f);
            } else {
                // En otras pestañas: ocultar completamente la imagen de fondo
                mapBackground.setVisibility(View.GONE);
            }
        }
        
        // Controlar la visibilidad del contenedor de fragments
        FrameLayout fragmentContainer = findViewById(R.id.fragmentContainer);
        if (fragmentContainer != null) {
            if (isIslas) {
                // En Home: ocultar el fragmentContainer para que se vea la imagen de fondo
                fragmentContainer.setVisibility(View.GONE);
            } else {
                // En otras pestañas: mostrar el fragmentContainer con fondo blanco
                fragmentContainer.setVisibility(View.VISIBLE);
                fragmentContainer.setBackgroundColor(android.graphics.Color.WHITE);
            }
        }
        
        // Cambiar color de la status bar según la pestaña
        setupStatusBarColor(isRetos);
        
        // Cambiar estilo del navegador: siempre fondo blanco, iconos y texto azul claro cuando está seleccionado, azul oscuro cuando no
        BottomNavigationView bottomNav = binding.bottomNav;
            bottomNav.setBackgroundColor(android.graphics.Color.WHITE);
            int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},  // Seleccionado
                new int[]{} // default (no seleccionado)
            };
            int[] colors = new int[]{
                android.graphics.Color.parseColor("#60A5FA"),  // Azul claro cuando está seleccionado
                android.graphics.Color.parseColor("#2563EB")  // Azul oscuro cuando no está seleccionado
            };
            ColorStateList tint = new ColorStateList(states, colors);
            bottomNav.setItemIconTintList(tint);
            bottomNav.setItemTextColor(tint);
    }
    
    // Nota: restaurarTopBar() fue eliminado porque topBar ya no existe en el layout

    /**
     * Configura el color de la status bar según si está en Retos o no
     * @param isRetos true si está en la pestaña de Retos, false en caso contrario
     */
    private void setupStatusBarColor(boolean isRetos) {
        int statusBarColor;
        if (isRetos) {
            // Retos: status bar blanca
            statusBarColor = android.graphics.Color.WHITE;
            android.util.Log.d("HomeActivity", "=== Configurando status bar BLANCA para Retos ===");
        } else {
            // Home y otras pestañas: status bar azul (#3988FF) - mismo color que los iconos
            statusBarColor = android.graphics.Color.parseColor("#3988FF");
            android.util.Log.d("HomeActivity", "=== Configurando status bar AZUL para Home/Otras ===");
        }
        
        // CRÍTICO: Habilitar el dibujo del fondo de la status bar ANTES de establecer el color
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        
        // Establecer el color de la status bar
        getWindow().setStatusBarColor(statusBarColor);
        
        // Obtener las flags actuales para preservar las de navegación
        int currentFlags = getWindow().getDecorView().getSystemUiVisibility();
        
        // Configurar el estilo del texto de la status bar
        // Para Android 11+ (API 30+), usar WindowInsetsController
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            androidx.core.view.WindowInsetsControllerCompat windowInsetsController = 
                androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            if (windowInsetsController != null) {
                if (isRetos) {
                    // Para fondo blanco en Retos, usar texto oscuro
                    windowInsetsController.setAppearanceLightStatusBars(true);
                    android.util.Log.d("HomeActivity", "WindowInsetsController: texto oscuro configurado para Retos");
                } else {
                    // Para fondos oscuros (azul), usar texto claro (blanco)
                    windowInsetsController.setAppearanceLightStatusBars(false);
                    android.util.Log.d("HomeActivity", "WindowInsetsController: texto claro configurado");
                }
            }
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Para Android 6.0+ (API 23+), usar setSystemUiVisibility
            int newFlags = currentFlags;
            if (isRetos) {
                // Para fondo blanco en Retos, activar texto oscuro
                newFlags = newFlags | android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                // Para fondos oscuros (azul), texto claro (blanco)
                newFlags = newFlags & ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            // NO usar LAYOUT_FULLSCREEN para la status bar - esto hace que el color no se vea
            // Solo usar LAYOUT_STABLE para mantener la estabilidad
            newFlags = (newFlags & ~android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) | 
                       android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            getWindow().getDecorView().setSystemUiVisibility(newFlags);
            android.util.Log.d("HomeActivity", "setSystemUiVisibility: flags configuradas, flags: " + newFlags);
        }
        
        // Forzar actualización múltiple para asegurar que se aplique
        getWindow().getDecorView().post(() -> {
            getWindow().setStatusBarColor(statusBarColor);
            android.util.Log.d("HomeActivity", "Post: Status bar color establecido: " + String.format("#%08X", statusBarColor));
        });
        
        getWindow().getDecorView().postDelayed(() -> {
            getWindow().setStatusBarColor(statusBarColor);
            int actualColor = getWindow().getStatusBarColor();
            android.util.Log.d("HomeActivity", "PostDelayed: Status bar color verificado: " + 
                String.format("#%08X", actualColor) + " (esperado: " + String.format("#%08X", statusBarColor) + ")");
        }, 100);
        
        // Aplicar una vez más después de un delay más largo para asegurar que se mantenga
        getWindow().getDecorView().postDelayed(() -> {
            if (!isRetos) {
                // Solo aplicar azul si no es Retos
                getWindow().setStatusBarColor(android.graphics.Color.parseColor("#3988FF"));
            }
        }, 300);
    }

    private void show(Fragment f) {
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragmentContainer, f)
                .commit();
        
        // Configurar padding del fragmentContainer según el tipo de fragment
        FrameLayout fragmentContainer = findViewById(R.id.fragmentContainer);
        boolean isDetalleSimulacro = f instanceof com.example.zavira_movil.detalleprogreso.FragmentDetalleSimulacro;
        
        if (fragmentContainer != null) {
            if (isDetalleSimulacro) {
                // Para FragmentDetalleSimulacro, agregar paddingTop para bajar el contenido
                // Usar post para asegurar que se aplique después de que el fragment se monte
                fragmentContainer.post(() -> {
                    int paddingTop = dp(40); // 40dp de padding superior
                    fragmentContainer.setPadding(0, paddingTop, 0, 0);
                });
                // También aplicar inmediatamente por si acaso
                fragmentContainer.setPadding(0, dp(40), 0, 0);
            } else {
                // Para otros fragments, sin padding (empiezan desde el top)
                fragmentContainer.setPadding(0, 0, 0, 0);
            }
        }
        
        // Aplicar cambios de status bar después de que el fragment se muestre
        // Si el fragment es RetosFragment, asegurar que la status bar sea naranja
        boolean isRetosFragment = f instanceof com.example.zavira_movil.ui.ranking.progreso.RetosFragment;
        boolean isProgresoFragment = f instanceof com.example.zavira_movil.ui.ranking.progreso.ProgresoFragment;
        isRetosActive = isRetosFragment;
        
        if (isRetosFragment) {
            // Aplicar color naranja inmediatamente
            setupStatusBarColor(true);
            
            // Aplicar múltiples veces con delays para asegurar que se establezca
            getWindow().getDecorView().post(() -> {
                setupStatusBarColor(true);
            });
            
            getWindow().getDecorView().postDelayed(() -> {
                setupStatusBarColor(true);
            }, 100);
            
            getWindow().getDecorView().postDelayed(() -> {
                setupStatusBarColor(true);
            }, 300);
            
            getWindow().getDecorView().postDelayed(() -> {
                setupStatusBarColor(true);
            }, 500);
        } else if (isProgresoFragment) {
            // Si es ProgresoFragment, el fragment se encarga de establecer el color verde
            // No hacer nada aquí para evitar conflictos
        } else {
            // Si no es Retos ni Progreso, aplicar color azul
            setupStatusBarColor(false);
        }
    }

    private int dp(int v) {
        return (int) (getResources().getDisplayMetrics().density * v);
    }
    
    /**
     * Carga el nombre del usuario y lo muestra en el header
     */
    private void cargarNombreUsuario() {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.getPerfilEstudiante().enqueue(new Callback<com.example.zavira_movil.model.Estudiante>() {
            @Override
            public void onResponse(Call<com.example.zavira_movil.model.Estudiante> call, Response<com.example.zavira_movil.model.Estudiante> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.zavira_movil.model.Estudiante estudiante = response.body();
                    // Obtener primer nombre y primer apellido
                    String nombreUsuario = estudiante.getNombreUsuario();
                    String apellido = estudiante.getApellido();
                    String nombreMostrar = "";
                    
                    // Obtener solo la primera palabra del nombre
                    String primerNombre = "";
                    if (nombreUsuario != null && !nombreUsuario.trim().isEmpty()) {
                        String[] partesNombre = nombreUsuario.trim().split("\\s+");
                        if (partesNombre.length > 0) {
                            primerNombre = partesNombre[0];
                        }
                    }
                    
                    // Obtener solo la primera palabra del apellido
                    String primerApellido = "";
                    if (apellido != null && !apellido.trim().isEmpty()) {
                        String[] partesApellido = apellido.trim().split("\\s+");
                        if (partesApellido.length > 0) {
                            primerApellido = partesApellido[0];
                        }
                    }
                    
                    // Combinar primer nombre y primer apellido con signo de cierre de admiración
                    if (!primerNombre.isEmpty() && !primerApellido.isEmpty()) {
                        nombreMostrar = primerNombre + " " + primerApellido + "!";
                    } else if (!primerNombre.isEmpty()) {
                        nombreMostrar = primerNombre + "!";
                    } else if (!primerApellido.isEmpty()) {
                        nombreMostrar = primerApellido + "!";
                    } else {
                        nombreMostrar = "Usuario!";
                    }
                    
                    TextView tvUserName = findViewById(R.id.tvUserName);
                    if (tvUserName != null) {
                        tvUserName.setText(nombreMostrar);
                    }
                    
                    // Cargar foto del usuario en el avatar
                    cargarFotoUsuario(estudiante);
                }
            }
            
            @Override
            public void onFailure(Call<com.example.zavira_movil.model.Estudiante> call, Throwable t) {
                // En caso de error, mantener el texto por defecto y usar icono de perfil
                android.util.Log.e("HomeActivity", "Error al cargar perfil del usuario", t);
                ImageView ivAvatar = findViewById(R.id.ivAvatar);
                if (ivAvatar != null) {
                    ivAvatar.setImageResource(R.drawable.usuario);
                }
            }
        });
    }
    
    /**
     * Carga la foto del usuario en el avatar del header
     */
    private void cargarFotoUsuario(com.example.zavira_movil.model.Estudiante estudiante) {
        ImageView ivAvatar = findViewById(R.id.ivAvatar);
        if (ivAvatar == null || estudiante == null) return;
        
        String fotoUrl = estudiante.getFotoUrl();
        
        // Intentar cargar desde archivo local primero (como en PerfilFragment)
        Integer userId = estudiante.getIdUsuario();
        String prefsKey = userId != null ? "foto_path_" + userId : "foto_path_tmp";
        android.content.SharedPreferences sp = getSharedPreferences("perfil_prefs", MODE_PRIVATE);
        String localPath = sp.getString(prefsKey, null);
        
        if (localPath != null) {
            java.io.File localFile = new java.io.File(localPath);
            if (localFile.exists()) {
                Glide.with(this)
                        .load(localFile)
                        .placeholder(R.drawable.usuario)
                        .error(R.drawable.usuario)
                        .circleCrop()
                        .into(ivAvatar);
                return;
            }
        }
        
        // Si no hay archivo local, cargar desde URL remota
        if (fotoUrl != null && !fotoUrl.trim().isEmpty() && !"null".equalsIgnoreCase(fotoUrl.trim())) {
            Glide.with(this)
                    .load(fotoUrl.trim())
                    .placeholder(R.drawable.usuario)
                    .error(R.drawable.usuario)
                    .circleCrop()
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.usuario);
        }
    }
    
    /**
     * Actualiza el badge de notificaciones no leídas
     */
    private void updateNotificationBadge() {
        TextView tvNotificationBadge = findViewById(R.id.tvNotificationBadge);
        if (tvNotificationBadge != null) {
            com.example.zavira_movil.notifications.NotificationStorage storage =
                new com.example.zavira_movil.notifications.NotificationStorage(this);
            int unreadCount = storage.getUnreadCount();

            if (unreadCount > 0) {
                tvNotificationBadge.setVisibility(View.VISIBLE);
                tvNotificationBadge.setText(String.valueOf(Math.min(unreadCount, 99))); // Máximo 99
            } else {
                tvNotificationBadge.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Formatea el nombre completo para mostrar nombre y apellido
     * Si tiene más de dos palabras, toma las primeras dos (nombre y apellido)
     */
    private String formatearNombre(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return "Usuario";
        }
        
        String nombre = nombreCompleto.trim();
        String[] partes = nombre.split("\\s+");
        
        if (partes.length == 0) {
            return "Usuario";
        } else if (partes.length == 1) {
            return partes[0];
        } else {
            // Nombre y apellido (primeras dos palabras)
            return partes[0] + " " + partes[1];
        }
    }
    
    /**
     * Muestra un Dialog informativo sobre la Isla del Conocimiento
     * Al hacer clic en "Siguiente", lleva al MapaActivity
     */
    private void mostrarDialogInfoConocimiento() {
        // Crear el Dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        
        // Inflar el layout del Dialog
        android.view.LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_isla_conocimiento_info, null);
        
        // Configurar el Dialog
        builder.setView(dialogView);
        builder.setCancelable(true);
        
        // Crear el Dialog
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        
        // Configurar el botón "Siguiente"
        Button btnSiguiente = dialogView.findViewById(R.id.btnSiguiente);
        if (btnSiguiente != null) {
            btnSiguiente.setOnClickListener(v -> {
                dialog.dismiss();
                // Abrir MapaActivity
                try {
                    // Crear un Subject temporal para Conocimiento
                    com.example.zavira_movil.model.Subject subjectConocimiento = new com.example.zavira_movil.model.Subject();
                    subjectConocimiento.title = "Conocimiento";
                    subjectConocimiento.iconRes = R.drawable.ic_science_24;
                    subjectConocimiento.headerDrawableRes = R.drawable.bg_header_science;
                    subjectConocimiento.done = 0;
                    subjectConocimiento.total = 5;
                    
                    Intent intent = new Intent(HomeActivity.this, com.example.zavira_movil.niveleshome.MapaActivity.class);
                    intent.putExtra("subject", subjectConocimiento);
                    android.util.Log.d("HomeActivity", "Abriendo MapaActivity con: " + subjectConocimiento.title);
                    startActivity(intent);
                } catch (Exception e) {
                    android.util.Log.e("HomeActivity", "ERROR al abrir MapaActivity", e);
                    android.widget.Toast.makeText(HomeActivity.this, "Error: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                }
            });
        }
        
        // Mostrar el Dialog
        dialog.show();
        
        // Configurar el Dialog DESPUÉS de mostrarlo para centrarlo correctamente
        android.view.Window window = dialog.getWindow();
        if (window != null) {
            // Configurar fondo transparente para que se vean los bordes redondeados
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            
            // Obtener parámetros de la ventana
            android.view.WindowManager.LayoutParams layoutParams = window.getAttributes();
            
            // Calcular dimensiones de la pantalla
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            int dialogWidth = (int) (screenWidth * 0.9);
            int maxHeight = (int) (screenHeight * 0.85);
            
            // Establecer tamaño y posición del dialog
            layoutParams.width = dialogWidth;
            layoutParams.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = android.view.Gravity.CENTER;
            layoutParams.horizontalMargin = 0f;
            layoutParams.verticalMargin = 0.15f; // Bajar el diálogo un poco más (15% desde el centro)
            
            // Aplicar parámetros
            window.setAttributes(layoutParams);
            
            // Configurar overlay semi-transparente
            window.setDimAmount(0.5f);
            
            // Si el contenido es muy alto, ajustar la altura del ScrollView después de que se mida
            dialogView.post(() -> {
                android.widget.ScrollView scrollView = (android.widget.ScrollView) dialogView;
                if (scrollView != null && scrollView.getChildCount() > 0) {
                    android.view.View contentView = scrollView.getChildAt(0);
                    if (contentView != null) {
                        // Forzar medida del contenido
                        contentView.measure(
                            android.view.View.MeasureSpec.makeMeasureSpec(dialogWidth, android.view.View.MeasureSpec.EXACTLY),
                            android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
                        );
                        int contentHeight = contentView.getMeasuredHeight();
                        if (contentHeight > maxHeight) {
                            android.view.ViewGroup.LayoutParams scrollParams = scrollView.getLayoutParams();
                            scrollParams.height = maxHeight;
                            scrollView.setLayoutParams(scrollParams);
                        }
                    }
                }
            });
        }
    }
    
}
