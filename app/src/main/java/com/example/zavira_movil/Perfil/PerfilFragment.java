package com.example.zavira_movil.Perfil;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
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
import com.example.zavira_movil.R;
import com.example.zavira_movil.databinding.FragmentPerfilBinding;
import com.example.zavira_movil.model.Estudiante;
import com.example.zavira_movil.model.KolbResultado;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {

    private FragmentPerfilBinding binding;
    private ApiService api;

    // ID del usuario (PUT /movil/perfil/{id} y para foto por usuario)
    private Integer perfilUserId = null;

    // Cámara/Galería
    private Uri cameraOutputUri;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;

    // Persistencia de foto
    private static final String PREFS_NAME = "perfil_prefs";
    private static final String KEY_FOTO_PATH_BASE = "foto_path";
    private static final String TMP_KEY = KEY_FOTO_PATH_BASE + "_tmp";
    private static final String AVATAR_FILE_BASE = "avatar"; // avatar_<id>.jpg

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestCameraPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                    if (granted) startCamera();
                });

        takePictureLauncher =
                registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                    if (success && cameraOutputUri != null) setPhoto(cameraOutputUri);
                    else eliminarFotoLocal();
                });

        pickImageLauncher =
                registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) setPhoto(uri);
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

        // 1) Intentar obtener el id desde el JWT
        perfilUserId = getUserIdFromToken();

        // Foto: picker
        binding.icon.setOnClickListener(v -> showPickerDialog());

        // Cargar lo que haya (si no hay id aún, usa clave y archivo temporales)
        cargarFotoPreferida(/*remoteUrl=*/null);

        // Editar info personal
        binding.rowEditarPerfil.setOnClickListener(v -> {
            if (perfilUserId == null) {
                android.widget.Toast.makeText(requireContext(), "No se puede editar: sin ID de usuario", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            abrirEditorContacto();
        });

        cargarPerfil();
        cargarKolb();
    }

    // ===================== ID =====================

    @Nullable
    private Integer getUserIdFromToken() {
        try {
            String token = com.example.zavira_movil.local.TokenManager.getToken(requireContext());
            if (token == null) return null;
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;

            String payloadJson = new String(
                    Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP),
                    StandardCharsets.UTF_8
            );
            JSONObject obj = new JSONObject(payloadJson);

            if (obj.has("id")) return obj.getInt("id");
            if (obj.has("user_id")) return obj.getInt("user_id");
            if (obj.has("uid")) return obj.getInt("uid");
            if (obj.has("sub")) {
                try { return Integer.parseInt(obj.get("sub").toString()); } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Nullable
    private Integer guessIdFromPerfil(Estudiante e) {
        try { return (Integer) e.getClass().getMethod("getIdUsuario").invoke(e); } catch (Exception ignored) {}
        try { return (Integer) e.getClass().getMethod("getId").invoke(e); } catch (Exception ignored) {}
        try { return (Integer) e.getClass().getMethod("getUserId").invoke(e); } catch (Exception ignored) {}
        try { return (Integer) e.getClass().getMethod("getIdEstudiante").invoke(e); } catch (Exception ignored) {}
        try {
            Object usuario = e.getClass().getMethod("getUsuario").invoke(e);
            if (usuario != null) {
                try { return (Integer) usuario.getClass().getMethod("getId").invoke(usuario); } catch (Exception ignored) {}
                try { return (Integer) usuario.getClass().getMethod("getUserId").invoke(usuario); } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ===================== Helpers foto por usuario =====================

    private String prefsKeyForUser() {
        return (perfilUserId == null) ? TMP_KEY : KEY_FOTO_PATH_BASE + "_" + perfilUserId;
    }

    private String fileNameForUser() {
        return (perfilUserId == null) ? AVATAR_FILE_BASE + "_tmp.jpg" : AVATAR_FILE_BASE + "_" + perfilUserId + ".jpg";
    }

    private File absoluteFileForUser() {
        return new File(requireContext().getFilesDir(), fileNameForUser());
    }

    // ===================== Cámara / Galería =====================

    private void showPickerDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Foto de perfil")
                .setItems(new CharSequence[]{"Tomar foto", "Elegir de la galería", "Eliminar foto"}, (d, which) -> {
                    if (which == 0) launchCameraFlow();
                    else if (which == 1) launchGallery();
                    else eliminarFotoLocal();
                })
                .show();
    }

    private void launchCameraFlow() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) startCamera();
        else requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void startCamera() {
        try {
            cameraOutputUri = createImageUri();
            takePictureLauncher.launch(cameraOutputUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void launchGallery() { pickImageLauncher.launch("image/*"); }

    private Uri createImageUri() throws IOException {
        File cacheDir = requireContext().getCacheDir();
        File file = File.createTempFile("avatar_", ".jpg", cacheDir);
        String authority = requireContext().getPackageName() + ".fileprovider";
        return FileProvider.getUriForFile(requireContext(), authority, file);
    }

    private void setPhoto(@NonNull Uri uri) {
        // Mostrar de inmediato
        Glide.with(requireContext())
                .load(uri)
                .placeholder(R.drawable.usuario)
                .into(binding.icon);

        // Guardar archivo por-usuario y persistir ruta
        File saved = copyUriToInternalFile(uri, fileNameForUser());
        if (saved != null) guardarPathFoto(saved.getAbsolutePath());
    }

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

    /**
     * Orden de carga:
     * 1) Buscar ruta en SharedPrefs por-usuario (o tmp).
     * 2) Si no hay o no existe el archivo, buscar directamente el archivo avatar_<id>.jpg;
     *    si existe, mostrarlo y reconstruir la preferencia.
     * 3) Si tampoco, usar remoteUrl (si viene).
     * 4) Si nada, robot.
     */
    private void cargarFotoPreferida(@Nullable String remoteUrl) {
        var sp   = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String k = prefsKeyForUser();
        String path = sp.getString(k, null);

        // 1) Preferencia existente
        if (path != null) {
            File f = new File(path);
            if (f.exists()) {
                Glide.with(this).load(f).placeholder(R.drawable.usuario).into(binding.icon);
                return;
            } else {
                sp.edit().remove(k).apply();
            }
        }

        // 2) Archivo por convención (por si borraron las prefs al cerrar sesión)
        File byName = absoluteFileForUser();
        if (byName.exists()) {
            Glide.with(this).load(byName).placeholder(R.drawable.usuario).into(binding.icon);
            // reconstruir preferencia
            sp.edit().putString(k, byName.getAbsolutePath()).apply();
            return;
        }

        // 3) URL remota del backend
        if (remoteUrl != null && !remoteUrl.trim().isEmpty() && !"null".equalsIgnoreCase(remoteUrl.trim())) {
            Glide.with(this).load(remoteUrl.trim()).placeholder(R.drawable.usuario).into(binding.icon);
            return;
        }

        // 4) Placeholder
        binding.icon.setImageResource(R.drawable.usuario);
    }

    private void guardarPathFoto(@NonNull String absolutePath) {
        requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(prefsKeyForUser(), absolutePath)
                .apply();
    }

    private void eliminarFotoLocal() {
        var sp   = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String k = prefsKeyForUser();
        String path = sp.getString(k, null);

        if (path != null) {
            try { File f = new File(path); if (f.exists()) f.delete(); } catch (Exception ignored) {}
        }
        sp.edit().remove(k).apply();
        binding.icon.setImageResource(R.drawable.usuario);
    }

    // ===================== Perfil / Kolb =====================

    private void cargarPerfil() {
        api.getPerfilEstudiante().enqueue(new Callback<Estudiante>() {
            @Override public void onResponse(Call<Estudiante> call, Response<Estudiante> resp) {
                if (!resp.isSuccessful() || resp.body() == null) { logHttpError("PROFILE_PERFIL", resp); return; }
                Estudiante e = resp.body();

                // Migración TMP -> id real si acabamos de conocer el id
                Integer oldId = perfilUserId;
                if (perfilUserId == null) perfilUserId = guessIdFromPerfil(e);
                if (oldId == null && perfilUserId != null) {
                    var sp = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    String tmpPath = sp.getString(TMP_KEY, null);
                    if (tmpPath != null) {
                        // mueve la preferencia a la key por usuario (el archivo ya está en /files)
                        sp.edit()
                                .remove(TMP_KEY)
                                .putString(prefsKeyForUser(), tmpPath)
                                .apply();
                    }
                }

                // Decide foto (local por-usuario > archivo por nombre > remota > robot)
                cargarFotoPreferida(e.getFotoUrl());

                binding.rowEditarPerfil.setEnabled(perfilUserId != null);

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
            }
            @Override public void onFailure(Call<Estudiante> call, Throwable t) { Log.e("PROFILE_PERFIL_FAIL", "onFailure", t); }
        });
    }

    private void cargarKolb() {
        api.obtenerResultado().enqueue(new Callback<KolbResultado>() {
            @Override public void onResponse(Call<KolbResultado> call, Response<KolbResultado> resp) {
                if (!resp.isSuccessful() || resp.body() == null) { logHttpError("PROFILE_KOLB", resp); return; }
                KolbResultado r = resp.body();
                binding.tvEstilo.setText(safe(r.getEstilo()));
                binding.tvFechaKolb.setText(formatearFechaFlexible(r.getFecha()));
                binding.tvCaracteristicas.setText(limpiarTexto(r.getCaracteristicas()));
                binding.tvRecomendaciones.setText(limpiarTexto(r.getRecomendaciones()));
            }
            @Override public void onFailure(Call<KolbResultado> call, Throwable t) { Log.e("PROFILE_KOLB_FAIL", "onFailure", t); }
        });
    }

    // ===================== Editar contacto =====================

    private void abrirEditorContacto() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View content = LayoutInflater.from(requireContext()).inflate(R.layout.editar_infopersonal, null, false);
        dialog.setContentView(content);

        TextInputEditText etCorreo    = content.findViewById(R.id.etCorreo);
        TextInputEditText etTelefono  = content.findViewById(R.id.etTelefono);
        TextInputEditText etDireccion = content.findViewById(R.id.etDireccion);
        MaterialButton btnGuardar     = content.findViewById(R.id.btnGuardar);

        etCorreo.setText(textOrEmpty(binding.tvCorreo));
        etTelefono.setText(textOrEmpty(binding.tvTelefono));
        etDireccion.setText(textOrEmpty(binding.tvDireccion));

        btnGuardar.setOnClickListener(v -> {
            String correo    = str(etCorreo);
            String telefono  = str(etTelefono);
            String direccion = str(etDireccion);

            if (!isEmpty(correo) && !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                etCorreo.setError("Correo inválido"); return;
            }
            if (!isEmpty(telefono) && !Pattern.compile("^\\+?[0-9\\s\\-()]{7,20}$").matcher(telefono).matches()) {
                etTelefono.setError("Teléfono inválido"); return;
            }
            if (!isEmpty(direccion) && direccion.length() > 255) {
                etDireccion.setError("Dirección demasiado larga"); return;
            }
            if (perfilUserId == null) {
                android.widget.Toast.makeText(requireContext(), "No se puede editar: sin ID de usuario", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            enviarEdicionContacto(perfilUserId, correo, direccion, telefono, dialog);
        });

        dialog.show();
    }

    private void enviarEdicionContacto(int userId, String correo, String direccion, String telefono, BottomSheetDialog dialog) {
        EditarPerfilRequest body = new EditarPerfilRequest(correo, direccion, telefono);
        api.editarPerfil(userId, body).enqueue(new Callback<EditarPerfilResponse>() {
            @Override
            public void onResponse(Call<EditarPerfilResponse> call, Response<EditarPerfilResponse> response) {
                if (!response.isSuccessful()) {
                    logHttpError("PROFILE_EDIT", response);
                    android.widget.Toast.makeText(requireContext(), "Error guardando cambios", android.widget.Toast.LENGTH_LONG).show();
                    return;
                }
                binding.tvCorreo.setText(isEmpty(correo) ? "-" : correo.trim());
                binding.tvTelefono.setText(isEmpty(telefono) ? "-" : telefono.trim());
                binding.tvDireccion.setText(isEmpty(direccion) ? "-" : direccion.trim());

                EditarPerfilResponse bodyResp = response.body();
                android.widget.Toast.makeText(
                        requireContext(),
                        (bodyResp != null && bodyResp.getMessage() != null) ? bodyResp.getMessage() : "Actualizado",
                        android.widget.Toast.LENGTH_SHORT
                ).show();

                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<EditarPerfilResponse> call, Throwable t) {
                Log.e("PROFILE_EDIT_FAIL", "onFailure", t);
                android.widget.Toast.makeText(requireContext(), "Error de red: " + t.getLocalizedMessage(), android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }

    // ===================== Utilidades =====================

    private static void logHttpError(String tag, Response<?> response) {
        try {
            ResponseBody err = response.errorBody();
            Log.e(tag, "HTTP " + response.code() + " - " + (err != null ? err.string() : "sin cuerpo"));
        } catch (Exception ignored) { }
    }

    private static String safe(String s) { return (s == null || s.trim().isEmpty()) ? "-" : s; }

    private static String limpiarTexto(String s) {
        if (s == null) return "-";
        String out = s.replace("\\t", " ").replace("\t", " ").replace("\\n", "\n").replace("\r", "");
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
                if (p.contains("'Z'") || p.endsWith("XXX")) in.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date d = in.parse(fechaIso);
                if (d != null) return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(d);
            } catch (ParseException ignored) { }
        }
        return fechaIso;
    }

    private String str(TextInputEditText et) { return et.getText() == null ? "" : et.getText().toString().trim(); }
    private boolean isEmpty(String s) { return s == null || s.trim().isEmpty(); }
    private String textOrEmpty(android.widget.TextView tv) {
        if (tv.getText() == null) return "";
        String t = tv.getText().toString().trim();
        return "-".equals(t) ? "" : t;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
