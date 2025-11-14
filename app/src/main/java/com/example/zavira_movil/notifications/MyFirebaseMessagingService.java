package com.example.zavira_movil.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.zavira_movil.Home.HomeActivity;
import com.example.zavira_movil.R;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "eduexce_notifications";
    
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        Log.d(TAG, "Mensaje recibido de: " + remoteMessage.getFrom());
        
        // Verificar si el mensaje contiene datos
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Datos del mensaje: " + remoteMessage.getData());
            handleDataMessage(remoteMessage.getData());
        }
        
        // Verificar si el mensaje contiene una notificación
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Notificación recibida - Título: " + title + ", Cuerpo: " + body);
            
            // Guardar la notificación en el historial
            saveNotificationToHistory(title, body, remoteMessage.getData());
            
            sendNotification(title, body, remoteMessage.getData());
        }
    }
    
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Nuevo token FCM: " + token);
        
        // Guardar el token localmente
        saveTokenToPreferences(token);
        
        // Enviar el token al servidor
        sendTokenToServer(token);
    }
    
    private void handleDataMessage(Map<String, String> data) {
        String title = data.get("title");
        String message = data.get("message");
        String type = data.get("type");
        
        if (title == null) title = getString(R.string.notification_title);
        if (message == null) message = getString(R.string.notification_message);
        
        // Guardar la notificación en el historial
        saveNotificationToHistory(title, message, data);
        
        sendNotification(title, message, data);
    }
    
    private void sendNotification(String title, String messageBody, Map<String, String> data) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        // Agregar datos extras al intent
        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );
        
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        
        // Determinar el color y estilo según el tipo de notificación
        int notificationColor = getNotificationColor(data);
        String notificationType = data != null ? data.get("tipo") : null;
        
        // Construir notificación con estilo expandido
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.iconoeduexce)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(messageBody)
                        .setBigContentTitle(title))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(notificationColor)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        
        // Agregar información adicional si está disponible
        if (data != null) {
            String area = data.get("area");
            String puntaje = data.get("puntaje");
            
            if (area != null && puntaje != null) {
                String infoLine = "📚 " + area + " • Puntaje: " + puntaje + "%";
                notificationBuilder.setSubText(infoLine);
            }
        }
        
        NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        // Crear canal de notificación para Android O y superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.default_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Canal para notificaciones de EduExce");
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setLightColor(notificationColor);
            notificationManager.createNotificationChannel(channel);
        }
        
        // Usar un ID único basado en el timestamp para permitir múltiples notificaciones
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
    
    /**
     * Determina el color de la notificación según el tipo
     */
    private int getNotificationColor(Map<String, String> data) {
        if (data == null) {
            return 0xFF3B82F6; // Azul por defecto
        }
        
        String tipo = data.get("tipo");
        String puntajeStr = data.get("puntaje");
        
        // Color según el tipo de notificación
        if ("puntaje_bajo_inmediato".equals(tipo)) {
            return 0xFFEF4444; // Rojo para puntaje bajo
        } else if ("recordatorio_practica".equals(tipo)) {
            return 0xFFF59E0B; // Naranja para recordatorios
        } else if ("logro_desbloqueado".equals(tipo)) {
            return 0xFF22C55E; // Verde para logros
        }
        
        // Color según el puntaje si está disponible
        if (puntajeStr != null) {
            try {
                int puntaje = Integer.parseInt(puntajeStr);
                if (puntaje < 40) {
                    return 0xFFEF4444; // Rojo
                } else if (puntaje < 70) {
                    return 0xFFF59E0B; // Naranja
                } else {
                    return 0xFF22C55E; // Verde
                }
            } catch (NumberFormatException e) {
                // Ignorar si no se puede parsear
            }
        }
        
        return 0xFF3B82F6; // Azul por defecto
    }
    
    /**
     * Guarda la notificación en el historial local
     */
    private void saveNotificationToHistory(String title, String message, Map<String, String> data) {
        try {
            NotificationStorage notificationStorage = new NotificationStorage(this);
            
            String tipo = data != null ? data.get("tipo") : null;
            String area = data != null ? data.get("area") : null;
            String puntaje = data != null ? data.get("puntaje") : null;
            
            NotificationItem item = new NotificationItem(
                title,
                message,
                tipo,
                area,
                puntaje,
                System.currentTimeMillis()
            );
            
            notificationStorage.saveNotification(item);
            Log.d(TAG, "✅ Notificación guardada en el historial");

            // Enviar broadcast para actualizar el badge en HomeActivity
            Intent intent = new Intent("com.example.zavira_movil.UPDATE_NOTIFICATION_BADGE");
            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error al guardar notificación en historial", e);
        }
    }
    
    private void saveTokenToPreferences(String token) {
        SharedPreferences prefs = getSharedPreferences("fcm_prefs", MODE_PRIVATE);
        prefs.edit().putString("fcm_token", token).apply();
    }
    
    private void sendTokenToServer(String token) {
        SharedPreferences prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE);
        String authToken = prefs.getString("token", null);
        
        if (authToken == null) {
            Log.w(TAG, "Usuario no autenticado, no se puede enviar el token al servidor");
            return;
        }
        
        try {
            // Obtener device_id único del dispositivo
            String deviceId = Settings.Secure.getString(
                getContentResolver(), 
                Settings.Secure.ANDROID_ID
            );
            
            // Construir body según el formato esperado por el backend
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("token", token);
            jsonBody.put("device_id", deviceId);
            jsonBody.put("platform", "android");
            
            RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.parse("application/json")
            );
            
            ApiService apiService = RetrofitClient.getInstance(this).create(ApiService.class);
            Call<Void> call = apiService.registerFCMToken(body);
            
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "✅ Token FCM registrado exitosamente en el servidor");
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? 
                                response.errorBody().string() : "Sin detalles";
                            Log.e(TAG, "❌ Error al registrar token FCM: " + response.code() + " - " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Error al registrar token FCM: " + response.code());
                        }
                    }
                }
                
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "❌ Fallo de red al enviar token FCM al servidor", t);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "❌ Error al preparar el token FCM para enviar", e);
        }
    }
}
