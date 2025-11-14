package com.example.zavira_movil.niveleshome;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Control de vidas/intentos por nivel y área.
 * - Nivel 1: Sin límite de intentos
 * - Niveles 2+: 3 vidas para pasar al siguiente nivel
 * - Recarga automática: 5 minutos por vida
 * - Recarga por detalle: media vida, solo una vez por intento fallido
 */
public final class LivesManager {

    private static final String PREFS = "lives_prefs";
    private static final int MAX_LIVES = 3;
    private static final int MIN_LEVEL = 1;
    private static final long RECARGA_AUTOMATICA_MS = 5 * 60 * 1000L; // 5 minutos en milisegundos
    private static final float RECARGA_DETALLE = 0.5f; // Media vida

    private LivesManager() {}

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Construir clave única por usuario, área y nivel. */
    private static String key(String userId, String area, int level) {
        if (TextUtils.isEmpty(area)) area = "default_area";
        if (TextUtils.isEmpty(userId)) userId = "default_user";
        return "lives_" + userId.trim() + "_" + area.trim() + "_level_" + level;
    }
    
    /** Construir clave para timestamps de vidas perdidas. */
    private static String keyTimestamp(String userId, String area, int level, int vidaIndex) {
        if (TextUtils.isEmpty(area)) area = "default_area";
        if (TextUtils.isEmpty(userId)) userId = "default_user";
        return "life_timestamp_" + userId.trim() + "_" + area.trim() + "_level_" + level + "_life_" + vidaIndex;
    }
    
    /** Construir clave para vidas parciales (recarga por detalle). */
    private static String keyPartialLives(String userId, String area, int level) {
        if (TextUtils.isEmpty(area)) area = "default_area";
        if (TextUtils.isEmpty(userId)) userId = "default_user";
        return "partial_lives_" + userId.trim() + "_" + area.trim() + "_level_" + level;
    }
    
    /** Construir clave para indicar si ya se usó recarga por detalle en este intento fallido. */
    private static String keyDetalleUsed(String userId, String area, int level) {
        if (TextUtils.isEmpty(area)) area = "default_area";
        if (TextUtils.isEmpty(userId)) userId = "default_user";
        return "detalle_used_" + userId.trim() + "_" + area.trim() + "_level_" + level;
    }
    
    /** Construir clave para timestamp de cuando se recargó media vida por detalle. */
    private static String keyPartialLivesTimestamp(String userId, String area, int level) {
        if (TextUtils.isEmpty(area)) area = "default_area";
        if (TextUtils.isEmpty(userId)) userId = "default_user";
        return "partial_lives_timestamp_" + userId.trim() + "_" + area.trim() + "_level_" + level;
    }

    /**
     * Obtiene las vidas restantes para un nivel.
     * - Nivel 1: Siempre retorna MAX_LIVES (sin límite visual)
     * - Niveles 2+: Retorna las vidas guardadas
     * IMPORTANTE: Si no hay valor guardado, retorna -1 para indicar que no está inicializado
     * Esto evita mostrar 3 vidas cuando realmente no hay datos
     */
    public static int getLives(Context c, String userId, String area, int level) {
        if (level <= MIN_LEVEL) {
            // Nivel 1: sin límite de intentos
            return MAX_LIVES;
        }
        // Usar -1 como valor por defecto para indicar "no inicializado"
        // Solo retornar MAX_LIVES si explícitamente se ha guardado un valor
        return prefs(c).getInt(key(userId, area, level), -1);
    }
    
    /**
     * Verifica si las vidas están inicializadas para un nivel
     */
    public static boolean hasLivesInitialized(Context c, String userId, String area, int level) {
        if (level <= MIN_LEVEL) {
            return true; // Nivel 1 siempre tiene vidas
        }
        return prefs(c).contains(key(userId, area, level));
    }

