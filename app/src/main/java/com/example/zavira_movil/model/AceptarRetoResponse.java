package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.List;

public class AceptarRetoResponse {

    @SerializedName("reto")
    public Reto reto;

    @SerializedName("sesiones")
    public List<Sesion> sesiones;

    @SerializedName("preguntas")
    public List<Pregunta> preguntas;

    public static class Reto {
        @SerializedName("id_reto") public int id_reto;
        @SerializedName("estado")  public String estado;
        @SerializedName("participantes") public List<Integer> participantes;
    }

    public static class Sesion {
        @SerializedName("id_usuario") public int id_usuario;
        @SerializedName("id_sesion")  public int id_sesion;
    }

    public static class Pregunta {
        @SerializedName("id_pregunta")       public Integer id_pregunta;
        @SerializedName("area")               public String area;
        @SerializedName("subtema")            public String subtema;
        @SerializedName("dificultad")         public String dificultad;
        @SerializedName("enunciado")          public String enunciado;
        @SerializedName("time_limit_seconds") public Integer time_limit_seconds;

        // IMPORTANTE: el backend a veces manda array de strings
        @SerializedName("opciones")
        public List<Opcion> opciones;
    }

    @JsonAdapter(OpcionAdapter.class)
    public static class Opcion {
        @SerializedName("key")  public String key;   // "A" | "B" | ...
        @SerializedName("text") public String text;  // texto visible
    }

    /** Adapta cada elemento de "opciones": puede ser STRING o {key,text} */
    public static class OpcionAdapter extends TypeAdapter<Opcion> {
        @Override public Opcion read(JsonReader in) throws IOException {
            Opcion o = new Opcion();
            JsonToken t = in.peek();
            if (t == JsonToken.STRING) { o.text = in.nextString(); return o; }
            if (t == JsonToken.BEGIN_OBJECT) {
                in.beginObject();
                while (in.hasNext()) {
                    String name = in.nextName();
                    if ("key".equals(name))  { o.key  = readMaybeNullString(in); }
                    else if ("text".equals(name)) { o.text = readMaybeNullString(in); }
                    else in.skipValue();
                }
                in.endObject();
                return o;
            }
            in.skipValue();
            return o;
        }
        private String readMaybeNullString(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) { in.nextNull(); return null; }
            return in.nextString();
        }
        @Override public void write(JsonWriter out, Opcion value) throws IOException {
            if (value == null) { out.nullValue(); return; }
            out.beginObject();
            out.name("key");  out.value(value.key);
            out.name("text"); out.value(value.text);
            out.endObject();
        }
    }

    /** Busca la sesión del usuario actual y la devuelve. Si no la encuentra, devuelve la primera o -1. */
    public int findSesionIdForUser(Integer myUserId) {
        if (sesiones == null || sesiones.isEmpty()) return -1;
        if (myUserId != null) {
            for (Sesion s : sesiones) {
                if (s != null && s.id_usuario == myUserId) return s.id_sesion;
            }
        }
        return sesiones.get(0).id_sesion;
    }
}
