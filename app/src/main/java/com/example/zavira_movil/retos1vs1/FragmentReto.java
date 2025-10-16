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
    private Button btnEnviar;
    private OpponentAdapter oppAdapter;

    private String selectedArea = null;
    private String selectedOpponentId = null;
    private String selectedOpponentName = null;

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
    }

    private void cargarOponentes() {
        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        api.listarOponentes().enqueue(new Callback<List<OpponentBackend>>() {
            @Override public void onResponse(Call<List<OpponentBackend>> call, Response<List<OpponentBackend>> resp) {
                if (!isAdded()) return;
                List<OpponentItem> items = new ArrayList<>();
                if (resp.isSuccessful() && resp.body() != null) {
                    for (OpponentBackend b : resp.body()) {
                        String id = String.valueOf(b.getIdUsuario() != null ? b.getIdUsuario() : 0);
                        String nom = b.getNombre() != null ? b.getNombre() : ("Usuario " + id);
                        String niv = b.getGrado() != null ? b.getGrado() : "";
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
                        true // esCreador
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
}
