package com.example.zavira_movil.progreso;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;

import java.util.ArrayList;
import java.util.List;

public class NivelesAdapter extends RecyclerView.Adapter<NivelesAdapter.VH> {

    private final List<Nivel> data = new ArrayList<>();

    // % global (0–100) que viene del anillo
    private int globalProgress = -1; // -1 = fallback a n.getValor()

    /** Llama esto antes de setData(...) */
    public void setGlobalProgress(int p) {
        if (p < 0) p = 0; if (p > 100) p = 100;
        this.globalProgress = p;
        notifyDataSetChanged();
    }

    public void setData(List<Nivel> niveles) {
        data.clear();
        if (niveles != null) data.addAll(niveles);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nivel, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Nivel n = data.get(pos);

        h.tvNombre.setText(n.getNombre());
        h.tvRango.setText(n.getRango());

        int min = n.getMin();
        int max = n.getMax();

        int gp = (globalProgress >= 0) ? globalProgress : n.getValor();
        if (gp < 0) gp = 0; if (gp > 100) gp = 100;

        boolean future  = gp <  min;
        boolean current = gp >= min && gp < max;
        boolean passed  = gp >= max;

        int progress;
        if (future)       progress = 0;     // aún no llega → 0%
        else if (passed)  progress = 100;   // superado → 100%
        else              progress = Math.round(100f * (gp - min) / (max - min)); // dentro del rango

        h.pb.setMax(100);
        h.pb.setProgress(progress);
        h.ivStar.setVisibility(current ? View.VISIBLE : View.INVISIBLE);

        // Colores por nivel
        int activeColor, dimColor;
        switch (n.getNombre()) {
            case "Básico":
                activeColor = ContextCompat.getColor(h.itemView.getContext(), R.color.level_basic);
                dimColor    = ContextCompat.getColor(h.itemView.getContext(), R.color.level_basic_dim);
                break;
            case "Intermedio":
                activeColor = ContextCompat.getColor(h.itemView.getContext(), R.color.level_intermediate);
                dimColor    = ContextCompat.getColor(h.itemView.getContext(), R.color.level_intermediate_dim);
                break;
            case "Avanzado":
                activeColor = ContextCompat.getColor(h.itemView.getContext(), R.color.level_advanced);
                dimColor    = ContextCompat.getColor(h.itemView.getContext(), R.color.level_advanced_dim);
                break;
            default: // Experto
                activeColor = ContextCompat.getColor(h.itemView.getContext(), R.color.level_expert);
                dimColor    = ContextCompat.getColor(h.itemView.getContext(), R.color.level_expert_dim);
                break;
        }

        // Dot a la izquierda: color del nivel (si quieres opacarlo cambia a dimColor cuando future)
        Drawable dot = h.dotLeft.getBackground();
        if (dot != null) {
            dot = dot.mutate();
            dot.setTint(future ? dimColor : activeColor);
            h.dotLeft.setBackground(dot);
        }

        // Barra: track gris SIEMPRE + progreso con color solo si alcanzado (current/passed)
        Drawable d = AppCompatResources.getDrawable(h.itemView.getContext(), R.drawable.progress_rounded);
        if (d != null) {
            d = d.mutate();
            int track = ContextCompat.getColor(h.itemView.getContext(), R.color.progress_track);

            if (d instanceof LayerDrawable) {
                LayerDrawable ld = (LayerDrawable) d;
                Drawable bg = ld.findDrawableByLayerId(android.R.id.background);
                Drawable pr = ld.findDrawableByLayerId(android.R.id.progress);

                if (bg != null) {
                    bg = bg.mutate();
                    DrawableCompat.setTint(bg, track); // fondo gris
                }
                if (pr != null) {
                    pr = pr.mutate();
                    DrawableCompat.setTint(pr, (future ? dimColor : activeColor)); // progreso
                }
                h.pb.setProgressDrawable(ld);
            } else {
                // Fallback: al menos deja el color correcto en progreso
                Drawable wrapped = DrawableCompat.wrap(d);
                DrawableCompat.setTint(wrapped, (future ? dimColor : activeColor));
                h.pb.setProgressDrawable(wrapped);
            }
        }
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvNombre, tvRango;
        ProgressBar pb;
        ImageView ivStar;
        View dotLeft;
        VH(@NonNull View v) {
            super(v);
            tvNombre = v.findViewById(R.id.tvNivelNombre);
            tvRango  = v.findViewById(R.id.tvNivelRango);
            pb       = v.findViewById(R.id.pbNivel);
            ivStar   = v.findViewById(R.id.ivNivelStar);
            dotLeft  = v.findViewById(R.id.dotLeft);
        }
    }
}
