package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class IslaCerrarRequest {
    @SerializedName("id_sesion")
    public int idSesion;

    @SerializedName("respuestas")
    public List<Respuesta> respuestas;

    public IslaCerrarRequest(int idSesion, List<Respuesta> respuestas) {
        this.idSesion = idSesion;
        this.respuestas = respuestas;
    }

    public static class Respuesta {
        @SerializedName("orden")
        public int orden;

        @SerializedName("opcion")
        public String opcion;

        @SerializedName("tiempo_empleado_seg")
        public int tiempo;

        public Respuesta(int orden, String opcion, int tiempo) {
            this.orden = orden;
            this.opcion = opcion;
            this.tiempo = tiempo;
        }
    }
}
