package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MateriaUi {
    @SerializedName("materias")
    private List<MateriaDetalle> materias;

    public List<MateriaDetalle> getMaterias() {
        return materias;
    }

    public void setMaterias(List<MateriaDetalle> materias) {
        this.materias = materias;
    }
}
