package com.example.zavira_movil;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;
import com.example.zavira_movil.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.Home.HomeActivity;
import com.example.zavira_movil.databinding.ActivityLoginBinding;
import com.example.zavira_movil.local.TokenManager;
import com.example.zavira_movil.local.UserSession;
import com.example.zavira_movil.model.LoginRequest;
import com.example.zavira_movil.model.LoginResponse;
import com.example.zavira_movil.model.KolbResultado;
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

        // Configurar status bar blanca
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(android.graphics.Color.WHITE);
        
        // Ocultar barra de navegación
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            androidx.core.view.WindowInsetsControllerCompat windowInsetsController =
                    androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            if (windowInsetsController != null) {
                windowInsetsController.setAppearanceLightStatusBars(true); // Texto oscuro sobre fondo blanco
                windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.navigationBars());
                windowInsetsController.setSystemBarsBehavior(
                    androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            flags = flags | android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            flags = flags | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            flags = flags | android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }

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

        // Configurar listeners de focus para los campos
        setupFieldListeners();

        // Iniciar animaciones de entrada
        iniciarAnimaciones();
    }

    private void setupFieldListeners() {
        // Configurar cursor azul para ambos campos
        setCursorColor(binding.etDocumento, android.graphics.Color.parseColor("#2563EB"));
        setCursorColor(binding.etPassword, android.graphics.Color.parseColor("#2563EB"));

        // Listener para el campo de documento
        binding.etDocumento.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Cuando tiene focus, aplicar borde azul
                binding.etDocumento.setBackgroundResource(R.drawable.bg_input_focused);
                clearFieldError(binding.etDocumento, binding.tilDocumento);
            } else {
                // Cuando pierde focus, volver al estado normal (si no hay error)
                if (!isFieldInError(binding.etDocumento)) {
                    binding.etDocumento.setBackgroundResource(R.drawable.bg_input_normal);
                }
            }
        });

        // Listener para el campo de contraseña
        binding.etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Cuando tiene focus, aplicar borde azul
                binding.etPassword.setBackgroundResource(R.drawable.bg_input_focused);
                clearFieldError(binding.etPassword, binding.tilPassword);
            } else {
                // Cuando pierde focus, volver al estado normal (si no hay error)
                if (!isFieldInError(binding.etPassword)) {
                    binding.etPassword.setBackgroundResource(R.drawable.bg_input_normal);
                }
            }
        });

        // Limpiar errores cuando el usuario empiece a escribir
        binding.etDocumento.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isFieldInError(binding.etDocumento)) {
                    clearFieldError(binding.etDocumento, binding.tilDocumento);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        binding.etPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isFieldInError(binding.etPassword)) {
                    clearFieldError(binding.etPassword, binding.tilPassword);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private static final String TAG_ERROR_STATE = "field_error_state";

    private void setCursorColor(android.widget.EditText editText, int color) {
        try {
            // Método para cambiar el color del cursor usando reflexión
            java.lang.reflect.Field fCursorDrawableRes = 
                android.widget.TextView.class.getDeclaredField("mCursorDrawableRes");
            fCursorDrawableRes.setAccessible(true);
            fCursorDrawableRes.setInt(editText, R.drawable.cursor_blue);

            java.lang.reflect.Field fEditor = android.widget.TextView.class.getDeclaredField("mEditor");
            fEditor.setAccessible(true);
            Object editor = fEditor.get(editText);
            
            if (editor != null) {
                String className = editor.getClass().getName();
                if (className.equals("android.widget.Editor")) {
                    java.lang.reflect.Field fCursorDrawable = editor.getClass().getDeclaredField("mCursorDrawable");
                    fCursorDrawable.setAccessible(true);
                    
                    android.graphics.drawable.Drawable[] drawables = new android.graphics.drawable.Drawable[2];
                    drawables[0] = getResources().getDrawable(R.drawable.cursor_blue);
                    drawables[1] = getResources().getDrawable(R.drawable.cursor_blue);
                    
                    fCursorDrawable.set(editor, drawables);
                }
            }
        } catch (Exception e) {
            // Si falla, no hacer nada - el cursor usará el color por defecto
            Log.d("CURSOR_COLOR", "No se pudo cambiar el color del cursor (esto es normal en algunas versiones de Android)");
        }
    }

    private boolean isFieldInError(android.widget.EditText field) {
        return field.getTag() != null && 
               TAG_ERROR_STATE.equals(field.getTag().toString());
    }

    private void setFieldError(android.widget.EditText field, 
                               com.google.android.material.textfield.TextInputLayout layout, 
                               String errorMessage) {
        field.setTag(TAG_ERROR_STATE);
        field.setBackgroundResource(R.drawable.bg_input_error);
        layout.setError(errorMessage);
        layout.setErrorEnabled(true);
    }

    private void clearFieldError(android.widget.EditText field, 
                                 com.google.android.material.textfield.TextInputLayout layout) {
        field.setTag(null);
        field.setBackgroundResource(R.drawable.bg_input_normal);
        layout.setError(null);
        layout.setErrorEnabled(false);
    }

    private void iniciarAnimaciones() {
        // Ocultar todos los elementos inicialmente
        binding.containerLogo.setAlpha(0f);
        binding.containerLogo.setScaleX(0.5f);
        binding.containerLogo.setScaleY(0.5f);
        
        binding.tvEduExce.setAlpha(0f);
        binding.tvEduExce.setTranslationY(-30f);
        
        binding.tilDocumento.setAlpha(0f);
        binding.tilDocumento.setTranslationY(30f);
        
        binding.tilPassword.setAlpha(0f);
        binding.tilPassword.setTranslationY(30f);
        
        binding.btnLogin.setAlpha(0f);
        binding.btnLogin.setScaleX(0.8f);
        binding.btnLogin.setScaleY(0.8f);
        
        binding.tvOlvideContra.setAlpha(0f);

        // Animación del logo (fade in + scale)
        AnimatorSet logoAnim = new AnimatorSet();
        ObjectAnimator logoFade = ObjectAnimator.ofFloat(binding.containerLogo, "alpha", 0f, 1f);
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(binding.containerLogo, "scaleX", 0.5f, 1f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(binding.containerLogo, "scaleY", 0.5f, 1f);
        logoAnim.playTogether(logoFade, logoScaleX, logoScaleY);
        logoAnim.setDuration(600);
        logoAnim.setInterpolator(new DecelerateInterpolator());
        logoAnim.start();

        // Animación del texto "EduExce" (fade in + slide up)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            AnimatorSet textAnim = new AnimatorSet();
            ObjectAnimator textFade = ObjectAnimator.ofFloat(binding.tvEduExce, "alpha", 0f, 1f);
            ObjectAnimator textSlide = ObjectAnimator.ofFloat(binding.tvEduExce, "translationY", -30f, 0f);
            textAnim.playTogether(textFade, textSlide);
            textAnim.setDuration(500);
            textAnim.setInterpolator(new DecelerateInterpolator());
            textAnim.start();
        }, 200);

        // Animación del campo de documento (fade in + slide up)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            AnimatorSet docAnim = new AnimatorSet();
            ObjectAnimator docFade = ObjectAnimator.ofFloat(binding.tilDocumento, "alpha", 0f, 1f);
            ObjectAnimator docSlide = ObjectAnimator.ofFloat(binding.tilDocumento, "translationY", 30f, 0f);
            docAnim.playTogether(docFade, docSlide);
            docAnim.setDuration(500);
            docAnim.setInterpolator(new DecelerateInterpolator());
            docAnim.start();
        }, 400);

        // Animación del campo de contraseña (fade in + slide up)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            AnimatorSet passAnim = new AnimatorSet();
            ObjectAnimator passFade = ObjectAnimator.ofFloat(binding.tilPassword, "alpha", 0f, 1f);
            ObjectAnimator passSlide = ObjectAnimator.ofFloat(binding.tilPassword, "translationY", 30f, 0f);
            passAnim.playTogether(passFade, passSlide);
            passAnim.setDuration(500);
            passAnim.setInterpolator(new DecelerateInterpolator());
            passAnim.start();
        }, 550);

        // Animación del botón (fade in + scale)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            AnimatorSet btnAnim = new AnimatorSet();
            ObjectAnimator btnFade = ObjectAnimator.ofFloat(binding.btnLogin, "alpha", 0f, 1f);
            ObjectAnimator btnScaleX = ObjectAnimator.ofFloat(binding.btnLogin, "scaleX", 0.8f, 1f);
            ObjectAnimator btnScaleY = ObjectAnimator.ofFloat(binding.btnLogin, "scaleY", 0.8f, 1f);
            btnAnim.playTogether(btnFade, btnScaleX, btnScaleY);
            btnAnim.setDuration(500);
            btnAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            btnAnim.start();
        }, 700);

        // Animación del enlace "¿Olvidaste tu contraseña?" (fade in)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            ObjectAnimator linkFade = ObjectAnimator.ofFloat(binding.tvOlvideContra, "alpha", 0f, 1f);
            linkFade.setDuration(400);
            linkFade.setInterpolator(new DecelerateInterpolator());
            linkFade.start();
        }, 850);
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

        // Validación de formato de documento (solo números y exactamente 10 caracteres)
        if (!doc.matches("\\d+")) {
            binding.etDocumento.setError("El documento solo debe contener números");
            binding.etDocumento.requestFocus();
            return;
        }
        
        if (doc.length() != 10) {
            binding.etDocumento.setError("El documento debe tener exactamente 10 caracteres");
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
                binding.btnLogin.setAlpha(1.0f);

                if (!response.isSuccessful()) {
                    // Usar ErrorHandler para mostrar error con opción de reintentar
                    com.example.zavira_movil.utils.ErrorHandler.handleHttpError(
                        LoginActivity.this,
                        response,
                        () -> doLogin() // Callback para reintentar
                    );
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
                        // Inicializar UserSession para que esté disponible en toda la app
                        UserSession.getInstance().setIdUsuario(userId);
                        Log.d("USER_ID_GUARDADO", "id=" + userId);
                    } else {
                        Log.w("USER_ID_GUARDADO", "No se pudo extraer el id del JWT");
                    }

                    Toast.makeText(LoginActivity.this, "¡Bienvenido/a!", Toast.LENGTH_SHORT).show();

                    // ✅ Registrar token FCM después del login exitoso
                    registerFCMToken();

                    // La sincronización se hará en goToHome() después de verificar los tests
                    goToHome();

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, "Credenciales Incorrectas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Restaurar estado del botón
                binding.progress.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
                binding.btnLogin.setAlpha(1.0f);

                // Usar ErrorHandler para manejar excepción de red
                com.example.zavira_movil.utils.ErrorHandler.handleNetworkException(
                    LoginActivity.this,
                    t,
                    () -> doLogin() // Callback para reintentar
                );
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

        // Primero verificar si ya completó el test de Kolb
        api.obtenerResultado().enqueue(new Callback<KolbResultado>() {
            @Override
            public void onResponse(Call<KolbResultado> call, Response<KolbResultado> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getEstilo() != null) {
                    // Ya completó Kolb, verificar diagnóstico
                    verificarDiagnostico();
                } else if (response.code() == 404) {
                    // 404 significa que no ha completado Kolb, ir al test de Kolb
                    Intent intent = new Intent(LoginActivity.this, InfoTestActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // Otro error (500, etc.) - ir a Home para que verifique allí
                    // No redirigir forzadamente al test porque puede ser un error de servidor
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    
                    // Sincronizar niveles desde el backend al iniciar sesión
                    int userId = TokenManager.getUserId(LoginActivity.this);
                    if (userId > 0) {
                        com.example.zavira_movil.sincronizacion.ProgresoSincronizador.getInstance()
                            .sincronizarDesdeBackend(LoginActivity.this, String.valueOf(userId));
                    }
                    
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<KolbResultado> call, Throwable t) {
                // En caso de error de red, ir a Home para que verifique allí
                // No redirigir forzadamente al test porque puede ser un error de conexión
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                
                // Sincronizar niveles desde el backend al iniciar sesión
                int userId = TokenManager.getUserId(LoginActivity.this);
                if (userId > 0) {
                    com.example.zavira_movil.sincronizacion.ProgresoSincronizador.getInstance()
                        .sincronizarDesdeBackend(LoginActivity.this, String.valueOf(userId));
                }
                
                startActivity(intent);
                finish();
            }
        });
    }

    private void verificarDiagnostico() {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        
        api.diagnosticoProgreso().enqueue(new Callback<DiagnosticoInicial>() {
            @Override
            public void onResponse(Call<DiagnosticoInicial> call, Response<DiagnosticoInicial> response) {
                Intent intent;

                if (response.isSuccessful() && response.body() != null && response.body().tieneDiagnostico) {
                    // Si ya completó el diagnóstico, ir a Home
                    // Y sincronizar progreso desde el backend
                    intent = new Intent(LoginActivity.this, HomeActivity.class);
                    
                    // Sincronizar progreso inmediatamente después de verificar diagnóstico
                    int userId = TokenManager.getUserId(LoginActivity.this);
                    if (userId > 0) {
                        com.example.zavira_movil.sincronizacion.ProgresoSincronizador.getInstance()
                            .sincronizarDesdeBackend(LoginActivity.this, String.valueOf(userId));
                    }
                } else if (response.code() == 404 || (response.body() != null && !response.body().tieneDiagnostico)) {
                    // Si no ha completado el diagnóstico, ir a InfoAcademico
                    intent = new Intent(LoginActivity.this, InfoAcademico.class);
                } else {
                    // Otro error - ir a Home para que verifique allí
                    intent = new Intent(LoginActivity.this, HomeActivity.class);
                    
                    // Intentar sincronizar aunque haya error
                    int userId = TokenManager.getUserId(LoginActivity.this);
                    if (userId > 0) {
                        com.example.zavira_movil.sincronizacion.ProgresoSincronizador.getInstance()
                            .sincronizarDesdeBackend(LoginActivity.this, String.valueOf(userId));
                    }
                }

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<DiagnosticoInicial> call, Throwable t) {
                // En caso de error de red, redirigir a Home por defecto
                // HomeActivity verificará nuevamente y mostrará el estado correcto
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                
                // Intentar sincronizar aunque haya error de red
                int userId = TokenManager.getUserId(LoginActivity.this);
                if (userId > 0) {
                    com.example.zavira_movil.sincronizacion.ProgresoSincronizador.getInstance()
                        .sincronizarDesdeBackend(LoginActivity.this, String.valueOf(userId));
                }
                
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
                
                // ⚠️ TEMPORAL: Mostrar token completo para testing con Firebase Console
                Log.d("FCM_TOKEN", "📱 Token FCM completo: " + token);
                Log.d("FCM_TOKEN", "===================================");
                Log.d("FCM_TOKEN", "COPIA ESTE TOKEN PARA FIREBASE CONSOLE:");
                Log.d("FCM_TOKEN", token);
                Log.d("FCM_TOKEN", "===================================");

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
                        jsonBody.toString(),
                        MediaType.parse("application/json")
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
