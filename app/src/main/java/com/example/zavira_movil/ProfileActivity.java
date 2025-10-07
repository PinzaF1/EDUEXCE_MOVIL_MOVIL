package com.example.zavira_movil;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.zavira_movil.databinding.ActivityProfileBinding;
import com.example.zavira_movil.local.TokenManager;
import com.example.zavira_movil.model.Estudiante;
import com.example.zavira_movil.model.KolbResultado;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private ApiService api;
    private static final int PICK_IMAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (TokenManager.getToken(this) == null) {
            irALoginYLimpiarPila();
            return;
        }

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        api = RetrofitClient.getInstance(this).create(ApiService.class);

        // Botón cerrar sesión
        binding.btnCerrarSesion.setOnClickListener(v -> {
            TokenManager.clearAll(ProfileActivity.this); // ✅ Usa el contexto correcto
            irALoginYLimpiarPila(); //
        });

        // Botón editar foto
        binding.btnEditarFoto.setOnClickListener(v -> mostrarOpcionesFoto());

        cargarPerfil();
        cargarKolb();
    }

    private void cargarPerfil() {
        api.getPerfilEstudiante().enqueue(new Callback<Estudiante>() {
            @Override
            public void onResponse(Call<Estudiante> call, Response<Estudiante> response) {
                if (!response.isSuccessful()) {
                    logHttpError("PROFILE_PERFIL", response);
                    Toast.makeText(ProfileActivity.this, "No se pudo cargar el perfil", Toast.LENGTH_SHORT).show();
                    return;
                }
                Estudiante e = response.body();
                if (e == null) {
                    Toast.makeText(ProfileActivity.this, "Perfil vacío", Toast.LENGTH_SHORT).show();
                    return;
                }
                binding.tvNombre.setText("Nombre: " + safe(e.getNombreUsuario()) + " " + safe(e.getApellido()));
                binding.tvDocumento.setText("Documento: " + safe(e.getNumeroDocumento()));
                binding.tvGrado.setText("Grado: " + safe(e.getGrado()));
                binding.tvCurso.setText("Curso: " + safe(e.getCurso()));
                binding.tvJornada.setText("Jornada: " + safe(e.getJornada()));
                binding.tvCorreo.setText("Correo: " + safe(e.getCorreo()));

                // Cargar foto si viene del backend
                if (e.getFotoUrl() != null && !e.getFotoUrl().isEmpty()) {
                    Glide.with(ProfileActivity.this)
                            .load(e.getFotoUrl())
                            .placeholder(R.drawable.ic_default_avatar)
                            .into(binding.ivFotoPerfil);
                }
            }

            @Override
            public void onFailure(Call<Estudiante> call, Throwable t) {
                Log.e("PROFILE_PERFIL_FAIL", "onFailure", t);
                Toast.makeText(ProfileActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarKolb() {
        api.obtenerResultado().enqueue(new Callback<KolbResultado>() {
            @Override
            public void onResponse(Call<KolbResultado> call, Response<KolbResultado> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    logHttpError("PROFILE_KOLB", response);
                    return;
                }
                KolbResultado r = response.body();
                binding.tvEstilo.setText("Estilo: " + safe(r.getEstilo()));
                binding.tvFechaKolb.setText("Fecha: " + formatearFechaFlexible(r.getFecha()));
                binding.tvCaracteristicas.setText("Características: " + limpiarTexto(r.getCaracteristicas()));
                binding.tvRecomendaciones.setText("Recomendaciones: " + limpiarTexto(r.getRecomendaciones()));
            }

            @Override
            public void onFailure(Call<KolbResultado> call, Throwable t) {
                Log.e("PROFILE_KOLB_FAIL", "onFailure", t);
            }
        });
    }

    private void mostrarOpcionesFoto() {
        String[] opciones = {"Elegir de galería", "Eliminar foto"};
        new android.app.AlertDialog.Builder(this)
                .setTitle("Editar foto de perfil")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) abrirGaleria();
                    else eliminarFoto();
                }).show();
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                File file = new File(imageUri.getPath()); // simplificado, usar FileUtils real en producción
                subirFoto(file);
            }
        }
    }

    private void subirFoto(File file) {
        // 🔹 Convierte el archivo en MultipartBody.Part para enviarlo al backend
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("foto", file.getName(), reqFile);

        api.subirFoto(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Foto actualizada", Toast.LENGTH_SHORT).show();
                    Glide.with(ProfileActivity.this).load(file).into(binding.ivFotoPerfil);
                } else {
                    Toast.makeText(ProfileActivity.this, "Error al subir foto", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void eliminarFoto() {
        api.eliminarFoto().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    binding.ivFotoPerfil.setImageResource(R.drawable.ic_default_avatar);
                    Toast.makeText(ProfileActivity.this, "Foto eliminada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Error al eliminar foto", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void irALoginYLimpiarPila() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private static void logHttpError(String tag, Response<?> response) {
        try {
            ResponseBody err = response.errorBody();
            Log.e(tag, "HTTP " + response.code() + " - " + (err != null ? err.string() : "sin cuerpo"));
        } catch (Exception ignored) {}
    }

    private static String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }

    private static String limpiarTexto(String s) {
        if (s == null) return "-";
        String out = s.replace("\\t", " ").replace("\t", " ")
                .replace("\\n", "\n").replace("\r", "");
        out = out.trim();
        return out.isEmpty() ? "-" : out;
    }

    private static String formatearFechaFlexible(String fechaIso) {
        if (fechaIso == null || fechaIso.trim().isEmpty()) return "-";
        List<String> patrones = Arrays.asList(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd"
        );
        for (String p : patrones) {
            try {
                SimpleDateFormat in = new SimpleDateFormat(p, Locale.getDefault());
                if (p.contains("'Z'") || p.endsWith("XXX")) {
                    in.setTimeZone(TimeZone.getTimeZone("UTC"));
                }
                Date d = in.parse(fechaIso);
                if (d != null) {
                    return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(d);
                }
            } catch (ParseException ignored) {}
        }
        return fechaIso;
    }
}
