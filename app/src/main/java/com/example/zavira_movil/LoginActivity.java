package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.Home.HomeActivity;
import com.example.zavira_movil.databinding.ActivityLoginBinding;
import com.example.zavira_movil.local.TokenManager;
import com.example.zavira_movil.local.UserSession;
import com.example.zavira_movil.model.LoginRequest;
import com.example.zavira_movil.model.LoginResponse;
import com.example.zavira_movil.model.KolbResultado;
import com.example.zavira_movil.progreso.DiagnosticoInicial;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.gson.Gson;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private ApiService api;

    // No necesitamos el enum Destino ya que siempre vamos a Home después del login exitoso

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        api = RetrofitClient.getInstance(this).create(ApiService.class);

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
                        // Inicializar UserSession para que esté disponible en toda la app
                        UserSession.getInstance().setIdUsuario(userId);
                        Log.d("USER_ID_GUARDADO", "id=" + userId);
                    } else {
                        Log.w("USER_ID_GUARDADO", "No se pudo extraer el id del JWT");
                    }

                    Toast.makeText(LoginActivity.this, "¡Bienvenido/a!", Toast.LENGTH_SHORT).show();
                    // La sincronización se hará en goToHome() después de verificar los tests
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
}
