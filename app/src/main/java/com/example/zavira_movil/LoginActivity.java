package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.Home.HomeActivity;
import com.example.zavira_movil.databinding.ActivityLoginBinding;
import com.example.zavira_movil.local.TokenManager;
import com.example.zavira_movil.model.LoginRequest;
import com.example.zavira_movil.model.LoginResponse;
import com.example.zavira_movil.notifications.NotificationHelper;
import com.example.zavira_movil.progreso.DiagnosticoInicial;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.gson.Gson;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private ApiService api;
    private NotificationHelper notificationHelper;

    // No necesitamos el enum Destino ya que siempre vamos a Home después del login exitoso

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        api = RetrofitClient.getInstance(this).create(ApiService.class);
        notificationHelper = new NotificationHelper(this);
        
        // Solicitar permisos de notificaciones (Android 13+)
        notificationHelper.requestNotificationPermission(this);

        // ✅ Si ya hay token => entra directo a HOME
        if (TokenManager.getToken(this) != null) {
            goToHome();
            return;
        }

        // Configurar el botón de inicio de sesión
        binding.btnLogin.setOnClickListener(v -> doLogin());

        // Enlace "¿Olvidaste tu contraseña?"
        binding.tvOlvideContra.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.zavira_movil.resetpassword.ResetPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void doLogin() {
        String doc = binding.etDocumento.getText().toString().trim();
        String pass = binding.etPassword.getText().toString().trim();

        // Validación de campos vacíos
        if (doc.isEmpty()) {
            binding.etDocumento.setError("Ingrese su número de documento");
            binding.etDocumento.requestFocus();
            return;
        }

        if (pass.isEmpty()) {
            binding.etPassword.setError("Ingrese su contraseña");
            binding.etPassword.requestFocus();
            return;
        }

        // Validación de formato de documento (solo números)
        if (!doc.matches("\\d+")) {
            binding.etDocumento.setError("El documento solo debe contener números");
            binding.etDocumento.requestFocus();
            return;
        }

        binding.progress.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false); // Deshabilitar botón durante la solicitud

        LoginRequest request = new LoginRequest(doc, pass);
        api.loginEstudiante(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                binding.progress.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);

                if (!response.isSuccessful()) {
                    String errorMessage = "Error en el servidor";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            if (!errorBody.isEmpty()) {
                                errorMessage = new Gson().fromJson(errorBody, String.class);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("LOGIN_ERROR", "Error al leer el mensaje de error", e);
                    }
                    
                    if (response.code() == 401) {
                        errorMessage = "Usuario o contraseña incorrectos";
                    }
                    
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    return;
                }

                if (response.body() == null) {
                    Toast.makeText(LoginActivity.this, "Respuesta vacía del servidor", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    String body = response.body().string().trim();
                    LoginResponse loginResponse = new Gson().fromJson(body, LoginResponse.class);

                    if (loginResponse.getToken() == null || loginResponse.getToken().isEmpty()) {
                        Toast.makeText(LoginActivity.this, "No se recibió token de autenticación", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Guarda token
                    TokenManager.setToken(LoginActivity.this, loginResponse.getToken());
                    Log.d("TOKEN_GUARDADO", loginResponse.getToken());

                    // Guarda userId desde el JWT (si tu TokenManager lo soporta)
                    int userId = TokenManager.extractUserIdFromJwt(loginResponse.getToken());
                    if (userId > 0) {
                        TokenManager.setUserId(LoginActivity.this, userId);
                        Log.d("USER_ID_GUARDADO", "id=" + userId);
                    } else {
                        Log.w("USER_ID_GUARDADO", "No se pudo extraer el id del JWT");
                    }

                    Toast.makeText(LoginActivity.this, "¡Bienvenido/a!", Toast.LENGTH_SHORT).show();
                    
                    // Registrar token FCM después del login exitoso
                    registerFCMToken();
                    
                    goToHome();

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, "Credenciales Incorrectas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                binding.progress.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
                
                String errorMessage = "Error de conexión";
                if (t instanceof java.net.ConnectException || t instanceof java.net.UnknownHostException) {
                    errorMessage = "No se pudo conectar al servidor. Verifica tu conexión a Internet.";
                } else if (t instanceof java.net.SocketTimeoutException) {
                    errorMessage = "Tiempo de espera agotado. Intenta de nuevo.";
                } else {
                    errorMessage = "Error: " + t.getMessage();
                }
                
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Navegación → InfoTestActivity
    private void goToInfoTest() {
        Intent i = new Intent(this, InfoTestActivity.class);

        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    // Navegación → HomeActivity
    private void goToHome() {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        String token = com.example.zavira_movil.local.TokenManager.getToken(this);

        if (token == null || token.isEmpty()) {
            // Si no hay token, redirigir al login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String bearer = token.startsWith("Bearer ") ? token : "Bearer " + token;

        // Llamar al endpoint para verificar el estado del diagnóstico
        api.diagnosticoProgreso().enqueue(new Callback<DiagnosticoInicial>() {
            @Override
            public void onResponse(Call<DiagnosticoInicial> call, Response<DiagnosticoInicial> response) {
                Intent intent;

                if (response.isSuccessful() && response.body() != null && response.body().tieneDiagnostico) {
                    // Si ya completó el diagnóstico, ir a Home
                    intent = new Intent(LoginActivity.this, HomeActivity.class);
                } else {
                    // Si no ha completado el diagnóstico, ir a InfoAcademico
                    intent = new Intent(LoginActivity.this, InfoAcademico.class);
                }

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<DiagnosticoInicial> call, Throwable t) {
                // En caso de error, redirigir a Home por defecto
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
    
    /**
     * Registra el token FCM en el servidor después del login exitoso
     */
    private void registerFCMToken() {
        notificationHelper.getCurrentToken(new NotificationHelper.OnTokenReceivedListener() {
            @Override
            public void onTokenReceived(String token) {
                if (token == null) {
                    Log.w("FCM_TOKEN", "⚠️ No se pudo obtener el token FCM");
                    return;
                }
                
                Log.d("FCM_TOKEN", "📱 Token FCM obtenido: " + token.substring(0, 20) + "...");
                
                // Verificar autenticación
                String authToken = TokenManager.getToken(LoginActivity.this);
                if (authToken == null) {
                    Log.w("FCM_TOKEN", "⚠️ No hay token de autenticación");
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
                    
                    Log.d("FCM_TOKEN", "📤 Enviando token al servidor...");
                    
                    ApiService apiService = RetrofitClient.getInstance(LoginActivity.this).create(ApiService.class);
                    Call<Void> call = apiService.registerFCMToken(body);
                    
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Log.d("FCM_TOKEN", "✅ Token FCM registrado exitosamente en el servidor");
                            } else {
                                try {
                                    String errorBody = response.errorBody() != null ? 
                                        response.errorBody().string() : "Sin detalles";
                                    Log.e("FCM_TOKEN", "❌ Error al registrar token: " + response.code() + " - " + errorBody);
                                } catch (Exception e) {
                                    Log.e("FCM_TOKEN", "❌ Error al registrar token: " + response.code());
                                }
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e("FCM_TOKEN", "❌ Fallo de red al registrar token FCM", t);
                        }
                    });
                } catch (Exception e) {
                    Log.e("FCM_TOKEN", "❌ Error al preparar el token para enviar", e);
                }
            }
        });
    }
}
