package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class CambiarPassword {
    @SerializedName("actual") public final String actual;
    @SerializedName("nueva")  public final String nueva;

    public CambiarPassword(String actual, String nueva) {
        this.actual = actual;
        this.nueva = nueva;
    }
}
