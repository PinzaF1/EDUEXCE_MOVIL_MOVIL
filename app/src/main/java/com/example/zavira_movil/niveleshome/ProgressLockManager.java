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

    /** Marca “Examen Final desbloqueado”: guardamos 6. */
    public static void unlockFinalExam(Context c, String userId, String area) {
        int now = getUnlockedLevel(c, userId, area);
        if (now < 6) {
            prefs(c).edit().putInt(key(userId, area), 6).apply();
        }
    }

    /**
     * Retroceso condicionado por fallo en un nivel (usa el nivel actual).
     * No baja nunca del 1.
     */
    public static void retrocederPorFallo(Context c, String userId, String area, int currentLevel) {
        int now = getUnlockedLevel(c, userId, area);
        if (currentLevel <= 1) return; // nunca retrocede del 1
        int target = Math.max(MIN_LEVEL, Math.min(now, currentLevel - 1));
        if (target < now) {
            prefs(c).edit().putInt(key(userId, area), target).apply();
        }
    }

    /**
     * ***Retrocompatibilidad***: algunas pantallas (p.ej. Simulacro) solo “bajan 1” tras N fallos.
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
}
