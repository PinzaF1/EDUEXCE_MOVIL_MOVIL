package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class MateriaDetalle {
    @SerializedName("nombre")
    private String nombre;

    @SerializedName("progreso")
    private int progreso;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getProgreso() {
        return progreso;
    }

    public void setProgreso(int progreso) {
        this.progreso = progreso;
    }
}
