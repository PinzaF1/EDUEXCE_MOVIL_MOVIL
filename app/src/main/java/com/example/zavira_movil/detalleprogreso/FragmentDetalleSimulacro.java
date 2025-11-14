package com.example.zavira_movil.detalleprogreso;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
        // El TabLayout ya está configurado en el XML con fondo azul oscuro y texto blanco
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Configurar padding del contenedor para bajar el contenido
        if (getActivity() != null) {
            android.widget.FrameLayout fragmentContainer = getActivity().findViewById(R.id.fragmentContainer);
            if (fragmentContainer != null) {
                // Usar post para asegurar que se aplique después de que la vista esté completamente montada
                fragmentContainer.post(() -> {
                    int paddingTop = (int) (40 * getResources().getDisplayMetrics().density); // 40dp
                    fragmentContainer.setPadding(0, paddingTop, 0, 0);
                });
                // También aplicar inmediatamente
                int paddingTop = (int) (40 * getResources().getDisplayMetrics().density); // 40dp
                fragmentContainer.setPadding(0, paddingTop, 0, 0);
            }
        }
    }

    private void cargarDatos(int idSesion) {
        if (!isAdded() || getContext() == null) return;
        
        if (idSesion <= 0) {
            Toast.makeText(getContext(), "Falta id del intento", Toast.LENGTH_LONG).show();
            return;
        }

        if (progress != null) progress.setVisibility(View.VISIBLE);

        // Usa tu RetrofitClient ya configurado (token, baseUrl, etc.)
        ProgresoService api = RetrofitClient
                .getInstance(getContext())
                .create(ProgresoService.class);

        Log.d("DETALLE_SIMU", "GET /movil/sesion/" + idSesion + "/detalle");

        api.getDetalleSesion(idSesion).enqueue(new Callback<ProgresoDetalleResponse>() {
            @Override
            public void onResponse(Call<ProgresoDetalleResponse> call, Response<ProgresoDetalleResponse> resp) {
                if (!isAdded() || getContext() == null) return;
                if (progress != null) progress.setVisibility(View.GONE);

                if (!resp.isSuccessful() || resp.body() == null) {
                    Log.e("DETALLE_SIMU", "HTTP " + resp.code());
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error " + resp.code(), Toast.LENGTH_LONG).show();
                    }
                    return;
                }

                ProgresoDetalleResponse d = resp.body();
                if (d == null || d.header == null) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Detalle sin header", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                bindHeader(d);
                if (pagerAdapter != null) {
                    // Primero establecer la materia, luego los datos
                    pagerAdapter.setMateria(nullSafe(d.header.materia));
                    pagerAdapter.setData(d); // -> Resumen, Preguntas, Análisis
                }
            }

            @Override
            public void onFailure(Call<ProgresoDetalleResponse> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                if (progress != null) progress.setVisibility(View.GONE);
                Log.e("DETALLE_SIMU", "onFailure", t);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Red: " + (t.getMessage() != null ? t.getMessage() : "Error desconocido"), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void bindHeader(ProgresoDetalleResponse d) {
        if (!isAdded() || getContext() == null) return;
        if (d == null || d.header == null) return;
        
        ProgresoDetalleResponse.Header h = d.header;
        android.content.Context ctx = getContext();

        // Texto principal
        if (tvMateria != null) tvMateria.setText(nullSafe(h.materia));
        if (tvFecha != null) {
            tvFecha.setText(formatFecha(h.fecha));
            tvFecha.setTextColor(Color.BLACK);
            tvFecha.setTypeface(tvFecha.getTypeface(), android.graphics.Typeface.BOLD);
        }
        if (tvNivel != null) tvNivel.setText(nullSafe(h.nivel));
        if (tvTiempo != null) {
            tvTiempo.setText(toMin(h.tiempo_total_seg));
            tvTiempo.setTextColor(ContextCompat.getColor(ctx, R.color.blue_time));
        }
        if (tvCorr != null) {
            tvCorr.setText(String.valueOf(h.correctas));
            tvCorr.setTextColor(ContextCompat.getColor(ctx, R.color.green_success));
        }
        if (tvInc != null) {
            tvInc.setText(String.valueOf(h.incorrectas));
            tvInc.setTextColor(ContextCompat.getColor(ctx, R.color.red_error));
        }

        // Porcentaje: si escala=porcentaje usa puntaje; si no, calcula según correctas/total
        int pct = (h.escala != null && h.escala.equalsIgnoreCase("porcentaje"))
                ? safeInt(h.puntaje)
                : (h.total > 0 ? Math.round(h.correctas * 100f / h.total) : 0);
        
        if (tvPuntaje != null) {
            tvPuntaje.setText(pct + "%");
            // Obtener color del área para el porcentaje
            int areaColor = obtenerColorArea(nullSafe(h.materia), ctx);
            tvPuntaje.setTextColor(areaColor);
        }
        
        // La materia ya se pasa al adapter en onResponse antes de setData
    }
    
    private int obtenerColorArea(String area, android.content.Context ctx) {
        if (area == null || ctx == null) return Color.parseColor("#B6B9C2");
        String a = area.toLowerCase().trim();
        
        try {
            // Isla del Conocimiento / Todas las áreas - Amarillo
            if (a.contains("conocimiento") || a.contains("isla") || 
                (a.contains("todas") && (a.contains("area") || a.contains("área")))) {
                return ContextCompat.getColor(ctx, R.color.area_conocimiento);
            }
            
            if (a.contains("matem")) return ContextCompat.getColor(ctx, R.color.area_matematicas);
            if (a.contains("lengua") || a.contains("lectura") || a.contains("espa") || a.contains("critica")) 
                return ContextCompat.getColor(ctx, R.color.area_lenguaje);
            if (a.contains("social") || a.contains("ciudad")) 
                return ContextCompat.getColor(ctx, R.color.area_sociales);
            if (a.contains("cien") || a.contains("biolo") || a.contains("fis") || a.contains("quim")) 
                return ContextCompat.getColor(ctx, R.color.area_ciencias);
            if (a.contains("ingl")) 
                return ContextCompat.getColor(ctx, R.color.area_ingles);
        } catch (Exception e) {
            return Color.parseColor("#B6B9C2");
        }
        
        return Color.parseColor("#B6B9C2");
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
