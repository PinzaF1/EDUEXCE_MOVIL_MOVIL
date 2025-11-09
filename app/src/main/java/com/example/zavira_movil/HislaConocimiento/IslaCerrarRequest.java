package com.example.zavira_movil.HislaConocimiento;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class IslaCerrarRequest {

    @SerializedName("id_sesion")
    private final int idSesion;

    @SerializedName("respuestas")
    private final List<Resp> respuestas;

    public IslaCerrarRequest(int idSesion, List<Resp> respuestas) {
        this.idSesion = idSesion;
        this.respuestas = respuestas;
    }

    public static class Resp {
        @SerializedName("id_pregunta") public final int idPregunta;
        @SerializedName("opcion")      public final String opcion; // "A".."D" o ""
        @SerializedName("tiempo_empleado_seg") public final Long tiempoEmpleadoSeg; // Tiempo en segundos (opcional)

        public Resp(int idPregunta, String opcion) {
            this.idPregunta = idPregunta;
            this.opcion = opcion == null ? "" : opcion.trim().toUpperCase();
            this.tiempoEmpleadoSeg = null;
        }
        
        public Resp(int idPregunta, String opcion, long tiempoEmpleadoSeg) {
            this.idPregunta = idPregunta;
            this.opcion = opcion == null ? "" : opcion.trim().toUpperCase();
            this.tiempoEmpleadoSeg = tiempoEmpleadoSeg > 0 ? tiempoEmpleadoSeg : null;
        }
    }
}
