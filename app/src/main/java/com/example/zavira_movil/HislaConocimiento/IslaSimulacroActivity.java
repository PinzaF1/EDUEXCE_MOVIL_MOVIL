package com.example.zavira_movil.HislaConocimiento;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.R;
import com.example.zavira_movil.progreso.DiagnosticoInicial;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla de selección de modalidad para la Isla del Conocimiento.
 * - Las tarjetas (Fácil / Difícil) SOLO seleccionan.
 * - El POST al backend se hace únicamente al pulsar el botón "Iniciar".
 */
public class IslaSimulacroActivity extends AppCompatActivity {

    private LinearLayout cardFacil, cardDificil;
    private android.widget.ImageView checkFacil, checkDificil;
    private Button btnIniciar;
    private android.widget.ImageButton btnBack;

    private String modalidadSeleccionada = null; // "facil" | "dificil"
    private boolean busy = false;

    private static final String STATE_MODO = "state_modo";

    private Bundle savedState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedState = savedInstanceState;
        
        // Verificar diagnóstico inicial antes de permitir acceso
        verificarDiagnosticoInicial();
    }
    
    private void verificarDiagnosticoInicial() {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.diagnosticoProgreso().enqueue(new Callback<DiagnosticoInicial>() {
            @Override
            public void onResponse(Call<DiagnosticoInicial> call, Response<DiagnosticoInicial> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DiagnosticoInicial diagnostico = response.body();
                    if (diagnostico.tieneDiagnostico) {
                        // Ya completó el diagnóstico inicial, permitir acceso
                        inicializarVista();
                    } else {
                        // No ha completado el diagnóstico inicial, bloquear y redirigir
                        Toast.makeText(IslaSimulacroActivity.this, 
                            "Debes completar el diagnóstico inicial para acceder a las prácticas", 
                            Toast.LENGTH_LONG).show();
                        
                        // Redirigir al diagnóstico después de un breve delay
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            Intent intent = new Intent(IslaSimulacroActivity.this, com.example.zavira_movil.InfoAcademico.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }, 1500);
                    }
                } else {
                    // Error en la respuesta, bloquear por seguridad
                    Toast.makeText(IslaSimulacroActivity.this, 
                        "Error al verificar el diagnóstico. Debes completar el diagnóstico inicial primero", 
                        Toast.LENGTH_LONG).show();
                    finish();
                }
            }
            
            @Override
            public void onFailure(Call<DiagnosticoInicial> call, Throwable t) {
                // En caso de error, bloquear por seguridad
                Toast.makeText(IslaSimulacroActivity.this, 
                    "Error al verificar el diagnóstico. Debes completar el diagnóstico inicial primero", 
                    Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
    
    private void inicializarVista() {
        setContentView(R.layout.activity_isla_modality);
        
        // Configurar para que respete la barra de estado
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }

        cardFacil   = findViewById(R.id.cardFacil);
        cardDificil = findViewById(R.id.cardDificil);
        checkFacil  = findViewById(R.id.checkFacil);
        checkDificil= findViewById(R.id.checkDificil);
        btnIniciar  = findViewById(R.id.btnIniciar);
        btnBack     = findViewById(R.id.btnBack);
        
        // Aplicar formato de negrita a los textos de "¿Cómo funciona?"
        aplicarFormatoTextos();
        
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (savedState != null) {
            modalidadSeleccionada = savedState.getString(STATE_MODO);
        }
        aplicarSeleccion(modalidadSeleccionada);

        // Las tarjetas SOLO seleccionan y consumen el touch
        bindSoloSeleccion(cardFacil, () -> aplicarSeleccion("facil"));
        bindSoloSeleccion(cardDificil, () -> aplicarSeleccion("dificil"));

        // El botón inicia el simulacro
        btnIniciar.setOnClickListener(v -> {
            if (modalidadSeleccionada == null || busy) return;

            new AlertDialog.Builder(this)
                    .setTitle("Confirmar")
                    .setMessage("Iniciar simulacro en modo " +
                            ("facil".equals(modalidadSeleccionada) ? "Fácil" : "Difícil") + "?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Sí, iniciar", (d, w) -> iniciar(modalidadSeleccionada))
                    .show();
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_MODO, modalidadSeleccionada);
    }
    
    private void aplicarFormatoTextos() {
        // Item 1: "25 preguntas en total..." - 3 líneas
        TextView tvItem1 = findViewById(R.id.tvItem1);
        if (tvItem1 != null) {
            String texto1 = "25 preguntas en total (5 por cada área: Matemáticas, Lenguaje, Ciencias, Sociales e Inglés)";
            SpannableString spannable1 = new SpannableString(texto1);
            // Negrita para "25"
            int start25 = texto1.indexOf("25");
            if (start25 >= 0) {
                spannable1.setSpan(new StyleSpan(Typeface.BOLD), start25, start25 + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            // Negrita para "preguntas"
            int startPreguntas = texto1.indexOf("preguntas");
            if (startPreguntas >= 0) {
                spannable1.setSpan(new StyleSpan(Typeface.BOLD), startPreguntas, startPreguntas + 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            tvItem1.setText(spannable1);
        }
        
        // Item 2: "Obtendrás un resultado por área y un porcentaje global" - 2 líneas
        TextView tvItem2 = findViewById(R.id.tvItem2);
        if (tvItem2 != null) {
            String texto2 = "Obtendrás un resultado por área y un porcentaje global";
            SpannableString spannable2 = new SpannableString(texto2);
            // Negrita para "resultado por área"
            int startResultado = texto2.indexOf("resultado por área");
            if (startResultado >= 0) {
                spannable2.setSpan(new StyleSpan(Typeface.BOLD), startResultado, startResultado + 18, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            // Negrita para "porcentaje global"
            int startPorcentaje = texto2.indexOf("porcentaje global");
            if (startPorcentaje >= 0) {
                spannable2.setSpan(new StyleSpan(Typeface.BOLD), startPorcentaje, startPorcentaje + 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            tvItem2.setText(spannable2);
        }
        
        // Item 3: "Elige entre modalidad fácil... o difícil..." - 2 líneas
        TextView tvItem3 = findViewById(R.id.tvItem3);
        if (tvItem3 != null) {
            String texto3 = "Elige entre modalidad fácil (sin límite de tiempo) o difícil (1 minuto por pregunta)";
            SpannableString spannable3 = new SpannableString(texto3);
            // Negrita para "fácil"
            int startFacil = texto3.indexOf("fácil");
            if (startFacil >= 0) {
                spannable3.setSpan(new StyleSpan(Typeface.BOLD), startFacil, startFacil + 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            // Negrita para "difícil"
            int startDificil = texto3.indexOf("difícil");
            if (startDificil >= 0) {
                spannable3.setSpan(new StyleSpan(Typeface.BOLD), startDificil, startDificil + 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            tvItem3.setText(spannable3);
        }
    }

    private void bindSoloSeleccion(LinearLayout card, Runnable onSelect) {
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> onSelect.run());
        card.setOnLongClickListener(v -> true); // consume long click
        card.setOnTouchListener((v, e) -> {
            if (e.getAction() == MotionEvent.ACTION_UP) v.performClick();
            return true; // SIEMPRE consumimos el touch
        });
    }

    private void aplicarSeleccion(@Nullable String modo) {
        modalidadSeleccionada = modo;

        boolean esFacil = "facil".equals(modo);
        boolean esDificil = "dificil".equals(modo);

        cardFacil.setSelected(esFacil);
        cardDificil.setSelected(esDificil);

        checkFacil.setVisibility(esFacil ? View.VISIBLE : View.GONE);
        // Para difícil: mostrar checkmark rojo si está seleccionado, círculo vacío si no
        if (esDificil) {
            checkDificil.setImageResource(R.drawable.ic_checkmark_red_circle);
            checkDificil.setVisibility(View.VISIBLE);
        } else {
            checkDificil.setImageResource(R.drawable.ic_checkmark_unselected);
            checkDificil.setVisibility(View.VISIBLE);
        }

        boolean habilitar = modo != null && !busy;
        btnIniciar.setEnabled(habilitar);
        btnIniciar.setAlpha(habilitar ? 1f : 0.6f);
        btnIniciar.setText("Iniciar Simulacro");
    }

    private void iniciar(String modalidad) {
        busy = true;
        aplicarSeleccion(modalidadSeleccionada); // refresca estado del botón
        Toast.makeText(this, "Iniciando modo " + modalidad + "…", Toast.LENGTH_SHORT).show();

        ApiService api = RetrofitClient.getInstance(getApplicationContext()).create(ApiService.class);
        Call<IslaSimulacroResponse> call = api.iniciarIslaSimulacro(new IslaSimulacroRequest(modalidad));

        call.enqueue(new Callback<IslaSimulacroResponse>() {
            @Override
            public void onResponse(Call<IslaSimulacroResponse> call, Response<IslaSimulacroResponse> response) {
                busy = false;
                aplicarSeleccion(modalidadSeleccionada);

                // Token vencido/no presente
                if (response.code() == 401) {
                    Toast.makeText(IslaSimulacroActivity.this,
                            "Token requerido. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show();
                    return;
                }

                // Detecta 404 por NGROK offline (ngrok agrega el header 'ngrok-error-code')
                String ngrokCode = response.headers().get("ngrok-error-code");
                if (response.code() == 404 && ngrokCode != null) {
                    Toast.makeText(IslaSimulacroActivity.this,
                            "Servidor ngrok OFFLINE. Inicia el túnel o actualiza la BASE_URL.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(IslaSimulacroActivity.this,
                            "No se pudo iniciar el simulacro (HTTP " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                IslaSimulacroResponse data = response.body();

                // Navega a tu pantalla de preguntas de la Isla
                Intent i = new Intent(IslaSimulacroActivity.this, IslaPreguntasActivity.class);
                i.putExtra("modalidad", modalidad);
                // Usa tu GsonHolder del paquete HislaConocimiento
                i.putExtra("payload", GsonHolder.gson().toJson(data));
                startActivity(i);
            }

            @Override
            public void onFailure(Call<IslaSimulacroResponse> call, Throwable t) {
                busy = false;
                aplicarSeleccion(modalidadSeleccionada);
                Toast.makeText(IslaSimulacroActivity.this,
                        "Error de red: " + (t.getMessage() == null ? "desconocido" : t.getMessage()),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
