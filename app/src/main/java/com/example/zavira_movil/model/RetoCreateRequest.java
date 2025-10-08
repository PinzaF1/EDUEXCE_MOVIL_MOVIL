package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class RetoCreateRequest {
    private int cantidad;
    private String area;

    @SerializedName("oponente_id")
    private int oponenteId;

    public RetoCreateRequest(int cantidad, String area, int oponenteId) {
        this.cantidad = cantidad;
        this.area = area;
        this.oponenteId = oponenteId;
    }
}
