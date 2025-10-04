package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class ProgresoMateria {
    @SerializedName(value = "nombre", alternate = {"area", "materia"})
    private String nombre;

    @SerializedName(value = "progreso", alternate = {"porcentaje"})
    private int progreso;

    public String getNombre() { return nombre; }
    public int getProgreso() { return progreso; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setProgreso(int progreso) { this.progreso = progreso; }
}
