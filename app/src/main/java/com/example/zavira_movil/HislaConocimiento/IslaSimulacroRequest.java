package com.example.zavira_movil.HislaConocimiento;

import com.google.gson.annotations.SerializedName;

public class IslaSimulacroRequest {
    @SerializedName("modalidad")
    private final String modalidad; // "facil" | "dificil"

    public IslaSimulacroRequest(String modalidad) { this.modalidad = modalidad; }
    public String getModalidad() { return modalidad; }
}
