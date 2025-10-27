package com.example.zavira_movil.resetpassword;

import com.google.gson.annotations.SerializedName;

public class RestablecerPasswordRequest {
    @SerializedName("correo")
    private String correo;

    @SerializedName("codigo")
    private String codigo;

    @SerializedName("nueva_password")
    private String nuevaPassword;

    public RestablecerPasswordRequest(String correo, String codigo, String nuevaPassword) {
        this.correo = correo;
        this.codigo = codigo;
        this.nuevaPassword = nuevaPassword;
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

    public String getNuevaPassword() {
        return nuevaPassword;
    }

    public void setNuevaPassword(String nuevaPassword) {
        this.nuevaPassword = nuevaPassword;
    }
}
