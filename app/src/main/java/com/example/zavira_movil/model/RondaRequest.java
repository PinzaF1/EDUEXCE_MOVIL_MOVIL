package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RondaRequest {
    @SerializedName("id_sesion") public int idSesion;
    @SerializedName("respuestas") public List<Item> respuestas;

    public RondaRequest(int idSesion, List<Item> respuestas) {
        this.idSesion = idSesion;
        this.respuestas = respuestas;
    }

    public static class Item {
        @SerializedName("orden")  public int orden;
        @SerializedName("opcion") public String opcion;
        public Item(int orden, String opcion) { this.orden = orden; this.opcion = opcion; }
    }
}
