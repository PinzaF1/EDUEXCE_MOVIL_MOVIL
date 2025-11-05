package com.example.zavira_movil.notifications;

public class NotificationItem {
    private String title;
    private String message;
    private String tipo;
    private String area;
    private String puntaje;
    private long timestamp;
    private boolean isRead;
    
    public NotificationItem() {
    }
    
    public NotificationItem(String title, String message, String tipo, String area, String puntaje, long timestamp) {
        this.title = title;
        this.message = message;
        this.tipo = tipo;
        this.area = area;
        this.puntaje = puntaje;
        this.timestamp = timestamp;
        this.isRead = false;
    }
    
    // Getters y Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    public String getArea() {
        return area;
    }
    
    public void setArea(String area) {
        this.area = area;
    }
    
    public String getPuntaje() {
        return puntaje;
    }
    
    public void setPuntaje(String puntaje) {
        this.puntaje = puntaje;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public void setRead(boolean read) {
        isRead = read;
    }
    
    /**
     * Retorna un texto formateado del tiempo transcurrido
     */
    public String getTimeAgo() {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        
        if (weeks > 0) {
            return weeks + " sem";
        } else if (days > 0) {
            return days + " d";
        } else if (hours > 0) {
            return hours + " h";
        } else if (minutes > 0) {
            return minutes + " min";
        } else {
            return "Ahora";
        }
    }
}
