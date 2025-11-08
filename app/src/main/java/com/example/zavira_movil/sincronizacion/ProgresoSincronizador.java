package com.example.zavira_movil.sincronizacion;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.example.zavira_movil.niveleshome.LivesManager;
import com.example.zavira_movil.niveleshome.ProgressLockManager;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Clase encargada de sincronizar niveles desbloqueados y vidas entre el dispositivo y el backend
 */
public class ProgresoSincronizador {
    private static final String TAG = "ProgresoSincronizador";
    private static final String PREFS_SYNC = "sync_progress_prefs";
    private static final String KEY_LAST_SYNC = "last_sync_timestamp";
    
    private static ProgresoSincronizador instance;
    private ApiService apiService;
    
    private ProgresoSincronizador() {
        // Constructor privado para singleton
    }
    
    public static synchronized ProgresoSincronizador getInstance() {
        if (instance == null) {
            instance = new ProgresoSincronizador();
        }
        return instance;
    }
    
    /**
     * Convierte el nombre de área del backend al nombre de área que usa Android en la UI
     * Backend: "Matematicas", "Sociales", "Lenguaje", "Ciencias", "Ingles"
     * UI Android: "Matemáticas", "Sociales y ciudadanas", "Lectura crítica", "Ciencias naturales", "Inglés"
     */
    private String convertirAreaBackendAUI(String areaBackend) {
        if (areaBackend == null) return areaBackend;
        
        String area = areaBackend.trim();
        
        // Mapear áreas del backend a áreas de UI
        if (area.equalsIgnoreCase("Matematicas")) {
            return "Matemáticas";
        } else if (area.equalsIgnoreCase("Sociales")) {
            return "Sociales y ciudadanas";
        } else if (area.equalsIgnoreCase("Lenguaje")) {
            return "Lectura crítica";
        } else if (area.equalsIgnoreCase("Ciencias")) {
            return "Ciencias naturales";
        } else if (area.equalsIgnoreCase("Ingles")) {
            return "Inglés";
        }
        
        // Si no coincide, retornar tal cual (puede que ya esté en formato UI)
        return area;
    }
    
