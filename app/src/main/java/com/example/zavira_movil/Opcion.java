package com.example.zavira_movil;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

// Con esta anotación NO necesitas cambiar RetrofitClient.
// Gson usará el adaptador de abajo para cada Opcion.
@JsonAdapter(OpcionStringOrObjectAdapter.class)
public class Opcion {
    @SerializedName("key")  private String key;   // "A" | "B" | "C" | "D"
    @SerializedName("text") private String text;  // texto visible

    public Opcion() {}                              // requerido por Gson
    public Opcion(String key, String text) {        // lo usa el adapter
        this.key = key;
        this.text = text;
    }

    public String getKey()  { return key; }
    public String getText() { return text; }
}