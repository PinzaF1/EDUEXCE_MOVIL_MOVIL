package com.example.zavira_movil;

import com.google.gson.*;
import java.lang.reflect.Type;

/** Permite parsear opciones como objeto {key,text} o como string "A. texto". */
public class OpcionStringOrObjectAdapter implements JsonDeserializer<Opcion> {

    @Override
    public Opcion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
            throws JsonParseException {

        if (json == null || json.isJsonNull()) return null;

        // Caso 1: ya viene como objeto {key, text}
        if (json.isJsonObject()) {
            JsonObject o = json.getAsJsonObject();
            String key  = getString(o, "key");
            String text = getString(o, "text");
            return new Opcion(key, text);
        }

        // Caso 2: viene como string "A. texto", "B) texto", "C - texto", etc.
        if (json.isJsonPrimitive()) {
            String raw = json.getAsString();
            if (raw == null) return null;
            raw = raw.trim();

            // key = primera letra A–D
            String key = raw.isEmpty() ? null : String.valueOf(Character.toUpperCase(raw.charAt(0)));

            // texto = quita "A.", "A)", "A -", "A:" al inicio + espacios
            String text = raw.replaceFirst("^[A-Da-d]\\s*[\\.|\\)|\\-|:]?\\s*", "").trim();

            return new Opcion(key, text);
        }

        throw new JsonParseException("Formato de opción no soportado: " + json);
    }

    private static String getString(JsonObject o, String k) {
        JsonElement e = o.get(k);
        return (e == null || e.isJsonNull()) ? null : e.getAsString();
    }
}
