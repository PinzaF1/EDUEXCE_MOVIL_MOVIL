package com.example.zavira_movil.niveleshome;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/** DTO tal cual lo envía tu API (para Retrofit/Gson). */
public class ApiQuestion implements Serializable {

    @SerializedName(value = "id_pregunta", alternate = {"idPregunta", "id"})
    public Integer idPregunta;

    @SerializedName("area")
    public String area;

    @SerializedName("subtema")
    public String subtema;

    @SerializedName("enunciado")
    public String enunciado;

    // En tu JSON: ["A. ...","B. ...", ...]
    @SerializedName("opciones")
    public List<String> opciones;

    // Opcional
    @SerializedName(value = "dificultad", alternate = {"nivel_dificultad"})
    public String dificultad;
}
