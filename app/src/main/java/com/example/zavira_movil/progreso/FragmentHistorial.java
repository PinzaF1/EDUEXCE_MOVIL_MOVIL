package com.example.zavira_movil.progreso;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.detalleprogreso.FragmentDetalleSimulacro;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentHistorial extends Fragment {

    private RecyclerView rv;
    private ProgressBar progress;
    private TextView tvError;
    private HistorialAdapter adapter;

    private int currentPage = 1;
    private final int pageSize = 20;

    public FragmentHistorial() { super(R.layout.fragment_historial); }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        rv = v.findViewById(R.id.rvPreguntas);
        progress = v.findViewById(R.id.progress);
        tvError = v.findViewById(R.id.tvError);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistorialAdapter();
        rv.setAdapter(adapter);

        // Click en item -> abrir detalle (Resumen)
        adapter.setOnItemClick(this::abrirDetalle);

        cargarHistorial(currentPage, pageSize);
    }

    private void cargarHistorial(int page, int limit) {
        mostrarCargando(true);
        tvError.setVisibility(View.GONE);

        ApiService api = RetrofitClient.getInstance(getContext()).create(ApiService.class);
        api.getHistorial(page, limit).enqueue(new Callback<HistorialResponse>() {
            @Override
            public void onResponse(Call<HistorialResponse> call, Response<HistorialResponse> resp) {
                mostrarCargando(false);

                if (!resp.isSuccessful() || resp.body() == null) {
                    mostrarError("HTTP " + resp.code());
                    return;
                }

                List<HistorialItem> items = resp.body().getItems();
                if (items == null || items.isEmpty()) {
                    mostrarError("Sin historial disponible");
                } else {
                    tvError.setVisibility(View.GONE);
                    adapter.setData(items);
                }
            }

            @Override
            public void onFailure(Call<HistorialResponse> call, Throwable t) {
                mostrarCargando(false);
                mostrarError("Fallo de red: " + (t.getMessage() != null ? t.getMessage() : ""));
            }
        });
    }

    private void mostrarCargando(boolean s) {
        if (progress != null) progress.setVisibility(s ? View.VISIBLE : View.GONE);
    }

    private void mostrarError(String msg) {
        if (tvError != null) {
            tvError.setText(msg != null ? msg : "Error");
            tvError.setVisibility(View.VISIBLE);
        }
        if (adapter != null) adapter.setData(Collections.emptyList());
    }

    // -------- Navegación al detalle (ENVÍA id_sesion) --------
    private void abrirDetalle(HistorialItem it) {
        if (!isAdded()) return;

        // Validar disponibilidad de detalle
        if (it != null && !it.isDetalleDisponible()) {
            android.widget.Toast.makeText(requireContext(),
                    "Este intento no tiene detalle disponible",
                    android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // Parsear intentoId -> id_sesion (int)
        int idSesion = safeParseInt(it != null ? it.getIntentoId() : null);
        if (idSesion <= 0) {
            android.widget.Toast.makeText(requireContext(),
                    "id_sesion faltante", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // Argumentos para el detalle
        Bundle b = new Bundle();
        b.putInt("id_sesion", idSesion);

        // Opcionales: puedes mostrarlos de inmediato mientras carga
        if (it != null) {
            b.putString("materia", it.getMateria());
            b.putInt("porcentaje", it.getPorcentaje());
            b.putString("nivel", it.getNivel());
            b.putString("fecha", it.getFecha());
        }

        // 👉 Forzar que el detalle abra en la pestaña "Resumen" (posición 0)
        b.putInt("initial_tab", 0); // el FragmentDetalleSimulacro puede leerlo y hacer pager.setCurrentItem(0,false)

        Fragment f = new FragmentDetalleSimulacro();
        f.setArguments(b);

        int root = idByName("fragmentContainer");
        if (root == 0) root = idByName("main_container");
        if (root == 0) root = android.R.id.content;

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(root, f)
                .addToBackStack("detalleSimulacro")
                .commit();
    }

    // helper para resolver id por nombre sin acoplarte a una sola Activity
    private int idByName(String name) {
        try {
            return getResources().getIdentifier(name, "id", requireContext().getPackageName());
        } catch (Exception e) {
            return 0;
        }
    }

    // parseo seguro de String -> int
    private int safeParseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return -1; }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rv = null; progress = null; tvError = null;
    }
}