    /**
     * Consume una vida cuando el estudiante no pasa el nivel.
     * - Nivel 1: No consume vidas (siempre retorna true)
     * - Niveles 2+: Consume una vida y retorna true si quedan vidas, false si se acabaron
     * IMPORTANTE: Si las vidas no están inicializadas (-1), inicializarlas con MAX_LIVES primero
     * CRÍTICO: Usar commit() en lugar de apply() para asegurar que la escritura se complete inmediatamente
     * También guarda el timestamp de cuando se perdió la vida para recarga automática
     */
    public static boolean consumeLife(Context c, String userId, String area, int level) {
        if (level <= MIN_LEVEL) {
            // Nivel 1: sin límite de intentos
            return true;
        }
        
        SharedPreferences.Editor editor = prefs(c).edit();
        String key = key(userId, area, level);
        int currentLives = prefs(c).getInt(key, -1);
        
        // Si no están inicializadas (-1), inicializar con MAX_LIVES
        if (currentLives == -1) {
            currentLives = MAX_LIVES;
            editor.putInt(key, currentLives);
            // Usar commit() para asegurar que se guarde inmediatamente
            if (!editor.commit()) {
                android.util.Log.e("LivesManager", "ERROR: No se pudo inicializar vidas");
                return false;
            }
            android.util.Log.d("LivesManager", "Vidas inicializadas: " + currentLives + " para nivel " + level);
        }
        
        // Verificar nuevamente después de la inicialización
        if (currentLives <= 0) {
            android.util.Log.w("LivesManager", "No hay vidas para consumir (currentLives=" + currentLives + ")");
            return false;
        }
        
        // Consumir UNA vida
        int newLives = currentLives - 1;
        editor = prefs(c).edit();
        editor.putInt(key, newLives);
        
        // Guardar timestamp de cuando se perdió esta vida (índice basado en vidas perdidas)
        // Si tenía 3 vidas y ahora tiene 2, perdió la vida índice 0 (primera vida perdida)
        int vidaPerdidaIndex = MAX_LIVES - currentLives;
        long timestampActual = System.currentTimeMillis();
        editor.putLong(keyTimestamp(userId, area, level, vidaPerdidaIndex), timestampActual);
        
        // Resetear flag de recarga por detalle usado (nuevo intento fallido)
        // IMPORTANTE: Solo resetear el flag cuando se consume una vida (intento fallido)
        // Esto permite que el usuario pueda recargar media vida viendo el detalle de este intento fallido
        editor.putBoolean(keyDetalleUsed(userId, area, level), false);
        
        // IMPORTANTE: NO resetear vidas parciales si ya existe una media vida cargándose
        // Si hay una media vida existente, debe mantenerse para que se pueda completar
        float partialLives = prefs(c).getFloat(keyPartialLives(userId, area, level), 0f);
        if (partialLives <= 0) {
            // Solo resetear si no hay vidas parciales existentes
            editor.putFloat(keyPartialLives(userId, area, level), 0f);
            editor.remove(keyPartialLivesTimestamp(userId, area, level));
        } else {
            // Si hay vidas parciales, mantenerlas (la media vida existente se completará cuando se vea el detalle)
            android.util.Log.d("LivesManager", "Manteniendo vidas parciales existentes: " + partialLives);
        }
        
        // Usar commit() para asegurar que se guarde inmediatamente
        if (!editor.commit()) {
            android.util.Log.e("LivesManager", "ERROR: No se pudo consumir vida");
            return currentLives > 1; // Retornar basado en el valor anterior
        }
        
        android.util.Log.d("LivesManager", "Vida consumida: " + currentLives + " -> " + newLives + " (nivel " + level + ")");
        return newLives > 0;
    }

    /**
     * Reinicia las vidas cuando el estudiante pasa al siguiente nivel.
     * Cuando pasa del nivel N al N+1, se reinician las vidas para el nivel N+1.
     */
    public static void resetLivesForNextLevel(Context c, String userId, String area, int nextLevel) {
        if (nextLevel > MIN_LEVEL) {
            prefs(c).edit().putInt(key(userId, area, nextLevel), MAX_LIVES).apply();
        }
    }

