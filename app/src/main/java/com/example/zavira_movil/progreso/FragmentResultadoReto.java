package com.example.zavira_movil.progreso;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.EstadoRetoResponse;
import com.example.zavira_movil.ui.ranking.progreso.RetosFragment;
import com.google.gson.Gson;

public class FragmentResultadoReto extends Fragment {

    public FragmentResultadoReto() { super(R.layout.fragment_result_reto); }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        TextView tvEmoji = v.findViewById(R.id.tvEmoji);
        TextView tvTitle = v.findViewById(R.id.tvTitle);
        TextView tvSubtitle = v.findViewById(R.id.tvSubtitle);
        TextView tvYouName = v.findViewById(R.id.tvYouName);
        TextView tvYouDetail = v.findViewById(R.id.tvYouDetail);
        TextView tvYouPoints = v.findViewById(R.id.tvYouPoints);
        TextView tvOppName = v.findViewById(R.id.tvOppName);
        TextView tvOppDetail = v.findViewById(R.id.tvOppDetail);
        TextView tvOppPoints = v.findViewById(R.id.tvOppPoints);
        Button btnVolver   = v.findViewById(R.id.btnVolver);
        btnVolver.setText("Aceptar");

        String json = getArguments()!=null ? getArguments().getString("estadoJson") : null;
        int totalPreg = getArguments()!=null ? getArguments().getInt("totalPreguntas", 25) : 25;
        if (json == null) { getParentFragmentManager().popBackStack(); return; }

        EstadoRetoResponse e = new Gson().fromJson(json, EstadoRetoResponse.class);

        int youCorrect = 0, oppCorrect = 0;
        int youTime = 0, oppTime = 0;

        if (e.jugadores != null && !e.jugadores.isEmpty()) {
            EstadoRetoResponse.Jugador j0 = e.jugadores.get(0);
            youCorrect = j0.correctas != null ? j0.correctas : 0;
            youTime = j0.tiempo_total_seg != null ? j0.tiempo_total_seg : 0;

            if (e.jugadores.size() > 1) {
                EstadoRetoResponse.Jugador j1 = e.jugadores.get(1);
                oppCorrect = j1.correctas != null ? j1.correctas : 0;
                oppTime = j1.tiempo_total_seg != null ? j1.tiempo_total_seg : 0;
            }
        }

        int youPts = youCorrect * 100;
        int oppPts = oppCorrect * 100;

        String titulo;
        if (e.ganador != null && e.jugadores != null && !e.jugadores.isEmpty()
                && e.jugadores.get(0).id_usuario != null) {
            Integer yoId = e.jugadores.get(0).id_usuario;
            if (e.ganador.equals(yoId)) { titulo = "¡Victoria!"; tvEmoji.setText("😄"); }
            else if (e.ganador == 0)    { titulo = "Empate";     tvEmoji.setText("😐"); }
            else                        { titulo = "Derrota";    tvEmoji.setText("😞"); }
        } else {
            if (youPts > oppPts) { titulo = "¡Victoria!"; tvEmoji.setText("😄"); }
            else if (youPts < oppPts) { titulo = "Derrota"; tvEmoji.setText("😞"); }
            else { titulo = "Empate"; tvEmoji.setText("😐"); }
        }

        tvTitle.setText(titulo);
        tvSubtitle.setText("Reto #" + (e.id_reto != null ? e.id_reto : ""));
        tvYouName.setText("Tú");
        tvOppName.setText("Oponente");
        tvYouDetail.setText(youCorrect + "/" + totalPreg + " correctas  •  " + youTime + "s");
        tvOppDetail.setText(oppCorrect + "/" + totalPreg + " correctas  •  " + oppTime + "s");
        tvYouPoints.setText(String.valueOf(youPts));
        tvOppPoints.setText(String.valueOf(oppPts));

        btnVolver.setOnClickListener(view -> volverARetos());
    }

    private void volverARetos() {
        if (!isAdded()) return;

        // 1) Si venimos dentro de RetosFragment, limpia su overlay (quiz/resultado)
        Fragment parent = getParentFragment();
        if (parent != null) {
            FragmentManager childFm = parent.getChildFragmentManager();
            childFm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            View parentView = parent.getView();
            if (parentView != null) {
                int overlayId = idByName("container");
                if (overlayId != 0) {
                    View overlay = parentView.findViewById(overlayId);
                    if (overlay != null) overlay.setVisibility(View.GONE);
                }
            }
        }

        // 2) Navega al FragmentGeneral en el contenedor principal
        int rootId = idByName("fragmentContainer");
        if (rootId == 0) rootId = idByName("main_container");
        if (rootId == 0) rootId = android.R.id.content;

        FragmentManager fm = requireActivity().getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction()
                .replace(rootId, new RetosFragment())
                .commit();
    }

    private int idByName(String name) {
        try {
            return getResources().getIdentifier(name, "id", requireContext().getPackageName());
        } catch (Exception ignored) { return 0; }
    }
}
