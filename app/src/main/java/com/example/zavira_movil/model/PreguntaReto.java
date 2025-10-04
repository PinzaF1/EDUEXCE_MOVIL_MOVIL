package com.example.zavira_movil.model;

import java.util.List;

public class PreguntaReto {
    private String id_pregunta;
    private String area;
    private String subtema;
    private String dificultad;
    private String estilo_kolb;
    private String pregunta;
    private List<Opcion> opciones;

    public String getArea() { return area; }
    public String getSubtema() { return subtema; }
    public String getDificultad() { return dificultad; }
    public String getEstilo_kolb() { return estilo_kolb; }
    public String getPregunta() { return pregunta; }
    public List<Opcion> getOpciones() { return opciones; }
}
