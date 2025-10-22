// ApiQuestion.java
package com.example.zavira_movil.niveleshome;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class ApiQuestion implements Serializable {
    @SerializedName("id_pregunta") public Integer id_pregunta;
    @SerializedName("area") public String area;
    @SerializedName("subtema") public String subtema;
    @SerializedName("enunciado") public String enunciado;
    @SerializedName("opciones") public List<String> opciones;
}
