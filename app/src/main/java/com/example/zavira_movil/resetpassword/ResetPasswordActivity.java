package com.example.zavira_movil.resetpassword;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.BasicResponse;
import com.example.zavira_movil.LoginActivity;
import com.example.zavira_movil.R;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    // ViewFlipper y vistas
    private ViewFlipper viewFlipper;
    private ImageView btnBack;

    // Paso 1: Solicitar código
    private EditText etCorreo;
    private Button btnEnviarCodigo;

    private ProgressBar progressStep1;

    // Paso 2: Verificar código
    private EditText etCodigo;
    private Button btnVerificarCodigo;

    private TextView tvReenviarCodigo;
    private TextView tvEmailEnviado;
    private TextView tvTimer;
    private ProgressBar progressStep2;

    // Paso 3: Nueva contraseña
    private TextInputEditText etNuevaPassword;
    private TextInputEditText etConfirmarPassword;
    private Button btnRestablecerPassword;
    private ProgressBar progressStep3;

    // Indicadores de paso
    private View indicatorStep1, indicatorStep2, indicatorStep3;

    // API
    private ApiService api;

    // Datos temporales
    private String correoActual;
    private String codigoActual;

    // Timers
    private CountDownTimer countDownTimer;  // Timer de 15 minutos
    private CountDownTimer resendTimer;     // Timer de 60 segundos para reenvío

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Inicializar API
        api = RetrofitClient.getInstance(this).create(ApiService.class);

        // Inicializar vistas
        initViews();
        setupListeners();

        // Mostrar paso 1
        showStep(0);
    }

    private void initViews() {
        viewFlipper = findViewById(R.id.viewFlipper);
        btnBack = findViewById(R.id.btnBack);

        // Paso 1
        etCorreo = findViewById(R.id.etCorreo);
        btnEnviarCodigo = findViewById(R.id.btnEnviarCodigo);

        progressStep1 = findViewById(R.id.progressStep1);

        // Paso 2
        etCodigo = findViewById(R.id.etCodigo);
        btnVerificarCodigo = findViewById(R.id.btnVerificarCodigo);

        tvReenviarCodigo = findViewById(R.id.tvReenviarCodigo);
        tvEmailEnviado = findViewById(R.id.tvEmailEnviado);
        tvTimer = findViewById(R.id.tvTimer);
        progressStep2 = findViewById(R.id.progressStep2);

        // Paso 3
        etNuevaPassword = findViewById(R.id.etNuevaPassword);
        etConfirmarPassword = findViewById(R.id.etConfirmarPassword);
        btnRestablecerPassword = findViewById(R.id.btnRestablecerPassword);
        progressStep3 = findViewById(R.id.progressStep3);

        // Indicadores
        indicatorStep1 = findViewById(R.id.indicatorStep1);
        indicatorStep2 = findViewById(R.id.indicatorStep2);
        indicatorStep3 = findViewById(R.id.indicatorStep3);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        // Paso 1: Enviar código
        btnEnviarCodigo.setOnClickListener(v -> solicitarCodigo());


        // Paso 2: Verificar código
        btnVerificarCodigo.setOnClickListener(v -> verificarCodigo());

        tvReenviarCodigo.setOnClickListener(v -> reenviarCodigo());

        // Paso 3: Restablecer contraseña
        btnRestablecerPassword.setOnClickListener(v -> restablecerPassword());
    }

    // ============================================
    // PASO 1: Solicitar código
    // ============================================
    private void solicitarCodigo() {
        String correo = etCorreo.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(correo)) {
            etCorreo.setError("Ingresa tu correo electrónico");
            etCorreo.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.setError("Correo electrónico inválido");
            etCorreo.requestFocus();
            return;
        }

        // Guardar correo
        correoActual = correo;

        // Mostrar loading
        btnEnviarCodigo.setEnabled(false);
        progressStep1.setVisibility(View.VISIBLE);

        // Crear request
        SolicitarCodigoRequest request = new SolicitarCodigoRequest(correo);

        // Llamar API
        api.solicitarCodigoRecuperacion(request).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                btnEnviarCodigo.setEnabled(true);
                progressStep1.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                    // Éxito - pasar al paso 2
                    Toast.makeText(ResetPasswordActivity.this, 
                        "Código enviado a tu correo", Toast.LENGTH_SHORT).show();
                    showStep(1);
                    startCountDownTimer();      // Iniciar timer de 15 minutos
                    startResendTimer();         // Iniciar timer de 60 segundos
                } else {
                    // Error
                    String mensaje = "Error al enviar el código";
                    if (response.body() != null && response.body().getMessage() != null) {
                        mensaje = response.body().getMessage();
                    }
                    Toast.makeText(ResetPasswordActivity.this, mensaje, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                btnEnviarCodigo.setEnabled(true);
                progressStep1.setVisibility(View.GONE);
                Toast.makeText(ResetPasswordActivity.this, 
                    "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ============================================
    // PASO 2: Verificar código
    // ============================================
    private void verificarCodigo() {
        String codigo = etCodigo.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(codigo)) {
            etCodigo.setError("Ingresa el código");
            etCodigo.requestFocus();
            return;
        }

        if (codigo.length() != 6) {
            etCodigo.setError("El código debe tener 6 dígitos");
            etCodigo.requestFocus();
            return;
        }

        // Guardar código
        codigoActual = codigo;

        // Mostrar loading
        btnVerificarCodigo.setEnabled(false);
        progressStep2.setVisibility(View.VISIBLE);

        // Crear request
        VerificarCodigoRequest request = new VerificarCodigoRequest(correoActual, codigo);

        // Llamar API
        api.verificarCodigoRecuperacion(request).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                btnVerificarCodigo.setEnabled(true);
                progressStep2.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isValid()) {
                    // Éxito - pasar al paso 3
                    Toast.makeText(ResetPasswordActivity.this, 
                        "Código verificado correctamente", Toast.LENGTH_SHORT).show();
                    stopCountDownTimer();  // Detener timer
                    showStep(2);
                } else {
                    // Error
                    String mensaje = "Código incorrecto o expirado";
                    if (response.body() != null && response.body().getMessage() != null) {
                        mensaje = response.body().getMessage();
                    }
                    Toast.makeText(ResetPasswordActivity.this, mensaje, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                btnVerificarCodigo.setEnabled(true);
                progressStep2.setVisibility(View.GONE);
                Toast.makeText(ResetPasswordActivity.this, 
                    "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void reenviarCodigo() {
        // Reutilizar la función de solicitar código
        Toast.makeText(this, "Reenviando código...", Toast.LENGTH_SHORT).show();
        
        // Mostrar loading
        progressStep2.setVisibility(View.VISIBLE);
        tvReenviarCodigo.setEnabled(false);

        SolicitarCodigoRequest request = new SolicitarCodigoRequest(correoActual);

        api.solicitarCodigoRecuperacion(request).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                progressStep2.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                    Toast.makeText(ResetPasswordActivity.this, 
                        "Código reenviado exitosamente", Toast.LENGTH_SHORT).show();
                    // Reiniciar timers
                    stopCountDownTimer();
                    startCountDownTimer();
                    startResendTimer();
                } else {
                    tvReenviarCodigo.setEnabled(true);
                    Toast.makeText(ResetPasswordActivity.this, 
                        "Error al reenviar el código", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                progressStep2.setVisibility(View.GONE);
                tvReenviarCodigo.setEnabled(true);
                Toast.makeText(ResetPasswordActivity.this, 
                    "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ============================================
    // PASO 3: Restablecer contraseña
    // ============================================
    private void restablecerPassword() {
        String nuevaPassword = etNuevaPassword.getText() != null ? 
            etNuevaPassword.getText().toString().trim() : "";
        String confirmarPassword = etConfirmarPassword.getText() != null ? 
            etConfirmarPassword.getText().toString().trim() : "";

        // Validaciones
        if (TextUtils.isEmpty(nuevaPassword)) {
            etNuevaPassword.setError("Ingresa tu nueva contraseña");
            etNuevaPassword.requestFocus();
            return;
        }

        if (nuevaPassword.length() < 6) {
            etNuevaPassword.setError("La contraseña debe tener al menos 6 caracteres");
            etNuevaPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmarPassword)) {
            etConfirmarPassword.setError("Confirma tu contraseña");
            etConfirmarPassword.requestFocus();
            return;
        }

        if (!nuevaPassword.equals(confirmarPassword)) {
            etConfirmarPassword.setError("Las contraseñas no coinciden");
            etConfirmarPassword.requestFocus();
            return;
        }

        // Mostrar loading
        btnRestablecerPassword.setEnabled(false);
        progressStep3.setVisibility(View.VISIBLE);

        // Crear request
        RestablecerPasswordRequest request = new RestablecerPasswordRequest(
            correoActual, codigoActual, nuevaPassword
        );

        // Llamar API
        api.restablecerPassword(request).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                btnRestablecerPassword.setEnabled(true);
                progressStep3.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                    // Éxito - volver al login
                    Toast.makeText(ResetPasswordActivity.this, 
                        "Contraseña restablecida exitosamente", Toast.LENGTH_LONG).show();
                    
                    // Regresar al login
                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // Error
                    String mensaje = "Error al restablecer la contraseña";
                    if (response.body() != null && response.body().getMessage() != null) {
                        mensaje = response.body().getMessage();
                    }
                    Toast.makeText(ResetPasswordActivity.this, mensaje, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                btnRestablecerPassword.setEnabled(true);
                progressStep3.setVisibility(View.GONE);
                Toast.makeText(ResetPasswordActivity.this, 
                    "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ============================================
    // Navegación entre pasos
    // ============================================
    private void showStep(int step) {
        viewFlipper.setDisplayedChild(step);
        updateIndicators(step);
    }

    private void updateIndicators(int currentStep) {
        // Resetear todos los indicadores
        indicatorStep1.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        indicatorStep2.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        indicatorStep3.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

        // Activar el indicador actual
        switch (currentStep) {
            case 0:
                indicatorStep1.setBackgroundColor(getResources().getColor(R.color.blue_zavira));
                break;
            case 1:
                indicatorStep1.setBackgroundColor(getResources().getColor(R.color.blue_zavira));
                indicatorStep2.setBackgroundColor(getResources().getColor(R.color.blue_zavira));
                break;
            case 2:
                indicatorStep1.setBackgroundColor(getResources().getColor(R.color.blue_zavira));
                indicatorStep2.setBackgroundColor(getResources().getColor(R.color.blue_zavira));
                indicatorStep3.setBackgroundColor(getResources().getColor(R.color.verde));
                break;
        }
    }

    // ============================================
    // Timers
    // ============================================
    
    /**
     * Inicia el timer de 15 minutos para expiración del código
     */
    private void startCountDownTimer() {
        // Cancelar timer anterior si existe
        stopCountDownTimer();
        
        // 15 minutos = 900,000 milisegundos
        countDownTimer = new CountDownTimer(900000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Calcular minutos y segundos
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                
                // Actualizar UI
                String timeText = String.format("Tiempo restante: %02d:%02d", minutes, seconds);
                tvTimer.setText(timeText);
                
                // Cambiar color cuando quedan menos de 2 minutos
                if (minutes < 2) {
                    tvTimer.setTextColor(getResources().getColor(R.color.rojo));
                } else {
                    tvTimer.setTextColor(getResources().getColor(R.color.naranja));
                }
            }

            @Override
            public void onFinish() {
                tvTimer.setText("Código expirado");
                tvTimer.setTextColor(getResources().getColor(R.color.rojo));
                Toast.makeText(ResetPasswordActivity.this, 
                    "El código ha expirado. Solicita uno nuevo.", Toast.LENGTH_LONG).show();
                
                // Deshabilitar botón de verificar
                btnVerificarCodigo.setEnabled(false);
            }
        };
        
        countDownTimer.start();
    }
    
    /**
     * Detiene el timer de 15 minutos
     */
    private void stopCountDownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
    
    /**
     * Inicia el timer de 60 segundos para habilitar el botón de reenvío
     */
    private void startResendTimer() {
        // Cancelar timer anterior si existe
        if (resendTimer != null) {
            resendTimer.cancel();
        }
        
        // Deshabilitar botón
        tvReenviarCodigo.setEnabled(false);
        
        // 60 segundos = 60,000 milisegundos
        resendTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvReenviarCodigo.setText("Reenviar (" + seconds + "s)");
            }

            @Override
            public void onFinish() {
                tvReenviarCodigo.setText("Reenviar");
                tvReenviarCodigo.setEnabled(true);
            }
        };
        
        resendTimer.start();
    }

    @Override
    public void onBackPressed() {
        int currentStep = viewFlipper.getDisplayedChild();
        
        if (currentStep > 0) {
            // Si no está en el primer paso, volver al paso anterior
            showStep(currentStep - 1);
        } else {
            // Si está en el primer paso, cerrar la actividad
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar timers al destruir la actividad
        stopCountDownTimer();
        if (resendTimer != null) {
            resendTimer.cancel();
        }
    }
}
