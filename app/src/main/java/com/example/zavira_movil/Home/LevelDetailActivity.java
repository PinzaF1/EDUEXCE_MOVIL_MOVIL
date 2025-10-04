package com.example.zavira_movil.Home;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zavira_movil.databinding.ActivityLevelDetailBinding;
import com.example.zavira_movil.model.Level;   // ✅ Importar el modelo externo
import com.example.zavira_movil.model.Subject;

/**
 * Muestra el detalle de un nivel (progreso + lista de subtemas).
 * Crea el SubtopicAdapter pasándole el área (subject.title) y el número de nivel (1..5).
 */
public class LevelDetailActivity extends AppCompatActivity {

    private ActivityLevelDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLevelDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Subject subject = (Subject) getIntent().getSerializableExtra("subject");
        int levelIndex = getIntent().getIntExtra("level_index", -1);

        if (subject == null || subject.levels == null || subject.levels.isEmpty() ||
                levelIndex < 0 || levelIndex >= subject.levels.size()) {
            finish();
            return;
        }

        // ✅ Usar el modelo externo Level
        Level lvl = subject.levels.get(levelIndex);

        // Header a color por materia
        if (subject.headerDrawableRes != 0) {
            binding.header.setBackgroundResource(subject.headerDrawableRes);
        }
        binding.tvSubject.setText(subject.title);
        binding.tvLevel.setText(lvl.name);
        binding.tvStatus.setText(lvl.status);

        // Progreso por subtemas
        int percent = lvl.subtopicsPercent();
        binding.progress.setProgress(percent);
        int total = (lvl.subtopics == null) ? 0 : lvl.subtopics.size();
        binding.tvProgress.setText(lvl.subtopicsDone() + "/" + total + " subtemas (" + percent + "%)");

        // Lista de subtemas -> al tocar abre QuizActivity con área + subtema + nivel
        binding.rvSubtopics.setLayoutManager(new LinearLayoutManager(this));
        int nivelNumero = levelIndex + 1; // nivel 1..5
        binding.rvSubtopics.setAdapter(new SubtopicAdapter(lvl.subtopics, subject.title, nivelNumero));
    }
}
