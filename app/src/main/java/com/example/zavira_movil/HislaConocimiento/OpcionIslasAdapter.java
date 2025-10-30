package com.example.zavira_movil.HislaConocimiento;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.regex.*;

public class OpcionIslasAdapter implements JsonDeserializer<OpcionIslas> {
    private static final Pattern P = Pattern.compile("^[A-Za-z]\\s*\\.\\s*(?:[A-Za-z]\\s*\\.\\s*)?(.*)$");

    @Override
    public OpcionIslas deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext c)
            throws JsonParseException {

        if (json.isJsonObject()) {
            JsonObject o = json.getAsJsonObject();
            String letra = get(o, "letra");
            String texto = get(o, "texto");
            if (texto == null) texto = get(o, "opcion");
            return new OpcionIslas(letra, texto);
        }

        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            String raw = json.getAsString();
            String letra = (raw != null && !raw.isEmpty()) ? ("" + Character.toUpperCase(raw.charAt(0))) : null;
            String texto = raw;

            Matcher m = P.matcher(raw == null ? "" : raw);
            if (m.find()) texto = m.group(1).trim();
            if (texto != null) texto = texto.replaceFirst("^[A-Za-z]\\s*\\.\\s*", "").trim();

            return new OpcionIslas(letra, texto);
        }

        throw new JsonParseException("Formato no soportado en opcion: " + json);
    }

    private static String get(JsonObject o, String k) {
        return (o.has(k) && !o.get(k).isJsonNull()) ? o.get(k).getAsString() : null;
    }
}
