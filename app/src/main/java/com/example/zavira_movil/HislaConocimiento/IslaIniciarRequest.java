package com.example.zavira_movil.HislaConocimiento;

/** Body para iniciar simulacro: {"modalidad":"facil"|"dificil"} */
public class IslaIniciarRequest {
    private String modalidad;

    public IslaIniciarRequest(String modalidad) {
        this.modalidad = modalidad;
    }

    public String getModalidad() {
        return modalidad;
    }
}
