package com.example.zavira_movil;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.Home.HomeActivity;
import com.example.zavira_movil.local.TokenManager;

public class MainActivity extends AppCompatActivity {

    private static final String TEXT_TO_WRITE = "EduExce";
    private static final int DELAY_BETWEEN_LETTERS = 150; // Milisegundos entre letras
    private static final int DELAY_AFTER_LOGO = 500; // Esperar antes de empezar a escribir
    private static final int DELAY_AFTER_TEXT = 800; // Esperar después de escribir todo el texto
    private static final int LOGO_ANIMATION_DURATION = 1000; // Duración de la animación del logo

    private TextView tvEduExce;
    private ImageView imgLogo;
    private Handler handler;
    private int currentLetterIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configurar status bar blanca
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        getWindow().setStatusBarColor(android.graphics.Color.WHITE);

        tvEduExce = findViewById(R.id.tvEduExce);
        imgLogo = findViewById(R.id.imgLogo);
        handler = new Handler(Looper.getMainLooper());

        // Iniciar animaciones
        startAnimations();
    }

    private void startAnimations() {
        // 1. Animar el logo (fade in)
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(imgLogo, "alpha", 0f, 1f);
        fadeIn.setDuration(LOGO_ANIMATION_DURATION);
        fadeIn.start();

        // 2. Después de que aparezca el logo, empezar a escribir el texto
        handler.postDelayed(() -> {
            startTypewriterEffect();
        }, DELAY_AFTER_LOGO + LOGO_ANIMATION_DURATION);
    }

    private void startTypewriterEffect() {
        currentLetterIndex = 0;
        tvEduExce.setText("");
        writeNextLetter();
    }

    private void writeNextLetter() {
        if (currentLetterIndex < TEXT_TO_WRITE.length()) {
            String currentText = TEXT_TO_WRITE.substring(0, currentLetterIndex + 1);
            tvEduExce.setText(currentText);
            currentLetterIndex++;
            
            // Programar la siguiente letra
            handler.postDelayed(this::writeNextLetter, DELAY_BETWEEN_LETTERS);
        } else {
            // Cuando termine de escribir, esperar un poco y navegar
            handler.postDelayed(this::navigateToNextScreen, DELAY_AFTER_TEXT);
        }
    }

    private void navigateToNextScreen() {
        TokenManager tokenManager = new TokenManager();
        String token = tokenManager.getToken(this);

        Intent intent;
        if (token != null && !token.isEmpty()) {
            // Si hay token → va al Home
            intent = new Intent(this, HomeActivity.class);
        } else {
            // Si no hay token → va al Login
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar handlers para evitar memory leaks
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
