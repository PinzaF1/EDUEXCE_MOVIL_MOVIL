package com.example.zavira_movil.HislaConocimiento;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Provee una sola instancia de Gson para toda la app.
 * Evita crear objetos Gson repetidos y facilita el .fromJson() / .toJson().
 */
public final class GsonHolder {
    private static final Gson GSON = new GsonBuilder()
            .setLenient()            // tolerante con JSONs no estrictos
            .serializeNulls()        // incluye nulls si llegaran a aparecer
            .create();

    private GsonHolder() { }

    public static Gson gson() {
        return GSON;
    }
}
