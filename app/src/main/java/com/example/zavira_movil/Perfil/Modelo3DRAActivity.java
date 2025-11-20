package com.example.zavira_movil.Perfil;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.R;

public class Modelo3DRAActivity extends AppCompatActivity {

    private HorizontalScrollView hsVideosRA;
    private LinearLayout llTarjetasVideos;
    private final Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private int currentCardIndex = 0;
    private boolean isUserInteracting = false;

    private final Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (hsVideosRA == null || llTarjetasVideos == null) return;
            int childCount = llTarjetasVideos.getChildCount();
            if (childCount == 0) return;

            // Si llegamos al final, saltar instantáneamente al inicio
            if (currentCardIndex >= childCount) {
                currentCardIndex = 0;
                hsVideosRA.scrollTo(0, 0); // sin animación para que no se vea que se devuelve
            }

            View card = llTarjetasVideos.getChildAt(currentCardIndex);
            if (card == null) return;

            // Calcular a dónde hacer scroll (alinear la tarjeta al inicio)
            int targetX = card.getLeft() - 16; // pequeño ajuste por padding
            if (targetX < 0) targetX = 0;

            hsVideosRA.smoothScrollTo(targetX, 0);

            // Animar escala de la tarjeta (crece y vuelve) de forma suave y más rápida
            card.animate()
                    .scaleX(1.06f)
                    .scaleY(1.06f)
                    .setDuration(300)
                    .withEndAction(() ->
                            card.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setDuration(300)
                                    .start()
                    )
                    .start();

            currentCardIndex = (currentCardIndex + 1) % childCount;
            autoScrollHandler.postDelayed(this, 2000); // cada 2 segundos, más ágil
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modelo_3d_ra);

        // Configurar flecha de retroceso
        ImageView ivBackArrow = findViewById(R.id.ivBackArrow);
        if (ivBackArrow != null) {
            ivBackArrow.setOnClickListener(v -> finish());
        }

        hsVideosRA = findViewById(R.id.hsVideosRA);
        llTarjetasVideos = findViewById(R.id.llTarjetasVideos);

        // Botones: abrir URLs correspondientes a cada área
        View btnVideoMatematicas = findViewById(R.id.btnVideoMatematicas);
        if (btnVideoMatematicas != null) {
            btnVideoMatematicas.setOnClickListener(v -> openUrl("https://lennyjazz.8thwall.app/ejemplo-mate/"));
        }

        View btnVideoLectura = findViewById(R.id.btnVideoLectura);
        if (btnVideoLectura != null) {
            btnVideoLectura.setOnClickListener(v -> openUrl("https://lennyjazz.8thwall.app/lenguaje/"));
        }

        View btnVideoIngles = findViewById(R.id.btnVideoIngles);
        if (btnVideoIngles != null) {
            btnVideoIngles.setOnClickListener(v -> openUrl("https://lennyjazz.8thwall.app/modelo-english/"));
        }

        View btnVideoSociales = findViewById(R.id.btnVideoSociales);
        if (btnVideoSociales != null) {
            btnVideoSociales.setOnClickListener(v -> openUrl("https://lennyjazz.8thwall.app/sociales-modelo/"));
        }

        View btnVideoCiencias = findViewById(R.id.btnVideoCiencias);
        if (btnVideoCiencias != null) {
            btnVideoCiencias.setOnClickListener(v -> openUrl("https://lennyjazz.8thwall.app/naturales/"));
        }

        // Cards de juegos RA (Doty y Bailarina)
        View cardDoty = findViewById(R.id.cardDoty);
        if (cardDoty != null) {
            cardDoty.setOnClickListener(v -> openUrl("https://danieldaz.8thwall.app/edudexce/"));
        }

        View cardBailarina = findViewById(R.id.cardBailarina);
        if (cardBailarina != null) {
            cardBailarina.setOnClickListener(v -> openUrl("https://anamaraziga.8thwall.app/movimientosbailando/"));
        }

        // Iniciar auto-scroll cuando el layout esté listo
        if (hsVideosRA != null && llTarjetasVideos != null) {
            hsVideosRA.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            hsVideosRA.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            startAutoScroll();
                        }
                    }
            );

            // Permitir interacción del usuario y pausar/reanudar auto-scroll
            hsVideosRA.setOnTouchListener((v, event) -> {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        isUserInteracting = true;
                        stopAutoScroll();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isUserInteracting = false;
                        // Reanudar después de un pequeño delay
                        autoScrollHandler.postDelayed(this::startAutoScroll, 2000);
                        break;
                }
                return false; // seguir permitiendo el scroll normal
            });
        }

        // Configurar status bar azul oscuro (igual que en Progreso)
        getWindow().setStatusBarColor(android.graphics.Color.parseColor("#2563EB"));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            androidx.core.view.WindowInsetsControllerCompat windowInsetsController =
                    androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            if (windowInsetsController != null) {
                windowInsetsController.setAppearanceLightStatusBars(false); // Texto blanco sobre fondo azul
            }
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            flags = flags & ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    private void startAutoScroll() {
        stopAutoScroll();
        autoScrollHandler.postDelayed(autoScrollRunnable, 1000); // arranque más rápido
    }

    private void stopAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoScroll();
    }

    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}



