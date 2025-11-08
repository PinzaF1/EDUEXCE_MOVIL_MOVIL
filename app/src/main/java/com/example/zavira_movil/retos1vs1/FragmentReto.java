package com.example.zavira_movil.retos1vs1;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.local.TokenManager;
import com.example.zavira_movil.oponente.OpponentAdapter;
import com.example.zavira_movil.oponente.OpponentBackend;
import com.example.zavira_movil.oponente.OpponentItem;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.text.Normalizer;
import java.util.*;

import retrofit2.*;

public class FragmentReto extends Fragment {

    private TextView chipMat, chipLen, chipCie, chipSoc, chipIng;
    private View btnEnviar;
    private OpponentAdapter oppAdapter;

    private String selectedArea = null;
    private String selectedOpponentId = null;
    private String selectedOpponentName = null;

    // [MARCADOR]
    private TextView tvVictorias;
    private TextView tvDerrotas;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_retos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        chipMat = v.findViewById(R.id.chipMat);
        chipLen = v.findViewById(R.id.chipLen);
        chipCie = v.findViewById(R.id.chipCie);
        chipSoc = v.findViewById(R.id.chipSoc);
        chipIng = v.findViewById(R.id.chipIng);
        btnEnviar = v.findViewById(R.id.btnEnviarReto);

        // [MARCADOR] localizar TextViews si existen
        tvVictorias = findMetricViewSafely(v, R.id.tvVictorias);
        tvDerrotas  = findMetricViewSafely(v, R.id.tvDerrotas);

        RecyclerView rvOpp = v.findViewById(R.id.rvOpponents);
        rvOpp.setLayoutManager(new LinearLayoutManager(getContext()));
        oppAdapter = new OpponentAdapter(op -> {
            selectedOpponentId = op.getId();
            selectedOpponentName = op.getNombre();
            oppAdapter.setSelectedId(selectedOpponentId);
            refreshSendState();
        });
        rvOpp.setAdapter(oppAdapter);

        cargarOponentes();

        View.OnClickListener chips = vv -> {
            clearChipSelection();
            vv.setSelected(true);
            selectedArea = ((TextView) vv).getText().toString();
            refreshSendState();
        };
        chipMat.setOnClickListener(chips);
        chipLen.setOnClickListener(chips);
        chipCie.setOnClickListener(chips);
        chipSoc.setOnClickListener(chips);
        chipIng.setOnClickListener(chips);

        btnEnviar.setOnClickListener(x -> crearRetoIrALobby());
        refreshSendState();

        // [MARCADOR] primer fetch
        cargarMarcador(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        // [MARCADOR] refresco al volver
        cargarMarcador(null);
        // Refrescar oponentes cuando se vuelve (para actualizar disponibilidad)
        cargarOponentes();
    }

    // [MARCADOR] público por si quieres llamarlo desde Resultados
    public void refreshMarcador() { cargarMarcador(null); }

