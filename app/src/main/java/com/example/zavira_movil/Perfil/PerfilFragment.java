package com.example.zavira_movil.Perfil;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.zavira_movil.LoginActivity;
import com.example.zavira_movil.R;
import com.example.zavira_movil.databinding.FragmentPerfilBinding;
import com.example.zavira_movil.local.TokenManager;
import com.example.zavira_movil.model.Estudiante;
import com.example.zavira_movil.model.KolbResultado;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {

    private FragmentPerfilBinding binding;
    private ApiService api;

    // --- Cámara/Galería
    private Uri cameraOutputUri;

    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;

    // ===== Persistencia de foto =====
    private static final String PREFS_NAME    = "perfil_prefs";
    private static final String KEY_FOTO_PATH = "foto_path";
    private static final String AVATAR_FILE   = "avatar.jpg";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Permiso de cámara
        requestCameraPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                    if (granted) startCamera();
                });

        // Tomar foto
        takePictureLauncher =
                registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                    if (success && cameraOutputUri != null) {
                        // Muestra y PERSISTE
                        setPhoto(cameraOutputUri);
                    } else {
                        eliminarFotoLocal();
                    }
                });

        // Elegir de galería
        pickImageLauncher =
                registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) {
                        // Muestra y PERSISTE
                        setPhoto(uri);
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);


        binding.icon.setOnClickListener(v -> showPickerDialog());

        // Cargar foto guardada (si existe)
        cargarFotoGuardadaSiExiste();

        cargarPerfil();
        cargarKolb();
    }

    // ===================== CÁMARA / GALERÍA =====================

    private void showPickerDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Foto de perfil")
                .setItems(new CharSequence[]{"Tomar foto", "Elegir de la galería", "Eliminar foto"}, (d, which) -> {
                    if (which == 0) {
                        launchCameraFlow();
                    } else if (which == 1) {
                        launchGallery();
                    } else {
                        eliminarFotoLocal();
                    }
                })
                .show();
    }

    private void launchCameraFlow() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        try {
            cameraOutputUri = createImageUri();
            takePictureLauncher.launch(cameraOutputUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void launchGallery() {
        pickImageLauncher.launch("image/*");
    }

    private Uri createImageUri() throws IOException {
        File cacheDir = requireContext().getCacheDir();
        File file = File.createTempFile("avatar_", ".jpg", cacheDir);
        String authority = requireContext().getPackageName() + ".fileprovider";
        return FileProvider.getUriForFile(requireContext(), authority, file);
    }

    // ===================== Foto: mostrar y PERSISTIR =====================

    private void setPhoto(@NonNull Uri uri) {
        // 1) Mostrar
        Glide.with(requireContext())
                .load(uri)
                .placeholder(R.drawable.robot)
                .into(binding.icon);

        // 2) Persistir: copiar a filesDir/avatar.jpg y guardar ruta en prefs
        File saved = copyUriToInternalFile(uri, AVATAR_FILE);
        if (saved != null) {
            guardarPathFoto(saved.getAbsolutePath());
        }
    }

    /** Copia el contenido del Uri a filesDir/filename. Devuelve el File o null si falla. */
    @Nullable
    private File copyUriToInternalFile(@NonNull Uri src, @NonNull String filename) {
        File outFile = new File(requireContext().getFilesDir(), filename);
        try (InputStream in = requireContext().getContentResolver().openInputStream(src);
             OutputStream out = new FileOutputStream(outFile)) {
            if (in == null) return null;
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            out.flush();
            return outFile;
        } catch (Exception e) {
            Log.e("PERFIL_COPY", "Error copiando imagen", e);
            return null;
        }
    }

    private void cargarFotoGuardadaSiExiste() {
        String path = requireContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_FOTO_PATH, null);

        if (path != null) {
            File f = new File(path);
            if (f.exists()) {
                Glide.with(this).load(f).placeholder(R.drawable.robot).into(binding.icon);
                return;
            } else {
                // limpia pref si el archivo ya no existe
                requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit().remove(KEY_FOTO_PATH).apply();
            }
        }
        binding.icon.setImageResource(R.drawable.robot);
    }

    private void guardarPathFoto(@NonNull String absolutePath) {
        requireContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_FOTO_PATH, absolutePath)
                .apply();
    }

    private void eliminarFotoLocal() {
        // borra archivo persistido (si existe) y limpia pref
        String path = requireContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_FOTO_PATH, null);

        if (path != null) {
            try {
                File f = new File(path);
                if (f.exists()) f.delete();
            } catch (Exception ignored) {}
        }

        requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().remove(KEY_FOTO_PATH).apply();

        binding.icon.setImageResource(R.drawable.robot);
    }

    // ===================== PERFIL / KOLB =====================

    private void cargarPerfil() {
        api.getPerfilEstudiante().enqueue(new Callback<Estudiante>() {
            @Override
            public void onResponse(Call<Estudiante> call, Response<Estudiante> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    logHttpError("PROFILE_PERFIL", resp);
                    return;
                }
                Estudiante e = resp.body();

                // >>> SOLO VALORES (sin "Institución: ", etc.)
                binding.tvInstitucion.setText(safe(e.getNombreInstitucion()));
                binding.tvNombre.setText(safe(e.getNombreUsuario()) + " " + safe(e.getApellido()));
                binding.tvDocumento.setText(safe(e.getNumeroDocumento()));
                binding.tvTipoDoc.setText(safe(e.getTipoDocumento()));
                binding.tvGrado.setText(safe(e.getGrado()));
                binding.tvCurso.setText(safe(e.getCurso()));
                binding.tvJornada.setText(safe(e.getJornada()));
                binding.tvCorreo.setText(safe(e.getCorreo()));
                binding.tvTelefono.setText(safe(e.getTelefono()));
                binding.tvDireccion.setText(safe(e.getDireccion()));
                String estado = (e.getIsActive() != null && e.getIsActive()) ? "Activo" : "Inactivo";
                binding.tvEstado.setText(estado);

                // Foto: si no hay local, usa la del backend
                String localPath = requireContext()
                        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .getString(KEY_FOTO_PATH, null);

                if ((localPath == null || localPath.isEmpty())
                        && e.getFotoUrl() != null && !e.getFotoUrl().isEmpty()) {
                    Glide.with(PerfilFragment.this)
                            .load(e.getFotoUrl())
                            .placeholder(R.drawable.robot)
                            .into(binding.icon);
                }
            }

            @Override
            public void onFailure(Call<Estudiante> call, Throwable t) {
                Log.e("PROFILE_PERFIL_FAIL", "onFailure", t);
            }
        });
    }

    private void cargarKolb() {
        api.obtenerResultado().enqueue(new Callback<KolbResultado>() {
            @Override
            public void onResponse(Call<KolbResultado> call, Response<KolbResultado> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    logHttpError("PROFILE_KOLB", resp);
                    return;
                }
                KolbResultado r = resp.body();

                // >>> SOLO VALORES (sin "Estilo: ", "Fecha: ", etc.)
                binding.tvEstilo.setText(safe(r.getEstilo()));
                binding.tvFechaKolb.setText(formatearFechaFlexible(r.getFecha()));
                binding.tvCaracteristicas.setText(limpiarTexto(r.getCaracteristicas()));
                binding.tvRecomendaciones.setText(limpiarTexto(r.getRecomendaciones()));
            }

            @Override
            public void onFailure(Call<KolbResultado> call, Throwable t) {
                Log.e("PROFILE_KOLB_FAIL", "onFailure", t);
            }
        });
    }


    // ===================== Helpers =====================

    private static void logHttpError(String tag, Response<?> response) {
        try {
            ResponseBody err = response.errorBody();
            Log.e(tag, "HTTP " + response.code() + " - " + (err != null ? err.string() : "sin cuerpo"));
        } catch (Exception ignored) { }
    }

    private static String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }

    private static String limpiarTexto(String s) {
        if (s == null) return "-";
        String out = s.replace("\\t", " ")
                .replace("\t", " ")
                .replace("\\n", "\n")
                .replace("\r", "");
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
                if (d != null) return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(d);
            } catch (ParseException ignored) { }
        }
        return fechaIso;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
