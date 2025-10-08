package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class KolbResultado {

    private String estudiante;
    private String documento;
    private String fecha;
    private Estilo estilo;     // objeto JSON { idEstilosAprendizajes, estilo, descripcion, caracteristicas, recomendaciones }
    private Totales totales;   // objeto JSON { ec, or, ca, ea }

    // ===== Getters “planos” (compatibles con el resto del app) =====
    public String getEstudiante() { return estudiante; }
    public String getDocumento()  { return documento; }
    public String getFecha()      { return fecha; }

    // Antes tenías getEstilo() -> String. Lo mantenemos:
    public String getEstilo() {
        return (estilo != null) ? estilo.estilo : null;
    }
    public String getDescripcion() {
        return (estilo != null) ? estilo.descripcion : null;
    }
    public String getCaracteristicas() {
        return (estilo != null) ? estilo.caracteristicas : null;
    }
    public String getRecomendaciones() {
        return (estilo != null) ? estilo.recomendaciones : null;
    }

    public Totales getTotales()   { return totales; }

    // ===== Si alguna pantalla quiere el objeto completo, también lo exponemos =====
    public Estilo  getEstiloObj() { return estilo; }

    // ===== Clases internas que mapean tal cual la respuesta =====
    public static class Estilo {
        @SerializedName("idEstilosAprendizajes")
        private int idEstilosAprendizajes;
        private String estilo;
        private String descripcion;
        private String caracteristicas;
        private String recomendaciones;

        public int getIdEstilosAprendizajes() { return idEstilosAprendizajes; }
        public String getEstiloRaw()          { return estilo; }
        public String getDescripcionRaw()     { return descripcion; }
        public String getCaracteristicasRaw() { return caracteristicas; }
        public String getRecomendacionesRaw() { return recomendaciones; }
    }

    public static class Totales {
        private int ec;
        private int or;
        private int ca;
        private int ea;

        public int getEc() { return ec; }
        public int getOr() { return or; }
        public int getCa() { return ca; }
        public int getEa() { return ea; }
    }
}
