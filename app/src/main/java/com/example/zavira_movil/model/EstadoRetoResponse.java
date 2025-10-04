package com.example.zavira_movil.model;

import java.util.List;

public class EstadoRetoResponse {
    public String id_reto;
    public String estado;
    public Integer ganador;
    public List<Jugador> jugadores;

    public static class Jugador {
        public Integer id_usuario;
        public Integer correctas;
        public Integer tiempo_total_seg;
    }
}
