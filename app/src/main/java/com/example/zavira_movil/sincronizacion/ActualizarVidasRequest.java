package com.example.zavira_movil.sincronizacion;

import com.google.gson.annotations.SerializedName;

public class ActualizarVidasRequest {
    @SerializedName("area")
    private String area;

    @SerializedName("nivel")
    private int nivel;

    @SerializedName("vidas")
    private int vidas;

    public ActualizarVidasRequest(String area, int nivel, int vidas) {
        this.area = area;
        this.nivel = nivel;
        this.vidas = vidas;
    }

    public String getArea() {
        return area;
    }

    public int getNivel() {
        return nivel;
    }

    public int getVidas() {
        return vidas;
    }
}


