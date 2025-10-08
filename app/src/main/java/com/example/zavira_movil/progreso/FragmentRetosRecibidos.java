// app/src/main/java/com/example/zavira_movil/progreso/FragmentRetosRecibidos.java
package com.example.zavira_movil.progreso;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.adapter.RecibidosAdapter;
import com.example.zavira_movil.model.AceptarRetoResponse;
import com.example.zavira_movil.model.RetoListItem;
import com.example.zavira_movil.model.RetoListItem;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.*;

public class FragmentRetosRecibidos extends Fragment {

    private ProgressBar pb;
    private TextView tvEmpty;
    private RecyclerView rv;
    private RecibidosAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inf.inflate(R.layout.fragment_retos_recibidos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        pb     = v.findViewById(R.id.progressRecibidos);
        tvEmpty= v.findViewById(R.id.tvEmpty);
        rv     = v.findViewById(R.id.rvRecibidos);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecibidosAdapter(this::aceptar);
        rv.setAdapter(adapter);

        cargar();
    }

    private void cargar() {
        mostrarCargando(true);
        tvEmpty.setVisibility(View.GONE);

        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        api.listarRetos().enqueue(new Callback<List<RetoListItem>>() {
            @Override public void onResponse(Call<List<RetoListItem>> call, Response<List<RetoListItem>> resp) {
                if (!isAdded()) return;
                mostrarCargando(false);

                if (!resp.isSuccessful() || resp.body() == null) {
                    tvEmpty.setText("Error HTTP " + resp.code());
                    tvEmpty.setVisibility(View.VISIBLE);
                    adapter.setData(new ArrayList<>());
                    return;
                }

                // Si quieres mostrar SOLO pendientes:
                List<RetoListItem> todos = resp.body();
                List<RetoListItem> pendientes = new ArrayList<>();
                for (RetoListItem r : todos) {
                    if ("pendiente".equalsIgnoreCase(r.getEstado())) pendientes.add(r);
                }

                if (pendientes.isEmpty()) {
                    tvEmpty.setText("No tienes retos recibidos.");
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }
                adapter.setData(pendientes);
            }
            @Override public void onFailure(Call<List<RetoListItem>> call, Throwable t) {
                if (!isAdded()) return;
                mostrarCargando(false);
                tvEmpty.setText("Fallo de red: " + (t.getMessage()!=null ? t.getMessage() : ""));
                tvEmpty.setVisibility(View.VISIBLE);
                adapter.setData(new ArrayList<>());
            }
        });
    }

    private void aceptar(RetoListItem it) {
        if (it == null || it.getIdReto() == null) {
            Toast.makeText(requireContext(),"Reto inválido",Toast.LENGTH_SHORT).show();
            return;
        }
        mostrarCargando(true);

        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        api.aceptarReto(String.valueOf(it.getIdReto())).enqueue(new Callback<AceptarRetoResponse>() {
            @Override public void onResponse(Call<AceptarRetoResponse> call, Response<AceptarRetoResponse> resp) {
                if (!isAdded()) return;
                mostrarCargando(false);

                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(requireContext(),"No se pudo aceptar ("+resp.code()+")", Toast.LENGTH_LONG).show();
                    return;
                }
                AceptarRetoResponse aceptar = resp.body();

                // Buscar mi sesión, si viene id_usuario en la sesión
                int idSesion = -1;
                if (aceptar.sesiones != null && !aceptar.sesiones.isEmpty()) {
                    // Si tu Sesion tiene id_usuario, puedes filtrar por él.
                    // Si no, usa el primero (como ya haces en el lobby).
                    idSesion = aceptar.sesiones.get(0).id_sesion;
                }
                if (idSesion <= 0) {
                    Toast.makeText(requireContext(),"Sin id_sesion",Toast.LENGTH_SHORT).show();
                    return;
                }

                // Ir DIRECTO al quiz con tu misma estructura
                Bundle args = new Bundle();
                args.putString("aceptarJson", new Gson().toJson(aceptar));
                args.putInt("idSesion", idSesion);
                args.putString(
                        "idReto",
                        (aceptar.reto != null && aceptar.reto.id_reto > 0)
                                ? String.valueOf(aceptar.reto.id_reto)
                                : String.valueOf(it.getIdReto())
                );


                FragmentQuiz f = new FragmentQuiz();
                f.setArguments(args);

                // Usar el mismo contenedor overlay si existe
                FragmentManager fm;
                int containerId;

                View parentView = (getParentFragment()!=null) ? getParentFragment().getView() : null;
                View overlay = (parentView!=null) ? parentView.findViewById(R.id.container) : null;

                if (overlay != null) {
                    overlay.setVisibility(View.VISIBLE);
                    fm = getParentFragment().getChildFragmentManager();
                    containerId = R.id.container;
                } else {
                    fm = requireActivity().getSupportFragmentManager();
                    containerId = R.id.fragmentContainer; // ajusta si tu activity usa otro id
                }

                fm.beginTransaction()
                        .replace(containerId, f)
                        .addToBackStack("quizDesdeRecibidos")
                        .commit();
            }

            @Override public void onFailure(Call<AceptarRetoResponse> call, Throwable t) {
                if (!isAdded()) return;
                mostrarCargando(false);
                Toast.makeText(requireContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarCargando(boolean s) {
        if (pb != null) pb.setVisibility(s ? View.VISIBLE : View.GONE);
    }
}
