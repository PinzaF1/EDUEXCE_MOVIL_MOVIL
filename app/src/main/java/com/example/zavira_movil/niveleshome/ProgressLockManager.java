package com.example.zavira_movil.niveleshome;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Control de niveles por área.
 * Niveles jugables: 1..5
 * Nivel 6 = bandera "Examen Final desbloqueado".
 */
public final class ProgressLockManager {

    private static final String PREFS = "progress_lock_prefs";
    private static final int MIN_LEVEL = 1;
    /** Usamos 6 como “final desbloqueado” */
    private static final int MAX_LEVEL = 6;

    private ProgressLockManager() {}

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Construir clave única por usuario y área (usa NOMBRE UI del área). */
    private static String key(String userId, String area) {
        if (TextUtils.isEmpty(area)) area = "default_area";
        if (TextUtils.isEmpty(userId)) userId = "default_user";
        return "unlocked_level_" + userId.trim() + "_" + area.trim();
    }

    /** Nivel más alto desbloqueado para esa área (1..6). Por defecto 1. */
    public static int getUnlockedLevel(Context c, String userId, String area) {
        int v = prefs(c).getInt(key(userId, area), MIN_LEVEL);
        if (v < MIN_LEVEL) v = MIN_LEVEL;
        if (v > MAX_LEVEL) v = MAX_LEVEL;
        return v;
    }

    /** true si ese nivel (1..5) está desbloqueado. */
    public static boolean isLevelUnlocked(Context c, String userId, String area, int level) {
        return level <= getUnlockedLevel(c, userId, area);
    }

    /** Desbloquea el siguiente nivel (para aprobados de 1..4). */
    public static void unlockNext(Context c, String userId, String area, int currentLevel) {
        int now = getUnlockedLevel(c, userId, area);
        int cap = 5;
        int target = Math.max(now, Math.min(cap, currentLevel + 1));
        prefs(c).edit().putInt(key(userId, area), target).apply();
    }

    /** Marca "Examen Final desbloqueado": guardamos 6. */
    public static void unlockFinalExam(Context c, String userId, String area) {
        int now = getUnlockedLevel(c, userId, area);
        if (now < 6) {
            prefs(c).edit().putInt(key(userId, area), 6).apply();
        }
    }

    /**
     * Retroceso condicionado por fallo en un nivel (usa el nivel actual).
     * Retrocede al nivel anterior (nivel actual - 1), pero nunca baja del 1.
     * CRÍTICO: Retrocede INMEDIATAMENTE cuando se agotan las vidas, bloqueando el nivel actual.
     */
    public static void retrocederPorFallo(Context c, String userId, String area, int currentLevel) {
        if (currentLevel <= 1) return; // nunca retrocede del 1
        
        // CRÍTICO: Retroceder INMEDIATAMENTE al nivel anterior para bloquear el nivel actual
        // Si está en nivel 5, retrocede a nivel 4
        // Si está en nivel 4, retrocede a nivel 3
        // etc.
        int target = Math.max(MIN_LEVEL, currentLevel - 1);
        
        android.util.Log.d("ProgressLockManager", "Retrocediendo de nivel " + currentLevel + " a nivel " + target + " (área: " + area + ")");
        
        // SIEMPRE actualizar para bloquear el nivel actual inmediatamente
        prefs(c).edit().putInt(key(userId, area), target).apply();
        
        // Verificar inmediatamente después de guardar
        int verificado = getUnlockedLevel(c, userId, area);
        if (verificado != target) {
            android.util.Log.e("ProgressLockManager", "ERROR: El retroceso no se aplicó correctamente. Esperado: " + target + ", Obtenido: " + verificado);
        } else {
            android.util.Log.d("ProgressLockManager", "✓ Retroceso aplicado correctamente: nivel " + currentLevel + " -> nivel " + target);
        }
    }

    /**
     * ***Retrocompatibilidad***: algunas pantallas (p.ej. Simulacro) solo "bajan 1" tras N fallos.
     * Mantengo este alias para no romper llamadas existentes.
     */
    public static void retrocederNivel(Context c, String userId, String area) {
        int now = getUnlockedLevel(c, userId, area);
        int target = Math.max(MIN_LEVEL, now - 1);
        if (target != now) {
            prefs(c).edit().putInt(key(userId, area), target).apply();
        }
    }

    /** Forzar set de nivel (útil para debug). */
    public static void setUnlockedLevel(Context c, String userId, String area, int level) {
        int target = Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, level));
        prefs(c).edit().putInt(key(userId, area), target).apply();
    }

    /** Reset a nivel 1. */
    public static void reset(Context c, String userId, String area) {
        prefs(c).edit().putInt(key(userId, area), MIN_LEVEL).apply();
    }

    /**
     * Sincroniza nivel desde el backend (para uso interno del sincronizador)
     */
    public static void syncFromBackend(Context c, String userId, String area, int nivel) {
        int target = Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, nivel));
        String key = key(userId, area);
        prefs(c).edit().putInt(key, target).apply();
        android.util.Log.d("ProgressLockManager", "syncFromBackend: userId=" + userId + ", area=" + area + ", nivel=" + target + ", key=" + key);
        
        // Verificar inmediatamente después de guardar
        int verificado = prefs(c).getInt(key, MIN_LEVEL);
        if (verificado != target) {
            android.util.Log.e("ProgressLockManager", "ERROR: El nivel no se guardó correctamente. Esperado: " + target + ", Obtenido: " + verificado);
        } else {
            android.util.Log.d("ProgressLockManager", "✓ Nivel guardado y verificado correctamente");
        }
    }

    /**
     * Desbloquea el siguiente nivel y sincroniza con el backend
     */
    public static void unlockNextAndSync(Context c, String userId, String area, int currentLevel) {
        unlockNext(c, userId, area, currentLevel);
        int newLevel = getUnlockedLevel(c, userId, area);
        // Sincronizar con backend de forma asíncrona
        com.example.zavira_movil.sincronizacion.ProgresoSincronizador.getInstance()
            .actualizarNivelEnBackend(c, userId, area, newLevel);
    }

    /**
     * Marca examen final desbloqueado y sincroniza con el backend
     */
    public static void unlockFinalExamAndSync(Context c, String userId, String area) {
        unlockFinalExam(c, userId, area);
        // Sincronizar con backend de forma asíncrona
        com.example.zavira_movil.sincronizacion.ProgresoSincronizador.getInstance()
            .actualizarNivelEnBackend(c, userId, area, 6);
    }

    /**
     * Retrocede por fallo y sincroniza con el backend
     */
    public static void retrocederPorFalloAndSync(Context c, String userId, String area, int currentLevel) {
        retrocederPorFallo(c, userId, area, currentLevel);
        int newLevel = getUnlockedLevel(c, userId, area);
        // Sincronizar con backend de forma asíncrona
        com.example.zavira_movil.sincronizacion.ProgresoSincronizador.getInstance()
            .actualizarNivelEnBackend(c, userId, area, newLevel);
    }
}
