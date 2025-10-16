package com.example.zavira_movil.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Subject implements Serializable {
    public String title;
    public int iconRes;
    public int headerDrawableRes;

    // progreso general del área (opcional)
    public int done = 0;
    public int total = 5; // 5 niveles

    // Modelo de niveles (si lo usas)
    public List<Level> levels = new ArrayList<>();

    // Subtemas (para la lista expandida)
    public List<Subtopic> subtopics = new ArrayList<>();

    public int percent() {
        if (total <= 0) return 0;
        return Math.max(0, Math.min(100, (int) (100.0 * done / total)));
    }

    // ✅ Usa el modelo anidado (extendido) para el adapter de subtemas
    public static class Subtopic implements Serializable {
        public String title;        // "Operaciones básicas"
        public String hint;         // "Responde correctamente para desbloquear el siguiente"
        public String statusText;   // "0%", "Listo", o "" si no aplica
        public boolean locked;      // bloqueado/desbloqueado
        public boolean done;        // si quieres marcar completado

        public Subtopic(String title) {
            this(title, "", "", false, false);
        }

        public Subtopic(String title, String hint, String statusText, boolean locked, boolean done) {
            this.title = title;
            this.hint = hint;
            this.statusText = statusText;
            this.locked = locked;
            this.done = done;
        }
    }
}
