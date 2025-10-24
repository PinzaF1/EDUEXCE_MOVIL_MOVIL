// ParadaRequest.java
package com.example.zavira_movil.niveleshome;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ParadaRequest implements Serializable {
    @SerializedName("area") public String area;
    @SerializedName("subtema") public String subtema;
    @SerializedName("nivel_orden") public int nivelOrden;
    @SerializedName("usa_estilo_kolb") public boolean usaEstiloKolb;
    @SerializedName("intento_actual") public int intentoActual;

    public ParadaRequest(String area, String subtema, int nivelOrden, boolean usaEstiloKolb, int intentoActual) {
        this.area = area;
        this.subtema = subtema;
        this.nivelOrden = nivelOrden;
        this.usaEstiloKolb = usaEstiloKolb;
        this.intentoActual = intentoActual;
    }
}
