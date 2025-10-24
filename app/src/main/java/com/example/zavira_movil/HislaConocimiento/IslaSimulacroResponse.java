package com.example.zavira_movil.HislaConocimiento;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class IslaSimulacroResponse {

    @SerializedName("sesion")
    private SesionDto sesion;

    @SerializedName("totalPreguntas")
    private Integer totalPreguntas;

    @SerializedName("preguntas")
    private List<PreguntaDto> preguntas;

    public SesionDto getSesion() { return sesion; }
    public Integer getTotalPreguntas() { return totalPreguntas; }
    public List<PreguntaDto> getPreguntas() { return preguntas; }

    public static class SesionDto {
        @SerializedName("idSesion")   private String idSesion;
        @SerializedName("idUsuario")  private String idUsuario;
        @SerializedName("tipo")       private String tipo;
        @SerializedName("area")       private String area;
        @SerializedName("subtema")    private String subtema;
        @SerializedName("nivelOrden") private Integer nivelOrden;
        @SerializedName("modo")       private String modo;
        @SerializedName("usaEstiloKolb") private Boolean usaEstiloKolb;
        @SerializedName("inicioAt")   private String inicioAt;
        @SerializedName("totalPreguntas") private Integer totalPreguntas;

        public String getIdSesion() { return idSesion; }
        public String getIdUsuario() { return idUsuario; }
        public String getTipo() { return tipo; }
        public String getArea() { return area; }
        public String getSubtema() { return subtema; }
        public Integer getNivelOrden() { return nivelOrden; }
        public String getModo() { return modo; }
        public Boolean getUsaEstiloKolb() { return usaEstiloKolb; }
        public String getInicioAt() { return inicioAt; }
        public Integer getTotalPreguntas() { return totalPreguntas; }
    }

    public static class PreguntaDto {
        // OJO: las claves vienen como id_pregunta, area, subtema, enunciado, opciones[]
        @SerializedName("id_pregunta") private String idPregunta;
        @SerializedName("area")        private String area;
        @SerializedName("subtema")     private String subtema;
        @SerializedName("enunciado")   private String enunciado;
        @SerializedName("opciones")    private List<String> opciones;

        public String getIdPregunta() { return idPregunta; }
        public String getArea() { return area; }
        public String getSubtema() { return subtema; }
        public String getEnunciado() { return enunciado; }
        public List<String> getOpciones() { return opciones; }
    }
}
