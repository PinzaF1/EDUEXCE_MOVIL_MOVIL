package com.example.zavira_movil.niveleshome;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Soporta variaciones:
 * - preguntas en raíz: "preguntas" / "preguntasPorSubtema"
 * - preguntas dentro de "sesion": idem
 * - idSesion como "idSesion" o "id_sesion" (string)
 */
public class ParadaResponse {

    @SerializedName("sesion")
    public Sesion sesion;

    @SerializedName("preguntas")
    public List<ApiQuestion> preguntas;

    @SerializedName(value = "preguntasPorSubtema", alternate = {"preguntas_por_subtema"})
    public List<ApiQuestion> preguntasPorSubtema;

    public static class Sesion {
        @SerializedName(value = "idSesion", alternate = {"id_sesion"})
        public String idSesion;

        @SerializedName("area")
        public String area;

        @SerializedName("subtema")
        public String subtema;

        @SerializedName(value = "nivelOrden", alternate = {"nivel_orden"})
        public Integer nivelOrden;

        @SerializedName(value = "totalPreguntas", alternate = {"total_preguntas"})
        public Integer totalPreguntas;

        @SerializedName("preguntas")
        public List<ApiQuestion> preguntas;

        @SerializedName(value = "preguntasPorSubtema", alternate = {"preguntas_por_subtema"})
        public List<ApiQuestion> preguntasPorSubtema;
    }
}
