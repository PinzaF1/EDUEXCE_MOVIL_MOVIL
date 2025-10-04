package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class UsuarioRanking {
    @SerializedName("id_usuario")
    private int idUsuario;

    private String nombre;
    private int promedio;
    private Integer posicion;

    public int getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public int getPromedio() { return promedio; }
    public Integer getPosicion() { return posicion; }
}
