package com.example.zavira_movil.ui.ranking;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.RankingResponse;
import com.example.zavira_movil.model.LogrosResponse;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RankingLogrosActivity extends AppCompatActivity {

    // UI
    private TextView tabRanking, tabLogros;
    private LinearLayout viewRanking, viewLogros;
    private RecyclerView rvTop;
    private RecyclerView rvBadges;          // NUEVO
    private TextView tvUserInitials, tvUserName, tvUserRank, tvUserPoints;

    // Data
    private final List<RankingResponse.Item> top = new ArrayList<>();
    private TopAdapter adapter;

    // Mis Logros
    private BadgesAdapter badgesAdapter;    // NUEVO
    private boolean badgesLoaded = false;   // NUEVO

    // API
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking_logros);

        // Retrofit con interceptor (token)
        RetrofitClient.init(getApplicationContext());
        api = RetrofitClient.getInstance().create(ApiService.class);

        bindViews();
        setupTabs();
        setupRecycler();

        loadRanking();   // consumo del ranking
    }

    private void bindViews() {
        tabRanking = findViewById(R.id.tabRanking);
        tabLogros  = findViewById(R.id.tabLogros);
        viewRanking = findViewById(R.id.viewRanking);
        viewLogros  = findViewById(R.id.viewLogros);

        rvTop = findViewById(R.id.rvTop);
        rvBadges = findViewById(R.id.rvBadges); // NUEVO

        tvUserInitials = findViewById(R.id.tvUserInitials);
        tvUserName     = findViewById(R.id.tvUserName);
        tvUserRank     = findViewById(R.id.tvUserRank);
        tvUserPoints   = findViewById(R.id.tvUserPoints);
    }

    private void setupTabs() {
        tabRanking.setOnClickListener(v -> {
            viewRanking.setVisibility(View.VISIBLE);
            viewLogros.setVisibility(View.GONE);
        });
        tabLogros.setOnClickListener(v -> {
            viewRanking.setVisibility(View.GONE);
            viewLogros.setVisibility(View.VISIBLE);
            // Carga los logros solo la primera vez que entras a la pestaña
            if (!badgesLoaded) {
                loadBadges();
                badgesLoaded = true;
            }
        });
    }

    private void setupRecycler() {
        // Ranking
        rvTop.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TopAdapter(top);
        rvTop.setAdapter(adapter);

        // Mis Logros
        rvBadges.setLayoutManager(new LinearLayoutManager(this));
        rvBadges.setNestedScrollingEnabled(false);
        badgesAdapter = new BadgesAdapter();
        rvBadges.setAdapter(badgesAdapter);
    }

    private void loadRanking() {
        api.getRanking().enqueue(new Callback<RankingResponse>() {
            @Override
            public void onResponse(@NonNull Call<RankingResponse> call,
                                   @NonNull Response<RankingResponse> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(RankingLogrosActivity.this,
                            "No se pudo cargar ranking", Toast.LENGTH_SHORT).show();
                    return;
                }
                RankingResponse data = resp.body();

                // Top 5
                top.clear();
                if (data.getTop5() != null) top.addAll(data.getTop5());
                adapter.notifyDataSetChanged();

                // Tarjeta “Tu posición actual”
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

                    // Iniciales
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

            @Override
            public void onFailure(@NonNull Call<RankingResponse> call, @NonNull Throwable t) {
                Toast.makeText(RankingLogrosActivity.this,
                        "No se pudo cargar ranking", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --------------------- MIS LOGROS ---------------------
    private void loadBadges() {
        api.getMisLogros().enqueue(new Callback<LogrosResponse>() {
            @Override
            public void onResponse(@NonNull Call<LogrosResponse> call,
                                   @NonNull Response<LogrosResponse> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(RankingLogrosActivity.this,
                            "No se pudo cargar logros", Toast.LENGTH_SHORT).show();
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

            @Override
            public void onFailure(@NonNull Call<LogrosResponse> call, @NonNull Throwable t) {
                Toast.makeText(RankingLogrosActivity.this,
                        "No se pudo cargar logros", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // -----------------------------------------------------

    /* -------- Adaptador simple para Top 5 -------- */
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
