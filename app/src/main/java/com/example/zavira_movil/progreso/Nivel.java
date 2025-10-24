package com.example.zavira_movil.progreso;

import com.google.gson.annotations.SerializedName;

public class Nivel {
    @SerializedName("nombre")  private String nombre;
    @SerializedName("min")     private int min;
    @SerializedName("max")     private int max;
    @SerializedName("rango")   private String rango;
    @SerializedName("actual")  private boolean actual;
    @SerializedName("valor")   private int valor;

    public String getNombre() { return nombre; }
    public int getMin() { return min; }
    public int getMax() { return max; }
    public String getRango() { return rango; }
    public boolean isActual() { return actual; }
    public int getValor() { return valor; }
}
