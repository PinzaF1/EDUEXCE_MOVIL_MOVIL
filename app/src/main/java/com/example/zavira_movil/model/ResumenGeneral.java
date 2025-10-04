package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class ResumenGeneral {
    @SerializedName(value = "progresoGeneral", alternate = {"progreso_general", "progreso"})
    private int progresoGeneral;

    public int getProgresoGeneral() { return progresoGeneral; }
    public void setProgresoGeneral(int progresoGeneral) { this.progresoGeneral = progresoGeneral; }
}
