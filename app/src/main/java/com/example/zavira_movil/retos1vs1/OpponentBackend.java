package com.example.zavira_movil.retos1vs1;

import com.google.gson.annotations.SerializedName;

public class OpponentBackend {
    @SerializedName("id_usuario") public Integer idUsuario;
    @SerializedName("nombre")    public String  nombre;
    @SerializedName("grado")     public String  grado;
    @SerializedName("curso")     public String  curso;
    @SerializedName("foto_url")  public String  fotoUrl;
    @SerializedName("estado")    public String  estado; // "disponible" | "en_reto"

    public Integer getIdUsuario() { return idUsuario; }
    public String  getNombre()    { return nombre; }
    public String  getGrado()     { return grado; }
    public String  getCurso()     { return curso; }
    public String  getFotoUrl()   { return fotoUrl; }
    public String  getEstado()    { return estado; }
}
