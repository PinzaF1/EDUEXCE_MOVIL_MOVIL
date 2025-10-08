package com.example.zavira_movil.progreso;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.adapter.OptionAdapter;
import com.example.zavira_movil.model.AceptarRetoResponse;
import com.example.zavira_movil.model.PreguntaReto;
import com.example.zavira_movil.model.RondaRequest;
import com.example.zavira_movil.model.RondaResponse;
import com.example.zavira_movil.model.EstadoRetoResponse;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentQuiz extends Fragment {

    private AceptarRetoResponse data;
    private String idReto;
    private int idSesion;

    private int index = 0;
    private final Map<Integer,String> marcadas = new HashMap<>();

    private TextView tvIndex, tvPregunta;
    private Button btnPrev, btnNext;
    private OptionAdapter optionsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        tvIndex     = v.findViewById(R.id.txtIndex);
        tvPregunta  = v.findViewById(R.id.txtPregunta);
        btnNext     = v.findViewById(R.id.btnNext);

        RecyclerView rv = v.findViewById(R.id.optionsList);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        optionsAdapter = new OptionAdapter(key -> {
            marcadas.put(index, key);
            btnNext.setEnabled(true);
        });
        rv.setAdapter(optionsAdapter);

        Bundle args = getArguments();
        if (args == null) { requireActivity().onBackPressed(); return; }
        String aceptarJson = args.getString("aceptarJson");
        idSesion = args.getInt("idSesion", -1);
        idReto   = args.getString("idReto", null);

        if (TextUtils.isEmpty(aceptarJson) || idSesion <= 0 || TextUtils.isEmpty(idReto)) {
            requireActivity().onBackPressed(); return;
        }

        data = new Gson().fromJson(aceptarJson, AceptarRetoResponse.class);

        render();

        btnPrev.setOnClickListener(v1 -> {
            if (index > 0) { index--; render(); }
        });

        btnNext.setOnClickListener(v12 -> {
            if (!marcadas.containsKey(index)) {
                Toast.makeText(getContext(), "Selecciona una opción", Toast.LENGTH_SHORT).show();
                return;
            }
            if (index < data.preguntas.size() - 1) {
                index++; render();
            } else {
                enviarRonda();
            }
        });
    }

    private void render() {
        if (data == null || data.preguntas == null || data.preguntas.isEmpty()) return;
        PreguntaReto p = data.preguntas.get(index);

        tvIndex.setText("Pregunta " + (index + 1) + "/" + data.preguntas.size());
        tvPregunta.setText(p.getPregunta());

        String sel = marcadas.get(index);
        optionsAdapter.submit(p.getOpciones(), sel);

        btnPrev.setEnabled(index > 0);
        btnNext.setText(index == data.preguntas.size() - 1 ? "Finalizar" : "Siguiente");
        btnNext.setEnabled(sel != null);
    }

    private void enviarRonda() {
        List<RondaRequest.Item> items = new ArrayList<>();
        for (int i = 0; i < data.preguntas.size(); i++) {
            String key = marcadas.get(i);
            if (key != null) items.add(new RondaRequest.Item(i + 1, key));
        }

        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        api.responderRonda(new RondaRequest(idSesion, items))
                .enqueue(new Callback<RondaResponse>() {
                    @Override public void onResponse(Call<RondaResponse> call, Response<RondaResponse> resp) {
                        consultarEstado();
                    }
                    @Override public void onFailure(Call<RondaResponse> call, Throwable t) {
                        consultarEstado();
                    }
                });
    }

    private void consultarEstado() {
        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        api.estadoReto(idReto).enqueue(new Callback<EstadoRetoResponse>() {
            @Override public void onResponse(Call<EstadoRetoResponse> call, Response<EstadoRetoResponse> resp) {
                if (!isAdded()) return;

                if (resp.isSuccessful() && resp.body() != null) {
                    EstadoRetoResponse e = resp.body();

                    Bundle b = new Bundle();
                    b.putString("estadoJson", new Gson().toJson(e));
                    b.putInt("totalPreguntas", (data != null && data.preguntas != null) ? data.preguntas.size() : 25);

                    FragmentResultadoReto f = new FragmentResultadoReto();
                    f.setArguments(b);

                    // ===== Navegación segura al mismo contenedor que usa el overlay =====
                    FragmentManager fm;
                    int containerId;

                    Fragment parent = getParentFragment();
                    View parentView = parent != null ? parent.getView() : null;
                    View containerInParent = (parentView != null) ? parentView.findViewById(R.id.container) : null;

                    if (containerInParent != null) {
                        // Estamos dentro de RetosFragment (usa su child FM)
                        fm = parent.getChildFragmentManager();
                        containerId = R.id.container;
                    } else {
                        // Fallback: contenedor principal de la Activity
                        fm = requireActivity().getSupportFragmentManager();
                        containerId = R.id.fragmentContainer;
                    }

                    fm.beginTransaction()
                            .replace(containerId, f)
                            .addToBackStack("resultadoReto")
                            .commit();
                } else {
                    // Si falla, solo vuelve al anterior en el mismo stack
                    if (getParentFragmentManager() != null) {
                        getParentFragmentManager().popBackStack();
                    }
                }
            }

            @Override public void onFailure(Call<EstadoRetoResponse> call, Throwable t) {
                if (!isAdded()) return;
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            }
        });
    }
}