    // Público para poder refrescar desde FragmentLoadingSalaReto cuando se abandona
    public void cargarOponentes() {
        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        api.listarOponentes().enqueue(new Callback<List<OpponentBackend>>() {
            @Override public void onResponse(Call<List<OpponentBackend>> call, Response<List<OpponentBackend>> resp) {
                if (!isAdded()) return;
                List<OpponentItem> items = new ArrayList<>();
                if (resp.isSuccessful() && resp.body() != null) {
                    for (OpponentBackend b : resp.body()) {
                        String id = String.valueOf(b.getIdUsuario() != null ? b.getIdUsuario() : 0);
                        String nom = b.getNombre() != null ? b.getNombre() : ("Usuario " + id);
                        // Formato: "10-A" (grado + curso)
                        String grado = b.getGrado() != null ? b.getGrado() : "";
                        String curso = b.getCurso() != null ? b.getCurso() : "";
                        String niv = (grado + (curso.isEmpty() ? "" : "-" + curso)).trim();
                        boolean on = "disponible".equalsIgnoreCase(b.getEstado());
                        items.add(new OpponentItem(id, nom, niv, 0, on));
                    }
                }
                oppAdapter.setData(items);
                selectedOpponentId = null; selectedOpponentName = null;
                refreshSendState();
            }

            @Override public void onFailure(Call<List<OpponentBackend>> call, Throwable t) {
                if (!isAdded()) return;
                oppAdapter.setData(Collections.emptyList());
                Toast.makeText(requireContext(), "Fallo oponentes: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearChipSelection() {
        chipMat.setSelected(false); chipLen.setSelected(false);
        chipCie.setSelected(false); chipSoc.setSelected(false);
        chipIng.setSelected(false);
    }

    private void refreshSendState() {
        boolean ready = !TextUtils.isEmpty(selectedArea) && !TextUtils.isEmpty(selectedOpponentId);
        btnEnviar.setEnabled(ready);
        btnEnviar.setAlpha(ready ? 1.0f : 0.5f); // Visual feedback cuando está deshabilitado
    }

    private void crearRetoIrALobby() {
        if (!btnEnviar.isEnabled()) return;

        String tok = TokenManager.getToken(requireContext());
        if (TextUtils.isEmpty(tok)) {
            Toast.makeText(requireContext(), "No hay token (login requerido)", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(selectedArea) || TextUtils.isEmpty(selectedOpponentId)) {
            Toast.makeText(requireContext(), "Elige área y oponente", Toast.LENGTH_SHORT).show();
            return;
        }

        String area = quitarTildes(selectedArea);
        int oppId;
        try { oppId = Integer.parseInt(selectedOpponentId); }
        catch (NumberFormatException e) { Toast.makeText(requireContext(), "Id de oponente inválido", Toast.LENGTH_SHORT).show(); return; }

        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        btnEnviar.setEnabled(false);

        api.crearReto(new RetoCreateRequest(25, area, oppId)).enqueue(new Callback<RetoCreadoResponse>() {
            @Override public void onResponse(Call<RetoCreadoResponse> call, Response<RetoCreadoResponse> resp) {
                btnEnviar.setEnabled(true);
                if (!isAdded() || getActivity() == null) return;

                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(requireContext(), "Error crear: " + resp.code(), Toast.LENGTH_SHORT).show();
                    return;
                }
                RetoCreadoResponse creado = resp.body();

                View overlay = getActivity().findViewById(R.id.container);
                if (overlay != null) overlay.setVisibility(View.VISIBLE);

                FragmentLoadingSalaReto f = FragmentLoadingSalaReto.newInstance(
                        String.valueOf(creado.getId_reto()),
                        selectedArea,
                        (selectedOpponentName != null ? selectedOpponentName : "Oponente"),
                        true
                );

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(overlay != null ? R.id.container : R.id.fragmentContainer, f)
                        .addToBackStack("salaCreador")
                        .commit();
            }

            @Override public void onFailure(Call<RetoCreadoResponse> call, Throwable t) {
                if (!isAdded()) return;
                btnEnviar.setEnabled(true);
                Toast.makeText(requireContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String quitarTildes(String s) {
        if (s == null) return null;
        String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        n = n.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return n.replace("ñ", "n").replace("Ñ", "N");
    }

    // ===== [MARCADOR] =====

    @Nullable
    private TextView findMetricViewSafely(@NonNull View thisRoot, int id) {
        View v = thisRoot.findViewById(id);
        if (v instanceof TextView) return (TextView) v;

        Fragment parent = getParentFragment();
        if (parent != null && parent.getView() != null) {
            v = parent.getView().findViewById(id);
            if (v instanceof TextView) return (TextView) v;
        }

        if (getActivity() != null) {
            v = getActivity().findViewById(id);
            if (v instanceof TextView) return (TextView) v;
        }
        return null;
    }

    private void cargarMarcador(@Nullable Integer idSesion) {
        if (!isAdded()) return;

        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        Call<MarcadorResponse> call = (idSesion != null)
                ? api.marcadorPorSesion(idSesion)
                : api.marcador();

        call.enqueue(new Callback<MarcadorResponse>() {
            @Override public void onResponse(Call<MarcadorResponse> c, Response<MarcadorResponse> r) {
                if (!isAdded() || r.body() == null || !r.isSuccessful()) return;
                pintarMarcador(r.body().victorias, r.body().derrotas);
            }
            @Override public void onFailure(Call<MarcadorResponse> c, Throwable t) { }
        });
    }

    private void pintarMarcador(int victorias, int derrotas) {
        if (tvVictorias != null) tvVictorias.setText(String.valueOf(victorias));
        if (tvDerrotas  != null) tvDerrotas.setText(String.valueOf(derrotas));
    }
}
