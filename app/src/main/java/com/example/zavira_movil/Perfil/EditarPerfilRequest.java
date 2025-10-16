package com.example.zavira_movil.Perfil;

public class EditarPerfilRequest {
    private String correo;
    private String direccion;
    private String telefono;

    public EditarPerfilRequest(String correo, String direccion, String telefono) {
        // Si alguna cadena viene vacía, envíala como null para "no cambiar"
        this.correo = isBlank(correo) ? null : correo;
        this.direccion = isBlank(direccion) ? null : direccion;
        this.telefono = isBlank(telefono) ? null : telefono;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public String getCorreo() { return correo; }
    public String getDireccion() { return direccion; }
    public String getTelefono() { return telefono; }
}
