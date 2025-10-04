package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LogrosResponse {

    @SerializedName("obtenidas")
    private List<Badge> obtenidas;

    @SerializedName("pendientes")
    private List<Badge> pendientes;

    public List<Badge> getObtenidas() { return obtenidas; }
    public List<Badge> getPendientes() { return pendientes; }

    public static class Badge {
        @SerializedName("codigo")      private String codigo;
        @SerializedName("nombre")      private String nombre;
        @SerializedName("descripcion") private String descripcion;
        @SerializedName("area")        private String area;

        public String getCodigo() { return codigo; }
        public String getNombre() { return nombre; }
        public String getDescripcion() { return descripcion; }
        public String getArea() { return area; }
    }
}
