package com.example.zavira_movil.HislaConocimiento;

import com.google.gson.annotations.SerializedName;

public class OpcionIslas {
    @SerializedName("letra") public String letra;
    @SerializedName("texto") public String texto;

    public OpcionIslas() {}
    public OpcionIslas(String letra, String texto) {
        this.letra = letra;
        this.texto = texto;
    }
}
