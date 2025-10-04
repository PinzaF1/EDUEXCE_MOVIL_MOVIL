package com.example.zavira_movil.ui.ranking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.LogrosResponse;
import com.example.zavira_movil.model.RankingResponse;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RankingLogrosFragment extends Fragment {

    private TextView tabRanking, tabLogros;
    private LinearLayout viewRanking, viewLogros;
    private RecyclerView rvTop, rvBadges;
    private TextView tvUserInitials, tvUserName, tvUserRank, tvUserPoints;

    private final List<RankingResponse.Item> top = new ArrayList<>();
    private TopAdapter adapter;

    private BadgesAdapter badgesAdapter;
    private boolean badgesLoaded = false;

    private ApiService api;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ranking_logros, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        RetrofitClient.init(requireContext());
        api = RetrofitClient.getInstance().create(ApiService.class);

        bindViews(v);
        setupTabs();
        setupRecycler();

        loadRanking();
    }

    private void bindViews(View root) {
        tabRanking = root.findViewById(R.id.tabRanking);
        tabLogros  = root.findViewById(R.id.tabLogros);
        viewRanking = root.findViewById(R.id.viewRanking);
        viewLogros  = root.findViewById(R.id.viewLogros);

        rvTop = root.findViewById(R.id.rvTop);
        rvBadges = root.findViewById(R.id.rvBadges);

        tvUserInitials = root.findViewById(R.id.tvUserInitials);
        tvUserName     = root.findViewById(R.id.tvUserName);
        tvUserRank     = root.findViewById(R.id.tvUserRank);
        tvUserPoints   = root.findViewById(R.id.tvUserPoints);
    }

    private void setupTabs() {
        tabRanking.setOnClickListener(v -> {
            viewRanking.setVisibility(View.VISIBLE);
            viewLogros.setVisibility(View.GONE);
        });
        tabLogros.setOnClickListener(v -> {
            viewRanking.setVisibility(View.GONE);
            viewLogros.setVisibility(View.VISIBLE);
            if (!badgesLoaded) { loadBadges(); badgesLoaded = true; }
        });
    }

    private void setupRecycler() {
        rvTop.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TopAdapter(top);
        rvTop.setAdapter(adapter);

        rvBadges.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBadges.setNestedScrollingEnabled(false);
        badgesAdapter = new BadgesAdapter();
        rvBadges.setAdapter(badgesAdapter);
    }

    private void loadRanking() {
        api.getRanking().enqueue(new Callback<RankingResponse>() {
            @Override public void onResponse(@NonNull Call<RankingResponse> call, @NonNull Response<RankingResponse> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(requireContext(), "No se pudo cargar ranking", Toast.LENGTH_SHORT).show();
                    return;
                }
                RankingResponse data = resp.body();

                top.clear();
                if (data.getTop5() != null) top.addAll(data.getTop5());
                adapter.notifyDataSetChanged();

                if (data.getPosiciones() != null && !data.getPosiciones().isEmpty()) {
                    RankingResponse.Item me = null;
                    for (RankingResponse.Item it : data.getPosiciones()) {
                        if (it.getPosicion() != null && it.getPosicion().equals(data.getPosicion())) {
                            me = it; break;
                        }
                    }
                    if (me == null) me = data.getPosiciones().get(0);

                    String nombre = me.getNombre() == null ? "Estudiante" : me.getNombre();
                    tvUserName.setText(nombre);
                    tvUserRank.setText("Puesto #" + (me.getPosicion() == null ? data.getPosicion() : me.getPosicion())
                            + " en el ranking general");
                    tvUserPoints.setText(String.valueOf(me.getPromedio()));

                    String ini = "JP";
                    String[] parts = nombre.trim().split("\\s+");
                    if (parts.length >= 2) {
                        ini = (parts[0].substring(0,1) + parts[parts.length-1].substring(0,1)).toUpperCase();
                    } else if (parts.length == 1 && parts[0].length() >= 1) {
                        ini = parts[0].substring(0,1).toUpperCase();
                    }
                    tvUserInitials.setText(ini);
                }
            }
            @Override public void onFailure(@NonNull Call<RankingResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "No se pudo cargar ranking", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBadges() {
        api.getMisLogros().enqueue(new Callback<LogrosResponse>() {
            @Override public void onResponse(@NonNull Call<LogrosResponse> call, @NonNull Response<LogrosResponse> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(requireContext(), "No se pudo cargar logros", Toast.LENGTH_SHORT).show();
                    return;
                }
                LogrosResponse data = resp.body();

                List<BadgesAdapter.Row> rows = new ArrayList<>();
                if (data.getObtenidas() != null && !data.getObtenidas().isEmpty()) {
                    rows.add(BadgesAdapter.Row.header("Obtenidas"));
                    for (LogrosResponse.Badge b : data.getObtenidas()) {
                        rows.add(BadgesAdapter.Row.item(b, true));
                    }
                }
                rows.add(BadgesAdapter.Row.header("Pendientes"));
                if (data.getPendientes() != null) {
                    for (LogrosResponse.Badge b : data.getPendientes()) {
                        rows.add(BadgesAdapter.Row.item(b, false));
                    }
                }
                badgesAdapter.setData(rows);
            }
            @Override public void onFailure(@NonNull Call<LogrosResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "No se pudo cargar logros", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* Adaptador Top 5 (igual al tuyo) */
    private static class TopAdapter extends RecyclerView.Adapter<TopAdapter.VH> {
        private final List<RankingResponse.Item> data;
        TopAdapter(List<RankingResponse.Item> d) { this.data = d; }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvRight;
            VH(@NonNull View v) {
                super(v);
                tvTitle = v.findViewById(android.R.id.text1);
                tvRight = v.findViewById(android.R.id.text2);
            }
        }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int viewType) {
            View v = View.inflate(p.getContext(), android.R.layout.simple_list_item_2, null);
            return new VH(v);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            RankingResponse.Item it = data.get(pos);
            h.tvTitle.setText((pos + 1) + ". " + (it.getNombre() == null ? "—" : it.getNombre()));
            h.tvRight.setText(it.getPromedio() + " pts");
        }

        @Override public int getItemCount() { return data.size(); }
    }
}
