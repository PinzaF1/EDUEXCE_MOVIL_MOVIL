package com.example.zavira_movil.niveleshome;

import com.google.gson.annotations.SerializedName;

public class ParadaRequest {
    @SerializedName("area") public String area;
    @SerializedName("subtema") public String subtema;

    @SerializedName(value = "nivel_orden", alternate = {"nivelOrden"})
    public int nivelOrden;

    @SerializedName(value = "usa_estilo_kolb")
    public boolean usaEstiloKolb;

    @SerializedName(value = "intento_actual", alternate = {"intentoActual"})
    public int intentoActual;

    public ParadaRequest(String area, String subtema, int nivelOrden, boolean usaEstiloKolb, int intentoActual) {
        this.area = area;
        this.subtema = subtema;
        this.nivelOrden = nivelOrden;
        this.usaEstiloKolb = usaEstiloKolb;
        this.intentoActual = intentoActual;
    }
}
