package com.example.zavira_movil.HislaConocimiento;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class IslaCerrarResultadoResponse implements Serializable {

    @SerializedName("aprueba") public boolean aprueba;
    @SerializedName("correctas") public int correctas;
    @SerializedName("puntaje") public int puntaje;
    @SerializedName("finAt") public String finAt;
    @SerializedName("puntajePorcentaje") public Integer puntajePorcentaje;
    @SerializedName("resultado") public String resultado;

    @SerializedName("detalleResumen")
    public List<Detalle> detalleResumen;

    @SerializedName("createdAt") public String createdAt;
    @SerializedName("updatedAt") public String updatedAt;

    @SerializedName("nivelOrden") public Integer nivelOrden;
    @SerializedName("resumenAreas") public Map<String, Area> resumenAreas;
    @SerializedName("global") public Global global;

    public static class Detalle implements Serializable {
        @SerializedName("id_pregunta") public int id_pregunta;
        @SerializedName("orden") public int orden;
        @SerializedName("correcta") public String correcta;
        @SerializedName("marcada") public String marcada;
        @SerializedName("es_correcta") public boolean es_correcta;
    }

    public static class Area implements Serializable {
        @SerializedName("total") public Integer total;
        @SerializedName("correctas") public Integer correctas;
        @SerializedName("porcentaje") public Integer porcentaje;
        @SerializedName("puntaje_icfes") public Integer puntaje_icfes;
    }

    public static class Global implements Serializable {
        @SerializedName("total") public Integer total;
        @SerializedName("correctas") public Integer correctas;
        @SerializedName("porcentaje") public Integer porcentaje;
        @SerializedName("puntaje_icfes") public Integer puntaje_icfes;
    }
}
