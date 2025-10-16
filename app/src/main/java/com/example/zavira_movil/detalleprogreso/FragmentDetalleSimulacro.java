package com.example.zavira_movil.detalleprogreso;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.zavira_movil.R;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class FragmentDetalleSimulacro extends Fragment {

    // ---- Vistas del header (usa tus IDs reales del layout) ----
    private TextView tvMateria, tvFecha, tvPuntaje, tvNivel, tvTiempo, tvCorr, tvInc;
    private ProgressBar progress;
    private View headerCard;

    // ---- ViewPager + Adapter ----
    private ViewPager2 pager;
    private DetalleSimuPagerAdapter pagerAdapter;

    // ---- Servicio local (no toca tu ApiService global) ----
    interface ProgresoService {
        @GET("movil/sesion/{id}/detalle")
        Call<ProgresoDetalleResponse> getDetalleSesion(@Path("id") int id);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_detalle_simulacro, container, false);

        // Header
        tvMateria = v.findViewById(R.id.tvMateria);
        tvFecha   = v.findViewById(R.id.tvFecha);
        tvPuntaje = v.findViewById(R.id.tvPuntaje);
        tvNivel   = v.findViewById(R.id.tvNivel);
        tvTiempo  = v.findViewById(R.id.tvTiempo);
        tvCorr    = v.findViewById(R.id.tvCorrectas);
        tvInc     = v.findViewById(R.id.tvIncorrectas);
        progress  = v.findViewById(R.id.progress);
        headerCard= v.findViewById(R.id.headerCard);

        // Pager + Tabs
        pager = v.findViewById(R.id.viewPager);
        pagerAdapter = new DetalleSimuPagerAdapter(requireActivity());
        pager.setAdapter(pagerAdapter);

        TabLayout tabs = v.findViewById(R.id.tabLayout);
        new TabLayoutMediator(tabs, pager, (tab, position) -> {
            if (position == 0) tab.setText("Resumen");
            else if (position == 1) tab.setText("Preguntas");
            else tab.setText("Análisis");
        }).attach();

        // Tab inicial: lee "initial_tab" si te lo mandan (por defecto 0 = Resumen)
        int initialTab = getArguments() != null ? getArguments().getInt("initial_tab", 0) : 0;
        pager.setCurrentItem(Math.max(0, Math.min(2, initialTab)), false);

        // id de sesión (OBLIGATORIO)
        int idSesion = getArguments() != null ? getArguments().getInt("id_sesion", 0) : 0;
        cargarDatos(idSesion);

        return v;
    }

    private void cargarDatos(int idSesion) {
        if (idSesion <= 0) {
            Toast.makeText(requireContext(), "Falta id del intento", Toast.LENGTH_LONG).show();
            return;
        }

        progress.setVisibility(View.VISIBLE);

        // Usa tu RetrofitClient ya configurado (token, baseUrl, etc.)
        ProgresoService api = RetrofitClient
                .getInstance(requireContext())
                .create(ProgresoService.class);

        Log.d("DETALLE_SIMU", "GET /movil/sesion/" + idSesion + "/detalle");

        api.getDetalleSesion(idSesion).enqueue(new Callback<ProgresoDetalleResponse>() {
            @Override
            public void onResponse(Call<ProgresoDetalleResponse> call, Response<ProgresoDetalleResponse> resp) {
                progress.setVisibility(View.GONE);

                if (!resp.isSuccessful() || resp.body() == null) {
                    Log.e("DETALLE_SIMU", "HTTP " + resp.code());
                    Toast.makeText(requireContext(), "Error " + resp.code(), Toast.LENGTH_LONG).show();
                    return;
                }

                ProgresoDetalleResponse d = resp.body();
                if (d.header == null) {
                    Toast.makeText(requireContext(), "Detalle sin header", Toast.LENGTH_SHORT).show();
                    return;
                }

                bindHeader(d);
                pagerAdapter.setData(d); // -> Resumen, Preguntas, Análisis
            }

            @Override
            public void onFailure(Call<ProgresoDetalleResponse> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Log.e("DETALLE_SIMU", "onFailure", t);
                Toast.makeText(requireContext(), "Red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindHeader(ProgresoDetalleResponse d) {
        ProgresoDetalleResponse.Header h = d.header;

        // Texto principal
        tvMateria.setText(nullSafe(h.materia));
        tvFecha.setText(formatFecha(h.fecha));
        tvNivel.setText(nullSafe(h.nivel));
        tvTiempo.setText(toMin(h.tiempo_total_seg));
        tvCorr.setText(String.valueOf(h.correctas));
        tvInc.setText(String.valueOf(h.incorrectas));

        // Porcentaje: si escala=porcentaje usa puntaje; si no, calcula según correctas/total
        int pct = (h.escala != null && h.escala.equalsIgnoreCase("porcentaje"))
                ? safeInt(h.puntaje)
                : (h.total > 0 ? Math.round(h.correctas * 100f / h.total) : 0);
        tvPuntaje.setText(pct + "%");
    }

    // ---------- Helpers ----------
    private String toMin(int seg) {
        int m = seg / 60, s = seg % 60;
        return (m > 0) ? (m + " min" + (s > 0 ? " " + s + "s" : "")) : (s + "s");
    }

    private String formatFecha(String iso) {
        return (iso != null && iso.length() >= 10) ? iso.substring(0, 10) : "";
    }

    private String nullSafe(String s) { return s == null ? "" : s; }

    private int safeInt(Integer i) { return i == null ? 0 : i; }
}
