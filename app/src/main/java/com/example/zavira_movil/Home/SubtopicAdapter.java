package com.example.zavira_movil.Home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.Subject;

import java.util.ArrayList;
import java.util.List;

public class SubtopicAdapter extends RecyclerView.Adapter<SubtopicAdapter.VH> {

    private final List<Subject.Subtopic> data = new ArrayList<>();
    private final String area;
    private final int nivel;

    public SubtopicAdapter(List<Subject.Subtopic> data, String area, int nivel) {
        if (data != null) this.data.addAll(data);
        this.area = area;
        this.nivel = Math.max(1, Math.min(5, nivel));
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subtopic_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Subject.Subtopic s = data.get(position);
        h.tvTitle.setText(s.title);
        h.cbDone.setChecked(s.done);

        h.itemView.setOnClickListener(v -> {
            try {
                Context ctx = v.getContext();
                Intent i = new Intent(ctx, QuizActivity.class); // directo
                i.putExtra(QuizActivity.EXTRA_AREA, area);
                i.putExtra(QuizActivity.EXTRA_SUBTEMA, s.title);
                i.putExtra(QuizActivity.EXTRA_NIVEL, nivel);
                ctx.startActivity(i);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(v.getContext(), "No se pudo abrir el quiz", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        CheckBox cbDone; TextView tvTitle;
        VH(@NonNull View v) {
            super(v);
            cbDone = v.findViewById(R.id.cbDone);
            tvTitle = v.findViewById(R.id.tvSubtopicTitle);
        }
    }
}
