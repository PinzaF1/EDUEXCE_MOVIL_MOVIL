package com.example.zavira_movil.HislaConocimiento;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para cerrar simulacro:
 * {
 *   "id_sesion": "1408",
 *   "respuestas": [ {"opcion":"A","orden":1,"tiempo":3}, ... ]
 * }
 */
public class IslaCerrarRequest {

    @SerializedName("id_sesion")
    private final String idSesion;

    @SerializedName("respuestas")
    private final List<Respuesta> respuestas;

    public IslaCerrarRequest(String idSesion, List<Respuesta> respuestas) {
        this.idSesion = idSesion;
        this.respuestas = (respuestas != null) ? respuestas : new ArrayList<>();
    }

    public String getIdSesion() { return idSesion; }
    public List<Respuesta> getRespuestas() { return respuestas; }

    public static class Respuesta {
        @SerializedName("opcion")
        private final String opcion; // "A".."D" o "" (sin responder)

        @SerializedName("orden")
        private final int orden;     // 1-based

        @SerializedName("tiempo")
        private final int tiempo;    // segundos >= 0

        public Respuesta(int orden, String opcion, int tiempo) {
            this.orden  = Math.max(1, orden);
            this.opcion = (opcion == null) ? "" : opcion;
            this.tiempo = Math.max(0, tiempo);
        }

        public String getOpcion() { return opcion; }
        public int getOrden() { return orden; }
        public int getTiempo() { return tiempo; }
    }
}
