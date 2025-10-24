package com.example.zavira_movil.detalleprogreso;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;

public class FragmentDetallePreguntas extends Fragment {
    private RecyclerView rv;
    private ProgresoDetalleResponse data;

    public void setData(ProgresoDetalleResponse data) {
        this.data = data;
        if (getView()!=null) bind();
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = inf.inflate(R.layout.fragment_detalle_preguntas, c, false);
        rv = v.findViewById(R.id.rvPreguntas);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        if (data!=null) bind();
        return v;
    }

    private void bind() {
        rv.setAdapter(new PreguntaDetalleAdapter(data.preguntas));
    }
}
