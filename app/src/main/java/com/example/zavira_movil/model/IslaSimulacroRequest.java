package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class IslaSimulacroRequest {
    @SerializedName("modalidad")
    public String modalidad;

    public IslaSimulacroRequest(String modalidad) {
        this.modalidad = modalidad;
    }
}
