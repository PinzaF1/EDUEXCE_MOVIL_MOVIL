package com.example.zavira_movil.model;

import java.util.List;

public class AceptarRetoResponse {
    public Reto reto;
    public List<Sesion> sesiones;
    public List<PreguntaReto> preguntas;

    public static class Reto {
        public String id_reto;
        public String estado;
        public Integer total_preguntas;
    }

    public static class Sesion {
        public int id_sesion;
        public int id_usuario; // si viene
    }
}
