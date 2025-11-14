package com.example.zavira_movil.ui.ranking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

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

    private TabLayout tabLayout;
    private LinearLayout viewRanking, viewLogros;
    private RecyclerView rvTop, rvBadges;
    private TextView tvUserInitials, tvUserName, tvUserRank, tvUserPoints;
    private ImageView ivMedal;
    private TextView tvMedalNumber;

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
        // IMPORTANTE: usa la versión con contexto (consistente con tu otro código)
        RetrofitClient.init(requireContext());
        api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);

        // Ocultar logo EduExce y campana en la Activity principal
        v.post(() -> ocultarTopBar());

        bindViews(v);
        setupRecycler();
        setupTabs();

        // Estado inicial: mostrar Ranking
        setActiveTab(true);
        // Seleccionar la primera tab (Ranking)
        TabLayout.Tab tab = tabLayout.getTabAt(0);
        if (tab != null) {
            tab.select();
        }

        loadRanking();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Asegurar que el topBar esté oculto
        if (getView() != null) {
            getView().post(() -> ocultarTopBar());
        }
    }
    
    @Override
    public void onDestroyView() {
        // Restaurar logo EduExce y campana al salir del fragmento
        restaurarTopBar();
        super.onDestroyView();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Asegurar que se restaure al pausar también
        restaurarTopBar();
    }
    
    /**
     * Oculta el topBar (logo EduExce y campana) de HomeActivity
     */
    private void ocultarTopBar() {
        if (getActivity() != null && isAdded()) {
            View topBar = getActivity().findViewById(R.id.topBar);
            if (topBar != null) {
                topBar.setVisibility(View.GONE);
            }
        }
    }
    
    /**
     * Restaura la visibilidad del topBar (logo EduExce y campana) de HomeActivity
     */
    private void restaurarTopBar() {
        if (getActivity() != null && isAdded()) {
            View topBar = getActivity().findViewById(R.id.topBar);
            if (topBar != null) {
                topBar.setVisibility(View.VISIBLE);
            }
        }
    }

    private void bindViews(View root) {
        tabLayout = root.findViewById(R.id.tabLayout);

        viewRanking = root.findViewById(R.id.viewRanking);
        viewLogros  = root.findViewById(R.id.viewLogros);

        rvTop    = root.findViewById(R.id.rvTop);
        rvBadges = root.findViewById(R.id.rvBadges);

        tvUserInitials = root.findViewById(R.id.tvUserInitials);
        tvUserName     = root.findViewById(R.id.tvUserName);
        tvUserRank     = root.findViewById(R.id.tvUserRank);
        tvUserPoints   = root.findViewById(R.id.tvUserPoints);
        ivMedal        = root.findViewById(R.id.ivMedal);
        tvMedalNumber  = root.findViewById(R.id.tvMedalNumber);
    }

    private void setupTabs() {
        // Agregar las tabs
        tabLayout.addTab(tabLayout.newTab().setText("Ranking"));
        tabLayout.addTab(tabLayout.newTab().setText("Logros"));
        
        // Listener para cambios de tab
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    // Ranking
                    setActiveTab(true);
                } else {
                    // Logros
                    setActiveTab(false);
                    if (!badgesLoaded) {
                        loadBadges();
                        badgesLoaded = true;
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // No hacer nada
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // No hacer nada
            }
        });
    }

    /** Alterna vistas y estados visuales de los tabs */
    private void setActiveTab(boolean rankingActive) {
        viewRanking.setVisibility(rankingActive ? View.VISIBLE : View.GONE);
        viewLogros.setVisibility(rankingActive ? View.GONE : View.VISIBLE);
    }

    private void setupRecycler() {
        rvTop.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TopAdapter(top);
        rvTop.setAdapter(adapter);

        badgesAdapter = new BadgesAdapter();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // Headers ocupan 2 columnas, items ocupan 1
                return badgesAdapter.getItemViewType(position) == BadgesAdapter.Row.TYPE_HEADER ? 2 : 1;
            }
        });
        rvBadges.setLayoutManager(gridLayoutManager);
        rvBadges.setNestedScrollingEnabled(false);
        rvBadges.setAdapter(badgesAdapter);
    }

    /** Carga ranking y pinta la tarjeta del usuario */
    private void loadRanking() {
        api.getRanking().enqueue(new Callback<RankingResponse>() {
            @Override
            public void onResponse(@NonNull Call<RankingResponse> call, @NonNull Response<RankingResponse> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(requireContext(), "No se pudo cargar ranking", Toast.LENGTH_SHORT).show();
                    return;
                }
                RankingResponse data = resp.body();

                // 1) Top del servidor
                top.clear();
                if (data.getTop5() != null) top.addAll(data.getTop5());

                // 2) Encontrar "yo"
                RankingResponse.Item me = null;
                if (data.getPosiciones() != null) {
                    for (RankingResponse.Item it : data.getPosiciones()) {
                        if (it.getPosicion() != null && it.getPosicion().equals(data.getPosicion())) {
                            me = it; break;
                        }
                    }
                }
                if (me == null && data.getPosiciones() != null && !data.getPosiciones().isEmpty()) {
                    me = data.getPosiciones().get(0); // fallback
                }

                // 3) Asegurar "yo" en top5 si no está
                if (me != null) {
                    boolean yaEsta = false;
                    for (RankingResponse.Item it : top) {
                        if (equalsIgnoreCaseSafe(it.getNombre(), me.getNombre())) { yaEsta = true; break; }
                    }
                    if (!yaEsta) {
                        while (top.size() > 4) top.remove(top.size() - 1);
                        if (top.size() < 5) top.add(me);
                    }
                }

                adapter.notifyDataSetChanged();

                // 4) Pintar tarjeta usuario
                if (me != null) {
                    String nombre = me.getNombre() == null ? "Estudiante" : me.getNombre();
                    // Formatear nombre para mostrar solo primer nombre y primer apellido
                    String nombreFormateado = formatearNombre(nombre);
                    tvUserName.setText(nombreFormateado);
                    
                    // Obtener posición
                    int posicion = me.getPosicion() != null ? me.getPosicion() : 
                                   (data.getPosicion() != null ? data.getPosicion() : 0);
                    
                    // Configurar texto según posición
                    String textoRanking;
                    if (posicion == 1) {
                        textoRanking = "Estás en el primer lugar del ranking";
                    } else if (posicion == 2) {
                        textoRanking = "Estás en el segundo lugar del ranking";
                    } else if (posicion == 3) {
                        textoRanking = "Estás en el tercer lugar del ranking";
                    } else {
                        textoRanking = "Estás en el lugar #" + posicion + " del ranking";
                    }
                    tvUserRank.setText(textoRanking);
                    tvUserPoints.setText(String.valueOf(me.getPromedio()));

                    // Configurar visualización según posición
                    if (posicion >= 1 && posicion <= 3) {
                        // Mostrar medalla con número para posiciones 1-3
                        // Ocultar iniciales
                        tvUserInitials.setVisibility(View.GONE);
                        
                        // Mostrar medalla y número
                        ivMedal.setVisibility(View.VISIBLE);
                        tvMedalNumber.setVisibility(View.VISIBLE);
                        tvMedalNumber.setText(String.valueOf(posicion));
                        
                        // Aplicar medalla según posición
                        if (posicion == 1) {
                            // Oro - primer lugar
                            ivMedal.setImageResource(R.drawable.medallaoro);
                        } else if (posicion == 2) {
                            // Plata - segundo lugar
                            ivMedal.setImageResource(R.drawable.medallaplata);
                        } else if (posicion == 3) {
                            // Bronce - tercer lugar
                            ivMedal.setImageResource(R.drawable.medallabronce);
                        }
                    } else {
                        // Mostrar iniciales para posición 4+
                        // Ocultar medalla
                        ivMedal.setVisibility(View.GONE);
                        tvMedalNumber.setVisibility(View.GONE);
                        
                        // Mostrar iniciales
                        tvUserInitials.setVisibility(View.VISIBLE);
                        String ini = "JP";
                        String[] parts = nombreFormateado.trim().split("\\s+");
                        if (parts.length >= 2) {
                            ini = (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
                        } else if (parts.length == 1 && parts[0].length() >= 1) {
                            ini = parts[0].substring(0, 1).toUpperCase();
                        }
                        tvUserInitials.setText(ini);
                        tvUserInitials.setBackgroundResource(R.drawable.bg_estilo2);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<RankingResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "No se pudo cargar ranking", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static boolean equalsIgnoreCaseSafe(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.trim().equalsIgnoreCase(b.trim());
    }

    /**
     * Formatea el nombre completo para mostrar solo primer nombre y primer apellido
     */
    private String formatearNombre(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return "Estudiante";
        }
        
        String nombre = nombreCompleto.trim();
        String[] partes = nombre.split("\\s+");
        
        if (partes.length == 0) {
            return "Estudiante";
        } else if (partes.length == 1) {
            return partes[0];
        } else if (partes.length == 2) {
            // Si hay 2 palabras: primera es nombre, segunda es apellido
            return partes[0] + " " + partes[1];
        } else {
            // Si hay 3 o más palabras: primera es primer nombre, tercera es primer apellido
            // (asumiendo estructura: Nombre SegundoNombre Apellido1 Apellido2)
            return partes[0] + " " + partes[2];
        }
    }

    private void loadBadges() {
        api.getMisLogros().enqueue(new Callback<LogrosResponse>() {
            @Override
            public void onResponse(@NonNull Call<LogrosResponse> call, @NonNull Response<LogrosResponse> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(requireContext(), "No se pudo cargar logros", Toast.LENGTH_SHORT).show();
                    return;
                }
                LogrosResponse data = resp.body();

                List<BadgesAdapter.Row> rows = new ArrayList<>();
                if (data.getObtenidas() != null && !data.getObtenidas().isEmpty()) {
                    for (LogrosResponse.Badge b : data.getObtenidas()) rows.add(BadgesAdapter.Row.item(b, true));
                }
                if (data.getPendientes() != null) {
                    for (LogrosResponse.Badge b : data.getPendientes()) rows.add(BadgesAdapter.Row.item(b, false));
                }
                badgesAdapter.setData(rows);
            }

            @Override
            public void onFailure(@NonNull Call<LogrosResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "No se pudo cargar logros", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /** =================== Top 5 Adapter =================== */
    private static class TopAdapter extends RecyclerView.Adapter<TopAdapter.VH> {

        private final List<RankingResponse.Item> data;
        TopAdapter(List<RankingResponse.Item> d) { this.data = d; }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvRank, tvName, tvPointsSmall, tvPointsRight;
            ImageView ivMedal;
            TextView tvMedalNumber;
            View bgRow;
            VH(@NonNull View v) {
                super(v);
                tvRank        = v.findViewById(R.id.tvRank);
                tvName        = v.findViewById(R.id.tvName);
                tvPointsSmall = v.findViewById(R.id.tvPointsSmall);
                tvPointsRight = v.findViewById(R.id.tvPointsRight);
                ivMedal       = v.findViewById(R.id.ivMedal);
                tvMedalNumber = v.findViewById(R.id.tvMedalNumber);
                bgRow         = v.findViewById(R.id.bgRow);
            }
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_top_student, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            RankingResponse.Item it = data.get(position);

            int rank = position + 1;
            
            // Configurar visualización según posición
            if (rank >= 1 && rank <= 3) {
                // Mostrar medalla con número para posiciones 1-3
                // Ocultar TextView de rank normal
                h.tvRank.setVisibility(View.GONE);
                
                // Mostrar medalla y número
                h.ivMedal.setVisibility(View.VISIBLE);
                h.tvMedalNumber.setVisibility(View.VISIBLE);
                h.tvMedalNumber.setText(String.valueOf(rank));
                
                // Aplicar medalla según posición
                if (rank == 1) {
                    // Oro - primer lugar
                    h.ivMedal.setImageResource(R.drawable.medallaoro);
                    if (h.bgRow != null) {
                        h.bgRow.setBackgroundResource(R.drawable.bg_card_border_oro);
                    }
                } else if (rank == 2) {
                    // Plata - segundo lugar
                    h.ivMedal.setImageResource(R.drawable.medallaplata);
                    if (h.bgRow != null) {
                        h.bgRow.setBackgroundResource(R.drawable.bg_card_border_plata);
                    }
                } else if (rank == 3) {
                    // Bronce - tercer lugar
                    h.ivMedal.setImageResource(R.drawable.medallabronce);
                    if (h.bgRow != null) {
                        h.bgRow.setBackgroundResource(R.drawable.bg_card_border_bronce);
                    }
                }
            } else {
                // Mostrar número normal para posición 4+
                // Ocultar medalla
                h.ivMedal.setVisibility(View.GONE);
                h.tvMedalNumber.setVisibility(View.GONE);
                
                // Mostrar TextView de rank normal
                h.tvRank.setVisibility(View.VISIBLE);
                h.tvRank.setText(String.valueOf(rank));
                
                // Borde blanco para posición 4+
                if (h.bgRow != null) {
                    h.bgRow.setBackgroundResource(R.drawable.bg_card_white);
                }
            }

            String nombre = (it.getNombre() == null || it.getNombre().trim().isEmpty()) ? "—" : it.getNombre();
            h.tvName.setText(nombre);

            int puntos = it.getPromedio();
            h.tvPointsRight.setText(String.valueOf(puntos));
        }

        @Override
        public int getItemCount() {
            return (data == null) ? 0 : data.size();
        }
    }
}