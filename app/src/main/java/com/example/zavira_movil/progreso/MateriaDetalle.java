package com.example.zavira_movil.progreso;

import com.google.gson.annotations.SerializedName;

public class MateriaDetalle {
    @SerializedName("nombre")
    private String nombre;

    @SerializedName("porcentaje")
    private int porcentaje;

    @SerializedName("etiqueta")
    private String etiqueta;

    public String getNombre() { return nombre; }
    public int getPorcentaje() { return porcentaje; }
    public String getEtiqueta() { return etiqueta; }
    
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setPorcentaje(int porcentaje) { this.porcentaje = porcentaje; }
    public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }
}
