package com.example.zavira_movil.HislaConocimiento;

import com.google.gson.annotations.SerializedName;

/**
 * Request para iniciar simulacro en isla.
 * Ejemplo enviado: { "modalidad": "facil" }  // o "dificil"
 */
public class IslaSimulacroRequest {

    @SerializedName("modalidad")
    private final String modalidad;

    public IslaSimulacroRequest(String modalidad) {
        this.modalidad = modalidad;
    }

    public String getModalidad() {
        return modalidad;
    }
}
