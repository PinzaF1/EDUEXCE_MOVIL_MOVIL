package com.example.zavira_movil.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Question implements Serializable {
    public String id_pregunta; // opcional
    public String area;
    public String subtema;
    public String dificultad;
    public String enunciado;
    public List<Option> opciones = new ArrayList<>();

    public void addOption(String key, String text) {
        Option o = new Option();
        o.key = key; o.text = text;
        opciones.add(o);
    }

    public static class Option implements Serializable {
        public String key;
        public String text;
    }
}
