package com.example.zavira_movil.Home;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.Perfil.ProfileActivity;
import com.example.zavira_movil.R;
import com.example.zavira_movil.databinding.ActivityHomeBinding;
import com.example.zavira_movil.model.KolbResultado;
import com.example.zavira_movil.niveleshome.DemoData;
import com.example.zavira_movil.niveleshome.SubjectAdapter;
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

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private SubjectAdapter adapter;
    private ImageView ivBackdrop; // lo mantengo como lo tenías
    private LinearLayoutManager lm;
    private boolean testKolbCompletado = true; // Inicializar como true para evitar bloqueos prematuras
    private boolean diagnosticoInicialCompletado = true; // Inicializar como true para evitar bloqueos prematuras

    private final ActivityResultLauncher<Intent> launcher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                    }
            );

    private static final Fragment BLANK_FRAGMENT = new Fragment();

    private android.content.BroadcastReceiver syncReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Registrar receiver para sincronización (solo si no está registrado)
        if (syncReceiver == null) {
            syncReceiver = new android.content.BroadcastReceiver() {
                @Override
                public void onReceive(android.content.Context context, android.content.Intent intent) {
                    if (intent != null && "com.example.zavira_movil.SYNC_COMPLETED".equals(intent.getAction())) {
                        android.util.Log.d("HomeActivity", "=== Broadcast SYNC_COMPLETED recibido ===");
                        // Actualizar adapter cuando la sincronización termine
                        runOnUiThread(() -> {
                            android.util.Log.d("HomeActivity", "Actualizando adapter después de sincronización");
                            if (adapter != null) {
                                // Notificar cambio completo para forzar rebinding de todos los ViewHolders
                                adapter.notifyDataSetChanged();
                                android.util.Log.d("HomeActivity", "Adapter actualizado con notifyDataSetChanged()");
                                
                                // CRÍTICO: También forzar actualización del RecyclerView principal
                                // Esto asegura que los ViewHolders se rebindean y los adapters internos se actualicen
                                if (binding != null && binding.rvSubjects != null) {
                                    // Forzar refresco del RecyclerView
                                    binding.rvSubjects.post(() -> {
                                        adapter.notifyDataSetChanged();
                                        android.util.Log.d("HomeActivity", "RecyclerView refrescado y adapters internos actualizados");
                                    });
                                }
                            } else {
                                android.util.Log.w("HomeActivity", "Adapter es null, no se puede actualizar");
                            }
                        });
                    }
                }
            };
            try {
                android.content.IntentFilter filter = new android.content.IntentFilter("com.example.zavira_movil.SYNC_COMPLETED");
                registerReceiver(syncReceiver, filter);
                android.util.Log.d("HomeActivity", "BroadcastReceiver registrado para SYNC_COMPLETED");
            } catch (Exception e) {
                android.util.Log.e("HomeActivity", "Error al registrar BroadcastReceiver", e);
            }
        }

        // Cargar nombre del usuario en el header
        cargarNombreUsuario();
        
        // RecyclerView único (el primer ítem es la Isla del Conocimiento dentro del SubjectAdapter)
        binding.rvSubjects.setLayoutManager(new LinearLayoutManager(this));
        // IMPORTANTE: Asumir que ambos tests están completados inicialmente
        // Esto evita que se muestre el mensaje de bloqueo antes de verificar el backend
        testKolbCompletado = true;
        diagnosticoInicialCompletado = true;
        
        // Crear adapter habilitado inicialmente - solo se bloqueará si la verificación confirma que falta algo
        adapter = new SubjectAdapter(DemoData.getSubjects(), intent -> {
            // Verificar estado actual antes de permitir CUALQUIER actividad
            // Solo bloquear si la verificación del backend confirmó que falta algo
            if (!testKolbCompletado) {
                // Redirigir silenciosamente al test de Kolb (sin mostrar mensaje)
                Intent intentKolb = new Intent(HomeActivity.this, com.example.zavira_movil.InfoTestActivity.class);
                intentKolb.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intentKolb);
                finish();
                return;
            }
            if (!diagnosticoInicialCompletado) {
                // Redirigir silenciosamente al diagnóstico (sin mostrar mensaje)
                Intent intentDiag = new Intent(HomeActivity.this, com.example.zavira_movil.InfoAcademico.class);
                intentDiag.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intentDiag);
                finish();
                return;
            }
            // Si ambos están completos, permitir la actividad
            launcher.launch(intent);
        });
        binding.rvSubjects.setAdapter(adapter);

        // ---------- Fondo del header dinámico SOLO en el encabezado ----------
        final ImageView ivParallax = findViewById(R.id.ivParallax);
        final View overlayFade     = findViewById(R.id.overlayFade);
        final View topBar          = findViewById(R.id.topBar);
        final RecyclerView rv      = findViewById(R.id.rvSubjects);

        // Imagen inicial: conocimiento (queda lista, pero oculta hasta que una tarjeta toque el header)
        ivParallax.setImageResource(R.drawable.fondoconocimiento);
        ivParallax.setAlpha(0f);
        overlayFade.setAlpha(0f);

        // Blur leve (Android 12+)
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            ivParallax.setRenderEffect(
                    android.graphics.RenderEffect.createBlurEffect(12f, 12f,
                            android.graphics.Shader.TileMode.CLAMP)
            );
        }

        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            Drawable lastDrawable = null;

            @Override public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);

                RecyclerView.LayoutManager _lm = rv.getLayoutManager();
                if (!(_lm instanceof LinearLayoutManager)) return;

                int firstPos = ((LinearLayoutManager) _lm).findFirstVisibleItemPosition();
                View first = ((LinearLayoutManager) _lm).findViewByPosition(firstPos);
                if (first == null) {
                    // Nada visible si no hay tarjeta visible
                    ivParallax.setAlpha(0f);
                    overlayFade.setAlpha(0f);
                    return;
                }

                // ¿La tarjeta ya "tocó" el borde inferior del header?
                int[] vLoc = new int[2];
                int[] bLoc = new int[2];
                first.getLocationOnScreen(vLoc);
                topBar.getLocationOnScreen(bLoc);

                int barBottom = bLoc[1] + topBar.getHeight();
                int overlap   = barBottom - vLoc[1]; // > 0 cuando cruzó el header

                if (overlap <= 0) {
                    // Mientras no toque el header, no se ve nada
                    ivParallax.setAlpha(0f);
                    overlayFade.setAlpha(0f);
                    return;
                }

                // Se nota un poquito más:
                // - Fondo (imagen de la tarjeta) hasta 0.42
                // - Velo blanco hasta 0.72
                int maxPx = dp(56);
                float imgA  = Math.min(1f, overlap / (float) maxPx) * 0.42f;
                float veloA = Math.min(1f, overlap / (float) maxPx) * 0.72f;
                ivParallax.setAlpha(imgA);
                overlayFade.setAlpha(veloA);

                // Tomar la imagen del header (flHeader) de la tarjeta que está tocando
                View flHeader = first.findViewById(R.id.flHeader);
                Drawable d = null;
                if (flHeader != null) {
                    // Si el header tiene un ImageView (tu SubjectAdapter lo añade como child 0)
                    if (flHeader instanceof android.widget.FrameLayout
                            && ((android.widget.FrameLayout) flHeader).getChildCount() > 0) {
                        View child0 = ((android.widget.FrameLayout) flHeader).getChildAt(0);
                        if (child0 instanceof ImageView) {
                            d = ((ImageView) child0).getDrawable();
                        }
                    }
                    // O intenta con el background del header si no hay hijo ImageView
                    if (d == null) d = flHeader.getBackground();
                }

                // Fallback para el item 0 (Conocimiento)
                if (d == null && firstPos == 0) {
                    d = getDrawable(R.drawable.fondoconocimiento);
                }

                // Evita reasignar si es el mismo drawable (previene flicker)
                if (d != null && d != lastDrawable) {
                    ivParallax.setImageDrawable(d);
                    lastDrawable = d;
                }
            }
        });
        // --------------------------------------------------------------------

        // CLICK EN LA CAMPANA → habilitado por defecto, solo se bloquea si la verificación confirma que falta algo
        View btnBell = findViewById(R.id.btnBell);
        View ivBell  = findViewById(R.id.ivBell);
        View.OnClickListener goNotifications = v -> {
            // Solo verificar si la verificación del backend confirmó que falta algo
            if (!testKolbCompletado) {
                // Redirigir silenciosamente al test de Kolb (sin mostrar mensaje)
                Intent intentKolb = new Intent(HomeActivity.this, com.example.zavira_movil.InfoTestActivity.class);
                intentKolb.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intentKolb);
                finish();
                return;
            }
            if (!diagnosticoInicialCompletado) {
                // Redirigir silenciosamente al diagnóstico (sin mostrar mensaje)
                Intent intentDiag = new Intent(HomeActivity.this, com.example.zavira_movil.InfoAcademico.class);
                intentDiag.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intentDiag);
                finish();
                return;
            }
            // Si ambos están completos, permitir la acción (no hacer nada por ahora)
        };
        if (btnBell != null) btnBell.setOnClickListener(goNotifications);
        if (ivBell != null) ivBell.setOnClickListener(goNotifications);

        // Bottom navigation - habilitado por defecto, solo se bloquea si la verificación confirma que falta algo
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            
            // Permitir solo perfil siempre
            if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, ProfileActivity.class));
                return false;
            }
            
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

        // Pestaña por defecto: Islas
        if (savedInstanceState == null) {
            show(BLANK_FRAGMENT);
            applyTabVisibility(true);
            binding.bottomNav.setSelectedItemId(R.id.nav_islas);
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
        
        // Verificar qué fragment está activo y ajustar el topBar según corresponda
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (currentFragment instanceof com.example.zavira_movil.ui.ranking.progreso.RetosFragment ||
            currentFragment instanceof com.example.zavira_movil.ui.ranking.progreso.ProgresoFragment ||
            currentFragment instanceof com.example.zavira_movil.ui.ranking.RankingLogrosFragment) {
            // Si estamos en Retos, Progreso o Logros, el topBar debe estar oculto
            View topBar = findViewById(R.id.topBar);
            if (topBar != null) {
                topBar.setVisibility(View.GONE);
            }
        }
        
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
        
        // Bloquear clicks en las islas - SIEMPRE crear el adapter bloqueado
        adapter = new SubjectAdapter(DemoData.getSubjects(), intent -> {
            // Verificar estado actual antes de permitir - hacer verificación final
            if (!testKolbCompletado) {
                // Verificar una vez más antes de bloquear
                android.util.Log.d("HomeActivity", "Verificando test de Kolb una vez más antes de bloquear...");
                ApiService api = RetrofitClient.getInstance(HomeActivity.this).create(ApiService.class);
                api.obtenerResultado().enqueue(new Callback<KolbResultado>() {
                    @Override
                    public void onResponse(Call<KolbResultado> call, Response<KolbResultado> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getEstilo() != null) {
                            // El test SÍ está completado - habilitar
                            android.util.Log.d("HomeActivity", "Test de Kolb confirmado como completado en verificación final");
                            testKolbCompletado = true;
                            habilitarInteracciones();
                            launcher.launch(intent);
                        } else if (response.code() == 404) {
                            // Realmente no está completado - redirigir silenciosamente
                            android.util.Log.d("HomeActivity", "Test de Kolb confirmado como NO completado (404)");
                            Intent intentKolb = new Intent(HomeActivity.this, com.example.zavira_movil.InfoTestActivity.class);
                            intentKolb.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intentKolb);
                            finish();
                        } else {
                            // Error - permitir acceso para evitar bloqueos incorrectos
                            android.util.Log.w("HomeActivity", "Error en verificación final - permitiendo acceso");
                            testKolbCompletado = true;
                            habilitarInteracciones();
                            launcher.launch(intent);
                        }
                    }
                    @Override
                    public void onFailure(Call<KolbResultado> call, Throwable t) {
                        // Error de red - permitir acceso para evitar bloqueos incorrectos
                        android.util.Log.w("HomeActivity", "Error de red en verificación final - permitiendo acceso");
                        testKolbCompletado = true;
                        habilitarInteracciones();
                        launcher.launch(intent);
                    }
                });
                return;
            }
            if (!diagnosticoInicialCompletado) {
                // Redirigir silenciosamente al diagnóstico (sin mostrar mensaje)
                Intent intentDiag = new Intent(HomeActivity.this, com.example.zavira_movil.InfoAcademico.class);
                intentDiag.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intentDiag);
                finish();
                return;
            }
            // Si ambos están completos, permitir la actividad
            launcher.launch(intent);
        });
        binding.rvSubjects.setAdapter(adapter);
        
        // Bloquear bottom navigation (excepto perfil)
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            
            // Permitir solo perfil
            if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, ProfileActivity.class));
                return false;
            }
            
            // Si falta test de Kolb, redirigir silenciosamente
            if (!testKolbCompletado) {
                Intent intentKolb = new Intent(HomeActivity.this, com.example.zavira_movil.InfoTestActivity.class);
                intentKolb.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intentKolb);
                finish();
                return false;
            }
            // Si falta diagnóstico, redirigir silenciosamente
            if (!diagnosticoInicialCompletado) {
                Intent intentDiag = new Intent(HomeActivity.this, com.example.zavira_movil.InfoAcademico.class);
                intentDiag.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intentDiag);
                finish();
                return false;
            }
            return false;
        });
        
        // Bloquear campana de notificaciones - redirigir silenciosamente si falta algo
        View btnBell = findViewById(R.id.btnBell);
        View ivBell = findViewById(R.id.ivBell);
        View.OnClickListener blockAction = v -> {
            // Si falta test de Kolb, redirigir silenciosamente
            if (!testKolbCompletado) {
                Intent intentKolb = new Intent(HomeActivity.this, com.example.zavira_movil.InfoTestActivity.class);
                intentKolb.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intentKolb);
                finish();
                return;
            }
            // Si falta diagnóstico, redirigir silenciosamente
            if (!diagnosticoInicialCompletado) {
                Intent intentDiag = new Intent(HomeActivity.this, com.example.zavira_movil.InfoAcademico.class);
                intentDiag.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intentDiag);
                finish();
                return;
            }
        };
        if (btnBell != null) btnBell.setOnClickListener(blockAction);
        if (ivBell != null) ivBell.setOnClickListener(blockAction);
    }
    
    private void habilitarInteracciones() {
        // Actualizar el adapter para mostrar el progreso sincronizado
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        // SOLO habilitar si AMBOS tests están completos
        if (!testKolbCompletado || !diagnosticoInicialCompletado) {
            // Si falta algún test, mantener bloqueado
            bloquearInteracciones();
            return;
        }
        
        // Restaurar interacciones normales
        adapter = new SubjectAdapter(DemoData.getSubjects(), intent -> launcher.launch(intent));
        binding.rvSubjects.setAdapter(adapter);
        
        setupBottomNav(binding.bottomNav);
        
        // Restaurar campana de notificaciones
        View btnBell = findViewById(R.id.btnBell);
        View ivBell = findViewById(R.id.ivBell);
        View.OnClickListener goNotifications = v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        };
        if (btnBell != null) btnBell.setOnClickListener(goNotifications);
        if (ivBell != null) ivBell.setOnClickListener(goNotifications);
    }

    private void setupBottomNav(BottomNavigationView nav) {
        //   Tinte azul al seleccionado y gris al resto (por CÓDIGO)  ⬇
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{} // default
        };
        int[] colors = new int[]{
                ContextCompat.getColor(this, R.color.primaryyy),
                ContextCompat.getColor(this, R.color.text_secondary)
        };
        ColorStateList tint = new ColorStateList(states, colors);
        nav.setItemIconTintList(tint);
        nav.setItemTextColor(tint);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_islas) {
                applyTabVisibility(true);
                // Restaurar topBar cuando volvemos a Inicio
                restaurarTopBar();
                show(BLANK_FRAGMENT);
                return true;
            }

            // PERFIL es una Activity (no Fragment)
            if (id == R.id.nav_perfil) {
                // Asegurar que el topBar esté oculto antes de navegar al perfil
                // para evitar que aparezca brevemente
                View topBar = findViewById(R.id.topBar);
                if (topBar != null) {
                    topBar.setVisibility(View.GONE);
                }
                // Navegar directamente al perfil
                startActivity(new Intent(this, ProfileActivity.class));
                return false; // mantiene la selección anterior
            }

            Fragment f;
            if (id == R.id.nav_progreso) {
                f = new ProgresoFragment();
            } else if (id == R.id.nav_logros) {
                f = new RankingLogrosFragment();
            } else if (id == R.id.nav_retos) {
                f = new RetosFragment();
            } else {
                return false;
            }

            applyTabVisibility(false);
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

    private void applyTabVisibility(boolean isIslas) {
        binding.rvSubjects.setVisibility(isIslas ? View.VISIBLE : View.GONE);
    }
    
    /**
     * Restaura la visibilidad del topBar (logo EduExce y campana)
     */
    private void restaurarTopBar() {
        View topBar = findViewById(R.id.topBar);
        if (topBar != null) {
            topBar.setVisibility(View.VISIBLE);
        }
    }

    private void show(Fragment f) {
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragmentContainer, f)
                .commit();
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
                }
            }
            
            @Override
            public void onFailure(Call<com.example.zavira_movil.model.Estudiante> call, Throwable t) {
                // En caso de error, mantener el texto por defecto
                android.util.Log.e("HomeActivity", "Error al cargar perfil del usuario", t);
            }
        });
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
}
