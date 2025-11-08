package com.example.zavira_movil.niveleshome;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Control de vidas/intentos por nivel y área.
 * - Nivel 1: Sin límite de intentos
 * - Niveles 2+: 3 vidas para pasar al siguiente nivel
 */
public final class LivesManager {

    private static final String PREFS = "lives_prefs";
    private static final int MAX_LIVES = 3;
    private static final int MIN_LEVEL = 1;

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
            // Sincronizar con backend de forma asíncrona
            com.example.zavira_movil.sincronizacion.ProgresoSincronizador.getInstance()
                .actualizarVidasEnBackend(c, userId, area, level, MAX_LIVES);
        }
    }
}

