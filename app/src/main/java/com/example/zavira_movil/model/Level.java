package com.example.zavira_movil.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Level implements Serializable {
    public String name;        // "Nivel 1: ..."
    public String status = "Disponible";
    public List<Subject.Subtopic> subtopics = new ArrayList<>();

    public Level(String name) {
        this.name = name;
    }

    public int subtopicsDone() {
        int c = 0;
        for (Subject.Subtopic s: subtopics) if (s.done) c++;
        return c;
    }

    public int subtopicsPercent() {
        if (subtopics == null || subtopics.isEmpty()) return 0;
        return (int) (100.0 * subtopicsDone() / subtopics.size());
    }

    public String getTitle() {
        return name;
    }
}
