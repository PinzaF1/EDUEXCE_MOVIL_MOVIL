package com.example.zavira_movil.retos1vs1;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RondaRequest {

    @SerializedName("id_sesion")
    public final int idSesion;

    @SerializedName("respuestas")
    public final List<Item> respuestas;

    // NUEVO: opcional
    @SerializedName("tiempo_total_seg")
    public Integer tiempoTotalSeg;

    public RondaRequest(int idSesion, List<Item> respuestas) {
        this.idSesion = idSesion;
        this.respuestas = respuestas;
    }

    public static class Item {
        @SerializedName("orden")  public final int orden;
        @SerializedName("opcion") public final String opcion;
        @SerializedName("tiempo_empleado_seg") public final Double tiempoEmpleadoSeg;

        public Item(int orden, String opcion, Double tiempoEmpleadoSeg) {
            this.orden = orden;
            this.opcion = opcion;
            this.tiempoEmpleadoSeg = tiempoEmpleadoSeg;
        }
    }
}
