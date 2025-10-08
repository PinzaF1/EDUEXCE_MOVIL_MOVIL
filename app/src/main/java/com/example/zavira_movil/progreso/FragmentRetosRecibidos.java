// app/src/main/java/com/example/zavira_movil/progreso/FragmentRetosRecibidos.java
package com.example.zavira_movil.progreso;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.example.zavira_movil.R;

public class FragmentRetosRecibidos extends Fragment {
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_retos_recibidos, container, false);
        TextView tv = v.findViewById(R.id.tvEmpty);
        if (tv != null) {
            tv.setText("No tienes retos recibidos por ahora.");
        }
        return v;
    }
}
