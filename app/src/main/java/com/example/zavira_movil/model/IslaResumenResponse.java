package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class IslaResumenResponse {
    @SerializedName("id_sesion")
    public int idSesion;

    @SerializedName("puntajes_por_area")
    public Map<String, Integer> puntajesPorArea;

    @SerializedName("puntaje_general")
    public int puntajeGeneral;

    @SerializedName("puntaje_icfes_global")
    public int puntajeIcfesGlobal;

    @SerializedName("correctas")
    public int correctas;

    @SerializedName("total_preguntas")
    public int totalPreguntas;

    @SerializedName("duracion_segundos")
    public int duracionSegundos;
}
