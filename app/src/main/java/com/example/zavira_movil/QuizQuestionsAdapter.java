package com.example.zavira_movil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.model.Question;

import java.util.ArrayList;
import java.util.List;

public class QuizQuestionsAdapter extends RecyclerView.Adapter<QuizQuestionsAdapter.VH> {

    private final List<Question> data = new ArrayList<>();
    private final List<String> marcadas = new ArrayList<>(); // "A".."D"

    public QuizQuestionsAdapter(List<Question> preguntas) {
        if (preguntas != null) data.addAll(preguntas);
        for (int i = 0; i < data.size(); i++) marcadas.add(null);
    }
    public List<String> getMarcadas() { return marcadas; }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_question, p, false);
        return new VH(view);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Question q = data.get(pos);
        h.tvEnunciado.setText(q.enunciado != null ? q.enunciado : "");

        h.group.setOnCheckedChangeListener(null);
        h.group.clearCheck();

        set(h.rbA, q, 0);
        set(h.rbB, q, 1);
        set(h.rbC, q, 2);
        set(h.rbD, q, 3);

        String saved = marcadas.get(pos);
        if ("A".equals(saved)) h.rbA.setChecked(true);
        else if ("B".equals(saved)) h.rbB.setChecked(true);
        else if ("C".equals(saved)) h.rbC.setChecked(true);
        else if ("D".equals(saved)) h.rbD.setChecked(true);

        h.group.setOnCheckedChangeListener((g, id) -> {
            String key = null;
            if (id == R.id.rbA) key = keyOf(q, 0);
            else if (id == R.id.rbB) key = keyOf(q, 1);
            else if (id == R.id.rbC) key = keyOf(q, 2);
            else if (id == R.id.rbD) key = keyOf(q, 3);
            marcadas.set(h.getBindingAdapterPosition(), key);
        });
    }

    private void set(RadioButton rb, Question q, int i) {
        if (q.opciones != null && q.opciones.size() > i && q.opciones.get(i) != null) {
            rb.setVisibility(View.VISIBLE);
            rb.setText(q.opciones.get(i).text != null ? q.opciones.get(i).text : "");
        } else {
            rb.setVisibility(View.GONE);
            rb.setText("");
        }
    }
    private String keyOf(Question q, int i) {
        if (q.opciones != null && q.opciones.size() > i && q.opciones.get(i) != null) {
            return q.opciones.get(i).key;
        }
        return null;
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvEnunciado; RadioGroup group; RadioButton rbA, rbB, rbC, rbD;
        VH(@NonNull View v) {
            super(v);
            tvEnunciado = v.findViewById(R.id.tvEnunciado);
            group = v.findViewById(R.id.group);
            rbA = v.findViewById(R.id.rbA);
            rbB = v.findViewById(R.id.rbB);
            rbC = v.findViewById(R.id.rbC);
            rbD = v.findViewById(R.id.rbD);
        }
    }
}