package com.example.zavira_movil;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QuizCerrarRequest {

    public static class RespuestaItem {
        @SerializedName("id_pregunta") public String idPregunta;
        @SerializedName("respuesta")   public String respuesta; // Cambiar de "seleccion" a "respuesta" para coincidir con backend
        public RespuestaItem(String idPregunta, String respuesta) {
            this.idPregunta = idPregunta; 
            this.respuesta = respuesta != null ? respuesta : "";
        }
    }

    @SerializedName("id_sesion")  private String idSesion;
    @SerializedName("respuestas") private List<RespuestaItem> respuestas;

    public QuizCerrarRequest(String idSesion, List<RespuestaItem> respuestas) {
        this.idSesion = idSesion; this.respuestas = respuestas;
    }
}
