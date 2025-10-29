package com.example.zavira_movil.retos1vs1;

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

        @SerializedName("area")       public String area;
        @SerializedName("subtema")    public String subtema;
        @SerializedName("dificultad") public String dificultad;
        @SerializedName("enunciado")  public String enunciado;

        // ⬇️ ANTES: List<Opcion>
        // @SerializedName("opciones")
        // public List<Opcion> opciones;

        // ⬇️ DESPUÉS: acepta array de strings
        @SerializedName("opciones")
        public List<String> opciones;

        @SerializedName("time_limit_seconds")
        public Integer time_limit_seconds;
    }


    public static class Opcion {
        @SerializedName("key")
        public String key;

        @SerializedName("text")
        public String text;
    }

    /** Busca la sesión del usuario actual y la devuelve. Si no la encuentra, devuelve la primera o -1. */
    public int findSesionIdForUser(Integer myUserId) {
        if (sesiones == null || sesiones.isEmpty()) return -1;
        if (myUserId != null) {
            for (Sesion s : sesiones) {
                if (s != null && s.id_usuario == myUserId) return s.id_sesion;
            }
        }
        return sesiones.get(0).id_sesion;
    }
}
