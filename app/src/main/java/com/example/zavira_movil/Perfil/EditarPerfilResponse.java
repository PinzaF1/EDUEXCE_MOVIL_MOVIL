package com.example.zavira_movil.Perfil;

public class EditarPerfilResponse {
    private String message;
    private PerfilContacto data;

    public String getMessage() { return message; }
    public PerfilContacto getData() { return data; }

    public static class PerfilContacto {
        private int id;
        private String correo;
        private String telefono;
        private String direccion;

        public int getId() { return id; }
        public String getCorreo() { return correo; }
        public String getTelefono() { return telefono; }
        public String getDireccion() { return direccion; }
    }
}
