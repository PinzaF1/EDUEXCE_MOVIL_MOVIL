package com.example.zavira_movil.HislaConocimiento;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo del JSON que devuelve:
 * POST /movil/isla/simulacro { "modalidad": "facil" | "dificil" }
 *
 * Estructura esperada (ejemplo):
 * {
 *   "sesion": { "idSesion":"1647", "idUsuario":"79", ... },
 *   "totalPreguntas": 25,
 *   "preguntas": [
 *     { "id_pregunta": "2189", "area": "...", "subtema": "...",
 *       "enunciado": "...", "opciones": ["A. ...","B. ...","C. ...","D. ..."] }
 *   ]
 * }
 */
public class IslaSimulacroResponse implements Serializable {

    @SerializedName("sesion")
    private SesionDto sesion;

    @SerializedName("totalPreguntas")
    private Integer totalPreguntas;

    @SerializedName("preguntas")
    private List<PreguntaDto> preguntas;

    // ---------- GETTERS DE NIVEL SUPERIOR ----------
    public SesionDto getSesion() { return sesion; }
    public Integer getTotalPreguntas() { return totalPreguntas; }
    public List<PreguntaDto> getPreguntas() { return preguntas; }

    /**
     * Helper: devuelve los IDs de las preguntas como enteros.
     * Si algún id viene nulo o no numérico, retorna 0 en esa posición.
     */
    public ArrayList<Integer> getQuestionIdsOrZero() {
        ArrayList<Integer> out = new ArrayList<>();
        if (preguntas == null) return out;
        for (PreguntaDto p : preguntas) {
            out.add(p.getIdPreguntaAsIntOrZero());
        }
        return out;
    }

    // ================== CLASES HIJAS ==================

    public static class SesionDto implements Serializable {
        @SerializedName("idSesion")   private String idSesion;
        @SerializedName("idUsuario")  private String idUsuario;
        @SerializedName("tipo")       private String tipo;
        @SerializedName("area")       private String area;
        @SerializedName("subtema")    private String subtema;
        @SerializedName("nivelOrden") private Integer nivelOrden;
        @SerializedName("modo")       private String modo;
        @SerializedName("usaEstiloKolb") private Boolean usaEstiloKolb;
        @SerializedName("inicioAt")   private String inicioAt;
        @SerializedName("totalPreguntas") private Integer totalPreguntas;

        // Getters
        public String getIdSesion() { return idSesion; }
        public String getIdUsuario() { return idUsuario; }
        public String getTipo() { return tipo; }
        public String getArea() { return area; }
        public String getSubtema() { return subtema; }
        public Integer getNivelOrden() { return nivelOrden; }
        public String getModo() { return modo; }
        public Boolean getUsaEstiloKolb() { return usaEstiloKolb; }
        public String getInicioAt() { return inicioAt; }
        public Integer getTotalPreguntas() { return totalPreguntas; }
    }

    /**
     * Pregunta del simulacro de la Isla del Conocimiento.
     * OJO: el backend envía "id_pregunta" (snake_case).
     */
    public static class PreguntaDto implements Serializable {

        @SerializedName("id_pregunta")
        private String idPregunta;

        @SerializedName("area")
        private String area;

        @SerializedName("subtema")
        private String subtema;

        @SerializedName("enunciado")
        private String enunciado;

        @SerializedName("opciones")
        private List<String> opciones;

        // -------- GETTERS QUE USA LA APP --------
        public String getIdPregunta() { return idPregunta; }
        public String getArea() { return area; }
        public String getSubtema() { return subtema; }
        public String getEnunciado() { return enunciado; }
        public List<String> getOpciones() { return opciones; }

        /** Devuelve el id_pregunta como entero, 0 si no es numérico. */
        public int getIdPreguntaAsIntOrZero() {
            try { return Integer.parseInt(String.valueOf(idPregunta)); }
            catch (Exception ignore) { return 0; }
        }
    }
}
