package com.example.zavira_movil.Perfil;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.zavira_movil.BasicResponse;
import com.example.zavira_movil.LoginActivity;
import com.example.zavira_movil.R;
import com.example.zavira_movil.databinding.FragmentPerfilBinding;
import com.example.zavira_movil.model.CambiarPassword;
import com.example.zavira_movil.model.Estudiante;
import com.example.zavira_movil.model.KolbResultado;
import com.example.zavira_movil.model.LoginRequest;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {

    private FragmentPerfilBinding binding;
    private ApiService api;
    private Integer perfilUserId = null;

    // Cámara/Galería
    private Uri cameraOutputUri;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;

    private static final String PREFS_NAME = "perfil_prefs";
    private static final String KEY_FOTO_PATH_BASE = "foto_path";
    private static final String TMP_KEY = KEY_FOTO_PATH_BASE + "_tmp";
    private static final String AVATAR_FILE_BASE = "avatar";

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
        perfilUserId = getUserIdFromToken();

        binding.icon.setOnClickListener(v -> showPickerDialog());
        cargarFotoPreferida(null);
        
        // Punto verde - hacerlo movible manualmente
        View statusDot = binding.getRoot().findViewById(R.id.statusDot);
        View fotoFrame = binding.getRoot().findViewById(R.id.fotoFrame);
        if (statusDot != null && fotoFrame != null) {
            // Posición inicial: esquina inferior derecha del círculo azul
            fotoFrame.post(() -> {
                float fotoX = fotoFrame.getX();
                float fotoY = fotoFrame.getY();
                float fotoWidth = fotoFrame.getWidth();
                float fotoHeight = fotoFrame.getHeight();
                // Posicionar en la esquina inferior derecha (mitad dentro, mitad fuera)
                statusDot.setX(fotoX + fotoWidth - 8);
                statusDot.setY(fotoY + fotoHeight - 8);
            });
            
            statusDot.setOnTouchListener(new android.view.View.OnTouchListener() {
                private float dX, dY;
                
                @Override
                public boolean onTouch(View v, android.view.MotionEvent event) {
                    switch (event.getActionMasked()) {
                        case android.view.MotionEvent.ACTION_DOWN:
                            dX = v.getX() - event.getRawX();
                            dY = v.getY() - event.getRawY();
                            break;
                        case android.view.MotionEvent.ACTION_MOVE:
                            v.setX(event.getRawX() + dX);
                            v.setY(event.getRawY() + dY);
                            break;
                        default:
                            return false;
                    }
                    return true;
                }
            });
        }
        
        // Icono de configuración - mostrar menú contextual
        View iconSettings = binding.getRoot().findViewById(R.id.iconSettings);
        if (iconSettings != null) {
            iconSettings.setOnClickListener(v -> mostrarMenuConfiguracion(v));
        }

        // Botón Editar Contacto
        View btnEditarContacto = binding.getRoot().findViewById(R.id.btnEditarContacto);
        if (btnEditarContacto != null) {
            btnEditarContacto.setOnClickListener(v -> {
                if (perfilUserId == null) {
                    android.widget.Toast.makeText(requireContext(), "No se puede editar: sin ID de usuario", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                abrirEditorContacto();
            });
        }

        // Click en "Cambiar contraseña"
        View rowCambiarContrasena = binding.getRoot().findViewById(R.id.rowCambiarContrasena);
        if (rowCambiarContrasena != null) {
            rowCambiarContrasena.setOnClickListener(v -> mostrarDialogoCambio());
        }

        // Click en "Cerrar sesión"
        View rowCerrarSesion = binding.getRoot().findViewById(R.id.rowCerrarSesion);
        if (rowCerrarSesion != null) {
            rowCerrarSesion.setOnClickListener(v -> confirmarCerrarSesion());
        }

        cargarPerfil();
        cargarKolb();

        binding.rowKolbTrigger.setOnClickListener(v -> showKolbDialog());
    }

    // Variable para guardar la descripción del resultado de Kolb
    private String descripcionKolb = null;

    // ---------------- Diálogo Kolb ----------------
    private void showKolbDialog() {
        // Inflar el layout del diálogo
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_kolb, null, false);

        // Pasar datos al layout
        ((android.widget.TextView) dialogView.findViewById(R.id.tvEstiloSheet))
                .setText(binding.tvEstilo.getText());
        ((android.widget.TextView) dialogView.findViewById(R.id.tvFechaSheet))
                .setText(binding.tvFechaKolb.getText());
        
        // Mostrar la descripción si está guardada, sino obtenerla del API
        TextView tvDescripcionSheet = dialogView.findViewById(R.id.tvDescripcionSheet);
        if (tvDescripcionSheet != null) {
            if (descripcionKolb != null && !descripcionKolb.trim().isEmpty()) {
                // Usar la descripción guardada
                tvDescripcionSheet.setText(limpiarTexto(descripcionKolb));
            } else {
                // Si no está guardada, obtenerla del API
                tvDescripcionSheet.setText("-");
                api.obtenerResultado().enqueue(new Callback<KolbResultado>() {
                    @Override
                    public void onResponse(Call<KolbResultado> call, Response<KolbResultado> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            KolbResultado r = resp.body();
                            String descripcion = r.getDescripcion();
                            if (descripcion != null && !descripcion.trim().isEmpty()) {
                                descripcionKolb = descripcion; // Guardar para uso futuro
                                tvDescripcionSheet.setText(limpiarTexto(descripcion));
                            } else {
                                tvDescripcionSheet.setText("-");
                            }
                        } else {
                            tvDescripcionSheet.setText("-");
                        }
                    }
                    @Override
                    public void onFailure(Call<KolbResultado> call, Throwable t) {
                        tvDescripcionSheet.setText("-");
                    }
                });
            }
        }
        
        ((android.widget.TextView) dialogView.findViewById(R.id.tvCaracteristicasSheet))
                .setText(binding.tvCaracteristicas.getText());
        ((android.widget.TextView) dialogView.findViewById(R.id.tvRecomendacionesSheet))
                .setText(binding.tvRecomendaciones.getText());

        // Construir sin botones del builder (el botón está en el XML)
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        // Click en "Cerrar" (definido en el XML)
        View btnCerrar = dialogView.findViewById(R.id.btnCerrarSheet);
        if (btnCerrar != null) btnCerrar.setOnClickListener(v -> dialog.dismiss());

        // Configurar diálogo mediano con scroll y márgenes
        dialog.setOnShowListener(dlg -> {
            if (dialog.getWindow() != null) {
                android.view.WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
                android.util.DisplayMetrics displayMetrics = new android.util.DisplayMetrics();
                requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screenWidth = displayMetrics.widthPixels;
                int screenHeight = displayMetrics.heightPixels;
                
                // Ancho: 85% de la pantalla (los márgenes laterales se aplicarán al CardView)
                layoutParams.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
                
                // Altura máxima: 80% de la pantalla (deja 10% arriba y 10% abajo)
                int maxHeight = (int) (screenHeight * 0.8);
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                
                // Calcular márgenes verticales (10% arriba y 10% abajo)
                int verticalMargin = (int) (screenHeight * 0.1);
                
                layoutParams.gravity = android.view.Gravity.CENTER;
                dialog.getWindow().setAttributes(layoutParams);
                
                // Fondo transparente para que se vea el card con bordes redondeados
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                // Dim background
                dialog.getWindow().setDimAmount(0.6f);
            }
        });
        
        // Helper para limitar altura del ScrollView y aplicar márgenes al CardView
        dialogView.post(() -> {
            android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            int screenHeight = dm.heightPixels;
            int maxHeight = (int) (screenHeight * 0.8); // 80% de altura (deja 10% arriba y 10% abajo)
            int verticalMargin = (int) (screenHeight * 0.1); // 10% de margen arriba y abajo
            
            // Encontrar el MaterialCardView (raíz del layout)
            com.google.android.material.card.MaterialCardView cardView = null;
            if (dialogView instanceof com.google.android.material.card.MaterialCardView) {
                cardView = (com.google.android.material.card.MaterialCardView) dialogView;
            } else if (dialogView instanceof android.view.ViewGroup) {
                cardView = findCardView((android.view.ViewGroup) dialogView);
            }
            
            if (cardView != null) {
                // Aplicar márgenes al CardView para crear espacio arriba y abajo
                android.view.ViewGroup.MarginLayoutParams cardParams = 
                    (android.view.ViewGroup.MarginLayoutParams) cardView.getLayoutParams();
                if (cardParams == null) {
                    cardParams = new android.view.ViewGroup.MarginLayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                }
                cardParams.topMargin = verticalMargin;
                cardParams.bottomMargin = verticalMargin;
                cardParams.leftMargin = (int) (dm.widthPixels * 0.075); // 7.5% a cada lado (15% total)
                cardParams.rightMargin = (int) (dm.widthPixels * 0.075);
                cardView.setLayoutParams(cardParams);
            }
            
            // Limitar altura del ScrollView
            ScrollView sv = dialogView.findViewById(R.id.scrollViewKolb);
            if (sv == null) {
                sv = findScrollView((android.view.ViewGroup) dialogView);
            }
            if (sv != null) {
                android.view.ViewGroup.LayoutParams params = sv.getLayoutParams();
                if (params != null) {
                    // Establecer altura máxima pero permitir que el contenido determine el scroll
                    params.height = maxHeight;
                    sv.setLayoutParams(params);
                }
                // Asegurar que el scroll funcione correctamente y pueda llegar hasta el final
                ScrollView finalSv = sv;
                sv.post(() -> {
                    // Habilitar scrollbars
                    finalSv.setVerticalScrollBarEnabled(true);
                    // Asegurar que el ScrollView pueda hacer scroll hasta el final
                    // Forzar layout del contenido para que se mida correctamente
                    finalSv.requestLayout();
                    finalSv.invalidate();
                });
            }
        });

        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }
    
    private ScrollView findScrollView(android.view.ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            android.view.View child = parent.getChildAt(i);
            if (child instanceof ScrollView) {
                return (ScrollView) child;
            } else if (child instanceof android.view.ViewGroup) {
                ScrollView found = findScrollView((android.view.ViewGroup) child);
                if (found != null) return found;
            }
        }
        return null;
    }
    
    private com.google.android.material.card.MaterialCardView findCardView(android.view.ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            android.view.View child = parent.getChildAt(i);
            if (child instanceof com.google.android.material.card.MaterialCardView) {
                return (com.google.android.material.card.MaterialCardView) child;
            } else if (child instanceof android.view.ViewGroup) {
                com.google.android.material.card.MaterialCardView found = findCardView((android.view.ViewGroup) child);
                if (found != null) return found;
            }
        }
        return null;
    }

    // ---------------- Util para ID ----------------
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

    // ---------------- Cámara/Galería ----------------
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
        // Limpiar cache de Glide para forzar recarga
        Glide.with(requireContext()).clear(binding.icon);
        
        // PRIMERO: Guardar la foto localmente para que esté disponible inmediatamente
        File saved = copyUriToInternalFile(uri, fileNameForUser());
        if (saved != null && saved.exists()) {
            // Guardar la ruta inmediatamente
            guardarPathFoto(saved.getAbsolutePath());
            
            // Cargar la foto desde el archivo guardado (más confiable que el URI)
            Glide.with(requireContext())
                    .load(saved)
                    .placeholder(R.drawable.usuario)
                    .skipMemoryCache(true) // No usar cache en memoria
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE) // No usar cache en disco
                    .into(binding.icon);
            
            // Notificar INMEDIATAMENTE a otras pantallas que la foto se actualizó
            notificarFotoActualizada(saved.getAbsolutePath());
            
            // Subir foto al servidor en segundo plano
            subirFotoAlServidor(saved);
        } else {
            // Si falla guardar, cargar directamente desde el URI
            Glide.with(requireContext())
                    .load(uri)
                    .placeholder(R.drawable.usuario)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                    .into(binding.icon);
        }
    }
    
    private void notificarFotoActualizada(String fotoPath) {
        // Enviar broadcast para notificar que la foto se actualizó
        android.content.Intent intent = new android.content.Intent("com.example.zavira_movil.FOTO_ACTUALIZADA");
        intent.putExtra("foto_path", fotoPath);
        requireContext().sendBroadcast(intent);
    }
    
    private void subirFotoAlServidor(@NonNull File fotoFile) {
        if (!fotoFile.exists()) {
            Log.e("PERFIL_UPLOAD", "El archivo no existe: " + fotoFile.getAbsolutePath());
            return;
        }

        try {
            // Crear RequestBody para el archivo
            RequestBody requestFile = RequestBody.create(
                    MediaType.parse("image/*"),
                    fotoFile
            );

            // Crear MultipartBody.Part con el nombre del campo "foto"
            MultipartBody.Part fotoPart = MultipartBody.Part.createFormData(
                    "foto",
                    fotoFile.getName(),
                    requestFile
            );

            // Llamar a la API para subir la foto
            api.subirFoto(fotoPart).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try {
                            String responseBody = response.body() != null ? response.body().string() : "";
                            Log.d("PERFIL_UPLOAD", "Foto subida correctamente: " + responseBody);
                            // Limpiar cache de Glide para forzar recarga
                            Glide.with(requireContext()).clear(binding.icon);
                            // Notificar que la foto se subió exitosamente
                            notificarFotoActualizada(fotoFile.getAbsolutePath());
                            // Recargar el perfil para obtener la nueva URL
                            cargarPerfil();
                        } catch (Exception e) {
                            Log.e("PERFIL_UPLOAD", "Error al leer respuesta", e);
                        }
                    } else {
                        Log.e("PERFIL_UPLOAD", "Error al subir foto: " + response.code());
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "";
                            Log.e("PERFIL_UPLOAD", "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e("PERFIL_UPLOAD", "Error al leer error body", e);
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("PERFIL_UPLOAD", "Error de red al subir foto", t);
                }
            });
        } catch (Exception e) {
            Log.e("PERFIL_UPLOAD", "Error al preparar foto para subir", e);
        }
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

    private void cargarFotoPreferida(@Nullable String remoteUrl) {
        // PRIMERO: Intentar cargar desde archivo local (igual que en HomeActivity)
        var sp = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String k = prefsKeyForUser();
        String path = sp.getString(k, null);

        if (path != null) {
            File f = new File(path);
            if (f.exists()) {
                Glide.with(this)
                        .load(f)
                        .placeholder(R.drawable.usuario)
                        .error(R.drawable.usuario)
                        .circleCrop()
                        .into(binding.icon);
                return;
            } else {
                sp.edit().remove(k).apply();
            }
        }

        File byName = absoluteFileForUser();
        if (byName.exists()) {
            Glide.with(this)
                    .load(byName)
                    .placeholder(R.drawable.usuario)
                    .error(R.drawable.usuario)
                    .circleCrop()
                    .into(binding.icon);
            sp.edit().putString(k, byName.getAbsolutePath()).apply();
            return;
        }
        
        // Si no hay archivo local, cargar desde URL remota
        if (remoteUrl != null && !remoteUrl.trim().isEmpty() && !"null".equalsIgnoreCase(remoteUrl.trim())) {
            Glide.with(this)
                    .load(remoteUrl.trim())
                    .placeholder(R.drawable.usuario)
                    .error(R.drawable.usuario)
                    .circleCrop()
                    .skipMemoryCache(false)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                    .into(binding.icon);
            return;
        }

        binding.icon.setImageResource(R.drawable.usuario);
    }

    private String prefsKeyForUser() {
        return (perfilUserId == null) ? TMP_KEY : KEY_FOTO_PATH_BASE + "_" + perfilUserId;
    }

    private String fileNameForUser() {
        return (perfilUserId == null) ? AVATAR_FILE_BASE + "_tmp.jpg" : AVATAR_FILE_BASE + "_" + perfilUserId + ".jpg";
    }

    private File absoluteFileForUser() {
        return new File(requireContext().getFilesDir(), fileNameForUser());
    }

    private void guardarPathFoto(@NonNull String absolutePath) {
        requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(prefsKeyForUser(), absolutePath)
                .apply();
    }

    private void eliminarFotoLocal() {
        var sp = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String k = prefsKeyForUser();
        String path = sp.getString(k, null);

        if (path != null) {
            try { File f = new File(path); if (f.exists()) f.delete(); } catch (Exception ignored) {}
        }
        sp.edit().remove(k).apply();
        binding.icon.setImageResource(R.drawable.usuario);
    }

    // ---------------- Perfil / Kolb ----------------
    private void cargarPerfil() {
        api.getPerfilEstudiante().enqueue(new Callback<Estudiante>() {
            @Override public void onResponse(Call<Estudiante> call, Response<Estudiante> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    logHttpError("PROFILE_PERFIL", resp);
                    return;
                }
                Estudiante e = resp.body();

                // Asegurar id
                if (perfilUserId == null) perfilUserId = guessIdFromPerfil(e);

                cargarFotoPreferida(e.getFotoUrl());
                // binding.rowEditarPerfil ya no existe - información personal es solo lectura

                // Encabezado: nombre completo del estudiante (sin formatear)
                String nombreCompleto = safe(e.getNombreUsuario()) + " " + safe(e.getApellido());
                // Mostrar el nombre completo tal como viene (sin formatear)
                binding.tvNombreHeader.setText(nombreCompleto.trim().isEmpty() ? "Estudiante" : nombreCompleto.trim().toUpperCase());

                // Subtítulo opcional “Estudiante de Grado X”
                String gradoSolo = safe(e.getGrado());
                if (!"-".equals(gradoSolo)) {
                } else {
                }

                // Grado combinado con curso -> “11 - C”
                String cursoSolo = safe(e.getCurso());
                String gradoCurso;
                if (!"-".equals(gradoSolo) && !"-".equals(cursoSolo)) {
                    gradoCurso = gradoSolo + " - " + cursoSolo;
                } else if (!"-".equals(gradoSolo)) {
                    gradoCurso = gradoSolo;
                } else if (!"-".equals(cursoSolo)) {
                    gradoCurso = cursoSolo;
                } else {
                    gradoCurso = "-";
                }
                binding.tvGrado.setText(gradoCurso);

                // Esconder el bloque de curso si estuviera en el layout
                View rowCurso = getView() != null ? getView().findViewById(R.id.rowCurso) : null;
                if (rowCurso != null) rowCurso.setVisibility(View.GONE);

                // Resto de campos de la tarjeta
                binding.tvInstitucion.setText(safe(e.getNombreInstitucion()));
                binding.tvTipoDoc.setText(safe(e.getTipoDocumento()));
                binding.tvDocumento.setText(safe(e.getNumeroDocumento()));
                binding.tvJornada.setText(safe(e.getJornada()));
                binding.tvCorreo.setText(safe(e.getCorreo()));
                binding.tvTelefono.setText(safe(e.getTelefono()));
                binding.tvDireccion.setText(safe(e.getDireccion()));
            }
            @Override public void onFailure(Call<Estudiante> call, Throwable t) {
                Log.e("PROFILE_PERFIL_FAIL", "onFailure", t);
            }
        });
    }

    private void cargarKolb() {
        api.obtenerResultado().enqueue(new Callback<KolbResultado>() {
            @Override public void onResponse(Call<KolbResultado> call, Response<KolbResultado> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    logHttpError("PROFILE_KOLB", resp);
                    if (resp.code() == 404) {
                        // Si no hay resultado, mostrar campos vacíos
                        binding.tvEstilo.setText("-");
                        binding.tvFechaKolb.setText("-");
                        binding.tvCaracteristicas.setText("-");
                        binding.tvRecomendaciones.setText("-");
                    }
                    return;
                }
                KolbResultado r = resp.body();
                
                // Log para debug
                android.util.Log.d("PROFILE_KOLB", "Estilo recibido: " + (r.getEstilo() != null ? r.getEstilo() : "null"));
                android.util.Log.d("PROFILE_KOLB", "Fecha recibida: " + (r.getFecha() != null ? r.getFecha() : "null"));
                android.util.Log.d("PROFILE_KOLB", "Descripción recibida: " + (r.getDescripcion() != null ? r.getDescripcion() : "null"));
                
                binding.tvEstilo.setText(safe(r.getEstilo()));
                binding.tvFechaKolb.setText(formatearFechaFlexible(r.getFecha()));
                binding.tvCaracteristicas.setText(limpiarTexto(r.getCaracteristicas()));
                binding.tvRecomendaciones.setText(limpiarTexto(r.getRecomendaciones()));
                
                // Guardar la descripción para usarla en el diálogo
                descripcionKolb = r.getDescripcion();
            }
            @Override public void onFailure(Call<KolbResultado> call, Throwable t) {
                Log.e("PROFILE_KOLB_FAIL", "onFailure", t);
                // Mostrar error al usuario
                binding.tvEstilo.setText("-");
                binding.tvFechaKolb.setText("-");
                binding.tvCaracteristicas.setText("-");
                binding.tvRecomendaciones.setText("-");
            }
        });
    }

    // ---------------- Editor de contacto ----------------
    private void abrirEditorContacto() {
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.editar_infopersonal, null, false);

        TextInputEditText etCorreo    = content.findViewById(R.id.etCorreo);
        TextInputEditText etTelefono  = content.findViewById(R.id.etTelefono);
        TextInputEditText etDireccion = content.findViewById(R.id.etDireccion);
        MaterialButton btnGuardar     = content.findViewById(R.id.btnGuardar);
        MaterialButton btnCancelar    = content.findViewById(R.id.btnCancelar);
        
        // Obtener los TextInputLayout para cambiar el color del borde al enfocar
        TextInputLayout tilCorreo = (TextInputLayout) etCorreo.getParent().getParent();
        TextInputLayout tilTelefono = (TextInputLayout) etTelefono.getParent().getParent();
        TextInputLayout tilDireccion = (TextInputLayout) etDireccion.getParent().getParent();

        // Prefill
        etCorreo.setText(textOrEmpty(binding.tvCorreo));
        etTelefono.setText(textOrEmpty(binding.tvTelefono));
        etDireccion.setText(textOrEmpty(binding.tvDireccion));
        
        // Cambiar color del borde a azul cuando el campo está enfocado
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            TextInputLayout til = null;
            if (v == etCorreo) til = tilCorreo;
            else if (v == etTelefono) til = tilTelefono;
            else if (v == etDireccion) til = tilDireccion;
            
            if (til != null) {
                if (hasFocus) {
                    til.setBoxStrokeColor(Color.parseColor("#2563EB"));
                } else {
                    til.setBoxStrokeColor(Color.parseColor("#2563EB"));
                }
            }
        };
        
        etCorreo.setOnFocusChangeListener(focusListener);
        etTelefono.setOnFocusChangeListener(focusListener);
        etDireccion.setOnFocusChangeListener(focusListener);

        // Diálogo centrado con bordes redondeados
        final AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(content)
                .create();
        dialog.setOnShowListener(dlg -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setDimAmount(0.6f);
                int w = (int) (getResources().getDisplayMetrics().widthPixels * 0.92f);
                dialog.getWindow().setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT);
                // Asegurar que el diálogo tenga bordes redondeados
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
        });

        // --- Habilitar/Deshabilitar Guardar según vacíos ---
        btnGuardar.setEnabled(false);
        btnGuardar.setAlpha(0.5f);

        TextWatcher watcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                boolean ok = !isEmpty(str(etCorreo))
                        && !isEmpty(str(etTelefono))
                        && !isEmpty(str(etDireccion));
                btnGuardar.setEnabled(ok);
                btnGuardar.setAlpha(ok ? 1f : 0.5f);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        };
        etCorreo.addTextChangedListener(watcher);
        etTelefono.addTextChangedListener(watcher);
        etDireccion.addTextChangedListener(watcher);

        // Cancelar
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        // Guardar
        btnGuardar.setOnClickListener(v -> {
            String correo    = str(etCorreo);
            String telefono  = str(etTelefono);
            String direccion = str(etDireccion);

            // Requeridos
            if (isEmpty(correo) || isEmpty(telefono) || isEmpty(direccion)) {
                if (isEmpty(correo))    etCorreo.setError("Requerido");
                if (isEmpty(telefono))  etTelefono.setError("Requerido");
                if (isEmpty(direccion)) etDireccion.setError("Requerido");
                android.widget.Toast.makeText(requireContext(),
                        "Completa todos los campos", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            // Validaciones de formato
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                etCorreo.setError("Correo inválido"); return;
            }
            if (!java.util.regex.Pattern.compile("^\\+?[0-9\\s\\-()]{7,20}$")
                    .matcher(telefono).matches()) {
                etTelefono.setError("Teléfono inválido"); return;
            }
            if (direccion.length() > 255) {
                etDireccion.setError("Dirección demasiado larga"); return;
            }
            if (perfilUserId == null) {
                android.widget.Toast.makeText(requireContext(),
                        "No se puede editar: sin ID de usuario", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            // Llamada a API; el diálogo se cierra al finalizar
            enviarEdicionContacto(perfilUserId, correo, direccion, telefono, dialog);
        });

        dialog.show();
    }


    private void enviarEdicionContacto(int userId,
                                       String correo,
                                       String direccion,
                                       String telefono,
                                       @Nullable android.app.Dialog dialogToClose) {

        EditarPerfilRequest body = new EditarPerfilRequest(correo, direccion, telefono);
        api.editarPerfil(userId, body).enqueue(new Callback<EditarPerfilResponse>() {
            @Override
            public void onResponse(Call<EditarPerfilResponse> call, Response<EditarPerfilResponse> response) {
                if (!response.isSuccessful()) {
                    logHttpError("PROFILE_EDIT", response);
                    android.widget.Toast.makeText(requireContext(),
                            "Error guardando cambios", android.widget.Toast.LENGTH_LONG).show();
                    return;
                }

                // Actualiza los valores visibles en el perfil
                binding.tvCorreo.setText(isEmpty(correo) ? "-" : correo.trim());
                binding.tvTelefono.setText(isEmpty(telefono) ? "-" : telefono.trim());
                binding.tvDireccion.setText(isEmpty(direccion) ? "-" : direccion.trim());

                String msg = (response.body() != null && response.body().getMessage() != null)
                        ? response.body().getMessage() : "Actualizado";
                android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_SHORT).show();

                if (dialogToClose != null && dialogToClose.isShowing()) dialogToClose.dismiss();
            }

            @Override
            public void onFailure(Call<EditarPerfilResponse> call, Throwable t) {
                Log.e("PROFILE_EDIT_FAIL", "onFailure", t);
                android.widget.Toast.makeText(requireContext(),
                        "Error de red: " + t.getLocalizedMessage(), android.widget.Toast.LENGTH_LONG).show();
            }
        });
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

    // ---------------- Utilidades ----------------
    private static void logHttpError(String tag, Response<?> response) {
        try {
            ResponseBody err = response.errorBody();
            Log.e(tag, "HTTP " + response.code() + " - " + (err != null ? err.string() : "sin cuerpo"));
        } catch (Exception ignored) { }
    }

    private static String safe(String s) { return (s == null || s.trim().isEmpty()) ? "-" : s; }

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

    // ---------------------------------------------------------------------
    // Menú de configuración
    // ---------------------------------------------------------------------
    private void mostrarMenuConfiguracion(View anchor) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_menu_configuracion, null);
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();
        
        // Cambiar contraseña
        View btnCambiarContrasena = dialogView.findViewById(R.id.btnCambiarContrasena);
        if (btnCambiarContrasena != null) {
            btnCambiarContrasena.setOnClickListener(v -> {
                dialog.dismiss();
                mostrarDialogoCambio();
            });
        }
        
        // Cerrar sesión
        View btnCerrarSesion = dialogView.findViewById(R.id.btnCerrarSesion);
        if (btnCerrarSesion != null) {
            btnCerrarSesion.setOnClickListener(v -> {
                dialog.dismiss();
                confirmarCerrarSesion();
            });
        }
        
        dialog.show();
        
        // Ajustar el tamaño y estilo del diálogo
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.85),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }

    // ---------------------------------------------------------------------
    // Cambiar contraseña
    // ---------------------------------------------------------------------
    private void mostrarDialogoCambio() {
        // Primero preguntar si recuerda su contraseña
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cambiar Contraseña")
                .setMessage("¿Recuerdas tu contraseña actual?")
                .setPositiveButton("Sí, la recuerdo", (d, w) -> {
                    // Flujo normal: pedir contraseña actual
                    mostrarDialogoCambioNormal();
                })
                .setNeutralButton("No, la olvidé", (d, w) -> {
                    // Ir a recuperación de contraseña
                    Intent intent = new Intent(requireContext(), com.example.zavira_movil.resetpassword.ResetPasswordActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Diálogo normal para cambiar contraseña (requiere contraseña actual)
     */
    private void mostrarDialogoCambioNormal() {
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialogo_cambiar_contrasena, null, false);

        TextInputEditText etActual = content.findViewById(R.id.etActual);
        TextInputEditText etNueva = content.findViewById(R.id.etNueva);
        TextInputEditText etConfirmar = content.findViewById(R.id.etConfirmar);

        // Botones del layout
        MaterialButton btnCancelar = content.findViewById(R.id.btnCancelar);
        MaterialButton btnGuardar = content.findViewById(R.id.btnGuardar);

        // Placeholders que se quitan al enfocar y vuelven si queda vacío
        final String H1 = "Ingresa tu contraseña actual";
        final String H2 = "Mínimo 8 caracteres";
        final String H3 = "Repite la nueva contraseña";

        etActual.setHint(H1);
        etNueva.setHint(H2);
        etConfirmar.setHint(H3);

        View.OnFocusChangeListener clearOnFocus = (v, hasFocus) -> {
            TextInputEditText et = (TextInputEditText) v;
            if (hasFocus) {
                et.setHint("");
            } else {
                boolean empty = et.getText() == null || et.getText().length() == 0;
                if (empty) {
                    if (et == etActual) et.setHint(H1);
                    else if (et == etNueva) et.setHint(H2);
                    else if (et == etConfirmar) et.setHint(H3);
                }
            }
        };
        etActual.setOnFocusChangeListener(clearOnFocus);
        etNueva.setOnFocusChangeListener(clearOnFocus);
        etConfirmar.setOnFocusChangeListener(clearOnFocus);

        final var dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(content)
                .create();

        // Acciones
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            String actual = etActual.getText() != null ? etActual.getText().toString().trim() : "";
            String nueva = etNueva.getText() != null ? etNueva.getText().toString().trim() : "";
            String conf = etConfirmar.getText() != null ? etConfirmar.getText().toString().trim() : "";

            // Validaciones
            if (TextUtils.isEmpty(actual) || TextUtils.isEmpty(nueva) || TextUtils.isEmpty(conf)) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            if (nueva.length() < 6) {
                etNueva.setError("Mínimo 6 caracteres");
                return;
            }
            if (!nueva.equals(conf)) {
                etConfirmar.setError("No coincide");
                return;
            }

            Dialog loading = showLoading();

            CambiarPassword body = new CambiarPassword(actual, nueva);

            api.cambiarPasswordMovil(body).enqueue(new Callback<BasicResponse>() {
                @Override
                public void onResponse(Call<BasicResponse> call, Response<BasicResponse> resp) {
                    if (loading.isShowing()) loading.dismiss();

                    if (resp.isSuccessful() && resp.body() != null && resp.body().isOk()) {
                        // Verificar login con la nueva clave
                        verificarLoginConNuevaClave(api, nueva, dialog);
                    } else {
                        String msg = "Error " + resp.code();
                        try {
                            if (resp.errorBody() != null) msg += ": " + resp.errorBody().string();
                        } catch (Exception ignored) {
                        }
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<BasicResponse> call, Throwable t) {
                    if (loading.isShowing()) loading.dismiss();
                    Toast.makeText(requireContext(), "Fallo de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        dialog.show();
    }

    /**
     * Llama al perfil para obtener numero_documento y prueba login con la nueva contraseña.
     */
    private void verificarLoginConNuevaClave(ApiService api, String nuevaClave, Dialog dialogCambio) {
        api.getPerfilEstudiante().enqueue(new Callback<Estudiante>() {
            @Override
            public void onResponse(Call<Estudiante> call, Response<Estudiante> rPerfil) {
                if (!rPerfil.isSuccessful() || rPerfil.body() == null) {
                    Toast.makeText(requireContext(), "Contraseña actualizada. No pude leer el perfil para verificar login.", Toast.LENGTH_LONG).show();
                    dialogCambio.dismiss();
                    return;
                }

                String numeroDoc = null;
                try {
                    numeroDoc = rPerfil.body().getNumeroDocumento();
                } catch (Exception ignored) {
                }

                if (numeroDoc == null || numeroDoc.trim().isEmpty()) {
                    Toast.makeText(requireContext(), "Contraseña actualizada. No pude obtener el documento del perfil.", Toast.LENGTH_LONG).show();
                    dialogCambio.dismiss();
                    return;
                }

                LoginRequest loginReq = new LoginRequest(numeroDoc.trim(), nuevaClave);

                api.loginEstudiante(loginReq).enqueue(new Callback<okhttp3.ResponseBody>() {
                    @Override
                    public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> rLogin) {
                        if (rLogin.isSuccessful()) {
                            Toast.makeText(requireContext(), "Contraseña actualizada correctamente", Toast.LENGTH_LONG).show();
                            dialogCambio.dismiss();
                        } else {
                            String msg = "Falló al actualizar la contraseña (" + rLogin.code() + ")";
                            try {
                                if (rLogin.errorBody() != null)
                                    msg += ": " + rLogin.errorBody().string();
                            } catch (Exception ignored) {
                            }
                            Toast.makeText(requireContext(), msg + " → Revisa persistencia/hash en el backend.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                        Toast.makeText(requireContext(), "No pude verificar login: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<Estudiante> call, Throwable t) {
                Toast.makeText(requireContext(), "Contraseña actualizada. No pude leer el perfil: " + t.getMessage(), Toast.LENGTH_LONG).show();
                dialogCambio.dismiss();
            }
        });
    }

    // ---------------------------------------------------------------------
    // Cerrar sesión
    // ---------------------------------------------------------------------
    private void confirmarCerrarSesion() {
        // Infla el layout personalizado
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_cerrar_sesion, null, false);

        final var dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(content)
                .create();

        // Fondo transparente para respetar el card redondeado
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
            );
        }

        // Botones
        MaterialButton btnCancelar = content.findViewById(R.id.btnCancelarCerrar);
        MaterialButton btnConfirmar = content.findViewById(R.id.btnConfirmarCerrar);

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnConfirmar.setOnClickListener(v -> {
            // Acción real de cerrar sesión
            com.example.zavira_movil.local.TokenManager.clearAll(requireContext());
            Intent i = new Intent(requireContext(), LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            requireActivity().finish();
            dialog.dismiss();
        });

        dialog.show();
    }

    // ---------------------------------------------------------------------
    // Helpers UI
    // ---------------------------------------------------------------------
    private Dialog showLoading() {
        Dialog d = new Dialog(requireContext());
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(new ProgressBar(requireContext()));
        d.setCancelable(false);
        d.show();
        return d;
    }
}
