package com.example.zavira_movil.resetpassword;

import com.google.gson.annotations.SerializedName;

public class VerificarCodigoRequest {
    @SerializedName("correo")
    private String correo;

    @SerializedName("codigo")
    private String codigo;

    public VerificarCodigoRequest(String correo, String codigo) {
        this.correo = correo;
        this.codigo = codigo;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}
