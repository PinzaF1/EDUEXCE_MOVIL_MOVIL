package com.example.zavira_movil.model;

public class ParadaRequest {
    public String area;
    public String subtema;
    public int nivel_orden;
    public boolean usa_estilo_kolb;
    public Integer intento_actual;

    public ParadaRequest(String area, String subtema, int nivel_orden, boolean usa_estilo_kolb, Integer intento_actual) {
        this.area = area;
        this.subtema = subtema;
        this.nivel_orden = nivel_orden;
        this.usa_estilo_kolb = usa_estilo_kolb;
        this.intento_actual = intento_actual;
    }
}
