package com.example.zavira_movil.notifications;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class NotificationHelper {
    
    private static final String TAG = "NotificationHelper";
    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    private Context context;
    
    public NotificationHelper(Context context) {
        this.context = context;
    }
    
    /**
     * Solicita permisos de notificaciones para Android 13+
     */
    public void requestNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    PERMISSION_REQUEST_CODE
                );
            }
        }
    }
    
    /**
     * Verifica si los permisos de notificaciones están otorgados
     */
    public boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true; // En versiones anteriores a Android 13, no se necesita permiso
    }
    
    /**
     * Obtiene el token FCM actual
     */
    public void getCurrentToken(OnTokenReceivedListener listener) {
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(Task<String> task) {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Error al obtener el token FCM", task.getException());
                        listener.onTokenReceived(null);
                        return;
                    }
                    
                    String token = task.getResult();
                    Log.d(TAG, "Token FCM obtenido: " + token);
                    
                    // Guardar token en SharedPreferences
                    saveTokenToPreferences(token);
                    
                    listener.onTokenReceived(token);
                }
            });
    }
    
    /**
     * Guarda el token FCM en SharedPreferences
     */
    private void saveTokenToPreferences(String token) {
        SharedPreferences prefs = context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE);
        prefs.edit().putString("fcm_token", token).apply();
    }
    
    /**
     * Obtiene el token FCM guardado en SharedPreferences
     */
    public String getSavedToken() {
        SharedPreferences prefs = context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE);
        return prefs.getString("fcm_token", null);
    }
    
    /**
     * Interfaz para callback cuando se obtiene el token
     */
    public interface OnTokenReceivedListener {
        void onTokenReceived(String token);
    }
}
