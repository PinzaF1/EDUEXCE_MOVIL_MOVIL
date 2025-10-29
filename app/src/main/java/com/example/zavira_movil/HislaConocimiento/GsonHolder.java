package com.example.zavira_movil.HislaConocimiento;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class GsonHolder {
    private static final Gson GSON = new GsonBuilder()
            .setLenient()
            .serializeNulls()
            .create();

    private GsonHolder() { }
    public static Gson gson() { return GSON; }
}
