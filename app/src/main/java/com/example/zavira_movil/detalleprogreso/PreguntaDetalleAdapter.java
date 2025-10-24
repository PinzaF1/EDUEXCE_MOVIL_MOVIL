package com.example.zavira_movil.detalleprogreso;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;

import java.util.List;

public class PreguntaDetalleAdapter extends RecyclerView.Adapter<PreguntaDetalleAdapter.VH> {

    private final List<ProgresoDetalleResponse.Pregunta> items;

    public PreguntaDetalleAdapter(List<ProgresoDetalleResponse.Pregunta> items) { this.items = items; }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_pregunta_detalle, p, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        ProgresoDetalleResponse.Pregunta q = items.get(pos);
        h.tvTitulo.setText("Pregunta " + q.orden);
        h.tvEnunciado.setText(q.enunciado);

        h.tvMarcada.setText("Marcada: " + (q.marcada==null? "-" : q.marcada));
        h.tvCorrecta.setText("Correcta: " + (q.correcta==null? "-" : q.correcta));

        h.itemView.setBackgroundResource(q.es_correcta ? R.drawable.bg_alt_verde : R.drawable.bg_alt_rojo);

        h.tvExplicacion.setText(q.explicacion==null? "" : q.explicacion);
        h.tvTiempo.setText(q.tiempo_empleado_seg==null? "" : (q.tiempo_empleado_seg + "s"));
    }

    @Override public int getItemCount() { return items==null?0:items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvEnunciado, tvMarcada, tvCorrecta, tvExplicacion, tvTiempo;
        VH(View v){
            super(v);
            tvTitulo = v.findViewById(R.id.tvTitulo);
            tvEnunciado = v.findViewById(R.id.tvEnunciado);
            tvMarcada = v.findViewById(R.id.tvMarcada);
            tvCorrecta = v.findViewById(R.id.tvCorrecta);
            tvExplicacion = v.findViewById(R.id.tvExplicacion);
            tvTiempo = v.findViewById(R.id.tvTiempo);
        }
    }
}
