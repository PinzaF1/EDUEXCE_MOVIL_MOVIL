package com.example.zavira_movil.Home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.Subject;

import java.util.ArrayList;
import java.util.List;

public class SubtopicAdapter extends RecyclerView.Adapter<SubtopicAdapter.VH> {

    private final List<Subject.Subtopic> data = new ArrayList<>();
    private final String area;
    private final int nivel; // 1..5

    public SubtopicAdapter(List<Subject.Subtopic> data, String area, int nivel) {
        if (data != null) this.data.addAll(data);
        this.area = area;
        this.nivel = Math.max(1, Math.min(5, nivel));
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subtopic_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Subject.Subtopic s = data.get(position);

        // Campos mínimos (compatibles con tu modelo actual)
        h.tvTitle.setText(s.title != null ? s.title : "Subtema");

        // Estos dos son opcionales: si tu modelo aún no los tiene, mostramos por defecto.
        String hint = (getFieldOrNull(s, "hint") != null) ? (String) getFieldOrNull(s, "hint")
                : "Responde correctamente para desbloquear el siguiente";
        String status = (getFieldOrNull(s, "statusText") != null) ? (String) getFieldOrNull(s, "statusText") : "";

        h.tvHint.setText(hint);
        h.tvRight.setText(status);

        // (Opcional) Bloqueo por nivel: desbloquea los primeros `nivel`, bloquea el resto
        boolean locked = position >= nivel; // cambia esta regla si tienes otra
        applyLockedStyle(h, locked);

        h.itemView.setOnClickListener(v -> {
            if (locked) {
                Toast.makeText(v.getContext(), "Completa el subtema anterior para desbloquear este", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                Context ctx = v.getContext();
                Intent i = new Intent(ctx, QuizActivity.class);
                i.putExtra(QuizActivity.EXTRA_AREA, area);
                i.putExtra(QuizActivity.EXTRA_SUBTEMA, s.title);
                i.putExtra(QuizActivity.EXTRA_NIVEL, nivel);
                ctx.startActivity(i);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(v.getContext(), "No se pudo abrir el quiz", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvHint, tvRight;
        ImageView ivChevron;
        VH(@NonNull View v) {
            super(v);
            tvTitle   = v.findViewById(R.id.tvSubtopicTitle);
            tvHint    = v.findViewById(R.id.tvHint);
            tvRight   = v.findViewById(R.id.tvRightStatus);
            ivChevron = v.findViewById(R.id.ivChevron);
        }
    }

    // ---------- Utils ----------

    private static void applyLockedStyle(VH h, boolean locked) {
        h.itemView.setEnabled(!locked);
        h.itemView.setAlpha(locked ? 0.75f : 1f);
        h.ivChevron.setAlpha(locked ? 0.35f : 1f);
        h.tvTitle.setTextColor(locked ? 0xFF9CA3AF : 0xFF111827); // gris claro / gris oscuro
        h.tvHint.setTextColor(locked ? 0xFF9CA3AF : 0xFF6B7280);  // gris claro / gris medio
    }

    /**
     * Permite leer campos opcionales (hint / statusText) si extendiste Subject.Subtopic.
     * Si no existen, devuelve null sin romper compilación.
     */
    private static Object getFieldOrNull(Object obj, String field) {
        try {
            return obj.getClass().getField(field).get(obj);
        } catch (Exception ignored) {
            return null;
        }
    }
}
