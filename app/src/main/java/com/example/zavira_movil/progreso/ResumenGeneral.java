package com.example.zavira_movil.progreso;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ResumenGeneral {

    @SerializedName("progresoGlobal")
    private int progresoGlobal;

    @SerializedName("nivelActual")
    private String nivelActual;

    @SerializedName("niveles")
    private List<Nivel> niveles;

    // --- si tenías más campos, déjalos como estaban ---

    public int getProgresoGlobal() { return progresoGlobal; }
    public String getNivelActual() { return nivelActual; }
    public List<Nivel> getNiveles() { return niveles; }
}
