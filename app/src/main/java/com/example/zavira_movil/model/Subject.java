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

    // ✅ Ahora apunta al modelo Level externo
    public List<Level> levels = new ArrayList<>();
    public List<Subtopic> subtopics;

    public int percent() {
        if (total <= 0) return 0;
        return Math.max(0, Math.min(100, (int) (100.0 * done / total)));
    }

    public static class Subtopic implements Serializable {
        public String title;
        public boolean done = false;
        public Subtopic(String title) { this.title = title; }
    }
}
