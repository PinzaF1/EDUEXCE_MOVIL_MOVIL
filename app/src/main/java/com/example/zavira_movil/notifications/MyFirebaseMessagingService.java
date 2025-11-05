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
        
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.iconoeduexce)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);
        
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
            notificationManager.createNotificationChannel(channel);
        }
        
        notificationManager.notify(0, notificationBuilder.build());
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
                MediaType.parse("application/json"),
                jsonBody.toString()
            );
            
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
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
