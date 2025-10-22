package com.example.zavira_movil.niveleshome;

import com.example.zavira_movil.model.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Mapea DTOs del backend (ApiQuestion) a tu modelo de app (Question). */
public final class ApiQuestionMapper {
    private ApiQuestionMapper() {}

    private static final Pattern LABELED = Pattern.compile("^\\s*([A-Za-z])\\s*[\\.)]\\s*(.*)$");

    public static Question toApp(ApiQuestion a) {
        if (a == null) return null;

        Question q = new Question();
        q.id_pregunta = (a.idPregunta != null) ? String.valueOf(a.idPregunta) : null;
        q.area        = a.area;
        q.subtema     = a.subtema;
        q.dificultad  = a.dificultad;
        q.enunciado   = a.enunciado;

        if (a.opciones != null) {
            int idx = 0;
            for (String raw : a.opciones) {
                if (raw == null) continue;
                String text = raw.trim();

                String key = null;
                String val = text;

                // Intenta extraer "A.", "B)", etc.
                Matcher m = LABELED.matcher(text);
                if (m.find()) {
                    key = m.group(1).toUpperCase();
                    val = m.group(2).trim();
                } else {
                    // Si no viene rotulada, rotula por posición
                    key = String.valueOf((char) ('A' + idx));
                }

                q.addOption(key, val);
                idx++;
            }
        }
        return q;
    }

    public static ArrayList<Question> toAppList(List<ApiQuestion> list) {
        ArrayList<Question> out = new ArrayList<>();
        if (list == null) return out;
        for (ApiQuestion a : list) {
            Question q = toApp(a);
            if (q != null) out.add(q);
        }
        return out;
    }
}
