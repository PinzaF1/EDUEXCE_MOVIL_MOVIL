package com.example.zavira_movil.oponente;

public class OpponentItem {
    private String id;
    private String nombre;
    private String nivel;
    private int wins;
    private boolean online;

    public OpponentItem(String id, String nombre, String nivel, int wins, boolean online) {
        this.id = id; this.nombre = nombre; this.nivel = nivel; this.wins = wins; this.online = online;
    }
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getNivel() { return nivel; }
    public int getWins() { return wins; }
    public boolean isOnline() { return online; }
}
