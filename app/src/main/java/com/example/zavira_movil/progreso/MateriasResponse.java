package com.example.zavira_movil.progreso;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MateriasResponse {
    @SerializedName("materias")
    private List<MateriaDetalle> materias;

    public List<MateriaDetalle> getMaterias() {
        return materias;
    }
}
