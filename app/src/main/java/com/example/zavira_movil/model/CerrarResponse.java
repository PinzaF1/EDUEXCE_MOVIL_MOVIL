package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class CerrarResponse {
    @SerializedName("aprueba")
    public Boolean aprueba;

    @SerializedName("correctas")
    public int correctas;

    @SerializedName("puntaje")
    public int puntaje;
}
