package com.example.zavira_movil.progreso;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentDiagnosticoInicial extends Fragment {

    private RecyclerView rv;
    private DiagnosticoHeaderAdapter headerAdapter;
    private DiagnosticoAdapter areasAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_diagnostico_inicial, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        rv = v.findViewById(R.id.rvDiagnostico);

        // 🔒 Siempre 1 por fila (LinearLayoutManager vertical)
        LinearLayoutManager lm = new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false);
        rv.setLayoutManager(lm);

        rv.setHasFixedSize(false);
        rv.setItemAnimator(null);

        headerAdapter = new DiagnosticoHeaderAdapter();
        areasAdapter  = new DiagnosticoAdapter();
        rv.setAdapter(new ConcatAdapter(headerAdapter, areasAdapter));

        cargarDiagnostico();
    }

    private void cargarDiagnostico() {
        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        api.diagnosticoProgreso().enqueue(new Callback<DiagnosticoInicial>() {
            @Override public void onResponse(Call<DiagnosticoInicial> call, Response<DiagnosticoInicial> res) {
                if (!isAdded()) return;

                if (!res.isSuccessful() || res.body() == null) {
                    headerAdapter.setData(false, 0, null);
                    areasAdapter.setData(new ArrayList<>());
                    return;
                }

                DiagnosticoInicial dto = res.body();
                int generalActual = (dto.progresoGeneral != null) ? dto.progresoGeneral.actual : 0;
                headerAdapter.setData(dto.tieneDiagnostico, generalActual, null);

                List<DiagnosticoAdapter.AreaItem> items = new ArrayList<>();
                if (dto.progresoPorArea != null) {
                    for (Map.Entry<String, DiagnosticoInicial.Area> e : dto.progresoPorArea.entrySet()) {
                        DiagnosticoInicial.Area a = e.getValue();
                        items.add(new DiagnosticoAdapter.AreaItem(
                                e.getKey(),
                                a != null ? a.inicial : 0,
                                a != null ? a.actual  : 0,
                                a != null ? a.delta   : 0
                        ));
                    }
                }
                areasAdapter.setData(items);
            }

            @Override public void onFailure(Call<DiagnosticoInicial> call, Throwable t) {
                if (!isAdded()) return;
                Log.e("Diag", "Fallo: " + t.getMessage());
                headerAdapter.setData(false, 0, null);
                areasAdapter.setData(new ArrayList<>());
            }
        });
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        rv = null;
    }
}
