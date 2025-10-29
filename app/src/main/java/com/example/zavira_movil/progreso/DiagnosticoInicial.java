package com.example.zavira_movil.progreso;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class DiagnosticoInicial {

    @SerializedName("tiene_diagnostico")
    public boolean tieneDiagnostico;

    @SerializedName("progreso_por_area")
    public Map<String, Area> progresoPorArea;

    @SerializedName("progreso_general")
    public Area progresoGeneral;

    public static class Area {
        public int inicial;
        public int actual; // % mostrado en tarjeta
        public int delta;
    }
}
