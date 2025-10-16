package com.example.zavira_movil;

import com.google.gson.annotations.SerializedName;

public class BasicResponse {
    @SerializedName("ok")      public Boolean ok;
    @SerializedName("success") public Boolean success;
    @SerializedName("message") public String message;

    public boolean isOk() { return Boolean.TRUE.equals(ok) || Boolean.TRUE.equals(success); }
    public String  getMessage() { return message; }
}
