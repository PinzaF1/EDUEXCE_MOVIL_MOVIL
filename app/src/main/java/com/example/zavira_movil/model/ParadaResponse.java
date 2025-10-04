package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/** Acepta snake_case y camelCase del backend */
public class ParadaResponse implements Serializable {

    @SerializedName(value = "sesion")
    public Sesion sesion;

    @SerializedName(value = "preguntas")
    public List<Question> preguntas;

    public static class Sesion implements Serializable {
        // ambos nombres posibles:
        @SerializedName(value = "id_sesion", alternate = {"idSesion"})
        public Integer id_sesion;

        @SerializedName(value = "total_preguntas", alternate = {"totalPreguntas"})
        public Integer total_preguntas;

        // ...campos extra si los necesitas
    }
}
