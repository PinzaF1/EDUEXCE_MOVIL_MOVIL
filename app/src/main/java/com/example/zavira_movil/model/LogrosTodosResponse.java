package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LogrosTodosResponse {

    @SerializedName("obtenidas")
    private List<LogrosResponse.Badge> obtenidas;

    @SerializedName("pendientes")
    private List<LogrosResponse.Badge> pendientes;

    public List<LogrosResponse.Badge> getObtenidas() { return obtenidas; }
    public List<LogrosResponse.Badge> getPendientes() { return pendientes; }
}
