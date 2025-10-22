package com.example.zavira_movil.niveleshome;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.databinding.ActivitySubjectDetailBinding;
import com.example.zavira_movil.local.UserSession;
import com.example.zavira_movil.model.Level;
import com.example.zavira_movil.model.Subject;

import java.util.ArrayList;
import java.util.List;

public class SubjectDetailActivity extends AppCompatActivity {

    private ActivitySubjectDetailBinding binding;
    private LevelAdapter adapter;
    private Subject subject;

    private ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySubjectDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        subject = (Subject) getIntent().getSerializableExtra("subject");
        if (subject == null) { finish(); return; }

        binding.tvSubjectTitle.setText(subject.title);
        binding.rvLevels.setLayoutManager(new LinearLayoutManager(this));

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && adapter != null) {
                        adapter.notifyDataSetChanged(); // refresca niveles y examen final
                    }
                }
        );

        adapter = new LevelAdapter(subject.levels, subject, intent -> launcher.launch(intent));
        binding.rvLevels.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    /** Lista de niveles 1..5 + Examen Final */
    static class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.VH> {
        private final List<Level> levels;
        private final Subject subject;
        private final OnStartActivity onStartActivity;

        interface OnStartActivity {
            void launch(Intent i);
        }

        LevelAdapter(List<Level> levels, Subject subject, OnStartActivity onStartActivity) {
            this.levels = levels;
            this.subject = subject;
            this.onStartActivity = onStartActivity;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_level_row, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            if (pos < levels.size()) {
                // ---------------- Niveles 1..5 ----------------
                Level l = levels.get(pos);
                int nivelNumero = pos + 1;

                h.tvName.setText("Nivel " + nivelNumero);
                String sub = (l.subtopics != null && !l.subtopics.isEmpty())
                        ? l.subtopics.get(0).title
                        : "—";
                h.tvSubtopic.setText(sub);

                String userId = String.valueOf(UserSession.getInstance().getIdUsuario());
                boolean unlocked = ProgressLockManager.isLevelUnlocked(
                        h.itemView.getContext(), userId, subject.title, nivelNumero);

                h.btnStart.setEnabled(unlocked);
                h.btnStart.setAlpha(unlocked ? 1f : 0.5f);
                h.btnStart.setText(unlocked ? "Comenzar" : "Bloqueado");

                h.btnStart.setOnClickListener(v -> {
                    if (!unlocked) return;
                    Intent i = new Intent(v.getContext(), QuizActivity.class);
                    i.putExtra(QuizActivity.EXTRA_AREA, subject.title);
                    i.putExtra(QuizActivity.EXTRA_SUBTEMA, sub);
                    i.putExtra(QuizActivity.EXTRA_NIVEL, nivelNumero);
                    onStartActivity.launch(i);
                });

            } else {
                // ---------------- Examen Final (Simulacro) ----------------
                h.tvName.setText("Examen Final");
                h.tvSubtopic.setText("Simulacro de " + subject.title);

                String userId = String.valueOf(UserSession.getInstance().getIdUsuario());
                boolean unlockedExam = ProgressLockManager.isFinalExamUnlocked(
                        h.itemView.getContext(), userId, subject.title);

                h.btnStart.setEnabled(unlockedExam);
                h.btnStart.setAlpha(unlockedExam ? 1f : 0.5f);
                h.btnStart.setText(unlockedExam ? "Comenzar" : "Bloqueado");

                h.btnStart.setOnClickListener(v -> {
                    if (!unlockedExam) return;

                    ArrayList<String> subs = new ArrayList<>();
                    if (subject.levels != null) {
                        for (Level L : subject.levels) {
                            String st = (L.subtopics != null && !L.subtopics.isEmpty())
                                    ? L.subtopics.get(0).title : null;
                            if (st != null && !st.trim().isEmpty()) subs.add(st);
                        }
                    }

                    Intent i = new Intent(v.getContext(), SimulacroActivity.class);
                    i.putExtra("area", subject.title);
                    i.putStringArrayListExtra("subtemas", subs);
                    onStartActivity.launch(i);
                });
            }
        }

        @Override public int getItemCount() {
            return (levels == null ? 0 : levels.size()) + 1; // + Examen Final
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvSubtopic;
            com.google.android.material.button.MaterialButton btnStart;
            VH(@NonNull View v) {
                super(v);
                tvName = v.findViewById(R.id.tvLevelName);
                tvSubtopic = v.findViewById(R.id.tvLevelSubtopic);
                btnStart = v.findViewById(R.id.btnStart);
            }
        }
    }
}
