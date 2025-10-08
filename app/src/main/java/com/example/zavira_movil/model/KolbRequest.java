// com.example.zavira_movil.model.KolbRequest
package com.example.zavira_movil.model;

import java.util.List;

public class KolbRequest {

    private List<Item> respuestas;

    public KolbRequest(List<Item> respuestas) {
        this.respuestas = respuestas;
    }

    public List<Item> getRespuestas() {
        return respuestas;
    }

    public void setRespuestas(List<Item> respuestas) {
        this.respuestas = respuestas;
    }

    // Cada pregunta = 1 ítem (id_item) con una sola opción elegida (valor 1..4)
    public static class Item {
        private int id_item; // usa el id que llega en PreguntasKolb (id_pregunta_estilo_aprendizajes)
        private int valor;   // 1..4

        public Item(int id_item, int valor) {
            this.id_item = id_item;
            this.valor = valor;
        }

        public int getId_item() { return id_item; }
        public void setId_item(int id_item) { this.id_item = id_item; }

        public int getValor() { return valor; }
        public void setValor(int valor) { this.valor = valor; }
    }
}
