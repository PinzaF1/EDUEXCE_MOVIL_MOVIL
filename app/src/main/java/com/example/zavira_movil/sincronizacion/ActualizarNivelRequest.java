package com.example.zavira_movil.sincronizacion;

import com.google.gson.annotations.SerializedName;

public class ActualizarNivelRequest {
    @SerializedName("area")
    private String area;

    @SerializedName("nivel")
    private int nivel;

    public ActualizarNivelRequest(String area, int nivel) {
        this.area = area;
        this.nivel = nivel;
    }

    public String getArea() {
        return area;
    }

    public int getNivel() {
        return nivel;
    }
}


