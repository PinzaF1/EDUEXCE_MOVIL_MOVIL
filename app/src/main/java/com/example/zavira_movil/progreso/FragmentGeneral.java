package com.example.zavira_movil.progreso;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentGeneral extends Fragment {

    private CircularProgressIndicator progresoGeneral;
    private TextView textoProgreso, tvNivelActual;
    private RecyclerView recyclerNiveles;      // mismo ID del XML
    private MateriasAdapter materiasAdapter;   // ← ahora materias

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_general, container, false);

        progresoGeneral = v.findViewById(R.id.progresoGeneral);
        textoProgreso   = v.findViewById(R.id.textoProgreso);
        tvNivelActual   = v.findViewById(R.id.tvNivelActual);
        recyclerNiveles = v.findViewById(R.id.recyclerNiveles);

        progresoGeneral.setIndeterminate(false);
        progresoGeneral.setMax(100);

        recyclerNiveles.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerNiveles.setNestedScrollingEnabled(false);
        recyclerNiveles.setHasFixedSize(false);
        recyclerNiveles.setItemAnimator(null);

        materiasAdapter = new MateriasAdapter();
        materiasAdapter.setLista(new ArrayList<>()); // vacío inicial
        recyclerNiveles.setAdapter(materiasAdapter);

        cargarResumen();   // anillo + nivel actual
        cargarMaterias();  // lista de materias
        return v;
    }

    private void cargarResumen() {
        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        api.getResumen().enqueue(new Callback<ResumenGeneral>() {
            @Override
            public void onResponse(Call<ResumenGeneral> call, Response<ResumenGeneral> res) {
                if (!isAdded()) return;

                if (res.isSuccessful() && res.body() != null) {
                    ResumenGeneral rg = res.body();

                    int valor = rg.getProgresoGlobal();
                    try {
                        progresoGeneral.setProgressCompat(valor, true);
                    } catch (NoSuchMethodError e) {
                        progresoGeneral.setProgress(valor);
                    }
                    textoProgreso.setText(valor + "%");

                    tvNivelActual.setText(rg.getNivelActual() != null ? rg.getNivelActual() : "");
                } else {
                    Log.e("API", "HTTP " + res.code());
                }
            }

            @Override
            public void onFailure(Call<ResumenGeneral> call, Throwable t) {
                if (!isAdded()) return;
                Log.e("API", "Fallo: " + t.getMessage());
            }
        });
    }

    private void cargarMaterias() {
        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        api.getMaterias().enqueue(new Callback<MateriasResponse>() {
            @Override
            public void onResponse(Call<MateriasResponse> call, Response<MateriasResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null && response.body().getMaterias() != null) {
                    materiasAdapter.setLista(response.body().getMaterias());
                } else {
                    Log.e("Materias", "Error HTTP: " + response.code());
                    materiasAdapter.setLista(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<MateriasResponse> call, Throwable t) {
                if (!isAdded()) return;
                Log.e("Materias", "Fallo: " + t.getMessage());
                materiasAdapter.setLista(new ArrayList<>());
            }
        });
    }
}