    /**
     * Reinicia las vidas para un nivel específico (útil cuando retrocede).
     */
    public static void resetLives(Context c, String userId, String area, int level) {
        if (level > MIN_LEVEL) {
            prefs(c).edit().putInt(key(userId, area, level), MAX_LIVES).apply();
        }
    }

    /**
     * Verifica si el estudiante puede intentar el nivel (tiene vidas).
     * - Nivel 1: Siempre retorna true
     * - Niveles 2+: Retorna true si tiene vidas > 0 O si no está inicializado (asume que tiene vidas)
     */
    public static boolean canAttempt(Context c, String userId, String area, int level) {
        if (level <= MIN_LEVEL) {
            return true;
        }
        int vidas = getLives(c, userId, area, level);
        // Si no está inicializado (-1), asumir que tiene vidas (aún no se ha sincronizado)
        if (vidas == -1) {
            return true;
        }
        return vidas > 0;
    }

    /**
     * Sincroniza vidas desde el backend (para uso interno del sincronizador)
     */
    public static void syncFromBackend(Context c, String userId, String area, int level, int vidas) {
        if (level > MIN_LEVEL) {
            int targetVidas = Math.max(0, Math.min(MAX_LIVES, vidas));
            String key = key(userId, area, level);
            prefs(c).edit().putInt(key, targetVidas).apply();
            android.util.Log.d("LivesManager", "syncFromBackend: userId=" + userId + ", area=" + area + ", level=" + level + ", vidas=" + targetVidas + ", key=" + key);
            
            // Verificar inmediatamente después de guardar
            int verificado = prefs(c).getInt(key, -1);
            if (verificado != targetVidas) {
                android.util.Log.e("LivesManager", "ERROR: Las vidas no se guardaron correctamente. Esperado: " + targetVidas + ", Obtenido: " + verificado);
            } else {
                android.util.Log.d("LivesManager", "✓ Vidas guardadas y verificadas correctamente");
            }
        }
    }

    /**
     * Consume una vida y sincroniza con el backend
     * CRÍTICO: Este método debe llamarse SOLO UNA VEZ por intento fallido
     */
    public static boolean consumeLifeAndSync(Context c, String userId, String area, int level) {
        // Obtener vidas ANTES de consumir para logging
        int vidasAntes = getLives(c, userId, area, level);
        android.util.Log.d("LivesManager", "consumeLifeAndSync: ANTES - vidas=" + vidasAntes + " (nivel " + level + ")");
        
        // Consumir UNA vida
        boolean result = consumeLife(c, userId, area, level);
        
        // Obtener vidas DESPUÉS de consumir
        int nuevasVidas = getLives(c, userId, area, level);
        android.util.Log.d("LivesManager", "consumeLifeAndSync: DESPUÉS - vidas=" + nuevasVidas + ", result=" + result + " (nivel " + level + ")");
        
        if (level > MIN_LEVEL) {
            // Sincronizar con backend de forma asíncrona
            com.example.zavira_movil.sincronizacion.ProgresoSincronizador.getInstance()
                .actualizarVidasEnBackend(c, userId, area, level, nuevasVidas);
        }
        return result;
    }

    /**
     * Reinicia las vidas para el siguiente nivel y sincroniza con el backend
     */
    public static void resetLivesForNextLevelAndSync(Context c, String userId, String area, int nextLevel) {
        resetLivesForNextLevel(c, userId, area, nextLevel);
        if (nextLevel > MIN_LEVEL) {
            // Sincronizar con backend de forma asíncrona
            com.example.zavira_movil.sincronizacion.ProgresoSincronizador.getInstance()
                .actualizarVidasEnBackend(c, userId, area, nextLevel, MAX_LIVES);
        }
    }

