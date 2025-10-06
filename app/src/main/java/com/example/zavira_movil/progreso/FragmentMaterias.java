package com.example.zavira_movil.progreso;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.adapter.ProgresoAdapter;
import com.example.zavira_movil.model.MateriaDetalle;
import com.example.zavira_movil.model.MateriasResponse;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentMaterias extends Fragment {

    private RecyclerView recyclerMaterias;
    private ProgresoAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_materias, container, false);

        recyclerMaterias = view.findViewById(R.id.recyclerMaterias); // ← ID exacto de tu XML
        recyclerMaterias.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerMaterias.setHasFixedSize(false);
        recyclerMaterias.setItemAnimator(null); // opcional: evita “saltos” al refrescar

        adapter = new ProgresoAdapter(new ArrayList<>()); // mismo adapter que ya usas
        recyclerMaterias.setAdapter(adapter);

        cargarMaterias();
        return view;
    }

    private void cargarMaterias() {
        ApiService apiService = RetrofitClient.getInstance(requireContext()).create(ApiService.class);

        apiService.getMaterias().enqueue(new Callback<MateriasResponse>() {
            @Override
            public void onResponse(Call<MateriasResponse> call, Response<MateriasResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<MateriaDetalle> materias = response.body().getMaterias();
                    adapter.setLista(materias != null ? materias : new ArrayList<>());
                } else {
                    Log.e("Materias", "Error HTTP: " + response.code());
                    adapter.setLista(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<MateriasResponse> call, Throwable t) {
                if (!isAdded()) return;
                Log.e("Materias", "Fallo: " + t.getMessage());
                adapter.setLista(new ArrayList<>());
            }
        });
    }
}
