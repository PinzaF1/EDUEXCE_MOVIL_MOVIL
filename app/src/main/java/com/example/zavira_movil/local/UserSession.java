package com.example.zavira_movil.local;

public class UserSession {
    private static UserSession instance;
    private int idUsuario;

    private UserSession() {}

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setIdUsuario(int id) {
        this.idUsuario = id;
    }

    public int getIdUsuario() {
        return idUsuario;
    }
}
