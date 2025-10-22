package com.example.zavira_movil.niveleshome;

import java.text.Normalizer;
import java.util.Locale;

public final class MapeadorArea {
    private MapeadorArea() {}

    // Literales canónicos para el backend
    public static final String LENGUAJE           = "Lenguaje";
    public static final String MATEMATICAS        = "Matematicas";
    public static final String SOCIALES           = "Sociales";
    public static final String CIENCIAS_NATURALES = "Ciencias Naturales";
    public static final String INGLES             = "Ingles";

    /** Normaliza: trim, minúsculas y sin acentos (para comparar). */
    private static String norm(String s) {
        if (s == null) return "";
        String t = Normalizer.normalize(s.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return t.toLowerCase(Locale.ROOT);
    }

    /** UI → API (área canónica). */
    public static String toApiArea(String uiTitle) {
        String s = norm(uiTitle);

        // Sociales / ciudadanas
        if (s.contains("social") || s.contains("socia") || s.contains("ciudada"))
            return SOCIALES;

        // Matemáticas
        if (s.startsWith("matem"))
            return MATEMATICAS;

        // Lenguaje (lectura crítica / lenguaje)
        if (s.startsWith("lengua") || s.startsWith("lenguaj") || s.contains("lectura"))
            return LENGUAJE;

        // Ciencias Naturales
        if (s.contains("ciencias naturales") || s.contains("naturales") || s.equals("ciencias"))
            return CIENCIAS_NATURALES;

        // Inglés
        if (s.startsWith("ingl") || s.equals("english"))
            return INGLES;

        // Fallback
        return uiTitle != null ? uiTitle.trim() : "";
    }

    /** Normaliza subtemas comunes a su forma exacta en tu banco. */
    public static String normalizeSubtema(String sub) {
        if (sub == null) return "";
        String raw = sub.trim();
        String t = norm(raw);

        // ---- Lenguaje / Lectura crítica ----
        if (t.equals("comprension literal"))     return "Comprensión lectora";
        if (t.equals("cohesion textual"))        return "Cohesión textual";
        if (t.equals("relaciones semanticas"))   return "Relaciones semánticas";
        if (t.equals("conectores logicos"))      return "Conectores lógicos";
        if (t.equals("figuras retoricas"))       return "Figuras retóricas";
        if (t.equals("proposito del autor"))     return "Propósito del autor";

        // ---- Matemáticas ----
        if (t.equals("aritmetica"))              return "Aritmética";
        if (t.equals("algebra"))                 return "Álgebra";
        if (t.equals("geometria"))               return "Geometría";
        if (t.equals("estadistica y probabilidad")) return "Estadística y Probabilidad";
        if (t.equals("funciones y graficas"))    return "Funciones y Gráficas";

        // ---- Sociales ----
        if (t.equals("constitucion de 1991 y organizacion del estado"))
            return "Constitución de 1991 y organización del Estado";
        if (t.equals("ciudadania"))              return "Ciudadanía";
        if (t.equals("pensamiento social"))      return "Pensamiento social";

        // ---- Ciencias Naturales ----
        if (t.equals("biologia"))                return "Biología";
        if (t.equals("quimica"))                 return "Química";
        if (t.equals("fisica"))                  return "Física";
        if (t.equals("ciencias de la tierra"))   return "Ciencias de la Tierra";
        if (t.equals("metodo cientifico"))       return "Método científico";

        // ---- Inglés ----
        if (t.equals("reading basico"))          return "Reading básico";
        if (t.equals("reading intermedio"))      return "Reading intermedio";
        if (t.equals("grammar y uso"))           return "Grammar y uso";
        if (t.equals("listening y contexto"))    return "Listening y contexto";
        if (t.equals("writing"))                 return "Writing";

        // Sin regla: envía tal cual vino de la UI
        return raw;
    }
}
