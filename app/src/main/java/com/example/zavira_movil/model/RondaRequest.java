package com.example.zavira_movil.model;

import java.util.List;

public class RondaRequest {
    private int id_sesion;
    private List<Item> respuestas;

    public RondaRequest(int id_sesion, List<Item> respuestas) {
        this.id_sesion = id_sesion; this.respuestas = respuestas;
    }
    public static class Item {
        private int orden;
        private String opcion;
        public Item(int orden, String opcion) { this.orden = orden; this.opcion = opcion; }
    }
}
