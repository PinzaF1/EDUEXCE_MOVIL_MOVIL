package com.example.zavira_movil.HislaConocimiento;

import java.util.List;

/** Item de la lista (estado UI). */
public class PregItem {
    public int orden;
    public int idPregunta;
    public String area;
    public String subtema;
    public String enunciado;
    public List<String> opciones;
    public String seleccion; // "A".."D" o null
}
