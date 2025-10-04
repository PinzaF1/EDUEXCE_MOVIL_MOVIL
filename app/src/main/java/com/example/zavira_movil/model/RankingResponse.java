package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RankingResponse {

    @SerializedName("top5")
    private List<Item> top5;

    @SerializedName("posicion")
    private Integer posicion;

    @SerializedName("total")
    private Integer total;

    @SerializedName("posiciones")
    private List<Item> posiciones;

    // GETTERS
    public List<Item> getTop5() { return top5; }
    public Integer getPosicion() { return posicion; }
    public Integer getTotal() { return total; }
    public List<Item> getPosiciones() { return posiciones; }

    /** Un único modelo para ambos arrays.
     *  En "top5" NO viene 'posicion';
     *  en "posiciones" SÍ viene 'posicion'.  */
    public static class Item {
        @SerializedName("id_usuario")
        private int idUsuario;

        @SerializedName("nombre")
        private String nombre;

        @SerializedName("promedio")
        private int promedio;

        // Solo presente en el array "posiciones"
        @SerializedName("posicion")
        private Integer posicion;

        public int getIdUsuario() { return idUsuario; }
        public String getNombre() { return nombre; }
        public int getPromedio() { return promedio; }
        public Integer getPosicion() { return posicion; }
    }
}