    /**
     * Reinicia las vidas para un nivel específico y sincroniza con el backend
     */
    public static void resetLivesAndSync(Context c, String userId, String area, int level) {
        resetLives(c, userId, area, level);
        if (level > MIN_LEVEL) {
            // Limpiar timestamps y flags de recarga
            SharedPreferences.Editor editor = prefs(c).edit();
            for (int i = 0; i < MAX_LIVES; i++) {
                editor.remove(keyTimestamp(userId, area, level, i));
            }
            editor.remove(keyPartialLives(userId, area, level));
            editor.remove(keyDetalleUsed(userId, area, level));
            editor.remove(keyPartialLivesTimestamp(userId, area, level));
            editor.apply();
            
            // Sincronizar con backend de forma asíncrona
            com.example.zavira_movil.sincronizacion.ProgresoSincronizador.getInstance()
                .actualizarVidasEnBackend(c, userId, area, level, MAX_LIVES);
        }
    }
    
    /**
     * Verifica y recarga vidas automáticamente si han pasado 5 minutos desde que se perdió.
     * Retorna las vidas actualizadas (incluyendo recarga automática).
     */
    public static int getLivesWithAutoRecharge(Context c, String userId, String area, int level) {
        if (level <= MIN_LEVEL) {
            return MAX_LIVES;
        }
        
        int currentLives = getLives(c, userId, area, level);
        if (currentLives == -1) {
            // No inicializado, retornar -1
            return -1;
        }
        
        // Obtener vidas parciales (de recarga por detalle)
        float partialLives = prefs(c).getFloat(keyPartialLives(userId, area, level), 0f);
        
        // Verificar cada vida perdida para ver si debe recargarse
        long currentTime = System.currentTimeMillis();
        int vidasPerdidas = MAX_LIVES - currentLives;
        int vidasARecargar = 0;
        
        SharedPreferences.Editor editor = prefs(c).edit();
        boolean needsUpdate = false;
        
        // Verificar vidas perdidas en orden (primero la más antigua)
        for (int i = 0; i < vidasPerdidas; i++) {
            long timestamp = prefs(c).getLong(keyTimestamp(userId, area, level, i), 0);
            if (timestamp > 0) {
                long tiempoTranscurrido = currentTime - timestamp;
                if (tiempoTranscurrido >= RECARGA_AUTOMATICA_MS) {
                    // Esta vida se ha recargado automáticamente
                    vidasARecargar++;
                    // Limpiar el timestamp
                    editor.remove(keyTimestamp(userId, area, level, i));
                    // Mover timestamps de vidas posteriores hacia adelante
                    for (int j = i + 1; j < vidasPerdidas; j++) {
                        long nextTimestamp = prefs(c).getLong(keyTimestamp(userId, area, level, j), 0);
                        if (nextTimestamp > 0) {
                            editor.putLong(keyTimestamp(userId, area, level, j - 1), nextTimestamp);
                            editor.remove(keyTimestamp(userId, area, level, j));
                        }
                    }
                    needsUpdate = true;
                    break; // Solo recargar una vida a la vez (la más antigua)
                }
            }
        }
        
        if (needsUpdate && vidasARecargar > 0) {
            // Recargar vida(s)
            int newLives = Math.min(MAX_LIVES, currentLives + vidasARecargar);
            editor.putInt(key(userId, area, level), newLives);
            editor.apply();
            android.util.Log.d("LivesManager", "Vida recargada automáticamente: " + currentLives + " -> " + newLives);
            currentLives = newLives;
        }
        
        // Si hay vidas parciales, verificar si el tiempo de recarga se completó
        if (partialLives > 0 && currentLives < MAX_LIVES) {
            long timestampRecarga = prefs(c).getLong(keyPartialLivesTimestamp(userId, area, level), 0);
            if (timestampRecarga > 0) {
                long tiempoTranscurrido = currentTime - timestampRecarga;
                long tiempoRecargaMediaVida = RECARGA_AUTOMATICA_MS / 2; // 2.5 minutos
                
                // Si ya pasó el tiempo de recarga (2.5 minutos), completar la vida
                if (tiempoTranscurrido >= tiempoRecargaMediaVida) {
                    // Completar la vida automáticamente
                    int nuevasVidas = currentLives + 1;
                    editor.putInt(key(userId, area, level), nuevasVidas);
                    editor.remove(keyPartialLives(userId, area, level));
                    editor.remove(keyPartialLivesTimestamp(userId, area, level));
                    
                    // IMPORTANTE: Si después de completar la media vida aún hay vidas vacías,
                    // crear/reemplazar el timestamp para la siguiente vida que comience desde ahora (5 minutos)
                    // Esto asegura que la siguiente vida comience a contar desde 0 cuando se completa la media vida
                    if (nuevasVidas < MAX_LIVES) {
                        // La siguiente vida en recargarse es la que está en el índice 0 (la más antigua)
                        // SIEMPRE reemplazar el timestamp con uno nuevo que comience desde ahora
                        // porque cuando se completa una media vida, la siguiente vida debe comenzar desde 0
                        long nuevoTimestamp = System.currentTimeMillis();
                        editor.putLong(keyTimestamp(userId, area, level, 0), nuevoTimestamp);
                        android.util.Log.d("LivesManager", "Timestamp reemplazado para vida siguiente después de completar media vida (comienza desde ahora): " + nuevoTimestamp);
                    }
                    
                    editor.apply();
                    currentLives = nuevasVidas;
                    android.util.Log.d("LivesManager", "Vida completada automáticamente desde parcial en getLivesWithAutoRecharge: " + (nuevasVidas - 1) + " -> " + nuevasVidas);
                } else {
                    // Aún no se completa, mantener las vidas parciales
                    float totalConParcial = currentLives + partialLives;
                    int totalEntero = (int) Math.floor(totalConParcial);
                    if (totalEntero > currentLives && totalEntero <= MAX_LIVES) {
                        editor.putInt(key(userId, area, level), totalEntero);
                        float nuevoParcial = totalConParcial - totalEntero;
                        if (nuevoParcial > 0) {
                            editor.putFloat(keyPartialLives(userId, area, level), nuevoParcial);
                        } else {
                            editor.remove(keyPartialLives(userId, area, level));
                        }
                        editor.apply();
                        currentLives = totalEntero;
                    }
                }
            } else {
                // Si no hay timestamp pero hay vidas parciales, intentar agregarlas al total
                float totalConParcial = currentLives + partialLives;
                int totalEntero = (int) Math.floor(totalConParcial);
                if (totalEntero > currentLives && totalEntero <= MAX_LIVES) {
                    editor.putInt(key(userId, area, level), totalEntero);
                    float nuevoParcial = totalConParcial - totalEntero;
                    if (nuevoParcial > 0) {
                        editor.putFloat(keyPartialLives(userId, area, level), nuevoParcial);
                    } else {
                        editor.remove(keyPartialLives(userId, area, level));
                    }
                    editor.apply();
                    currentLives = totalEntero;
                }
            }
        }
        
        return currentLives;
    }
    
