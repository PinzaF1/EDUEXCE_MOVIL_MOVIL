package com.example.zavira_movil.Home;

import java.util.Locale;

public final class AreaMapper {
    private AreaMapper() {}

    public static String toApiArea(String uiTitle) {
        if (uiTitle == null) return "";

        String s = uiTitle.trim().toLowerCase(Locale.ROOT);

        // Matemáticas
        if (s.startsWith("matem")) return "Matemáticas";

        // Lectura crítica -> Lenguaje (así está en tu banco)
        if (s.contains("lectura")) return "Lenguaje";
        if (s.startsWith("lengua")) return "Lenguaje";

        // Ciencias naturales
        if (s.contains("ciencias") && s.contains("natural")) return "Ciencias";
        if (s.equals("ciencias")) return "Ciencias";

        // Sociales y ciudadanas
        if (s.contains("social")) return "Sociales";
        if (s.contains("soci"))   return "Sociales";

        // Inglés
        if (s.startsWith("ingl"))  return "Inglés";
        if (s.equals("english"))   return "Inglés";

        // Fallback
        return uiTitle.trim();
    }

    /** Normaliza algunos subtemas comunes a la forma más usada en el banco. */
    public static String normalizeSubtema(String sub) {
        if (sub == null) return "";
        String t = sub.trim();

        // Lenguaje
        if (t.equalsIgnoreCase("Comprensión literal"))       return "Comprensión lectora";
        if (t.equalsIgnoreCase("Cohesión textual"))          return "Cohesión y coherencia";
        if (t.equalsIgnoreCase("Figuras retóricas"))         return "Figuras retóricas";
        if (t.equalsIgnoreCase("Conectores lógicos"))        return "Conectores lógicos";

        // Matemáticas
        if (t.equalsIgnoreCase("Aritmética"))                return "Aritmética";
        if (t.equalsIgnoreCase("Álgebra"))                   return "Álgebra";
        if (t.equalsIgnoreCase("Geometría"))                 return "Geometría";
        if (t.equalsIgnoreCase("Estadística y Probabilidad"))return "Estadística y probabilidad";
        if (t.equalsIgnoreCase("Funciones y Gráficas"))      return "Funciones y gráficas";

        return t;
    }
}
