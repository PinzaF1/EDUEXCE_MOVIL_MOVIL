package com.example.zavira_movil.niveleshome;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class CerrarRequest implements Serializable {
    @SerializedName("id_sesion") public Integer idSesion;
    @SerializedName("respuestas") public List<Respuesta> respuestas;

    public CerrarRequest(Integer idSesion, List<Respuesta> respuestas) {
        this.idSesion = idSesion;
        this.respuestas = respuestas;
    }

    public static class Respuesta implements Serializable {
        @SerializedName("orden")  public int orden;
        @SerializedName("id_pregunta") public Integer idPregunta;  // Nullable
        @SerializedName("opcion") public String opcion;

        public Respuesta(int orden, Integer idPregunta, String opcion) {
            this.orden = orden;
            this.idPregunta = idPregunta;
            this.opcion = opcion;
        }
    }
}
