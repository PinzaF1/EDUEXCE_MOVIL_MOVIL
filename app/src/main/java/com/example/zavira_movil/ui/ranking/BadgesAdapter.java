package com.example.zavira_movil.ui.ranking;

import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.LogrosResponse;

import java.util.ArrayList;
import java.util.List;

public class BadgesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static class Row {
        public static final int TYPE_HEADER = 0;
        public static final int TYPE_ITEM = 1;

        int type;
        String header;
        LogrosResponse.Badge badge;
        boolean obtenida;

        static Row header(String h) {
            Row r = new Row(); r.type = TYPE_HEADER; r.header = h; return r;
        }
        static Row item(LogrosResponse.Badge b, boolean o) {
            Row r = new Row(); r.type = TYPE_ITEM; r.badge = b; r.obtenida = o; return r;
        }
    }

    private final List<Row> rows = new ArrayList<>();

    public void setData(List<Row> data) {
        rows.clear();
        if (data != null) rows.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) { return rows.get(position).type; }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == Row.TYPE_HEADER)
            return new HeaderVH(inf.inflate(R.layout.item_badge_header, parent, false));
        else
            return new ItemVH(inf.inflate(R.layout.item_badge, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        Row row = rows.get(position);
        if (row.type == Row.TYPE_HEADER)
            ((HeaderVH) h).bind(row.header);
        else
            ((ItemVH) h).bind(row.badge, row.obtenida);
    }

    @Override
    public int getItemCount() { return rows.size(); }

    // Header
    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tvHeader;
        HeaderVH(@NonNull View v) { super(v); tvHeader = v.findViewById(R.id.tvHeader); }
        void bind(String title) { tvHeader.setText(title); }
    }

    // Item
    static class ItemVH extends RecyclerView.ViewHolder {
        View root;
        TextView tvNombre, tvDescripcion, tvArea, tvEstado;
        ImageView ivIcon;

        ItemVH(@NonNull View v) {
            super(v);
            root = v.findViewById(R.id.rootBadge);
            tvNombre = v.findViewById(R.id.tvNombre);
            tvDescripcion = v.findViewById(R.id.tvDescripcion);
            tvArea = v.findViewById(R.id.tvArea);
            tvEstado = v.findViewById(R.id.tvEstado);
            ivIcon = v.findViewById(R.id.ivIcon);
        }

        void bind(LogrosResponse.Badge b, boolean obtenida) {
            tvNombre.setText(b.getNombre());
            tvDescripcion.setText(b.getDescripcion());
            if (tvArea != null) {
                tvArea.setText(b.getArea());
            }

            // Asigna iconos y colores de borde por área
            String area = b.getArea() != null ? b.getArea().toLowerCase().trim() : "";
            int borderDrawable;
            
            if (obtenida) {
                // Si está obtenida, usar el color según el área
                if (area.contains("leng") || area.contains("lectura")) {
                    ivIcon.setImageResource(R.drawable.insignialectura);
                    borderDrawable = R.drawable.bg_badge_card_lenguaje;
                } else if (area.contains("ciencia")) {
                    ivIcon.setImageResource(R.drawable.insigniaciencia);
                    borderDrawable = R.drawable.bg_badge_card_ciencias;
                } else if (area.contains("social")) {
                    ivIcon.setImageResource(R.drawable.insigniasociales);
                    borderDrawable = R.drawable.bg_badge_card_sociales;
                } else if (area.contains("matem")) {
                    ivIcon.setImageResource(R.drawable.insigniamatematicas);
                    borderDrawable = R.drawable.bg_badge_card_matematicas;
                } else if (area.contains("ingl")) {
                    ivIcon.setImageResource(R.drawable.insigniaingles);
                    borderDrawable = R.drawable.bg_badge_card_ingles;
                } else if (area.contains("conocimiento") || area.contains("conocim")) {
                    ivIcon.setImageResource(R.drawable.insigniaconocimiento);
                    borderDrawable = R.drawable.bg_badge_card_lenguaje;
                } else {
                    ivIcon.setVisibility(View.GONE);
                    borderDrawable = R.drawable.bg_badge_card_gris;
                }
                // Quitar filtro de color cuando está obtenida (mostrar colores originales)
                ivIcon.clearColorFilter();
            } else {
                // Si está pendiente, usar gris para borde y fondo
                if (area.contains("leng") || area.contains("lectura")) {
                    ivIcon.setImageResource(R.drawable.insignialectura);
                } else if (area.contains("ciencia")) {
                    ivIcon.setImageResource(R.drawable.insigniaciencia);
                } else if (area.contains("social")) {
                    ivIcon.setImageResource(R.drawable.insigniasociales);
                } else if (area.contains("matem")) {
                    ivIcon.setImageResource(R.drawable.insigniamatematicas);
                } else if (area.contains("ingl")) {
                    ivIcon.setImageResource(R.drawable.insigniaingles);
                } else if (area.contains("conocimiento") || area.contains("conocim")) {
                    ivIcon.setImageResource(R.drawable.insigniaconocimiento);
                } else {
                    ivIcon.setVisibility(View.GONE);
                }
                borderDrawable = R.drawable.bg_badge_card_gris;
                // Aplicar filtro gris a la imagen cuando está pendiente (convertir a escala de grises)
                ColorMatrix colorMatrix = new ColorMatrix();
                colorMatrix.setSaturation(0); // Convertir a escala de grises
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
                ivIcon.setColorFilter(filter);
            }

            // Aplicar borde y fondo según el estado
            root.setBackgroundResource(borderDrawable);

            // Estado
            if (obtenida) {
                tvEstado.setText("✅ Obtenida");
                tvEstado.setTextColor(Color.parseColor("#2E7D32"));
            } else {
                tvEstado.setText("Pendiente");
                tvEstado.setTextColor(Color.parseColor("#9CA3AF"));
            }
        }
    }
}
