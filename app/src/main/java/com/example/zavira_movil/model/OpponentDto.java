package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class OpponentDto {
    @SerializedName("id_usuario") public int idUsuario;
    @SerializedName("nombre")     public String nombre;
    @SerializedName("nivel")      public String nivel;
    @SerializedName("victorias")  public Integer victorias;
    @SerializedName("online")     public Boolean online;
}
