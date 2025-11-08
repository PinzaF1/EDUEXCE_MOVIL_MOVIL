package com.example.zavira_movil.sincronizacion;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class SincronizacionResponse {
    @SerializedName("niveles")
    private Map<String, Integer> niveles;

    @SerializedName("vidas")
    private Map<String, Map<String, Integer>> vidas;

    public Map<String, Integer> getNiveles() {
        return niveles;
    }

    public Map<String, Map<String, Integer>> getVidas() {
        return vidas;
    }
}


