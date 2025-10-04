package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class HistorialItem {
    @SerializedName("intentoId")         private String  intentoId;
    @SerializedName("materia")           private String  materia;
    @SerializedName("porcentaje")        private int     porcentaje;
    @SerializedName("nivel")             private String  nivel;
    @SerializedName("fecha")             private String  fecha;
    @SerializedName("detalleDisponible") private boolean detalleDisponible;

    public String  getIntentoId()        { return intentoId; }
    public String  getMateria()          { return materia; }
    public int     getPorcentaje()       { return porcentaje; }
    public String  getNivel()            { return nivel; }
    public String  getFecha()            { return fecha; }
    public boolean isDetalleDisponible() { return detalleDisponible; }
}
