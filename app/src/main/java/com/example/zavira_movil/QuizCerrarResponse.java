package com.example.zavira_movil;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class QuizCerrarResponse {
    @SerializedName("id_sesion")
    public Integer idSesion;
    
    @SerializedName("id_usuario")
    public Integer idUsuario; // Campo adicional del backend
    
    @SerializedName("puntajes_por_area")
    public Map<String, Integer> puntajesPorArea;
    
    @SerializedName("puntajes_icfes_por_area")
    public Map<String, Integer> puntajesIcfesPorArea;
    
    @SerializedName("puntaje_general")
    public Integer puntajeGeneral;
    
    @SerializedName("puntaje_general_icfes")
    public Integer puntajeGeneralIcfes;
    
    @SerializedName("total_correctas")
    public Integer totalCorrectas;
    
    @SerializedName("total_preguntas")
    public Integer totalPreguntas;
    
    @SerializedName("detalle")
    public List<DetalleItem> detalle;
    
    public static class DetalleItem {
        @SerializedName("id_pregunta")
        public Integer idPregunta;
        
        @SerializedName("area")
        public String area;
        
        @SerializedName("correcta")
        public String correcta;
        
        @SerializedName("marcada")
        public String marcada;
        
        @SerializedName("es_correcta")
        public Boolean esCorrecta;
        
        @SerializedName("explicacion")
        public String explicacion;
    }
}

