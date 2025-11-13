// ParadaResponse.java
package com.example.zavira_movil.niveleshome;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class ParadaResponse implements Serializable {

    @SerializedName("preguntas")
    public List<ApiQuestion> preguntas;

    @SerializedName("preguntasPorSubtema")
    public List<ApiQuestion> preguntasPorSubtema;

    @SerializedName("sesion")
    public Sesion sesion;

    public static class Sesion implements Serializable {
        @SerializedName("idSesion") public Integer idSesion;  // ← Cambiado de String a Integer
        @SerializedName("preguntas") public List<ApiQuestion> preguntas;
        @SerializedName("preguntasPorSubtema") public List<ApiQuestion> preguntasPorSubtema;
    }
}
