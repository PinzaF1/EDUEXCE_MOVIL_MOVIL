package com.example.zavira_movil.retos1vs1;

import android.content.Context;
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
import com.example.zavira_movil.local.TokenManager;
import com.example.zavira_movil.model.RondaRequest;
import com.example.zavira_movil.model.RondaResponse;
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
    private final Map<Integer, String> marcadas = new HashMap<>();

    private TextView tvIndex, tvPregunta;
    private Button btnPrev, btnNext;
    private OptionAdapter optionsAdapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        tvIndex    = v.findViewById(R.id.txtIndex);
        tvPregunta = v.findViewById(R.id.txtPregunta);
        btnNext    = v.findViewById(R.id.btnNext);

        RecyclerView rv = v.findViewById(R.id.optionsList);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        optionsAdapter = new OptionAdapter(key -> {
            marcadas.put(index, key);
            if (btnNext != null) btnNext.setEnabled(true);
        });
        rv.setAdapter(optionsAdapter);

        Bundle args = getArguments();
        if (args == null) { requireActivity().onBackPressed(); return; }

        String aceptarJson = args.getString("aceptarJson");
        idSesion = args.getInt("idSesion", -1);
        idReto   = args.getString("idReto", null);

        if (TextUtils.isEmpty(aceptarJson) || idSesion <= 0 || TextUtils.isEmpty(idReto)) {
            requireActivity().onBackPressed();
            return;
        }

        data = new Gson().fromJson(aceptarJson, AceptarRetoResponse.class);
        render();

        if (btnPrev != null) btnPrev.setOnClickListener(v1 -> { if (index > 0) { index--; render(); } });
        if (btnNext != null) btnNext.setOnClickListener(v12 -> {
            if (!marcadas.containsKey(index)) {
                Toast.makeText(getContext(), "Selecciona una opción", Toast.LENGTH_SHORT).show();
                return;
            }
            if (index < data.preguntas.size() - 1) { index++; render(); }
            else { enviarRonda(); }
        });
    }

    private void render() {
        if (data == null || data.preguntas == null || data.preguntas.isEmpty()) return;

        AceptarRetoResponse.Pregunta p = data.preguntas.get(index);
        tvIndex.setText("Pregunta " + (index + 1) + "/" + data.preguntas.size());
        tvPregunta.setText(p.enunciado != null ? p.enunciado : "Pregunta sin texto");

        // ⬇️ ahora p.opciones es List<String>
        List<String> opciones = new ArrayList<>();
        if (p.opciones != null) opciones.addAll(p.opciones);

        String sel = marcadas.get(index);
        optionsAdapter.submit(opciones, sel);

        if (btnPrev != null) btnPrev.setEnabled(index > 0);
        if (btnNext != null) {
            btnNext.setText(index == data.preguntas.size() - 1 ? "Finalizar" : "Siguiente");
            btnNext.setEnabled(sel != null);
        }
    }

    private String flagKey() {
        Integer my = null; try { my = TokenManager.getUserId(requireContext()); } catch (Exception ignored) {}
        return "ronda_" + idReto + "_" + (my==null?"0":String.valueOf(my));
    }

    private void marcarEntregada() {
        if (!isAdded() || TextUtils.isEmpty(idReto)) return;
        requireContext().getSharedPreferences("retos1v1", Context.MODE_PRIVATE)
                .edit().putBoolean(flagKey(), true).apply();
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
                        marcarEntregada();
                        consultarEstado();
                    }
                    @Override public void onFailure(Call<RondaResponse> call, Throwable t) {
                        marcarEntregada();
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

                    FragmentManager fm = requireActivity().getSupportFragmentManager();
                    int containerId = R.id.fragmentContainer;
                    View root = requireActivity().findViewById(containerId);
                    if (root == null) containerId = android.R.id.content;

                    fm.beginTransaction()
                            .replace(containerId, f)
                            .addToBackStack("resultadoReto")
                            .commit();
                } else {
                    requireActivity().onBackPressed();
                }
            }
            @Override public void onFailure(Call<EstadoRetoResponse> call, Throwable t) {
                if (!isAdded()) return;
                requireActivity().onBackPressed();
            }
        });
    }
}
