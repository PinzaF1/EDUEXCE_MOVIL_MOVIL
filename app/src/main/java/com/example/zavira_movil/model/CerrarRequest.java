package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CerrarRequest {
    @SerializedName("id_sesion")
    public Integer idSesion;

    @SerializedName("respuestas")
    public List<Respuesta> respuestas;

    public CerrarRequest(Integer idSesion, List<Respuesta> respuestas) {
        this.idSesion = idSesion;
        this.respuestas = respuestas;
    }

    public static class Respuesta {
        @SerializedName("orden")
        public int orden;

        @SerializedName("opcion")
        public String opcion;

        public Respuesta(int orden, String opcion) {
            this.orden = orden;
            this.opcion = opcion;
        }
    }
}
