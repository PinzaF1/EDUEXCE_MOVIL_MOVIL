package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class IslaSimulacroResponse {
    @SerializedName("sesion")
    public Sesion sesion;

    @SerializedName("preguntas")
    public List<Question> preguntas;

    public static class Sesion {
        @SerializedName("id_sesion")
        public int idSesion;

        @SerializedName("modalidad")
        public String modalidad;

        @SerializedName("total")
        public int total;
    }
}
