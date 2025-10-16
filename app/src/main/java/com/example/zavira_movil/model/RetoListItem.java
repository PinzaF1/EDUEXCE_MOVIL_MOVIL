// app/src/main/java/com/example/zavira_movil/model/RetoListado.java
package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class RetoListItem {

    @SerializedName("id_reto")   private Integer idReto;
    @SerializedName("area")      private String area;
    @SerializedName("estado")    private String estado;    // "pendiente", "en_curso", "finalizado"
    @SerializedName("creado_en") private String creadoEn;  // fecha string (ISO/lo que devuelva backend)

    @SerializedName("creador")   private UserRef creador;   // quien te retó
    // (si el backend también envía "invitado", puedes añadirlo sin romper nada)

    public static class UserRef {
        @SerializedName("id_usuario") private Integer idUsuario;
        @SerializedName("nombre")     private String nombre;

        public Integer getIdUsuario() { return idUsuario; }
        public String getNombre()     { return nombre; }
    }

    public Integer getIdReto() { return idReto; }
    public String  getArea()   { return area; }
    public String  getEstado() { return estado; }
    public String  getCreadoEn(){ return creadoEn; }
    public UserRef getCreador(){ return creador; }
}