    /**
     * Sincroniza los datos desde el backend al dispositivo local
     * Se debe llamar después del login
     * Si hay datos locales más nuevos que los del backend, también sincroniza al backend
     */
    public void sincronizarDesdeBackend(Context context, String userId) {
        if (apiService == null) {
            apiService = RetrofitClient.getInstance(context).create(ApiService.class);
        }
        
        Log.d(TAG, "=== INICIANDO SINCRONIZACIÓN DESDE BACKEND ===");
        Log.d(TAG, "Usuario ID: " + userId);
        
        apiService.obtenerProgresoSincronizacion().enqueue(new Callback<SincronizacionResponse>() {
            @Override
            public void onResponse(Call<SincronizacionResponse> call, Response<SincronizacionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SincronizacionResponse data = response.body();
                    
                    Log.d(TAG, "=== PROCESANDO DATOS DEL BACKEND ===");
                    
                    // Obtener niveles locales antes de sincronizar
                    Map<String, Integer> nivelesLocales = new HashMap<>();
                    String[] areasUI = {"Matemáticas", "Sociales y ciudadanas", "Lectura crítica", "Ciencias naturales", "Inglés"};
                    for (String areaUI : areasUI) {
                        int nivel = ProgressLockManager.getUnlockedLevel(context, userId, areaUI);
                        String areaApi = convertirAreaUIApi(areaUI);
                        nivelesLocales.put(areaApi, nivel);
                        Log.d(TAG, "Nivel local: área " + areaApi + " = nivel " + nivel);
                    }
                    
                    // Sincronizar niveles desbloqueados
                    Map<String, Integer> niveles = data.getNiveles();
                    int nivelesSincronizados = 0;
                    boolean hayNivelesLocalesMasNuevos = false;
                    
                    if (niveles != null && !niveles.isEmpty()) {
                        Log.d(TAG, "Niveles recibidos del backend: " + niveles.size());
                        for (Map.Entry<String, Integer> entry : niveles.entrySet()) {
                            String areaBackend = entry.getKey(); // Área del backend
                            int nivelBackend = entry.getValue();
                            int nivelLocal = nivelesLocales.getOrDefault(areaBackend, 1);
                            
                            // Convertir área del backend a área de UI que usa Android
                            String areaUI = convertirAreaBackendAUI(areaBackend);
                            
                            // CRÍTICO: Siempre usar el nivel del backend (ya tiene la lógica de retroceso correcta)
                            // El backend ya calculó correctamente el nivel desbloqueado considerando niveles bloqueados (vidas = 0)
                            ProgressLockManager.syncFromBackend(context, userId, areaUI, nivelBackend);
                            Log.d(TAG, "✓ Nivel " + nivelBackend + " sincronizado para área " + areaUI + " (backend: " + areaBackend + ")");
                            
                            // Si el nivel local era mayor que el del backend, significa que Android tenía un nivel bloqueado
                            // que el backend ya retrocedió correctamente
                            if (nivelLocal > nivelBackend) {
                                Log.w(TAG, "⚠ Área " + areaBackend + ": nivel local (" + nivelLocal + ") > nivel backend (" + nivelBackend + ") - backend retrocedió correctamente");
                            }
                            
                            nivelesSincronizados++;
                            
                            // Verificar que se guardó correctamente
                            int nivelVerificado = ProgressLockManager.getUnlockedLevel(context, userId, areaUI);
                            Log.d(TAG, "  Verificado: nivel guardado = " + nivelVerificado);
                        }
                    } else {
                        Log.w(TAG, "⚠ No se recibieron niveles del backend (null o vacío)");
                        
                        // Si no hay niveles en el backend pero hay niveles locales, sincronizar al backend
                        for (Map.Entry<String, Integer> entry : nivelesLocales.entrySet()) {
                            String areaApi = entry.getKey();
                            int nivelLocal = entry.getValue();
                            if (nivelLocal > 1) {
                                Log.d(TAG, "⚠ No hay datos en backend para área " + areaApi + ", pero hay nivel local " + nivelLocal + ". Sincronizando al backend...");
                                String areaUI = convertirAreaBackendAUI(areaApi);
                                actualizarNivelEnBackend(context, userId, areaUI, nivelLocal);
                                hayNivelesLocalesMasNuevos = true;
                            }
                        }
                    }
                    
                    // Sincronizar vidas
                    Map<String, Map<String, Integer>> vidas = data.getVidas();
                    int vidasSincronizadas = 0;
                    if (vidas != null && !vidas.isEmpty()) {
                        Log.d(TAG, "Vidas recibidas del backend: " + vidas.size() + " áreas");
                        for (Map.Entry<String, Map<String, Integer>> areaEntry : vidas.entrySet()) {
                            String areaBackend = areaEntry.getKey(); // Área del backend: "Matematicas", "Sociales", etc.
                            Map<String, Integer> nivelesVidas = areaEntry.getValue();
                            if (nivelesVidas != null && !nivelesVidas.isEmpty()) {
                                // Convertir área del backend a área de UI que usa Android
                                String areaUI = convertirAreaBackendAUI(areaBackend);
                                Log.d(TAG, "Área: " + areaBackend + " → " + areaUI + " (" + nivelesVidas.size() + " niveles)");
                                
                                for (Map.Entry<String, Integer> nivelEntry : nivelesVidas.entrySet()) {
                                    try {
                                        int nivel = Integer.parseInt(nivelEntry.getKey());
                                        int vidasCount = nivelEntry.getValue();
                                        // Guardar usando el área de UI para que coincida con lo que usa Android
                                        LivesManager.syncFromBackend(context, userId, areaUI, nivel, vidasCount);
                                        vidasSincronizadas++;
                                        Log.d(TAG, "  ✓ Nivel " + nivel + ": " + vidasCount + " vidas");
                                        
                                        // CRÍTICO: Si un nivel tiene vidas = 0, está bloqueado y debe retrocederse
                                        // El backend ya calculó el nivel desbloqueado correctamente, pero debemos asegurarnos
                                        // de que el móvil también retroceda si detecta vidas = 0
                                        if (vidasCount == 0 && nivel > 1) {
                                            Log.w(TAG, "  ⚠ Nivel " + nivel + " bloqueado por 3 intentos fallidos (vidas = 0). Retrocediendo automáticamente.");
                                            // Retroceder automáticamente al nivel anterior
                                            int nivelActual = ProgressLockManager.getUnlockedLevel(context, userId, areaUI);
                                            if (nivelActual >= nivel) {
                                                // Solo retroceder si el nivel actual es mayor o igual al bloqueado
                                                int nivelRetrocedido = Math.max(1, nivel - 1);
                                                ProgressLockManager.syncFromBackend(context, userId, areaUI, nivelRetrocedido);
                                                Log.d(TAG, "  ✓ Retrocedido automáticamente de nivel " + nivel + " a nivel " + nivelRetrocedido);
                                                
                                                // Reiniciar vidas para el nivel retrocedido (3 vidas nuevas)
                                                LivesManager.resetLivesAndSync(context, userId, areaUI, nivelRetrocedido);
                                                Log.d(TAG, "  ✓ Vidas reiniciadas para nivel " + nivelRetrocedido + " (3 vidas)");
                                            }
                                        }
                                        
                                        // Verificar que se guardó correctamente
                                        int vidasVerificadas = LivesManager.getLives(context, userId, areaUI, nivel);
                                        Log.d(TAG, "    Verificado: vidas guardadas = " + vidasVerificadas);
                                    } catch (NumberFormatException e) {
                                        Log.e(TAG, "  ✗ Error parseando nivel: " + nivelEntry.getKey(), e);
                                    }
                                }
                            } else {
                                Log.w(TAG, "  ⚠ Área " + areaBackend + " tiene nivelesVidas null o vacío");
                            }
                        }
                    } else {
                        Log.w(TAG, "⚠ No se recibieron vidas del backend (null o vacío)");
                    }
                    
                    // Guardar timestamp de última sincronización
                    guardarTimestampSync(context);
                    Log.d(TAG, "=== SINCRONIZACIÓN COMPLETADA ===");
                    Log.d(TAG, "Niveles sincronizados: " + nivelesSincronizados);
                    Log.d(TAG, "Vidas sincronizadas: " + vidasSincronizadas);
                    
                    // Si hay niveles locales más nuevos que los del backend, hacer una sincronización completa al backend
                    if (hayNivelesLocalesMasNuevos) {
                        Log.d(TAG, "Sincronizando todos los datos locales al backend (hay niveles locales más nuevos)...");
                        sincronizarABackend(context, userId);
                    }
                    
                    // Notificar que la sincronización terminó para actualizar la UI
                    try {
                        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                        handler.post(() -> {
                            try {
                                // Enviar broadcast para que HomeActivity actualice el adapter
                                android.content.Intent intent = new android.content.Intent("com.example.zavira_movil.SYNC_COMPLETED");
                                context.sendBroadcast(intent);
                                Log.d(TAG, "✓ Broadcast enviado para actualizar UI");
                            } catch (Exception e) {
                                Log.e(TAG, "✗ Error al enviar broadcast", e);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "✗ Error al crear handler para broadcast", e);
                    }
                } else {
                    Log.e(TAG, "✗ Error al obtener datos de sincronización: HTTP " + response.code());
                    if (response.code() == 401) {
                        Log.e(TAG, "Token expirado o inválido - se requiere nuevo login");
                    } else if (response.body() == null) {
                        Log.e(TAG, "El servidor no retornó datos (body es null)");
                    }
                }
            }
            
            @Override
            public void onFailure(Call<SincronizacionResponse> call, Throwable t) {
                Log.e(TAG, "✗ Error al sincronizar desde backend", t);
                if (t != null) {
                    Log.e(TAG, "Mensaje: " + t.getMessage());
                    Log.e(TAG, "Causa: " + (t.getCause() != null ? t.getCause().getMessage() : "null"));
                }
            }
        });
    }
    
    /**
     * Sincroniza todos los datos locales al backend
     */
    public void sincronizarABackend(Context context, String userId) {
        if (apiService == null) {
            apiService = RetrofitClient.getInstance(context).create(ApiService.class);
        }
        
        Log.d(TAG, "Iniciando sincronización al backend para usuario: " + userId);
        
        // Obtener todos los niveles desbloqueados
        // Usar áreas de UI que son las que usa Android internamente
        Map<String, Integer> niveles = new HashMap<>();
        String[] areasUI = {"Matemáticas", "Sociales y ciudadanas", "Lectura crítica", "Ciencias naturales", "Inglés"};
        for (String areaUI : areasUI) {
            int nivel = ProgressLockManager.getUnlockedLevel(context, userId, areaUI);
            // Convertir área de UI a área del backend antes de enviar
            String areaApi = convertirAreaUIApi(areaUI);
            niveles.put(areaApi, nivel);
            Log.d(TAG, "Nivel " + nivel + " para área " + areaApi + " (UI: " + areaUI + ")");
        }
        
        // Obtener todas las vidas
        Map<String, Map<String, Integer>> vidas = new HashMap<>();
        for (String areaUI : areasUI) {
            // Convertir área de UI a área del backend
            String areaApi = convertirAreaUIApi(areaUI);
            Map<String, Integer> vidasPorNivel = new HashMap<>();
            for (int nivel = 2; nivel <= 6; nivel++) {
                int vidasCount = LivesManager.getLives(context, userId, areaUI, nivel);
                // Solo agregar si las vidas están inicializadas (no es -1)
                if (vidasCount >= 0) {
                    vidasPorNivel.put(String.valueOf(nivel), vidasCount);
                    Log.d(TAG, "Vidas " + vidasCount + " para nivel " + nivel + " del área " + areaApi + " (UI: " + areaUI + ")");
                }
            }
            if (!vidasPorNivel.isEmpty()) {
                vidas.put(areaApi, vidasPorNivel);
            }
        }
        
        // Enviar al backend
        SincronizarTodoRequest request = new SincronizarTodoRequest(niveles, vidas);
        apiService.sincronizarProgreso(request).enqueue(new Callback<com.example.zavira_movil.BasicResponse>() {
            @Override
            public void onResponse(Call<com.example.zavira_movil.BasicResponse> call, Response<com.example.zavira_movil.BasicResponse> response) {
                if (response.isSuccessful()) {
                    guardarTimestampSync(context);
                    Log.d(TAG, "Sincronización al backend completada exitosamente");
                } else {
                    Log.e(TAG, "Error al sincronizar al backend: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<com.example.zavira_movil.BasicResponse> call, Throwable t) {
                Log.e(TAG, "Error al sincronizar al backend", t);
            }
        });
    }
    
    /**
     * Convierte el nombre de área de UI al nombre de área que espera el backend
     * UI Android: "Matemáticas", "Sociales y ciudadanas", "Lectura crítica", "Ciencias naturales", "Inglés"
     * Backend: "Matematicas", "Sociales", "Lenguaje", "Ciencias", "Ingles"
     */
    private String convertirAreaUIApi(String areaUI) {
        if (areaUI == null) return areaUI;
        
        String area = areaUI.trim();
        
        // Mapear áreas de UI a áreas del backend
        if (area.equalsIgnoreCase("Matemáticas")) {
            return "Matematicas";
        } else if (area.equalsIgnoreCase("Sociales y ciudadanas")) {
            return "Sociales";
        } else if (area.equalsIgnoreCase("Lectura crítica")) {
            return "Lenguaje";
        } else if (area.equalsIgnoreCase("Ciencias naturales")) {
            return "Ciencias";
        } else if (area.equalsIgnoreCase("Inglés")) {
            return "Ingles";
        }
        
        // Si no coincide, usar MapeadorArea para convertir (puede que ya esté en formato API)
        return com.example.zavira_movil.niveleshome.MapeadorArea.toApiArea(area);
    }
    
    /**
     * Actualiza un nivel desbloqueado en el backend
     * @param area Área en formato UI (ej: "Matemáticas")
     */
    public void actualizarNivelEnBackend(Context context, String userId, String area, int nivel) {
        if (apiService == null) {
            apiService = RetrofitClient.getInstance(context).create(ApiService.class);
        }
        
        // Convertir área de UI a área del backend
        String areaApi = convertirAreaUIApi(area);
        Log.d(TAG, "=== ACTUALIZANDO NIVEL EN BACKEND ===");
        Log.d(TAG, "Usuario: " + userId);
        Log.d(TAG, "Área UI: " + area);
        Log.d(TAG, "Área API: " + areaApi);
        Log.d(TAG, "Nivel: " + nivel);
        
        // Validar que el nivel sea válido
        if (nivel < 1 || nivel > 6) {
            Log.e(TAG, "ERROR: Nivel inválido (" + nivel + "). Debe estar entre 1 y 6.");
            return;
        }
        
        ActualizarNivelRequest request = new ActualizarNivelRequest(areaApi, nivel);
        apiService.actualizarNivelDesbloqueado(request).enqueue(new Callback<com.example.zavira_movil.BasicResponse>() {
            @Override
            public void onResponse(Call<com.example.zavira_movil.BasicResponse> call, Response<com.example.zavira_movil.BasicResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✓ Nivel " + nivel + " actualizado exitosamente en backend para área " + areaApi);
                    
                    // Verificar inmediatamente después de actualizar que se guardó correctamente
                    // Hacer una consulta al backend para verificar que el nivel se guardó
                    android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                    handler.postDelayed(() -> {
                        verificarNivelEnBackend(context, userId, areaApi, nivel);
                    }, 1000); // Esperar 1 segundo para que el backend procese la actualización
                } else {
                    Log.e(TAG, "✗ ERROR al actualizar nivel en backend: HTTP " + response.code());
                    if (response.code() == 400) {
                        Log.e(TAG, "Error 400: Solicitud inválida (área o nivel incorrectos)");
                    } else if (response.code() == 401) {
                        Log.e(TAG, "Error 401: Token expirado o inválido");
                    } else if (response.code() == 500) {
                        Log.e(TAG, "Error 500: Error interno del servidor");
                    }
                    
                    // Intentar sincronizar todo el progreso local al backend como respaldo
                    Log.d(TAG, "Intentando sincronizar todo el progreso local al backend como respaldo...");
                    sincronizarABackend(context, userId);
                }
            }
            
            @Override
            public void onFailure(Call<com.example.zavira_movil.BasicResponse> call, Throwable t) {
                Log.e(TAG, "✗ ERROR de red al actualizar nivel en backend", t);
                if (t != null) {
                    Log.e(TAG, "Mensaje: " + t.getMessage());
                    Log.e(TAG, "Causa: " + (t.getCause() != null ? t.getCause().getMessage() : "null"));
                }
                
                // Intentar sincronizar todo el progreso local al backend como respaldo
                Log.d(TAG, "Intentando sincronizar todo el progreso local al backend como respaldo...");
                sincronizarABackend(context, userId);
            }
        });
    }
    
    /**
     * Verifica que el nivel se guardó correctamente en el backend
     */
    private void verificarNivelEnBackend(Context context, String userId, String areaApi, int nivelEsperado) {
        Log.d(TAG, "Verificando nivel en backend: área=" + areaApi + ", nivel esperado=" + nivelEsperado);
        
        if (apiService == null) {
            apiService = RetrofitClient.getInstance(context).create(ApiService.class);
        }
        
        apiService.obtenerProgresoSincronizacion().enqueue(new Callback<SincronizacionResponse>() {
            @Override
            public void onResponse(Call<SincronizacionResponse> call, Response<SincronizacionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SincronizacionResponse data = response.body();
                    Map<String, Integer> niveles = data.getNiveles();
                    
                    if (niveles != null && niveles.containsKey(areaApi)) {
                        int nivelBackend = niveles.get(areaApi);
                        if (nivelBackend == nivelEsperado) {
                            Log.d(TAG, "✓ Verificación exitosa: nivel en backend = " + nivelBackend + " (esperado: " + nivelEsperado + ")");
                        } else {
                            Log.w(TAG, "⚠ Verificación falló: nivel en backend = " + nivelBackend + " (esperado: " + nivelEsperado + ")");
                        }
                    } else {
                        Log.w(TAG, "⚠ Verificación falló: área " + areaApi + " no encontrada en respuesta del backend");
                    }
                } else {
                    Log.e(TAG, "✗ Error al verificar nivel en backend: HTTP " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<SincronizacionResponse> call, Throwable t) {
                Log.e(TAG, "✗ Error de red al verificar nivel en backend", t);
            }
        });
    }
    
    /**
     * Actualiza las vidas en el backend
     * @param area Área en formato UI (ej: "Matemáticas")
     */
    public void actualizarVidasEnBackend(Context context, String userId, String area, int nivel, int vidas) {
        if (apiService == null) {
            apiService = RetrofitClient.getInstance(context).create(ApiService.class);
        }
        
        // Convertir área de UI a área del backend
        String areaApi = convertirAreaUIApi(area);
        Log.d(TAG, "Actualizando " + vidas + " vidas para nivel " + nivel + " del área " + areaApi + " (UI: " + area + ") en backend");
        
        ActualizarVidasRequest request = new ActualizarVidasRequest(areaApi, nivel, vidas);
        apiService.actualizarVidas(request).enqueue(new Callback<com.example.zavira_movil.BasicResponse>() {
            @Override
            public void onResponse(Call<com.example.zavira_movil.BasicResponse> call, Response<com.example.zavira_movil.BasicResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Vidas actualizadas exitosamente en backend");
                } else {
                    Log.e(TAG, "Error al actualizar vidas en backend: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<com.example.zavira_movil.BasicResponse> call, Throwable t) {
                Log.e(TAG, "Error de red al actualizar vidas en backend", t);
            }
        });
    }
    
    private void guardarTimestampSync(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_SYNC, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_LAST_SYNC, System.currentTimeMillis()).apply();
    }
    
    public long getLastSyncTimestamp(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_SYNC, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_LAST_SYNC, 0);
    }
}

