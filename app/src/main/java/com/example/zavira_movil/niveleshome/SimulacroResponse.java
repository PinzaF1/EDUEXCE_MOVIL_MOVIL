package com.example.zavira_movil.niveleshome;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SimulacroResponse {

    @SerializedName("sesion")
    public Sesion sesion;

    @SerializedName("preguntas")
    public List<ApiQuestion> preguntas;

    public static class Sesion {
        @SerializedName(value = "idSesion", alternate = {"id_sesion"})
        public String idSesion;
    }
}
