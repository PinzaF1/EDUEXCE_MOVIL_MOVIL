// ApiQuestionMapper.java
package com.example.zavira_movil.niveleshome;

import com.example.zavira_movil.model.Question;
import java.util.ArrayList;
import java.util.List;

public final class ApiQuestionMapper {
    private ApiQuestionMapper() {}

    public static ArrayList<Question> toAppList(List<ApiQuestion> apiQs) {
        ArrayList<Question> out = new ArrayList<>();
        if (apiQs == null) return out;

        for (ApiQuestion q : apiQs) {
            if (q == null) continue;
            Question qq = new Question();
            qq.id_pregunta = q.id_pregunta != null ? String.valueOf(q.id_pregunta) : null;
            qq.area       = q.area;
            qq.subtema    = q.subtema;
            qq.enunciado  = q.enunciado;
            if (q.opciones != null) {
                // Genera claves A, B, C, D...
                for (int i = 0; i < q.opciones.size(); i++) {
                    String key = String.valueOf((char)('A' + i));
                    String text = q.opciones.get(i);
                    qq.addOption(key, text);
                }
            }
            out.add(qq);
        }
        return out;
    }
}
