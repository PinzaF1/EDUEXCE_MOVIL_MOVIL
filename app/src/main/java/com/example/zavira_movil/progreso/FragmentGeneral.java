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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentGeneral extends Fragment {

    private CircularProgressIndicator progresoGeneral;
    private TextView textoProgreso, tvNivelActual;
    private RecyclerView recyclerNiveles;
    private NivelesAdapter nivelesAdapter;

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

        // Para que el RecyclerView se expanda dentro del ScrollView
        recyclerNiveles.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerNiveles.setNestedScrollingEnabled(false);
        recyclerNiveles.setHasFixedSize(false);
        recyclerNiveles.setItemAnimator(null);

        nivelesAdapter = new NivelesAdapter();
        recyclerNiveles.setAdapter(nivelesAdapter);

        cargarResumen();
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
                    Log.d("API", "progresoGlobal=" + valor);

                    try {
                        progresoGeneral.setProgressCompat(valor, true);
                    } catch (NoSuchMethodError e) {
                        progresoGeneral.setProgress(valor);
                    }
                    textoProgreso.setText(valor + "%");

                    tvNivelActual.setText(rg.getNivelActual() != null ? rg.getNivelActual() : "");

                    if (rg.getNiveles() != null) {
                        // >>> IMPORTANTE: primero el % global, luego la lista
                        nivelesAdapter.setGlobalProgress(valor);
                        nivelesAdapter.setData(rg.getNiveles());
                    }
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
}
