package com.example.zavira_movil.retos1vs1;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentRetosRecibidos extends Fragment {

    private ProgressBar pb;
    private TextView tvEmpty;
    private RecyclerView rv;
    private RecibidosAdapter adapter;

    private Handler handler;
    private int polls = 0;
    private final Runnable poller = this::cargar;

    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inf, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inf.inflate(R.layout.fragment_retos_recibidos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        pb      = v.findViewById(R.id.progressRecibidos);
        tvEmpty = v.findViewById(R.id.tvEmpty);
        rv      = v.findViewById(R.id.rvRecibidos);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecibidosAdapter(this::aceptarYIrSala);
        rv.setAdapter(adapter);

        handler = new Handler();
        polls = 0;
        cargar();
    }

    @Override
    public void onDestroyView() {
        if (handler != null) { handler.removeCallbacksAndMessages(null); handler = null; }
        super.onDestroyView();
    }

    private void cargar() {
        if (!isAdded()) return;
        mostrarCargando(true);
        tvEmpty.setVisibility(View.GONE);

        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        Call<List<RetoListItem>> call = api.listarRetos();

        call.enqueue(new Callback<List<RetoListItem>>() {
            @Override public void onResponse(Call<List<RetoListItem>> call, Response<List<RetoListItem>> resp) {
                if (!isAdded()) return;
                mostrarCargando(false);

                List<RetoListItem> lista = (resp.isSuccessful() && resp.body() != null)
                        ? resp.body()
                        : new ArrayList<>();

                adapter.setData(lista);

                if (lista.isEmpty()) {
                    tvEmpty.setText("No tienes retos recibidos.");
                    tvEmpty.setVisibility(View.VISIBLE);
                    if (handler != null && polls < 20) {
                        polls++;
                        handler.postDelayed(poller, polls < 10 ? 800 : 1500);
                    }
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }
            }

            @Override public void onFailure(Call<List<RetoListItem>> call, Throwable t) {
                if (!isAdded()) return;
                mostrarCargando(false);
                tvEmpty.setText("Fallo de red: " + (t.getMessage() != null ? t.getMessage() : ""));
                tvEmpty.setVisibility(View.VISIBLE);
                adapter.setData(new ArrayList<>());
                if (handler != null && polls < 5) {
                    polls++;
                    handler.postDelayed(poller, 1500);
                }
            }
        });
    }

    /** Acepta el reto (primero sin body; si falla, con body {} ) y abre sala como retado. */
    private void aceptarYIrSala(RetoListItem it) {
        if (!isAdded()) return;

        if (it == null || it.getIdReto() == null) {
            Toast.makeText(requireContext(), "Reto inválido", Toast.LENGTH_SHORT).show();
            return;
        }
        mostrarCargando(true);

        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        final String retoId = String.valueOf(it.getIdReto());

        // 1) Intento sin body
        api.aceptarReto(retoId).enqueue(new Callback<AceptarRetoResponse>() {
            @Override public void onResponse(Call<AceptarRetoResponse> call, Response<AceptarRetoResponse> resp) {
                if (!isAdded()) return;

                if (resp.isSuccessful() && resp.body() != null) {
                    mostrarCargando(false);
                    abrirSalaDespuesDeAceptar(it);
                } else {
                    // 2) Fallback con body {}
                    Map<String, Object> body = new HashMap<>();
                    api.aceptarRetoConBody(retoId, body).enqueue(new Callback<AceptarRetoResponse>() {
                        @Override public void onResponse(Call<AceptarRetoResponse> call2, Response<AceptarRetoResponse> resp2) {
                            if (!isAdded()) return;
                            mostrarCargando(false);
                            if (!resp2.isSuccessful() || resp2.body() == null) {
                                Toast.makeText(requireContext(), "No se pudo aceptar (" + resp2.code() + ")", Toast.LENGTH_LONG).show();
                                return;
                            }
                            abrirSalaDespuesDeAceptar(it);
                        }
                        @Override public void onFailure(Call<AceptarRetoResponse> call2, Throwable t2) {
                            if (!isAdded()) return;
                            mostrarCargando(false);
                            Toast.makeText(requireContext(), t2.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override public void onFailure(Call<AceptarRetoResponse> call, Throwable t) {
                if (!isAdded()) return;
                // 2) Fallback con body {} si la falla fue de red/protocolo
                Map<String, Object> body = new HashMap<>();
                api.aceptarRetoConBody(retoId, body).enqueue(new Callback<AceptarRetoResponse>() {
                    @Override public void onResponse(Call<AceptarRetoResponse> call2, Response<AceptarRetoResponse> resp2) {
                        if (!isAdded()) return;
                        mostrarCargando(false);
                        if (!resp2.isSuccessful() || resp2.body() == null) {
                            Toast.makeText(requireContext(), "No se pudo aceptar (" + resp2.code() + ")", Toast.LENGTH_LONG).show();
                            return;
                        }
                        abrirSalaDespuesDeAceptar(it);
                    }
                    @Override public void onFailure(Call<AceptarRetoResponse> call2, Throwable t2) {
                        if (!isAdded()) return;
                        mostrarCargando(false);
                        Toast.makeText(requireContext(), t2.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void abrirSalaDespuesDeAceptar(RetoListItem it) {
        Fragment f = FragmentLoadingSalaReto.newInstance(
                String.valueOf(it.getIdReto()),
                (it.getArea() != null ? it.getArea() : ""),
                (it.getCreador() != null && it.getCreador().getNombre() != null
                        ? it.getCreador().getNombre() : "Oponente"),
                false // esCreador = false (porque estamos en Recibidos)
        );

        FragmentManager fm = requireActivity().getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragmentContainer, f)
                .addToBackStack("salaRetado")
                .commit();
    }

    private void mostrarCargando(boolean s) {
        if (pb != null) pb.setVisibility(s ? View.VISIBLE : View.GONE);
    }
}
