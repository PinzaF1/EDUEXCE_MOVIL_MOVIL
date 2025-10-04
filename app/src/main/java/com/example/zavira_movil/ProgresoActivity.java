package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.adapter.HistorialAdapter;
import com.example.zavira_movil.adapter.ProgresoAdapter;
import com.example.zavira_movil.local.TokenManager;
import com.example.zavira_movil.model.HistorialItem;
import com.example.zavira_movil.model.ProgresoMateria;
import com.example.zavira_movil.model.ResumenGeneral;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProgresoActivity extends AppCompatActivity {

    private static final String TAG = "PA"; // logs cortos: PA/...

    private CircularProgressIndicator progresoGeneral;
    private TextView textoProgreso;

    private TabLayout tabLayout;
    private FrameLayout contenedorTabs;
    private View generalContainer;

    private RecyclerView rvMaterias, rvHistorial;
    private ProgresoAdapter adapterMaterias;
    private HistorialAdapter adapterHistorial;

    private final List<ProgresoMateria> listaMaterias = new ArrayList<>();
    private final List<HistorialItem> listaHistorial = new ArrayList<>();

    private ApiService api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_progreso);

            // 1) TOKEN
            String token = TokenManager.getToken(this);
            Log.d(TAG, "tokenLen=" + (token == null ? 0 : token.length()));
            if (token == null || token.isEmpty()) {
                Toast.makeText(this, "Sesión expirada. Inicia sesión.", Toast.LENGTH_LONG).show();
                irALoginYTerminar();
                return;
            }
            String authHeader = "Bearer " + token;

            // 2) REFERENCIAS UI (con validación de nulls)
            progresoGeneral = findViewById(R.id.progresoGeneral);
            textoProgreso    = findViewById(R.id.textoProgreso);
            tabLayout        = findViewById(R.id.tabLayout);
            contenedorTabs   = findViewById(R.id.contenedorTabs);
            generalContainer = findViewById(R.id.generalContainer);

            if (progresoGeneral == null || textoProgreso == null || tabLayout == null
                    || contenedorTabs == null || generalContainer == null) {
                Toast.makeText(this,
                        "Layout inválido: faltan IDs requeridos en activity_progreso.xml",
                        Toast.LENGTH_LONG).show();
                Log.e(TAG, "Algún view es null. Revisa IDs: tabLayout, generalContainer, progresoGeneral, textoProgreso, contenedorTabs");
                finish(); // salimos sin crashear
                return;
            }

            // 3) TABS + LISTAS
            tabLayout.addTab(tabLayout.newTab().setText("General"));
            tabLayout.addTab(tabLayout.newTab().setText("Materias"));
            tabLayout.addTab(tabLayout.newTab().setText("Historial"));

            rvMaterias = new RecyclerView(this);
            rvMaterias.setLayoutManager(new LinearLayoutManager(this));
            rvMaterias.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));
            adapterMaterias = new ProgresoAdapter(listaMaterias);
            rvMaterias.setAdapter(adapterMaterias);

            rvHistorial = new RecyclerView(this);
            rvHistorial.setLayoutManager(new LinearLayoutManager(this));
            rvHistorial.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));
            adapterHistorial = new HistorialAdapter(listaHistorial);
            rvHistorial.setAdapter(adapterHistorial);

            // 4) API SERVICE
            api = RetrofitClient.getInstance(this).create(ApiService.class);

            // 5) LISTENER TABS (a prueba de nulos)
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override public void onTabSelected(TabLayout.Tab tab) {
                    if (tab == null) return;
                    contenedorTabs.removeAllViews();
                    int pos = tab.getPosition();
                    if (pos == 0) {
                        generalContainer.setVisibility(View.VISIBLE);
                    } else if (pos == 1) {
                        generalContainer.setVisibility(View.GONE);
                        contenedorTabs.addView(rvMaterias);
                    } else if (pos == 2) {
                        generalContainer.setVisibility(View.GONE);
                        contenedorTabs.addView(rvHistorial);
                    }
                }
                @Override public void onTabUnselected(TabLayout.Tab tab) {}
                @Override public void onTabReselected(TabLayout.Tab tab) {}
            });

            // 6) ESTADO INICIAL SEGURO
            if (tabLayout.getTabCount() > 0 && tabLayout.getTabAt(0) != null) {
                tabLayout.getTabAt(0).select();
            }
            generalContainer.setVisibility(View.VISIBLE);
            contenedorTabs.removeAllViews();
            progresoGeneral.setIndeterminate(false);
            progresoGeneral.setProgress(0);
            textoProgreso.setText("0%");

            // 7) CARGA DATOS (con manejo de 401/403)
            cargarResumen(authHeader);
            cargarMaterias(authHeader);
            cargarHistorial(authHeader);

        } catch (Throwable t) {
            Log.e(TAG, "onCreate fatal", t);
            Toast.makeText(this, "Ocurrió un error y se cerró la pantalla.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void cargarResumen(String authHeader) {
        api.getProgresoGeneral(authHeader).enqueue(new Callback<ResumenGeneral>() {
            @Override public void onResponse(Call<ResumenGeneral> call, Response<ResumenGeneral> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    int v = resp.body().getProgresoGeneral();
                    progresoGeneral.setIndeterminate(false);
                    progresoGeneral.setProgress(v);
                    textoProgreso.setText(v + "%");
                } else if (resp.code() == 401 || resp.code() == 403) {
                    Toast.makeText(ProgresoActivity.this, "Sesión no válida (" + resp.code() + ").", Toast.LENGTH_LONG).show();
                    irALoginYTerminar();
                } else {
                    Log.w(TAG, "Resumen HTTP " + resp.code());
                    Toast.makeText(ProgresoActivity.this, "HTTP " + resp.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<ResumenGeneral> call, Throwable t) {
                Log.e(TAG, "Resumen onFailure", t);
                Toast.makeText(ProgresoActivity.this, "Red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarMaterias(String authHeader) {
        api.getProgresoMaterias(authHeader).enqueue(new Callback<List<ProgresoMateria>>() {
            @Override public void onResponse(Call<List<ProgresoMateria>> call, Response<List<ProgresoMateria>> resp) {
                listaMaterias.clear();
                if (resp.isSuccessful() && resp.body() != null) {
                    listaMaterias.addAll(resp.body());
                } else if (resp.code() == 401 || resp.code() == 403) {
                    Toast.makeText(ProgresoActivity.this, "Sesión no válida (" + resp.code() + ").", Toast.LENGTH_LONG).show();
                    irALoginYTerminar();
                    return;
                } else {
                    Log.w(TAG, "Materias HTTP " + resp.code());
                    Toast.makeText(ProgresoActivity.this, "HTTP " + resp.code(), Toast.LENGTH_SHORT).show();
                }
                adapterMaterias.notifyDataSetChanged();
            }
            @Override public void onFailure(Call<List<ProgresoMateria>> call, Throwable t) {
                Log.e(TAG, "Materias onFailure", t);
                Toast.makeText(ProgresoActivity.this, "Error materias: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarHistorial(String authHeader) {
        api.getHistorial(authHeader, null, 1, 20, null, null).enqueue(new Callback<List<HistorialItem>>() {
            @Override public void onResponse(Call<List<HistorialItem>> call, Response<List<HistorialItem>> resp) {
                listaHistorial.clear();
                if (resp.isSuccessful() && resp.body() != null) {
                    listaHistorial.addAll(resp.body());
                } else if (resp.code() == 401 || resp.code() == 403) {
                    Toast.makeText(ProgresoActivity.this, "Sesión no válida (" + resp.code() + ").", Toast.LENGTH_LONG).show();
                    irALoginYTerminar();
                    return;
                } else {
                    Log.w(TAG, "Historial HTTP " + resp.code());
                    Toast.makeText(ProgresoActivity.this, "HTTP " + resp.code(), Toast.LENGTH_SHORT).show();
                }
                adapterHistorial.notifyDataSetChanged();
            }
            @Override public void onFailure(Call<List<HistorialItem>> call, Throwable t) {
                Log.e(TAG, "Historial onFailure", t);
                Toast.makeText(ProgresoActivity.this, "Error historial: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void irALoginYTerminar() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }
}
