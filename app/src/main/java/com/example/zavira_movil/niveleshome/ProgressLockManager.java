package com.example.zavira_movil.niveleshome;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Controla qué nivel está desbloqueado por usuario/área (1..5),
 * permite avanzar, retroceder y manejar la bandera del Examen Final.
 */
public final class ProgressLockManager {

    private static final String PREFS = "progress_lock_prefs";
    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 5;

    private static final String SEP = "::";

    private ProgressLockManager() {}

    // ---------------- Internos ----------------

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Clave de nivel por usuario/área. */
    private static String keyLevel(String userId, String area) {
        if (TextUtils.isEmpty(userId)) userId = "default_user";
        if (TextUtils.isEmpty(area))   area   = "default_area";
        return "unlocked_level" + SEP + userId.trim() + SEP + area.trim();
    }

    /** Clave para bandera de Examen Final por usuario/área. */
    private static String keyExam(String userId, String area) {
        if (TextUtils.isEmpty(userId)) userId = "default_user";
        if (TextUtils.isEmpty(area))   area   = "default_area";
        return "final_exam" + SEP + userId.trim() + SEP + area.trim();
    }

    // ---------------- Niveles ----------------

    /** Nivel más alto desbloqueado (1..5). Por defecto 1. */
    public static int getUnlockedLevel(Context c, String userId, String area) {
        int v = prefs(c).getInt(keyLevel(userId, area), MIN_LEVEL);
        if (v < MIN_LEVEL) v = MIN_LEVEL;
        if (v > MAX_LEVEL) v = MAX_LEVEL;
        return v;
    }

    /** true si ese nivel está desbloqueado. */
    public static boolean isLevelUnlocked(Context c, String userId, String area, int level) {
        return level <= getUnlockedLevel(c, userId, area);
    }

    /** Define explícitamente el nivel máximo desbloqueado (clamp 1..5). */
    public static void setUnlockedLevel(Context c, String userId, String area, int level) {
        int target = Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, level));
        prefs(c).edit().putInt(keyLevel(userId, area), target).apply();
    }

    /** Desbloquea el siguiente nivel respecto al nivel actual. No excede 5. */
    public static void unlockNext(Context c, String userId, String area, int currentLevel) {
        int now = getUnlockedLevel(c, userId, area);
        int target = Math.max(now, Math.min(MAX_LEVEL, currentLevel + 1));
        prefs(c).edit().putInt(keyLevel(userId, area), target).apply();
    }

    /**
     * Retrocede un nivel (pensado para regla <80%). No baja de 1.
     * Devuelve el nivel que quedó como máximo desbloqueado.
     */
    public static int revertOneLevel(Context c, String userId, String area, int currentLevel) {
        int newLevel = Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, currentLevel - 1));
        int stored = getUnlockedLevel(c, userId, area);
        if (stored > newLevel) {
            setUnlockedLevel(c, userId, area, newLevel);
        }
        return newLevel;
    }

    /** Retrocede un nivel respecto al nivel guardado (sin parámetro de nivel actual). */
    public static int retrocederNivel(Context c, String userId, String area) {
        int now = getUnlockedLevel(c, userId, area);
        int target = Math.max(MIN_LEVEL, now - 1);
        setUnlockedLevel(c, userId, area, target);
        return target;
    }

    /** Resetea el progreso a nivel 1. */
    public static void reset(Context c, String userId, String area) {
        setUnlockedLevel(c, userId, area, MIN_LEVEL);
        setFinalExamUnlocked(c, userId, area, false);
    }

    // ---------------- Examen Final ----------------

    /** Marca/Desmarca el Examen Final como desbloqueado. */
    public static void setFinalExamUnlocked(Context c, String userId, String area, boolean unlocked) {
        prefs(c).edit().putBoolean(keyExam(userId, area), unlocked).apply();
    }

    /** ¿El Examen Final está desbloqueado para este usuario/área? */
    public static boolean isFinalExamUnlocked(Context c, String userId, String area) {
        return prefs(c).getBoolean(keyExam(userId, area), false);
    }
}
