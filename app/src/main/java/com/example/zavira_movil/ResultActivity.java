package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.Home.HomeActivity;
import com.example.zavira_movil.databinding.ActivityResultBinding;
import com.example.zavira_movil.model.KolbResultado;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultActivity extends AppCompatActivity {

    private ActivityResultBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvNombreCompleto.setVisibility(View.GONE);
        binding.btnIrHome.setOnClickListener(v -> {
            Intent i = new Intent(ResultActivity.this, HomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });

        String extraFecha  = getIntent().getStringExtra("fecha");
        String extraEstilo = getIntent().getStringExtra("estilo");
        String extraCarac  = getIntent().getStringExtra("caracteristicas");
        String extraRec    = getIntent().getStringExtra("recomendaciones");

        binding.tvFecha.setText("Fecha: " + formatearFechaFlexible(extraFecha));
        binding.tvEstilo.setText("Estilo: " + safe(extraEstilo));
        binding.tvCaracteristicas.setText("Características: " + limpiarTexto(extraCarac));
        binding.tvRecomendaciones.setText("Recomendaciones: " + limpiarTexto(extraRec));

        boolean falta = isEmpty(extraFecha) || isEmpty(extraEstilo) || isEmpty(extraCarac) || isEmpty(extraRec);
        if (falta) {
            apiService = RetrofitClient.getInstance(this).create(ApiService.class);
            apiService.obtenerResultado().enqueue(new Callback<KolbResultado>() {
                @Override public void onResponse(Call<KolbResultado> call, Response<KolbResultado> r) {
                    if (!r.isSuccessful() || r.body() == null) {
                        Log.e("KOLB_RESULT", "code=" + r.code());
                        return;
                    }
                    KolbResultado k = r.body();

                    binding.tvFecha.setText("Fecha: " + formatearFechaFlexible(k.getFecha()));
                    binding.tvEstilo.setText("Estilo: " + safe(k.getEstilo()));
                    binding.tvCaracteristicas.setText("Características: " + limpiarTexto(k.getCaracteristicas()));
                    binding.tvRecomendaciones.setText("Recomendaciones: " + limpiarTexto(k.getRecomendaciones()));

                    if (k.getEstudiante() != null) {
                        binding.tvNombreCompleto.setVisibility(View.VISIBLE);
                        String doc = k.getDocumento() == null ? "" : (" • " + k.getDocumento());
                        binding.tvNombreCompleto.setText(k.getEstudiante() + doc);
                    }
                }
                @Override public void onFailure(Call<KolbResultado> call, Throwable t) {
                    Toast.makeText(ResultActivity.this, "No se pudo completar el resultado", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private static boolean isEmpty(String s) { return s == null || s.trim().isEmpty(); }
    private static String safe(String s) { return isEmpty(s) ? "-" : s; }

    private String limpiarTexto(String s) {
        if (s == null) return "-";
        String out = s.replace("\\t", " ")
                .replace("\t", " ")
                .replace("\\n", "\n")
                .replace("\r", "")
                .trim();
        return out.isEmpty() ? "-" : out;
    }

    private String formatearFechaFlexible(String iso) {
        if (isEmpty(iso)) return "-";
        List<String> pats = Arrays.asList(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd"
        );
        for (String p : pats) {
            try {
                SimpleDateFormat in = new SimpleDateFormat(p, Locale.getDefault());
                if (p.contains("'Z'") || p.endsWith("XXX")) in.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date d = in.parse(iso);
                if (d != null) return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(d);
            } catch (ParseException ignored) {}
        }
        return iso;
    }
}
