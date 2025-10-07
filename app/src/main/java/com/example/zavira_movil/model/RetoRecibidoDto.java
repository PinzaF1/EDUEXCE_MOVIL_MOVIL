// app/src/main/java/com/example/zavira_movil/model/RetoRecibidoItem.java
package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class RetoRecibidoDto {

    @SerializedName("id_reto") public String idReto;
    @SerializedName("area")    public String area;
    @SerializedName("estado")  public String estado; // "pendiente", etc.
    @SerializedName("fecha")   public String fecha;  // ISO u otro formato

    @SerializedName("remitente") public Usuario remitente;

    public static class Usuario {
        @SerializedName("id_usuario") public Integer idUsuario;
        @SerializedName("nombre")     public String  nombre;
        @SerializedName("nivel")      public String  nivel; // opcional
    }
}
