package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class IslaCerrarResponse {
    @SerializedName("id_sesion")
    public int idSesion;

    @SerializedName("resultado_base")
    public Resultado resultadoBase;

    public static class Resultado {
        @SerializedName("aprueba")
        public boolean aprueba;

        @SerializedName("correctas")
        public int correctas;

        @SerializedName("puntaje")
        public int puntaje;
    }
}
