package com.example.zavira_movil.Home;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.Perfil.ProfileActivity;
import com.example.zavira_movil.R;
import com.example.zavira_movil.databinding.ActivityHomeBinding;
import com.example.zavira_movil.niveleshome.DemoData;
import com.example.zavira_movil.niveleshome.SubjectAdapter;
import com.example.zavira_movil.ui.ranking.RankingLogrosFragment;
import com.example.zavira_movil.ui.ranking.progreso.ProgresoFragment;
import com.example.zavira_movil.ui.ranking.progreso.RetosFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private SubjectAdapter adapter;
    private ImageView ivBackdrop; // lo mantengo como lo tenías
    private LinearLayoutManager lm;

    private final ActivityResultLauncher<Intent> launcher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                    }
            );

    private static final Fragment BLANK_FRAGMENT = new Fragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // RecyclerView único (el primer ítem es la Isla del Conocimiento dentro del SubjectAdapter)
        binding.rvSubjects.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubjectAdapter(DemoData.getSubjects(), intent -> launcher.launch(intent));
        binding.rvSubjects.setAdapter(adapter);

        // ---------- Fondo del header dinámico SOLO en el encabezado ----------
        final ImageView ivParallax = findViewById(R.id.ivParallax);
        final View overlayFade     = findViewById(R.id.overlayFade);
        final View topBar          = findViewById(R.id.topBar);
        final RecyclerView rv      = findViewById(R.id.rvSubjects);

        // Imagen inicial: conocimiento (queda lista, pero oculta hasta que una tarjeta toque el header)
        ivParallax.setImageResource(R.drawable.fondoconocimiento);
        ivParallax.setAlpha(0f);
        overlayFade.setAlpha(0f);

        // Blur leve (Android 12+)
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            ivParallax.setRenderEffect(
                    android.graphics.RenderEffect.createBlurEffect(12f, 12f,
                            android.graphics.Shader.TileMode.CLAMP)
            );
        }

        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            Drawable lastDrawable = null;

            @Override public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);

                RecyclerView.LayoutManager _lm = rv.getLayoutManager();
                if (!(_lm instanceof LinearLayoutManager)) return;

                int firstPos = ((LinearLayoutManager) _lm).findFirstVisibleItemPosition();
                View first = ((LinearLayoutManager) _lm).findViewByPosition(firstPos);
                if (first == null) {
                    // Nada visible si no hay tarjeta visible
                    ivParallax.setAlpha(0f);
                    overlayFade.setAlpha(0f);
                    return;
                }

                // ¿La tarjeta ya "tocó" el borde inferior del header?
                int[] vLoc = new int[2];
                int[] bLoc = new int[2];
                first.getLocationOnScreen(vLoc);
                topBar.getLocationOnScreen(bLoc);

                int barBottom = bLoc[1] + topBar.getHeight();
                int overlap   = barBottom - vLoc[1]; // > 0 cuando cruzó el header

                if (overlap <= 0) {
                    // Mientras no toque el header, no se ve nada
                    ivParallax.setAlpha(0f);
                    overlayFade.setAlpha(0f);
                    return;
                }

                // Se nota un poquito más:
                // - Fondo (imagen de la tarjeta) hasta 0.42
                // - Velo blanco hasta 0.72
                int maxPx = dp(56);
                float imgA  = Math.min(1f, overlap / (float) maxPx) * 0.42f;
                float veloA = Math.min(1f, overlap / (float) maxPx) * 0.72f;
                ivParallax.setAlpha(imgA);
                overlayFade.setAlpha(veloA);

                // Tomar la imagen del header (flHeader) de la tarjeta que está tocando
                View flHeader = first.findViewById(R.id.flHeader);
                Drawable d = null;
                if (flHeader != null) {
                    // Si el header tiene un ImageView (tu SubjectAdapter lo añade como child 0)
                    if (flHeader instanceof android.widget.FrameLayout
                            && ((android.widget.FrameLayout) flHeader).getChildCount() > 0) {
                        View child0 = ((android.widget.FrameLayout) flHeader).getChildAt(0);
                        if (child0 instanceof ImageView) {
                            d = ((ImageView) child0).getDrawable();
                        }
                    }
                    // O intenta con el background del header si no hay hijo ImageView
                    if (d == null) d = flHeader.getBackground();
                }

                // Fallback para el item 0 (Conocimiento)
                if (d == null && firstPos == 0) {
                    d = getDrawable(R.drawable.fondoconocimiento);
                }

                // Evita reasignar si es el mismo drawable (previene flicker)
                if (d != null && d != lastDrawable) {
                    ivParallax.setImageDrawable(d);
                    lastDrawable = d;
                }
            }
        });
        // --------------------------------------------------------------------

        // CLICK EN LA CAMPANA → abre NotificationsActivity
        View btnBell = findViewById(R.id.btnBell);
        View ivBell  = findViewById(R.id.ivBell);
        View.OnClickListener goNotifications = v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        };
        if (btnBell != null) btnBell.setOnClickListener(goNotifications);
        if (ivBell  != null) ivBell.setOnClickListener(goNotifications);

        // Bottom navigation
        setupBottomNav(binding.bottomNav);

        // Pestaña por defecto: Islas
        if (savedInstanceState == null) {
            show(BLANK_FRAGMENT);
            applyTabVisibility(true);
            binding.bottomNav.setSelectedItemId(R.id.nav_islas);
        }
    }

    private void setupBottomNav(BottomNavigationView nav) {
        //   Tinte azul al seleccionado y gris al resto (por CÓDIGO)  ⬇
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{} // default
        };
        int[] colors = new int[]{
                ContextCompat.getColor(this, R.color.primaryyy),
                ContextCompat.getColor(this, R.color.text_secondary)
        };
        ColorStateList tint = new ColorStateList(states, colors);
        nav.setItemIconTintList(tint);
        nav.setItemTextColor(tint);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_islas) {
                applyTabVisibility(true);
                show(BLANK_FRAGMENT);
                return true;
            }

            // PERFIL es una Activity (no Fragment)
            if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, ProfileActivity.class));
                return false; // mantiene la selección anterior
            }

            Fragment f;
            if (id == R.id.nav_progreso) {
                f = new ProgresoFragment();
            } else if (id == R.id.nav_logros) {
                f = new RankingLogrosFragment();
            } else if (id == R.id.nav_retos) {
                f = new RetosFragment();
            } else {
                return false;
            }

            applyTabVisibility(false);
            show(f);
            return true;
        });
    }

    private void applyTabVisibility(boolean isIslas) {
        binding.rvSubjects.setVisibility(isIslas ? View.VISIBLE : View.GONE);
    }

    private void show(Fragment f) {
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragmentContainer, f)
                .commit();
    }

    private int dp(int v) {
        return (int) (getResources().getDisplayMetrics().density * v);
    }
}
