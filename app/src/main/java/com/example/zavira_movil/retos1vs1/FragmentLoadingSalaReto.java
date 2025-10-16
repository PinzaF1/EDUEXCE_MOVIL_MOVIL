package com.example.zavira_movil.retos1vs1;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.zavira_movil.R;
import com.example.zavira_movil.local.TokenManager;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.gson.Gson;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentLoadingSalaReto extends Fragment {

    public FragmentLoadingSalaReto() { super(R.layout.fragment_loading_sala_reto); }

    public static FragmentLoadingSalaReto newInstance(String idReto, String area, String opName, boolean esCreador) {
        Bundle b = new Bundle();
        b.putString("idReto", idReto);
        b.putString("area", area);
        b.putString("opName", opName);
        b.putBoolean("esCreador", esCreador);
        FragmentLoadingSalaReto f = new FragmentLoadingSalaReto();
        f.setArguments(b);
        return f;
    }

    private String idReto, area, opName;
    private boolean esCreador;

    private TextView tvArea, tvOponente;
    private Button btnEntrar;
    private ProgressBar pb;

    private Handler handler;
    private boolean launching = false;
    private static final long POLL_MS = 1200L;

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        tvArea     = v.findViewById(R.id.tvArea);
        tvOponente = v.findViewById(R.id.tvOponente);
        btnEntrar  = v.findViewById(R.id.btnEntrar);
        pb         = v.findViewById(R.id.pb);

        Bundle args = getArguments();
        if (args != null) {
            idReto    = args.getString("idReto");
            area      = args.getString("area", "");
            opName    = args.getString("opName", "");
            esCreador = args.getBoolean("esCreador", false);
        }

        tvArea.setText(area);
        tvOponente.setText(!TextUtils.isEmpty(opName) ? opName : "Oponente");

        // MUY IMPORTANTE: cada vez que entras a la sala, limpia el banderín
        clearEntregadaFlag();

        handler = new Handler();

        if (esCreador) {
            btnEntrar.setVisibility(View.GONE);
            pb.setVisibility(View.VISIBLE);
            pollEstado();
        } else {
            btnEntrar.setVisibility(View.VISIBLE);
            btnEntrar.setOnClickListener(v1 -> aceptarYOEntrar());
            pb.setVisibility(View.VISIBLE);
            pollEstado();
        }
    }

    // ===== Helpers de preferencias (banderín por usuario y por reto) =====
    private Integer myId() {
        try { return TokenManager.getUserId(requireContext()); } catch (Exception e) { return null; }
    }
    private String flagKey() { return "ronda_" + (idReto==null?"":idReto) + "_" + (myId()==null?"0":String.valueOf(myId())); }

    private void clearEntregadaFlag() {
        if (!isAdded() || TextUtils.isEmpty(idReto)) return;
        requireContext().getSharedPreferences("retos1v1", Context.MODE_PRIVATE)
                .edit().putBoolean(flagKey(), false).apply();
    }
    private boolean yaEntregue() {
        if (!isAdded() || TextUtils.isEmpty(idReto)) return false;
        return requireContext().getSharedPreferences("retos1v1", Context.MODE_PRIVATE)
                .getBoolean(flagKey(), false);
    }

    // ===== Polling a /estado =====
    private void pollEstado() {
        if (!isAdded() || launching || TextUtils.isEmpty(idReto)) return;

        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        api.estadoReto(idReto).enqueue(new Callback<EstadoRetoResponse>() {
            @Override public void onResponse(Call<EstadoRetoResponse> call, Response<EstadoRetoResponse> resp) {
                if (!isAdded() || launching) return;

                if (resp.isSuccessful() && resp.body() != null) {
                    EstadoRetoResponse e = resp.body();
                    String st = e.estado != null ? e.estado.toLowerCase() : "";

                    if ("en_curso".equals(st)) {
                        fetchSesionesYEntrar();
                        return;
                    }

                    if ("finalizado".equals(st)) {
                        if (yaEntregue()) {
                            irAResultado(e);
                            return;
                        }
                        // Si marcó finalizado pero yo no he enviado, intento conseguir mi sesión y jugar
                        fetchSesionesYEntrar();
                        return;
                    }
                }
                reintentar(FragmentLoadingSalaReto.this::pollEstado);
            }
            @Override public void onFailure(Call<EstadoRetoResponse> call, Throwable t) {
                if (!isAdded() || launching) return;
                reintentar(FragmentLoadingSalaReto.this::pollEstado);
            }
        });
    }

    private void reintentar(Runnable r) {
        if (handler != null) handler.postDelayed(r, POLL_MS);
    }

    // ===== Retado acepta y entra =====
    private void aceptarYOEntrar() {
        if (!isAdded() || launching) return;
        btnEntrar.setEnabled(false);
        pb.setVisibility(View.VISIBLE);

        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        api.aceptarRetoConBody(idReto, new HashMap<>()).enqueue(new Callback<AceptarRetoResponse>() {
            @Override public void onResponse(Call<AceptarRetoResponse> call, Response<AceptarRetoResponse> resp) {
                if (!isAdded() || launching) return;
                if (resp.isSuccessful() && resp.body()!=null && tieneSesion(resp.body())) {
                    lanzarQuiz(resp.body());
                } else {
                    btnEntrar.setEnabled(true);
                    reintentar(FragmentLoadingSalaReto.this::pollEstado);
                }
            }
            @Override public void onFailure(Call<AceptarRetoResponse> call, Throwable t) {
                if (!isAdded() || launching) return;
                btnEntrar.setEnabled(true);
                Toast.makeText(requireContext(), "No se pudo aceptar. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ===== Conseguir sesiones/preguntas y entrar =====
    private void fetchSesionesYEntrar() {
        if (!isAdded() || launching) return;

        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        api.aceptarRetoConBody(idReto, new HashMap<>()).enqueue(new Callback<AceptarRetoResponse>() {
            @Override public void onResponse(Call<AceptarRetoResponse> call, Response<AceptarRetoResponse> resp) {
                if (!isAdded() || launching) return;
                if (resp.isSuccessful() && resp.body()!=null && tieneSesion(resp.body())) {
                    lanzarQuiz(resp.body());
                } else {
                    reintentar(FragmentLoadingSalaReto.this::fetchSesionesYEntrar);
                }
            }
            @Override public void onFailure(Call<AceptarRetoResponse> call, Throwable t) {
                if (!isAdded() || launching) return;
                reintentar(FragmentLoadingSalaReto.this::fetchSesionesYEntrar);
            }
        });
    }

    private boolean tieneSesion(AceptarRetoResponse a) {
        return a != null && a.sesiones != null && !a.sesiones.isEmpty() && a.sesiones.get(0).id_sesion > 0;
    }

    // ===== Navegación =====
    private void lanzarQuiz(AceptarRetoResponse aceptar) {
        if (!isAdded() || aceptar == null) return;

        int idSesion = (aceptar.sesiones != null && !aceptar.sesiones.isEmpty())
                ? aceptar.sesiones.get(0).id_sesion : -1;
        if (idSesion <= 0) {
            reintentar(FragmentLoadingSalaReto.this::fetchSesionesYEntrar);
            return;
        }

        launching = true;

        Bundle args = new Bundle();
        args.putString("aceptarJson", new Gson().toJson(aceptar));
        args.putInt("idSesion", idSesion);
        args.putString("idReto", aceptar.reto != null ? String.valueOf(aceptar.reto.id_reto) : idReto);

        FragmentQuiz f = new FragmentQuiz();
        f.setArguments(args);

        FragmentManager fm = requireActivity().getSupportFragmentManager();
        fm.beginTransaction()
                .replace(resolveContainerId(), f)
                .addToBackStack("quiz")
                .commit();
    }

    private void irAResultado(EstadoRetoResponse e) {
        if (!isAdded()) return;

        Bundle b = new Bundle();
        b.putString("estadoJson", new Gson().toJson(e));
        b.putInt("totalPreguntas", 25);

        FragmentResultadoReto f = new FragmentResultadoReto();
        f.setArguments(b);

        FragmentManager fm = requireActivity().getSupportFragmentManager();
        fm.beginTransaction()
                .replace(resolveContainerId(), f)
                .addToBackStack("resultadoReto")
                .commit();
    }

    private int resolveContainerId() {
        View parent = getParentFragment() != null ? getParentFragment().getView() : null;
        View overlay = parent != null ? parent.findViewById(R.id.container) : null;
        if (overlay != null) { overlay.setVisibility(View.VISIBLE); return R.id.container; }
        View root = requireActivity().findViewById(R.id.fragmentContainer);
        return (root != null) ? R.id.fragmentContainer : android.R.id.content;
    }

    @Override
    public void onDestroyView() {
        if (handler != null) { handler.removeCallbacksAndMessages(null); handler = null; }
        super.onDestroyView();
    }
}
