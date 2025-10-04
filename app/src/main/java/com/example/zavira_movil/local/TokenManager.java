package com.example.zavira_movil.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.example.zavira_movil.LoginActivity;

import org.json.JSONObject;

public class TokenManager {
    private static final String PREF = "auth_prefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";

    /** Guarda token */
    public static void saveToken(Context ctx, String token) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_TOKEN, token).apply();
    }

    /** Obtiene token */
    public static String getToken(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return sp.getString(KEY_TOKEN, null);
    }

    /** Limpia todo */
    public static void clearAll(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }

    /** Alias de saveToken */
    public static void setToken(LoginActivity loginActivity, String token) {
        saveToken(loginActivity, token);
    }

    /** Guarda userId explícitamente */
    public static void setUserId(Context ctx, int userId) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putInt(KEY_USER_ID, userId).apply();
    }

    /** Obtiene userId guardado, o si no existe, intenta sacarlo del JWT */
    public static int getUserId(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        int saved = sp.getInt(KEY_USER_ID, -1);
        if (saved != -1) return saved;

        // fallback: decodificar del JWT
        String token = getToken(ctx);
        return extractUserIdFromJwt(token);
    }

    /** Decodifica el JWT para obtener userId si viene en el payload */
    public static int extractUserIdFromJwt(String token) {
        try {
            if (token == null || !token.contains(".")) return -1;

            String[] parts = token.split("\\.");
            if (parts.length < 2) return -1;

            String payloadJson = new String(Base64.decode(parts[1], Base64.URL_SAFE));
            JSONObject payload = new JSONObject(payloadJson);

            if (payload.has("id_usuario")) {
                return payload.getInt("id_usuario");
            } else if (payload.has("userId")) {
                return payload.getInt("userId");
            } else if (payload.has("sub")) {
                return payload.getInt("sub");
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