    /**
     * Obtiene el tiempo restante en milisegundos para recargar la próxima vida automáticamente.
     * Retorna 0 si no hay vidas por recargar o si la próxima ya está lista.
     * IMPORTANTE: Si hay media vida (partialLives > 0), el tiempo de recarga es la mitad (2.5 minutos)
     * y debe retroceder desde 2.5 minutos hasta 0.
     */
    public static long getTiempoRestanteRecarga(Context c, String userId, String area, int level) {
        if (level <= MIN_LEVEL) {
            return 0;
        }
        
        // IMPORTANTE: Verificar vidas parciales ANTES de llamar getLivesWithAutoRecharge
        // para evitar que se limpien antes de calcular el tiempo
        float partialLives = getPartialLives(c, userId, area, level);
        
        // Si hay media vida, calcular el tiempo restante basado en el timestamp de recarga
        if (partialLives > 0) {
            long timestampRecarga = prefs(c).getLong(keyPartialLivesTimestamp(userId, area, level), 0);
            if (timestampRecarga > 0) {
                long currentTime = System.currentTimeMillis();
                long tiempoTranscurrido = currentTime - timestampRecarga;
                long tiempoRecargaMediaVida = RECARGA_AUTOMATICA_MS / 2; // 2.5 minutos
                long tiempoRestante = tiempoRecargaMediaVida - tiempoTranscurrido;
                
                // Si ya pasó el tiempo, retornar 0 (la vida se completará en getLivesWithAutoRecharge)
                if (tiempoRestante <= 0) {
                    return 0;
                }
                
                // Retornar el tiempo restante que va retrocediendo cada segundo
                return tiempoRestante;
            } else {
                // Si no hay timestamp pero hay vidas parciales, crear uno ahora
                // Esto puede pasar si se recargó antes de implementar el timestamp
                SharedPreferences.Editor editor = prefs(c).edit();
                long nuevoTimestamp = System.currentTimeMillis();
                editor.putLong(keyPartialLivesTimestamp(userId, area, level), nuevoTimestamp);
                editor.commit(); // Usar commit() para guardar inmediatamente
                android.util.Log.d("LivesManager", "Timestamp creado para media vida: " + nuevoTimestamp);
                return RECARGA_AUTOMATICA_MS / 2;
            }
        }
        
        // Si no hay media vida, obtener vidas actuales y calcular el tiempo normal
        int currentLives = getLives(c, userId, area, level);
        if (currentLives == -1 || currentLives >= MAX_LIVES) {
            return 0; // No hay vidas por recargar
        }
        
        // Calcular el tiempo normal basado en timestamps
        int vidasPerdidas = MAX_LIVES - currentLives;
        long currentTime = System.currentTimeMillis();
        long tiempoMinimo = Long.MAX_VALUE;
        
        // Encontrar el timestamp más antiguo (la próxima vida en recargarse)
        for (int i = 0; i < vidasPerdidas; i++) {
            long timestamp = prefs(c).getLong(keyTimestamp(userId, area, level, i), 0);
            if (timestamp > 0) {
                long tiempoTranscurrido = currentTime - timestamp;
                long tiempoRestante = RECARGA_AUTOMATICA_MS - tiempoTranscurrido;
                if (tiempoRestante > 0 && tiempoRestante < tiempoMinimo) {
                    tiempoMinimo = tiempoRestante;
                }
            }
        }
        
        return tiempoMinimo == Long.MAX_VALUE ? 0 : Math.max(0, tiempoMinimo);
    }
    
