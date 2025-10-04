package com.example.zavira_movil.adapter;

import android.graphics.drawable.Drawable;
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
import com.example.zavira_movil.model.Nivel;

import java.util.ArrayList;
import java.util.List;

public class NivelesAdapter extends RecyclerView.Adapter<NivelesAdapter.VH> {

    private final List<Nivel> data = new ArrayList<>();

    public void setData(List<Nivel> niveles) {
        data.clear();
        if (niveles != null) data.addAll(niveles);  // pinta TODO lo que llega (los 4)
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nivel, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Nivel n = data.get(pos);

        // textos
        h.tvNombre.setText(n.getNombre());
        h.tvRango.setText(n.getRango());

        // progreso relativo al rango del nivel
        int min = n.getMin(), max = n.getMax(), valor = n.getValor();
        int progress = 0;
        if (max > min) {
            int clamped = Math.max(min, Math.min(valor, max));
            progress = Math.round(100f * (clamped - min) / (max - min));
        }
        h.pb.setMax(100);
        h.pb.setProgress(progress);

        // nivel actual -> estrella visible
        final boolean esActual = n.isActual();
        h.ivStar.setVisibility(esActual ? View.VISIBLE : View.INVISIBLE);

        // ===== Color por nivel (activo fuerte, inactivo opaco) =====
        int colorFinal;
        switch (n.getNombre()) {
            case "Básico":
                colorFinal = ContextCompat.getColor(
                        h.itemView.getContext(),
                        esActual ? R.color.level_basic : R.color.level_basic_dim
                );
                break;
            case "Intermedio":
                colorFinal = ContextCompat.getColor(
                        h.itemView.getContext(),
                        esActual ? R.color.level_intermediate : R.color.level_intermediate_dim
                );
                break;
            case "Avanzado":
                colorFinal = ContextCompat.getColor(
                        h.itemView.getContext(),
                        esActual ? R.color.level_advanced : R.color.level_advanced_dim
                );
                break;
            default: // "Experto"
                colorFinal = ContextCompat.getColor(
                        h.itemView.getContext(),
                        esActual ? R.color.level_expert : R.color.level_expert_dim
                );
                break;
        }

        // Punto (dotLeft) tintado
        Drawable dot = h.dotLeft.getBackground();
        if (dot != null) {
            dot = dot.mutate();
            dot.setTint(colorFinal);
            h.dotLeft.setBackground(dot);
        }

        // Barra de progreso tintada sobre tu drawable redondeado
        Drawable base = AppCompatResources.getDrawable(h.itemView.getContext(), R.drawable.progress_rounded);
        if (base != null) {
            Drawable wrapped = DrawableCompat.wrap(base.mutate());
            DrawableCompat.setTint(wrapped, colorFinal);
            h.pb.setProgressDrawable(wrapped);
        }
        // ===========================================================
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvNombre, tvRango;
        ProgressBar pb;
        ImageView ivStar;
        View dotLeft; // puntito a la izquierda

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
