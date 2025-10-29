package com.example.zavira_movil;

import com.google.gson.annotations.SerializedName;

public class BasicResponse {
    @SerializedName("ok")      public Boolean ok;
    @SerializedName("success") public Boolean success;
    @SerializedName("valid")   public Boolean valid;  // Para verificación de código
    @SerializedName("message") public String message;

    public boolean isOk() { return Boolean.TRUE.equals(ok) || Boolean.TRUE.equals(success); }
    public boolean isValid() { return Boolean.TRUE.equals(valid); }
    public String  getMessage() { return message; }
}