    /**
     * Completa una vida entera cuando el tiempo de recarga de media vida se agota.
     */
    private static void completarVidaDesdeParcial(Context c, String userId, String area, int level) {
        int currentLives = getLives(c, userId, area, level);
        float partialLives = getPartialLives(c, userId, area, level);
        
        if (currentLives < MAX_LIVES && partialLives > 0) {
            // Completar la vida
            int nuevasVidas = currentLives + 1;
            SharedPreferences.Editor editor = prefs(c).edit();
            editor.putInt(key(userId, area, level), nuevasVidas);
            editor.remove(keyPartialLives(userId, area, level));
            editor.remove(keyPartialLivesTimestamp(userId, area, level));
            editor.apply();
            
            android.util.Log.d("LivesManager", "Vida completada automáticamente desde parcial: " + currentLives + " -> " + nuevasVidas);
        }
    }
    
    /**
     * Recarga media vida por ver el detalle/historial.
     * Solo funciona una vez por intento fallido.
     * LÓGICA:
     * - Si hay una media vida existente cargándose → completarla (llenar la que está a la mitad)
     * - Si no hay media vida → agregar media vida a la vida que acaba de perder (2.5 minutos)
     * Retorna true si se recargó, false si ya se usó en este intento fallido.
     */
    public static boolean recargarPorDetalle(Context c, String userId, String area, int level) {
        if (level <= MIN_LEVEL) {
            return false; // Nivel 1 no tiene vidas
        }
        
        // Verificar si ya se usó la recarga por detalle en este intento fallido
        boolean yaUsado = prefs(c).getBoolean(keyDetalleUsed(userId, area, level), false);
        if (yaUsado) {
            android.util.Log.d("LivesManager", "Recarga por detalle ya usada en este intento fallido");
            return false;
        }
        
        // Obtener vidas actuales
        int currentLives = getLivesWithAutoRecharge(c, userId, area, level);
        if (currentLives == -1) {
            currentLives = MAX_LIVES;
        }
        
        // Si ya tiene todas las vidas, no recargar
        if (currentLives >= MAX_LIVES) {
            return false;
        }
        
        // Verificar si hay una media vida existente cargándose
        float partialLives = prefs(c).getFloat(keyPartialLives(userId, area, level), 0f);
        SharedPreferences.Editor editor = prefs(c).edit();
        
        if (partialLives > 0) {
            // CASO 1: Hay una media vida existente → completarla (llenar la que está a la mitad)
            // Completar la vida sumando la media vida existente
            int nuevasVidas = currentLives + 1;
            editor.putInt(key(userId, area, level), nuevasVidas);
            editor.remove(keyPartialLives(userId, area, level));
            editor.remove(keyPartialLivesTimestamp(userId, area, level));
            
            // IMPORTANTE: Si después de completar la media vida aún hay vidas vacías,
            // crear/reemplazar el timestamp para la siguiente vida que comience desde ahora (5 minutos)
            // Esto asegura que la siguiente vida comience a contar desde 0 cuando se completa la media vida
            if (nuevasVidas < MAX_LIVES) {
                // La siguiente vida en recargarse es la que está en el índice 0 (la más antigua)
                // SIEMPRE reemplazar el timestamp con uno nuevo que comience desde ahora
                long nuevoTimestamp = System.currentTimeMillis();
                editor.putLong(keyTimestamp(userId, area, level, 0), nuevoTimestamp);
                android.util.Log.d("LivesManager", "Recarga por detalle: Timestamp reemplazado para vida siguiente después de completar media vida (comienza desde ahora): " + nuevoTimestamp);
            }
            
            android.util.Log.d("LivesManager", "Recarga por detalle: Completando media vida existente: " + currentLives + " -> " + nuevasVidas);
        } else {
            // CASO 2: No hay media vida → agregar media vida a la vida que acaba de perder
            // Agregar media vida
            partialLives = RECARGA_DETALLE;
            
            // Verificar si se completa una vida entera
            float totalConParcial = currentLives + partialLives;
            int totalEntero = (int) Math.floor(totalConParcial);
            
            if (totalEntero > currentLives && totalEntero <= MAX_LIVES) {
                // Se completó al menos una vida entera
                editor.putInt(key(userId, area, level), totalEntero);
                float nuevoParcial = totalConParcial - totalEntero;
                if (nuevoParcial > 0) {
                    editor.putFloat(keyPartialLives(userId, area, level), nuevoParcial);
                    // Guardar timestamp solo si queda vida parcial
                    long timestampActual = System.currentTimeMillis();
                    editor.putLong(keyPartialLivesTimestamp(userId, area, level), timestampActual);
                    android.util.Log.d("LivesManager", "Timestamp guardado para vida parcial restante: " + timestampActual);
                } else {
                    editor.remove(keyPartialLives(userId, area, level));
                    editor.remove(keyPartialLivesTimestamp(userId, area, level));
                }
                android.util.Log.d("LivesManager", "Recarga por detalle: " + currentLives + " -> " + totalEntero + " (media vida agregada)");
            } else {
                // Solo se agregó vida parcial - guardar timestamp para contar el tiempo (2.5 minutos)
                editor.putFloat(keyPartialLives(userId, area, level), partialLives);
                // Guardar timestamp de cuando se recargó la media vida
                long timestampActual = System.currentTimeMillis();
                editor.putLong(keyPartialLivesTimestamp(userId, area, level), timestampActual);
                android.util.Log.d("LivesManager", "Recarga por detalle: " + currentLives + " + " + partialLives + " (media vida parcial, timestamp guardado: " + timestampActual + ")");
            }
        }
        
        // Marcar que se usó la recarga por detalle
        editor.putBoolean(keyDetalleUsed(userId, area, level), true);
        // Usar commit() para asegurar que se guarde inmediatamente
        editor.commit();
        
        return true;
    }
    
