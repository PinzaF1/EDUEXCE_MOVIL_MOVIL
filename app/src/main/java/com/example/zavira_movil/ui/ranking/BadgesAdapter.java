package com.example.zavira_movil.ui.ranking;

import android.graphics.Color;
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

    // Fila genérica con 2 tipos: header e item
    public static class Row {
        public static final int TYPE_HEADER = 0;
        public static final int TYPE_ITEM   = 1;

        int type;
        String header;
        LogrosResponse.Badge badge;
        boolean obtenida;

        static Row header(String h) { Row r = new Row(); r.type = TYPE_HEADER; r.header = h; return r; }
        static Row item(LogrosResponse.Badge b, boolean o) { Row r = new Row(); r.type = TYPE_ITEM; r.badge = b; r.obtenida = o; return r; }
    }

    private final List<Row> rows = new ArrayList<>();

    public void setData(List<Row> data) {
        rows.clear();
        if (data != null) rows.addAll(data);
        notifyDataSetChanged();
    }

    @Override public int getItemViewType(int position) { return rows.get(position).type; }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == Row.TYPE_HEADER) {
            return new HeaderVH(inf.inflate(R.layout.item_badge_header, parent, false));
        } else {
            return new ItemVH(inf.inflate(R.layout.item_badge, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        Row row = rows.get(position);
        if (row.type == Row.TYPE_HEADER) {
            ((HeaderVH) h).bind(row.header);
        } else {
            ((ItemVH) h).bind(row.badge, row.obtenida);
        }
    }

    @Override public int getItemCount() { return rows.size(); }

    /* ---- ViewHolders ---- */
    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tvHeader;
        HeaderVH(@NonNull View v) { super(v); tvHeader = v.findViewById(R.id.tvHeader); }
        void bind(String title) { tvHeader.setText(title); }
    }

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
            tvArea.setText(b.getArea());

            if (obtenida) {
                root.setBackgroundResource(R.drawable.bg_badge_obtenida);
                tvEstado.setText("✅ Obtenida");
                tvEstado.setTextColor(Color.parseColor("#2E7D32"));
            } else {
                root.setBackgroundResource(R.drawable.bg_badge_pendiente);
                tvEstado.setText("Pendiente");
                tvEstado.setTextColor(Color.parseColor("#9CA3AF"));
            }
        }
    }
}
