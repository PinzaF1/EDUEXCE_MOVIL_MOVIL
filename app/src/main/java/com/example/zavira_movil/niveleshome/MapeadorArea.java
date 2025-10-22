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

        if (s.contains("social") || s.contains("ciudada")) return SOCIALES;
        if (s.startsWith("matem"))                          return MATEMATICAS;
        if (s.startsWith("lengua") || s.contains("lectura"))return LENGUAJE;
        if (s.contains("naturales") || s.equals("ciencias"))return CIENCIAS_NATURALES;
        if (s.startsWith("ingl") || s.equals("english"))    return INGLES;

        return uiTitle != null ? uiTitle.trim() : "";
    }

    /** Normaliza subtemas comunes a su forma exacta en tu banco. */
    public static String normalizeSubtema(String sub) {
        if (sub == null) return "";
        String raw = sub.trim();
        String t = norm(raw);

        // ---- Matemáticas ----
        if (t.equals("operaciones con numeros enteros")) return "Operaciones con números enteros";
        if (t.equals("razones y proporciones")) return "Razones y proporciones";
        if (t.equals("regla de tres simple y compuesta")) return "Regla de tres simple y compuesta";
        if (t.equals("porcentajes y tasas (aumento, descuento, interes simple)")
                || t.equals("porcentajes y tasas (aumento, descuento, interes simple)")) // por si sin acento
            return "Porcentajes y tasas (aumento, descuento, interés simple)";
        if (t.equals("funciones y graficas")) return "Funciones y Gráficas";

        // ---- Lenguaje / Lectura crítica ----
        if (t.equals("comprension lectora (sentido global y local)")) return "Comprensión lectora (sentido global y local)";
        if (t.equals("conectores logicos (causa, contraste, condicion, secuencia)")) return "Conectores lógicos (causa, contraste, condición, secuencia)";
        if (t.equals("identificacion de argumentos y contraargumentos")) return "Identificación de argumentos y contraargumentos";
        if (t.equals("idea principal y proposito comunicativo")) return "Idea principal y propósito comunicativo";
        if (t.equals("hecho vs. opinion e inferencias")) return "Hecho vs. opinión e inferencias";

        // ---- Sociales ----
        if (t.equals("constitucion de 1991 y organizacion del estado")) return "Constitución de 1991 y organización del Estado";
        if (t.equals("historia de colombia - frente nacional")) return "Historia de Colombia - Frente Nacional";
        if (t.equals("guerras mundiales y guerra fria")) return "Guerras Mundiales y Guerra Fría";
        if (t.equals("geografia de colombia (mapas, territorio y ambiente)")) return "Geografía de Colombia (mapas, territorio y ambiente)";
        if (t.equals("economia y ciudadania economica (globalizacion y desigualdad)")) return "Economía y ciudadanía económica (globalización y desigualdad)";

        // ---- Ciencias Naturales ----
        if (t.equals("indagacion cientifica (variables, control e interpretacion de datos)")) return "Indagación científica (variables, control e interpretación de datos)";
        if (t.equals("fuerzas, movimiento y energia")) return "Fuerzas, movimiento y energía";
        if (t.equals("materia y cambios (mezclas, reacciones y conservacion)")) return "Materia y cambios (mezclas, reacciones y conservación)";
        if (t.equals("genetica y herencia")) return "Genética y herencia";
        if (t.equals("ecosistemas y cambio climatico (cts)")) return "Ecosistemas y cambio climático (CTS)";

        // ---- Inglés ----
        if (t.equals("verb to be (am, is, are)")) return "Verb to be (am, is, are)";
        if (t.equals("present simple (afirmacion, negacion y preguntas)")) return "Present Simple (afirmación, negación y preguntas)";
        if (t.equals("past simple (verbos regulares e irregulares)")) return "Past Simple (verbos regulares e irregulares)";
        if (t.equals("comparatives and superlatives")) return "Comparatives and superlatives";
        if (t.equals("subject/object pronouns & possessive adjectives")) return "Subject/Object pronouns & Possessive adjectives";

        // Sin regla: envía tal cual vino de la UI
        return raw;
    }
}
