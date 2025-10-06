package com.example.zavira_movil.progreso;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.AceptarRetoResponse;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentLoadingSalaReto extends Fragment {

    public static FragmentLoadingSalaReto newInstance(String idReto, String area, String opName) {
        Bundle b = new Bundle();
        b.putString("idReto", idReto);
        b.putString("area", area);
        b.putString("opName", opName);
        FragmentLoadingSalaReto f = new FragmentLoadingSalaReto();
        f.setArguments(b);
        return f;
    }

    private String idReto, area, opName;
    private Button btnComenzar;
    private ProgressBar pb;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_loading_sala_reto, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        TextView tvArea = v.findViewById(R.id.tvArea);
        TextView tvOp   = v.findViewById(R.id.tvOponente);
        btnComenzar     = v.findViewById(R.id.btnComenzar);
        pb              = v.findViewById(R.id.pb);

        Bundle args = getArguments();
        if (args != null) {
            idReto  = args.getString("idReto");
            area    = args.getString("area", "");
            opName  = args.getString("opName", "");
        }
        tvArea.setText(area);
        tvOp.setText(opName);

        btnComenzar.setOnClickListener(v1 -> aceptarYEntrar());
    }

    private void aceptarYEntrar() {
        if (idReto == null || idReto.isEmpty()) {
            Toast.makeText(getContext(), "id_reto inválido", Toast.LENGTH_SHORT).show();
            return;
        }
        btnComenzar.setEnabled(false);
        pb.setVisibility(View.VISIBLE);

        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        api.aceptarReto(idReto).enqueue(new Callback<AceptarRetoResponse>() {
            @Override public void onResponse(Call<AceptarRetoResponse> call, Response<AceptarRetoResponse> resp) {
                btnComenzar.setEnabled(true);
                pb.setVisibility(View.GONE);

                if (!resp.isSuccessful() || resp.body()==null) {
                    Toast.makeText(getContext(), "Error aceptar: " + resp.code(), Toast.LENGTH_SHORT).show();
                    return;
                }
                AceptarRetoResponse aceptar = resp.body();

                int idSesion = (aceptar.sesiones!=null && !aceptar.sesiones.isEmpty())
                        ? aceptar.sesiones.get(0).id_sesion : -1;
                if (idSesion <= 0) {
                    Toast.makeText(getContext(), "Sin id_sesion", Toast.LENGTH_SHORT).show();
                    return;
                }

                // → Entrar al Quiz
                Bundle args = new Bundle();
                args.putString("aceptarJson", new Gson().toJson(aceptar));
                args.putInt("idSesion", idSesion);
                args.putString("idReto", aceptar.reto != null ? aceptar.reto.id_reto : idReto);

                FragmentQuiz f = new FragmentQuiz();
                f.setArguments(args);

                // MOSTRAR OVERLAY del layout padre (RetosFragment)
                View parentView = requireParentFragment().getView();
                if (parentView != null) {
                    View overlay = parentView.findViewById(R.id.container);
                    if (overlay != null) overlay.setVisibility(View.VISIBLE);
                }

                // NAVEGAR dentro del overlay (manager del padre)
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.container, f)
                        .addToBackStack(null)
                        .commit();
            }

            @Override public void onFailure(Call<AceptarRetoResponse> call, Throwable t) {
                btnComenzar.setEnabled(true);
                pb.setVisibility(View.GONE);
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
