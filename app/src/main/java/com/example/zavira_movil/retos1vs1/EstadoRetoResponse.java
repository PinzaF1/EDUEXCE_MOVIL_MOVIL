// app/src/main/java/com/example/zavira_movil/retos1vs1/EstadoRetoResponse.java
package com.example.zavira_movil.retos1vs1;

import java.util.List;

public class EstadoRetoResponse {
    public Integer id_reto;
    public String  estado;           // "pendiente" | "en_curso" | "finalizado"
    public Integer ganador;          // id del ganador, o 0 si empate
    public List<Jugador> jugadores;

    public static class Jugador {
        public Integer id_usuario;
        public Integer correctas;
        public Integer tiempo_total_seg;
    }
}
