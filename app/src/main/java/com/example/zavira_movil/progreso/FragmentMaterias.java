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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.zavira_movil.R;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentMaterias extends Fragment {

    private RecyclerView recyclerMaterias;
    private DiagnosticoAdapter contentAdapter;
    private DiagnosticoHeaderAdapter headerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_materias, container, false);

        recyclerMaterias = view.findViewById(R.id.recyclerMaterias);
        GridLayoutManager glm = new GridLayoutManager(requireContext(), 2);
        // que el header ocupe las 2 columnas
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override public int getSpanSize(int position) {
                // header es el primer adapter (1 item) => span 2
                return position == 0 ? 2 : 1;
            }
        });
        recyclerMaterias.setLayoutManager(glm);
        recyclerMaterias.setHasFixedSize(false);
        recyclerMaterias.setItemAnimator(null);

        headerAdapter  = new DiagnosticoHeaderAdapter();
        contentAdapter = new DiagnosticoAdapter();

        ConcatAdapter concat = new ConcatAdapter(headerAdapter, contentAdapter);
        recyclerMaterias.setAdapter(concat);

        cargarDiagnosticoInicial();
        return view;
    }

    private void cargarDiagnosticoInicial() {
        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        api.diagnosticoProgreso().enqueue(new Callback<DiagnosticoInicial>() {
            @Override
            public void onResponse(Call<DiagnosticoInicial> call, Response<DiagnosticoInicial> r) {
                if (!isAdded()) return;

                if (!r.isSuccessful() || r.body() == null) {
                    Log.e("DiagInicial", "HTTP " + r.code());
                    headerAdapter.setData(false, 0, null);
                    contentAdapter.setData(new ArrayList<>());
                    return;
                }

                DiagnosticoInicial d = r.body();

                // Header: porcentaje general → d.progreso_general.actual
                Integer generalActual = (d.progresoGeneral != null) ? d.progresoGeneral.actual : 0;
                headerAdapter.setData(d.tieneDiagnostico, generalActual, /*fechaOpcional*/ null);

                // Orden amigable (como el diseño)
                Map<String, String> orden = new LinkedHashMap<>();
                orden.put("Matemáticas", "Matemáticas");
                orden.put("Lenguaje", "Lenguaje");
                orden.put("Ciencias Naturales", "Ciencias");
                orden.put("sociales", "Sociales");
                orden.put("Inglés", "Inglés");

                List<DiagnosticoAdapter.AreaItem> items = new ArrayList<>();
                if (d.progresoPorArea != null) {
                    for (Map.Entry<String,String> e : orden.entrySet()) {
                        DiagnosticoInicial.Area a = d.progresoPorArea.get(e.getKey());
                        if (a == null) continue;
                        items.add(new DiagnosticoAdapter.AreaItem(
                                e.getValue(),
                                clamp(a.inicial),
                                clamp(a.actual),
                                a.delta));
                    }
                }
                contentAdapter.setData(items);
            }

            @Override
            public void onFailure(Call<DiagnosticoInicial> call, Throwable t) {
                if (!isAdded()) return;
                Log.e("DiagInicial", "Fallo: " + (t.getMessage()!=null?t.getMessage():""));
                headerAdapter.setData(false, 0, null);
                contentAdapter.setData(new ArrayList<>());
            }
        });
    }

    private int clamp(int v){ return Math.max(0, Math.min(100, v)); }
}
