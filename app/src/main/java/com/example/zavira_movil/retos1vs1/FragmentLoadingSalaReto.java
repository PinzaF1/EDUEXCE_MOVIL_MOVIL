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

    private String spName() { return "retos1v1"; }
    private Integer myId() { try { return TokenManager.getUserId(requireContext()); } catch (Exception e) { return null; } }
    private String keyAceptadoPorCreador() { return "aceptado_creador_" + (idReto==null?"":idReto); }
    private boolean creadorYaLlamoAceptar() {
        if (!isAdded()) return false;
        return requireContext().getSharedPreferences(spName(), Context.MODE_PRIVATE)
                .getBoolean(keyAceptadoPorCreador(), false);
    }
    private void marcarCreadorLlamoAceptar() {
        if (!isAdded()) return;
        requireContext().getSharedPreferences(spName(), Context.MODE_PRIVATE)
                .edit().putBoolean(keyAceptadoPorCreador(), true).apply();
    }

    private String keyEntregada() {
        Integer my = myId();
        return "ronda_" + (idReto==null?"":idReto) + "_" + (my==null?"0":String.valueOf(my));
    }
    private void clearEntregadaFlag() {
        if (!isAdded() || TextUtils.isEmpty(idReto)) return;
        requireContext().getSharedPreferences(spName(), Context.MODE_PRIVATE)
                .edit().putBoolean(keyEntregada(), false).apply();
    }
    private boolean yaEntregue() {
        if (!isAdded() || TextUtils.isEmpty(idReto)) return false;
        return requireContext().getSharedPreferences(spName(), Context.MODE_PRIVATE)
                .getBoolean(keyEntregada(), false);
    }

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

    @Override public void onDestroyView() {
        if (handler != null) { handler.removeCallbacksAndMessages(null); handler = null; }
        super.onDestroyView();
    }

    private void reintentar(Runnable r) { if (handler != null) handler.postDelayed(r, POLL_MS); }

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
                        if (esCreador) {
                            if (creadorYaLlamoAceptar()) {
                                fetchSesionesSoloConAceptarUnaVez(false);
                            } else {
                                fetchSesionesSoloConAceptarUnaVez(true);
                            }
                            return;
                        } else {
                            aceptarYOEntrar();
                            return;
                        }
                    }

                    if ("finalizado".equals(st)) {
                        if (yaEntregue()) { irAResultado(e); return; }
                        if (esCreador) {
                            if (!creadorYaLlamoAceptar()) fetchSesionesSoloConAceptarUnaVez(true);
                        } else {
                            aceptarYOEntrar();
                        }
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

    private void aceptarYOEntrar() {
        if (!isAdded() || launching) return;
        if (esCreador) return;
        if (btnEntrar != null) btnEntrar.setEnabled(false);
        if (pb != null) pb.setVisibility(View.VISIBLE);

        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        api.aceptarRetoConBody(idReto, new HashMap<>()).enqueue(new Callback<AceptarRetoResponse>() {
            @Override public void onResponse(Call<AceptarRetoResponse> call, Response<AceptarRetoResponse> resp) {
                if (!isAdded() || launching) return;
                if (resp.isSuccessful() && resp.body()!=null && tieneSesion(resp.body())) {
                    lanzarQuiz(resp.body());
                } else {
                    if (btnEntrar != null) btnEntrar.setEnabled(true);
                    reintentar(FragmentLoadingSalaReto.this::pollEstado);
                }
            }
            @Override public void onFailure(Call<AceptarRetoResponse> call, Throwable t) {
                if (!isAdded() || launching) return;
                if (btnEntrar != null) btnEntrar.setEnabled(true);
                Toast.makeText(requireContext(), "No se pudo aceptar. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
                reintentar(FragmentLoadingSalaReto.this::pollEstado);
            }
        });
    }

    private void fetchSesionesSoloConAceptarUnaVez(boolean conJitter) {
        if (!isAdded() || launching) return;

        Runnable work = () -> {
            if (!isAdded() || launching) return;

            ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
            marcarCreadorLlamoAceptar();

            api.aceptarRetoConBody(idReto, new HashMap<>()).enqueue(new Callback<AceptarRetoResponse>() {
                @Override public void onResponse(Call<AceptarRetoResponse> call, Response<AceptarRetoResponse> resp) {
                    if (!isAdded() || launching) return;
                    if (resp.isSuccessful() && resp.body()!=null && tieneSesion(resp.body())) {
                        lanzarQuiz(resp.body());
                    } else {
                        reintentar(FragmentLoadingSalaReto.this::pollEstado);
                    }
                }
                @Override public void onFailure(Call<AceptarRetoResponse> call, Throwable t) {
                    if (!isAdded() || launching) return;
                    reintentar(FragmentLoadingSalaReto.this::pollEstado);
                }
            });
        };

        if (conJitter) {
            long delay = 1200 + (long)(Math.random() * 800);
            if (handler != null) handler.postDelayed(work, delay);
        } else {
            work.run();
        }
    }

    private boolean tieneSesion(AceptarRetoResponse a) {
        if (a == null || a.sesiones == null || a.sesiones.isEmpty()) return false;
        Integer my = myId();
        int id = a.findSesionIdForUser(my);
        return id > 0;
    }

    private void lanzarQuiz(AceptarRetoResponse aceptar) {
        if (!isAdded() || aceptar == null) return;

        int idSesion = aceptar.findSesionIdForUser(myId());
        if (idSesion <= 0) { reintentar(FragmentLoadingSalaReto.this::pollEstado); return; }

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
        // Si conoces la sesión aquí, puedes pasarla:
        // b.putInt("idSesion", <miIdSesion>);

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
}
