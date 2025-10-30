// app/src/main/java/com/example/zavira_movil/retos1vs1/FragmentQuiz.java
package com.example.zavira_movil.retos1vs1;

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
    private final Map<Integer, Long> tiemposPorPregunta = new HashMap<>(); // tiempo en ms por pregunta
    private long tiempoInicioPreguntaActual;

    private TextView tvLeft, tvIndex, tvRight, tvPregunta;
    private Button btnNext;
    private OptionAdapter optionsAdapter;

    private long startMillis;
    private int tiempoTotalSeg;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // IDs EXACTOS de tu XML
        tvLeft     = v.findViewById(R.id.txtLeft);
        tvIndex    = v.findViewById(R.id.txtIndex);
        tvRight    = v.findViewById(R.id.txtRight);
        tvPregunta = v.findViewById(R.id.txtPregunta);
        btnNext    = v.findViewById(R.id.btnNext);

        if (tvLeft != null)  tvLeft.setText("Tú");
        if (tvRight != null) tvRight.setText("Oponente");

        RecyclerView rv = v.findViewById(R.id.optionsList);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setHasFixedSize(true);

        optionsAdapter = new OptionAdapter(key -> {
            marcadas.put(index, key);          // key = "A","B","C","D" según posición
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

        startMillis = System.currentTimeMillis();
        tiempoInicioPreguntaActual = System.currentTimeMillis(); // iniciar timer de primera pregunta

        if (btnNext != null) {
            btnNext.setOnClickListener(v12 -> {
                if (!marcadas.containsKey(index)) {
                    Toast.makeText(getContext(), "Selecciona una opción", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Guardar tiempo de la pregunta actual
                long tiempoEmpleado = System.currentTimeMillis() - tiempoInicioPreguntaActual;
                tiemposPorPregunta.put(index, tiempoEmpleado);
                
                if (index < data.preguntas.size() - 1) {
                    index++;
                    tiempoInicioPreguntaActual = System.currentTimeMillis(); // reiniciar timer
                    render();
                } else {
                    enviarRonda();
                }
            });
        }
    }

    private void render() {
        if (data == null || data.preguntas == null || data.preguntas.isEmpty()) return;

        AceptarRetoResponse.Pregunta p = data.preguntas.get(index);

        if (tvIndex != null) {
            tvIndex.setText("Pregunta " + (index + 1) + "/" + data.preguntas.size());
        }
        if (tvPregunta != null) {
            tvPregunta.setText(p.enunciado != null ? p.enunciado : "Pregunta sin texto");
        }

        // --- Construir SIEMPRE List<String> para tu OptionAdapter ---
        List<String> opTexts = buildOptionTexts(p);

        String sel = marcadas.get(index);            // "A", "B", ...
        optionsAdapter.submit(opTexts, sel);         // <-- ahora coincide con tu adapter

        if (btnNext != null) {
            btnNext.setText(index == data.preguntas.size() - 1 ? "Finalizar" : "Siguiente");
            btnNext.setEnabled(sel != null);
        }
    }

    /** Convierte p.opciones (strings u objetos) en List<String> de textos visibles */
    private List<String> buildOptionTexts(AceptarRetoResponse.Pregunta p) {
        List<String> list = new ArrayList<>();
        if (p == null || p.opciones == null) return list;

        // p.opciones puede ser List<AceptarRetoResponse.Opcion> (con .text/.key) o ya ser strings si usaste el TypeAdapter
        List<?> raw = (List<?>) (Object) p.opciones; // cast ancho por si la deserialización varía
        for (Object any : raw) {
            if (any instanceof AceptarRetoResponse.Opcion) {
                AceptarRetoResponse.Opcion o = (AceptarRetoResponse.Opcion) any;
                String text = (o.text != null) ? o.text : (o.key != null ? o.key : "");
                list.add(text);
            } else if (any instanceof String) {
                list.add((String) any);
            } else {
                list.add(String.valueOf(any));
            }
        }
        return list;
    }

    private String flagKey() {
        Integer my = null; try { my = TokenManager.getUserId(requireContext()); } catch (Exception ignored) {}
        return "ronda_" + idReto + "_" + (my==null?"0":String.valueOf(my));
    }

    private void marcarEntregada() {
        if (!isAdded() || TextUtils.isEmpty(idReto)) return;
        requireContext().getSharedPreferences("retos1v1", android.content.Context.MODE_PRIVATE)
                .edit().putBoolean(flagKey(), true).apply();
    }

    private void enviarRonda() {
        tiempoTotalSeg = (int) ((System.currentTimeMillis() - startMillis) / 1000L);

        // El backend espera la opción marcada por clave (A/B/C/D...), que guarda tu OptionAdapter
        List<RondaRequest.Item> items = new ArrayList<>();
        for (int i = 0; i < data.preguntas.size(); i++) {
            String key = marcadas.get(i);
            if (key != null) {
                // Obtener tiempo en segundos (con decimales)
                Long tiempoMs = tiemposPorPregunta.get(i);
                Double tiempoSeg = (tiempoMs != null) ? (tiempoMs / 1000.0) : 0.0;
                
                items.add(new RondaRequest.Item(i + 1, key, tiempoSeg));
            }
        }

        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        RondaRequest payload = new RondaRequest(idSesion, items);
        payload.tiempoTotalSeg = tiempoTotalSeg;

        api.responderRonda(payload).enqueue(new Callback<RondaResponse>() {
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
                    b.putInt("totalPreguntas",
                            (data != null && data.preguntas != null) ? data.preguntas.size() : 25);
                    b.putInt("idSesion", idSesion);
                    b.putInt("tiempoTotalSeg", tiempoTotalSeg);

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
