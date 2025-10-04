package com.example.zavira_movil.model;

import java.util.List;

public class SimulacroRequest {
    public String area;
    public List<String> subtemas;

    public SimulacroRequest(String area, List<String> subtemas) {
        this.area = area;
        this.subtemas = subtemas;
    }
}
