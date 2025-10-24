package com.example.zavira_movil.detalleprogreso;

import java.util.List;

public class ProgresoDetalleResponse {
    public Header header;
    public Resumen resumen;
    public List<Pregunta> preguntas;
    public Analisis analisis;

    public static class Header {
        public String materia;
        public String fecha;
        public String nivel;
        public int nivelOrden;
        public int puntaje;
        public String escala;
        public int tiempo_total_seg;
        public int correctas;
        public int incorrectas;
        public int total;
    }

    public static class Resumen {
        public String cambio;
        public String mensaje;
        public int nivelActual;
    }

    public static class Pregunta {
        public int orden;
        public int id_pregunta;
        public String area;
        public String subtema;
        public String enunciado;
        public String correcta;          // "A"|"B"|"C"|"D"
        public String marcada;           // "A"|"B"|"C"|"D"
        public boolean es_correcta;
        public String explicacion;
        public Integer tiempo_empleado_seg; // puede venir null
    }

    public static class Analisis {
        public List<String> fortalezas;
        public List<String> mejoras;
        public List<String> recomendaciones;
    }
}
