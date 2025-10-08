package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class Estudiante {

    @SerializedName("id_usuario")
    private Integer idUsuario;

    @SerializedName(
            value = "nombre_institucion",
            alternate = {"institucion", "institucion_nombre", "nombreInstitucion"}
    )
    private String nombreInstitucion;

    @SerializedName(
            value = "nombre_usuario",
            alternate = {"nombre", "name", "username"}
    )
    private String nombreUsuario;

    @SerializedName("apellido")
    private String apellido;

    @SerializedName("numero_documento")
    private String numeroDocumento;

    @SerializedName(
            value = "tipo_documento",
            alternate = {"tipo_doc", "tdoc", "tipoDocumento"}
    )
    private String tipoDocumento;

    @SerializedName("grado")
    private String grado;

    @SerializedName("curso")
    private String curso;

    @SerializedName("jornada")
    private String jornada;

    @SerializedName("correo")
    private String correo;

    @SerializedName("telefono")
    private String telefono;

    @SerializedName("direccion")
    private String direccion;

    @SerializedName(value = "foto_url", alternate = {"foto", "avatar_url"})
    private String fotoUrl;

    @SerializedName("is_active")
    private Boolean isActive;

    // Getters
    public Integer getIdUsuario()           { return idUsuario; }
    public String  getNombreInstitucion()   { return nombreInstitucion; }
    public String  getNombreUsuario()       { return nombreUsuario; }
    public String  getApellido()            { return apellido; }
    public String  getNumeroDocumento()     { return numeroDocumento; }
    public String  getTipoDocumento()       { return tipoDocumento; }
    public String  getGrado()               { return grado; }
    public String  getCurso()               { return curso; }
    public String  getJornada()             { return jornada; }
    public String  getCorreo()              { return correo; }
    public String  getTelefono()            { return telefono; }
    public String  getDireccion()           { return direccion; }
    public String  getFotoUrl()             { return fotoUrl; }
    public Boolean getIsActive()            { return isActive; }
}
