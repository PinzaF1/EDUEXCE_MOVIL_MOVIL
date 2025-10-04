package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.databinding.ActivityLoginBinding;
import com.example.zavira_movil.local.TokenManager;
import com.example.zavira_movil.model.LoginRequest;
import com.example.zavira_movil.model.LoginResponse;
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

    // <<< NUEVO: destino después de validar >>>
    private enum Destino { INFO_TEST, HOME }
    private Destino destinoPendiente = Destino.INFO_TEST;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        api = RetrofitClient.getInstance(this).create(ApiService.class);

        // Si ya hay token, entra directo a InfoTest
        if (TokenManager.getToken(this) != null) {
            goToInfoTest();
            return;
        }

        // Ambos botones usan la misma validación; cambian SOLO el destino
        binding.btnLogin.setOnClickListener(v -> {
            destinoPendiente = Destino.INFO_TEST; // después del login → InfoTest
            doLogin();
        });

        binding.btnLoginprovicional.setOnClickListener(v -> {
            destinoPendiente = Destino.HOME; // después del login → Home
            doLogin();
        });
    }

    private void doLogin() {
        String doc = binding.etDocumento.getText().toString().trim();
        String pass = binding.etPassword.getText().toString().trim();

        if (doc.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Documento y contraseña son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progress.setVisibility(View.VISIBLE);

        LoginRequest request = new LoginRequest(doc, pass);
        api.loginEstudiante(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                binding.progress.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(LoginActivity.this, "Error en el login", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    String body = response.body().string().trim();
                    LoginResponse loginResponse = new Gson().fromJson(body, LoginResponse.class);

                    if (loginResponse.getToken() == null || loginResponse.getToken().isEmpty()) {
                        Toast.makeText(LoginActivity.this, "No se recibió token", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Guardar token
                    TokenManager.setToken(LoginActivity.this, loginResponse.getToken());
                    Log.d("TOKEN_GUARDADO", loginResponse.getToken());

                    // Guardar userId extraído del JWT
                    int userId = TokenManager.extractUserIdFromJwt(loginResponse.getToken());
                    if (userId > 0) {
                        TokenManager.setUserId(LoginActivity.this, userId);
                        Log.d("USER_ID_GUARDADO", "id=" + userId);
                    } else {
                        Log.w("USER_ID_GUARDADO", "No se pudo extraer el id del JWT");
                    }

                    Toast.makeText(LoginActivity.this, "Bienvenido/a", Toast.LENGTH_SHORT).show();

                    // <<< NUEVO: navegar según el botón que se pulsó >>>
                    if (destinoPendiente == Destino.HOME) {
                        goToHome();
                    } else {
                        goToInfoTest();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, "Credenciales Incorrectas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                binding.progress.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Navegación → InfoTestActivity
    private void goToInfoTest() {
        Intent i = new Intent(this, InfoTestActivity.class);
        startActivity(i);
        finish();
    }

    // Navegación → HomeActivity
    private void goToHome() {
        Intent i = new Intent(this, com.example.zavira_movil.ui.ranking.RankingLogrosActivity.class);
        startActivity(i);
        finish();
    }
}