    /**
     * Verifica si se puede usar la recarga por detalle (no se ha usado en este intento fallido).
     */
    public static boolean puedeRecargarPorDetalle(Context c, String userId, String area, int level) {
        if (level <= MIN_LEVEL) {
            return false;
        }
        
        // Verificar si ya se usó
        boolean yaUsado = prefs(c).getBoolean(keyDetalleUsed(userId, area, level), false);
        if (yaUsado) {
            return false;
        }
        
        // Verificar si tiene todas las vidas
        int currentLives = getLivesWithAutoRecharge(c, userId, area, level);
        if (currentLives == -1) {
            currentLives = MAX_LIVES;
        }
        
        return currentLives < MAX_LIVES;
    }
    
    /**
     * Obtiene las vidas parciales (de recarga por detalle) para un nivel.
     * Retorna un valor entre 0.0 y 1.0 que representa la fracción de vida parcial.
     */
    public static float getPartialLives(Context c, String userId, String area, int level) {
        if (level <= MIN_LEVEL) {
            return 0f;
        }
        return prefs(c).getFloat(keyPartialLives(userId, area, level), 0f);
    }
    
    /**
     * Limpia vidas parciales y crea un nuevo timestamp para la siguiente vida vacía.
     * Se usa cuando el usuario presiona "Reintentar" sin ver el detalle.
     * La vida queda completamente vacía y comienza a recargarse desde ahora (5 minutos).
     */
    public static void limpiarVidasParcialesYCrearTimestamp(Context c, String userId, String area, int level) {
        if (level <= MIN_LEVEL) {
            return;
        }
        
        SharedPreferences.Editor editor = prefs(c).edit();
        
        // Limpiar vidas parciales
        editor.remove(keyPartialLives(userId, area, level));
        editor.remove(keyPartialLivesTimestamp(userId, area, level));
        
        // Obtener vidas actuales
        int currentLives = getLives(c, userId, area, level);
        if (currentLives == -1) {
            currentLives = MAX_LIVES;
        }
        
        // Si hay vidas vacías, crear/reemplazar timestamp para la siguiente vida que comience desde ahora
        if (currentLives < MAX_LIVES) {
            int vidasPerdidas = MAX_LIVES - currentLives;
            // La siguiente vida en recargarse es la que está en el índice 0 (la más antigua)
            long nuevoTimestamp = System.currentTimeMillis();
            editor.putLong(keyTimestamp(userId, area, level, 0), nuevoTimestamp);
            android.util.Log.d("LivesManager", "Vidas parciales limpiadas y timestamp creado para vida siguiente (comienza desde ahora): " + nuevoTimestamp);
        }
        
        editor.commit();
    }
    
    /**
     * Formatea el tiempo restante en formato legible (ej: "4 min 30s" o "2 min" o "45s").
     */
    public static String formatearTiempoRestante(long tiempoMs) {
        if (tiempoMs <= 0) {
            return "0s";
        }
        
        long segundos = tiempoMs / 1000;
        long minutos = segundos / 60;
        long segundosRestantes = segundos % 60;
        
        if (minutos > 0) {
            if (segundosRestantes > 0) {
                return minutos + " min " + segundosRestantes + "s";
            } else {
                return minutos + " min";
            }
        } else {
            return segundos + "s";
        }
    }
}

