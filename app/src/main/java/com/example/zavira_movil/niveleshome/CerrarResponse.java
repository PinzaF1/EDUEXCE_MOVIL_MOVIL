package com.example.zavira_movil.niveleshome;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class CerrarResponse implements Serializable {
    @SerializedName("aprueba") public Boolean aprueba;
    @SerializedName("correctas") public Integer correctas;

    // a veces llega "puntaje" (0-100) o "puntajePorcentaje"
    @SerializedName("puntaje") public Integer puntaje;
    @SerializedName("puntajePorcentaje") public Integer puntajePorcentaje;

    @SerializedName("detalleResumen")
    public List<Detalle> detalleResumen;

    public Integer getPorcentaje() {
        if (puntaje != null) return puntaje;
        if (puntajePorcentaje != null) return puntajePorcentaje;
        return null;
    }

    public static class Detalle implements Serializable {
        @SerializedName("id_pregunta") public Integer id_pregunta;
        @SerializedName("orden") public Integer orden;
        @SerializedName("correcta") public String correcta;
        @SerializedName("marcada") public String marcada;
        @SerializedName("es_correcta") public Boolean es_correcta;
    }
}
