package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
public class AceptarRetoResponse {

    @SerializedName("reto")
    public Reto reto;

    @SerializedName("sesiones")
    public List<Sesion> sesiones;

    @SerializedName("preguntas")
    public List<Pregunta> preguntas;

    public static class Reto {
        @SerializedName("id_reto")
        public int id_reto;

        @SerializedName("estado")
        public String estado;

        @SerializedName("participantes")
        public List<Integer> participantes;
    }

    public static class Sesion {
        @SerializedName("id_usuario")
        public int id_usuario;

        @SerializedName("id_sesion")
        public int id_sesion;
    }

    public static class Pregunta {
        @SerializedName("id_pregunta")
        public Integer id_pregunta;

        @SerializedName("area")
        public String area;

        @SerializedName("subtema")
        public String subtema;

        @SerializedName("dificultad")
        public String dificultad;

        @SerializedName("enunciado")
        public String enunciado;

        @SerializedName("opciones")
        public List<Opcion> opciones;

        @SerializedName("time_limit_seconds")
        public Integer time_limit_seconds;
    }

    public static class Opcion {
        @SerializedName("key")
        public String key;

        @SerializedName("text")
        public String text;
    }
}
