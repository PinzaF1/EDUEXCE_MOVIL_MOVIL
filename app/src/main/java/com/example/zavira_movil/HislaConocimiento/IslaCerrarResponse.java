package com.example.zavira_movil.HislaConocimiento;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/** Respuesta de POST /movil/isla/simulacro/cerrar */
public class IslaCerrarResponse implements Serializable {

    @SerializedName("id_sesion")
    public int idSesion;

    @SerializedName("resultado_base")
    public Resultado resultadoBase;

    public static class Resultado implements Serializable {
        @SerializedName("aprueba")  public boolean aprueba;
        @SerializedName("correctas")public int correctas;
        @SerializedName("puntaje")  public int puntaje;
    }
}
