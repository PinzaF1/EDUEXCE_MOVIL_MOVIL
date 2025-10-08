package com.example.zavira_movil.progreso;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.adapter.OpponentAdapter;
import com.example.zavira_movil.local.TokenManager;
import com.example.zavira_movil.model.*;
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

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        chipMat = v.findViewById(R.id.chipMat);
        chipLen = v.findViewById(R.id.chipLen);
        chipCie = v.findViewById(R.id.chipCie);
        chipSoc = v.findViewById(R.id.chipSoc);
        chipIng = v.findViewById(R.id.chipIng);
        btnEnviar = v.findViewById(R.id.btnEnviarReto);

        RecyclerView rvOpp = v.findViewById(R.id.rvOpponents);
        rvOpp.setLayoutManager(new LinearLayoutManager(getContext()));
        oppAdapter = new OpponentAdapter(op -> {
            selectedOpponentId   = op.getId();
            selectedOpponentName = op.getNombre();
            oppAdapter.setSelectedId(selectedOpponentId); // pinta seleccionado
            refreshSendState();
        });
        rvOpp.setAdapter(oppAdapter);

        // ======= CARGA REAL DESDE BACKEND =======
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

    private void clearChipSelection() {
        chipMat.setSelected(false); chipLen.setSelected(false);
        chipCie.setSelected(false); chipSoc.setSelected(false); chipIng.setSelected(false);
    }

    /** Habilita el botón SOLO si hay área y oponente seleccionados */
    private void refreshSendState() {
        boolean ready = !TextUtils.isEmpty(selectedArea) && !TextUtils.isEmpty(selectedOpponentId);
        btnEnviar.setEnabled(ready);
    }

    /** Llama /movil/oponentes y llena el adapter sin cambiar tu UI */
    private void cargarOponentes() {
        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        api.listarOponentes().enqueue(new Callback<List<OpponentRaw>>() {
            @Override public void onResponse(Call<List<OpponentRaw>> call, Response<List<OpponentRaw>> resp) {
                if (!isAdded()) return;

                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(requireContext(), "Error oponentes: " + resp.code(), Toast.LENGTH_SHORT).show();
                    oppAdapter.setData(Collections.emptyList());
                    return;
                }

                List<OpponentItem> lista = new ArrayList<>();
                for (OpponentRaw r : resp.body()) {
                    String id = String.valueOf(r.idUsuario);
                    String nivel = (r.grado != null || r.curso != null)
                            ? ((r.grado != null ? r.grado : "") + "° " + (r.curso != null ? r.curso : "")).trim()
                            : "—";
                    boolean online = r.estado != null && r.estado.equalsIgnoreCase("disponible");
                    // wins no viene; lo marcamos 0 (tu item lo muestra a la derecha)
                    lista.add(new OpponentItem(id,
                            r.nombre != null ? r.nombre : "Sin nombre",
                            nivel, 0, online));
                }
                oppAdapter.setData(lista);
            }

            @Override public void onFailure(Call<List<OpponentRaw>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Fallo oponentes: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                oppAdapter.setData(Collections.emptyList());
            }
        });
    }

    /** 1) Crear reto -> 2) Ir a “Sala de Reto” (Comenzar) */
    private void crearRetoIrALobby() {
        if (!btnEnviar.isEnabled()) return;

        String tok = TokenManager.getToken(requireContext());
        if (TextUtils.isEmpty(tok)) {
            Toast.makeText(requireContext(),"No hay token (login requerido)",Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(selectedArea) || TextUtils.isEmpty(selectedOpponentId)) {
            Toast.makeText(requireContext(),"Elige área y oponente",Toast.LENGTH_SHORT).show();
            return;
        }

        String area = quitarTildes(selectedArea);

        int oppId;
        try {
            oppId = Integer.parseInt(selectedOpponentId);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Id de oponente inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        btnEnviar.setEnabled(false);

        // Enviamos (cantidad, area, oponente_id) — backend real
        api.crearReto(new RetoCreateRequest(25, area, oppId)).enqueue(new Callback<RetoCreadoResponse>() {
            @Override public void onResponse(Call<RetoCreadoResponse> call, Response<RetoCreadoResponse> resp) {
                btnEnviar.setEnabled(true);

                if (!isAdded() || getActivity()==null) return;

                if (!resp.isSuccessful() || resp.body()==null) {
                    Toast.makeText(requireContext(),"Error crear: "+resp.code(),Toast.LENGTH_SHORT).show();
                    return;
                }
                RetoCreadoResponse creado = resp.body();

                View overlay = getActivity().findViewById(R.id.container);
                if (overlay == null) {
                    Toast.makeText(requireContext(),
                            "No se encontró el contenedor 'container' en la Activity",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                overlay.setVisibility(View.VISIBLE);

                try {
                    String opName = (selectedOpponentName != null && !selectedOpponentName.isEmpty())
                            ? selectedOpponentName : "Oponente";

                    FragmentLoadingSalaReto f = FragmentLoadingSalaReto
                            .newInstance(creado.getId_reto(), selectedArea, opName);

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, f)
                            .addToBackStack(null)
                            .commit();
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Error al abrir lobby: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
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
        n = n.replaceAll("\\p{InCombiningDiacriticalMarks}+","");
        return n.replace("ñ","n").replace("Ñ","N");
    }
}
