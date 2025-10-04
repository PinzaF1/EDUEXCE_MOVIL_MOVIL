package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HistorialResponse {
    @SerializedName("items")
    private List<HistorialItem> items;

    @SerializedName("page")
    private int page;

    @SerializedName("limit")
    private int limit;

    @SerializedName("total")
    private int total;

    public List<HistorialItem> getItems() { return items; }
    public int getPage()  { return page; }
    public int getLimit() { return limit; }
    public int getTotal() { return total; }
}
