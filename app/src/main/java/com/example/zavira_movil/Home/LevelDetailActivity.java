package com.example.zavira_movil.Home;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zavira_movil.databinding.ActivityLevelDetailBinding;
import com.example.zavira_movil.model.Level;   // ✅ Importar el modelo externo
import com.example.zavira_movil.model.Subject;
import com.example.zavira_movil.niveleshome.SubtopicAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;

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

    public static class IslaSimulacroRequest {
        private String modalidad;

        public IslaSimulacroRequest(String modalidad) { this.modalidad = modalidad; }
        public String getModalidad() { return modalidad; }
        public void setModalidad(String modalidad) { this.modalidad = modalidad; }
    }

    /** DTO EXACTO para /movil/isla/simulacro */
    public static class IslaSimulacroResponse {

        @SerializedName("sesion")
        private Sesion sesion;

        @SerializedName("totalPreguntas")
        private Integer totalPreguntas;

        @SerializedName("preguntas")
        private List<PreguntaDto> preguntas;

        // ---- Getters ----
        public Sesion getSesion() { return sesion; }
        public Integer getTotalPreguntas() { return totalPreguntas; }
        public List<PreguntaDto> getPreguntas() { return preguntas; }

        // ================== Tipos anidados ==================
        public static class Sesion {
            @SerializedName("idSesion") private String idSesion;
            @SerializedName("idUsuario") private String idUsuario;
            @SerializedName("tipo") private String tipo;
            @SerializedName("area") private String area;
            @SerializedName("subtema") private String subtema;
            @SerializedName("nivelOrden") private Integer nivelOrden;
            @SerializedName("modo") private String modo;
            @SerializedName("usaEstiloKolb") private Boolean usaEstiloKolb;
            @SerializedName("inicioAt") private String inicioAt;
            @SerializedName("totalPreguntas") private Integer totalPreguntas;

            public String getIdSesion() { return idSesion; }
            public String getIdUsuario() { return idUsuario; }
            public String getTipo() { return tipo; }
            public String getArea() { return area; }
            public String getSubtema() { return subtema; }
            public Integer getNivelOrden() { return nivelOrden; }
            public String getModo() { return modo; }
            public Boolean getUsaEstiloKolb() { return usaEstiloKolb; }
            public String getInicioAt() { return inicioAt; }
            public Integer getTotalPreguntas() { return totalPreguntas; }
        }

        public static class PreguntaDto {
            @SerializedName("id_pregunta") private String idPregunta;
            @SerializedName("area") private String area;
            @SerializedName("subtema") private String subtema;
            @SerializedName("enunciado") private String enunciado;

            // OJO: el backend envía arreglo de STRINGS
            @SerializedName("opciones") private List<String> opciones;

            public String getIdPregunta() { return idPregunta; }
            public String getArea() { return area; }
            public String getSubtema() { return subtema; }
            public String getEnunciado() { return enunciado; }
            public List<String> getOpciones() { return opciones; }
        }
    }
}
