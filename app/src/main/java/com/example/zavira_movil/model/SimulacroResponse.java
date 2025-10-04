package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SimulacroResponse {
    @SerializedName("sesion")
    public Sesion sesion;

    @SerializedName("totalPreguntas")
    public int totalPreguntas;

    @SerializedName("preguntas")
    public List<Question> preguntas;

    public static class Sesion {
        @SerializedName("idSesion")   // mapea exactamente al JSON
        public Integer idSesion;

        @SerializedName("area")
        public String area;
    }
}
