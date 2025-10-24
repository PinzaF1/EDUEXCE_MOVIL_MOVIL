// app/src/main/java/com/example/zavira_movil/model/OpponentRaw.java
package com.example.zavira_movil.oponente;

import com.google.gson.annotations.SerializedName;

public class OpponentRaw {
    @SerializedName("id_usuario") public int idUsuario;
    @SerializedName("nombre")     public String nombre;
    @SerializedName("grado")      public String grado;    // "10", "11", etc.
    @SerializedName("curso")      public String curso;    // "A", "B", "C"
    @SerializedName("estado")     public String estado;   // "disponible" | "ocupado"
    @SerializedName("foto_url")   public String fotoUrl;  // puede venir null
}
