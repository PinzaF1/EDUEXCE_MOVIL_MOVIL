package com.example.zavira_movil.retos1vs1;

import com.google.gson.annotations.SerializedName;

public class RetoListItem {

    @SerializedName("id_reto")
    private Integer idReto;

    @SerializedName("area")
    private String area;

    @SerializedName("estado") // "pendiente" | "en_curso" | "finalizado"
    private String estado;

    @SerializedName("creado_en")
    private String creadoEn;

    @SerializedName("creador")
    private Creador creador;  // opcional, según backend

    public static class Creador {
        @SerializedName("id_usuario") private Integer idUsuario;
        @SerializedName("nombre")     private String nombre;
        @SerializedName("grado")      private String grado;
        @SerializedName("curso")      private String curso;
        @SerializedName("foto_url")   private String fotoUrl;

        public Integer getIdUsuario() { return idUsuario; }
        public String getNombre() { return nombre; }
        public String getGrado() { return grado; }
        public String getCurso() { return curso; }
        public String getFotoUrl() { return fotoUrl; }
    }

    public Integer getIdReto() { return idReto; }
    public String getArea() { return area; }
    public String getEstado() { return estado; }
    public String getCreadoEn() { return creadoEn; }
    public Creador getCreador() { return creador; }
}
