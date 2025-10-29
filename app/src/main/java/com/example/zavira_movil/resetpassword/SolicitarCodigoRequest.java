package com.example.zavira_movil.resetpassword;

import com.google.gson.annotations.SerializedName;

public class SolicitarCodigoRequest {
    @SerializedName("correo")
    private String correo;

    public SolicitarCodigoRequest(String correo) {
        this.correo = correo;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}
