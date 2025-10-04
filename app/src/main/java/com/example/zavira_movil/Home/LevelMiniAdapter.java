package com.example.zavira_movil.Home;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.local.ProgressLockManager;
import com.example.zavira_movil.local.UserSession;
import com.example.zavira_movil.model.Level;
import com.example.zavira_movil.model.Subject;

import java.util.List;

/**
 * Adapter para mostrar los niveles dentro de un Subject (ej: Matemáticas).
 * Incluye los 5 niveles normales + el Examen Final (25 preguntas).
 */
public class LevelMiniAdapter extends RecyclerView.Adapter<LevelMiniAdapter.Holder> {

    private final List<Level> niveles;
    private final Subject subject;
    private final SubjectAdapter.OnStartActivity launcher;

    public LevelMiniAdapter(List<Level> niveles, Subject subject, SubjectAdapter.OnStartActivity launcher) {
        this.niveles = niveles;
        this.subject = subject;
        this.launcher = launcher;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_level, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        String userId = String.valueOf(UserSession.getInstance().getIdUsuario());

        if (position < niveles.size()) {
            // ---------------- Niveles normales ----------------
            Level nivel = niveles.get(position);
            int nivelNumero = position + 1;

            h.txtLevel.setText(nivel.getTitle() != null ? nivel.getTitle() : "Nivel " + nivelNumero);

            boolean enabled = ProgressLockManager.isLevelUnlocked(
                    h.itemView.getContext(),
                    userId,
                    subject.title,
                    nivelNumero
            );

            h.itemView.setEnabled(enabled);
            h.itemView.setAlpha(enabled ? 1f : 0.5f);

            h.itemView.setOnClickListener(v -> {
                if (!enabled || launcher == null) return;

                Intent i = new Intent(v.getContext(), QuizActivity.class);
                i.putExtra(QuizActivity.EXTRA_AREA, subject.title);
                String subtema = (nivel.subtopics != null && !nivel.subtopics.isEmpty())
                        ? nivel.subtopics.get(0).title
                        : "";
                i.putExtra(QuizActivity.EXTRA_SUBTEMA, subtema);
                i.putExtra(QuizActivity.EXTRA_NIVEL, nivelNumero);
                launcher.launch(i);
            });

        } else {
            // ---------------- Examen Final (25 preguntas) ----------------
            h.txtLevel.setText("Examen Final");

            boolean unlocked = ProgressLockManager.getUnlockedLevel(
                    h.itemView.getContext(),
                    userId,
                    subject.title
            ) >= 5; // Se desbloquea al terminar nivel 5

            h.itemView.setEnabled(unlocked);
            h.itemView.setAlpha(unlocked ? 1f : 0.5f);

            h.itemView.setOnClickListener(v -> {
                if (!unlocked || launcher == null) return;
                Intent i = new Intent(v.getContext(), SimulacroActivity.class);
                i.putExtra("area", subject.title);
                launcher.launch(i);
            });
        }
    }

    @Override
    public int getItemCount() {
        // 5 niveles + 1 examen final
        return (niveles == null ? 0 : niveles.size()) + 1;
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView txtLevel;

        public Holder(@NonNull View itemView) {
            super(itemView);
            txtLevel = itemView.findViewById(R.id.txtLevel);
        }
    }
}
