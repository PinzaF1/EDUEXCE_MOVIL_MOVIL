package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Sesion implements Serializable {
    @SerializedName("idSesion")
    public Integer idSesion;

    // el resto de campos si los necesitas
    public String area;
    public String subtema;
    public Integer totalPreguntas;
    public Integer correctas;
}
