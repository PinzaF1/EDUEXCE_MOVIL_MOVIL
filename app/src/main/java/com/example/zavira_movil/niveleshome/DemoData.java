package com.example.zavira_movil.niveleshome;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.Level;
import com.example.zavira_movil.model.Subject;

import java.util.*;

public class DemoData {

    public static List<Subject> getSubjects() {
        List<Subject> list = new ArrayList<>();

        // ========================= MATEMÁTICAS
        Subject mate = subjectBase("Matemáticas", R.drawable.ic_math_24, R.drawable.bg_header_math);
        mate.levels = Arrays.asList(
                level("Nivel 1", "Operaciones con números enteros"),
                level("Nivel 2", "Razones y proporciones"),
                level("Nivel 3", "Regla de tres simple y compuesta"),
                level("Nivel 4", "Porcentajes y tasas (aumento, descuento, interés simple)"),
                level("Nivel 5", "Funciones y Gráficas")
        );
        list.add(mate);

        // ========================= LECTURA CRÍTICA (área API = Lenguaje)
        Subject lect = subjectBase("Lectura crítica", R.drawable.ic_read_24, R.drawable.bg_header_reading);
        lect.levels = Arrays.asList(
                level("Nivel 1", "Comprensión lectora (sentido global y local)"),
                level("Nivel 2", "Conectores lógicos (causa, contraste, condición, secuencia)"),
                level("Nivel 3", "Identificación de argumentos y contraargumentos"),
                level("Nivel 4", "Idea principal y propósito comunicativo"),
                level("Nivel 5", "Hecho vs. opinión e inferencias")
        );
        list.add(lect);

        // ========================= SOCIALES
        Subject soc = subjectBase("Sociales y ciudadanas", R.drawable.ic_social_24, R.drawable.bg_header_social);
        soc.levels = Arrays.asList(
                level("Nivel 1", "Constitución de 1991 y organización del Estado"),
                level("Nivel 2", "Historia de Colombia - Frente Nacional"),
                level("Nivel 3", "Guerras Mundiales y Guerra Fría"),
                level("Nivel 4", "Geografía de Colombia (mapas, territorio y ambiente"),
                level("Nivel 5", "Economía y ciudadanía económica (globalización y desigualdad)")
        );
        list.add(soc);

        // ========================= CIENCIAS NATURALES
        Subject cie = subjectBase("Ciencias naturales", R.drawable.ic_science_24, R.drawable.bg_header_science);
        cie.levels = Arrays.asList(
                level("Nivel 1", "Indagación científica (variables, control e interpretación de datos)"),
                level("Nivel 2", "Fuerzas, movimiento y energía"),
                level("Nivel 3", "Materia y cambios (mezclas, reacciones y conservación)"),
                level("Nivel 4", "Genética y herencia"),
                level("Nivel 5", "Ecosistemas y cambio climático (CTS)")
        );
        list.add(cie);

        // ========================= INGLÉS
        Subject eng = subjectBase("Inglés", R.drawable.ic_english_24, R.drawable.bg_header_english);
        eng.levels = Arrays.asList(
                level("Nivel 1", "Verb to be (am, is, are)"),
                level("Nivel 2", "Present Simple (afirmación, negación y preguntas)"),
                level("Nivel 3", "Past Simple (verbos regulares e irregulares)"),
                level("Nivel 4", "Comparatives and superlatives"),
                level("Nivel 5", "Subject/Object pronouns & Possessive adjectives")
        );
        list.add(eng);

        return list;
    }

    // helpers
    private static Subject subjectBase(String title, int icon, int header) {
        Subject s = new Subject();
        s.title = title;
        s.iconRes = icon;
        s.headerDrawableRes = header;
        s.done = 0;
        s.total = 5;
        return s;
    }

    // Usamos Level externo, no Subject.Level
    private static Level level(String name, String subtopic) {
        Level l = new Level(name);
        l.subtopics.add(new Subject.Subtopic(subtopic));
        return l;
    }
}
