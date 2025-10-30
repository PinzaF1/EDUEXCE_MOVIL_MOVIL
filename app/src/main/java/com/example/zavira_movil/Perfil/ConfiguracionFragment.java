package com.example.zavira_movil.Perfil;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.zavira_movil.BasicResponse;
import com.example.zavira_movil.LoginActivity;
import com.example.zavira_movil.R;
import com.example.zavira_movil.model.CambiarPassword;
import com.example.zavira_movil.model.Estudiante;
import com.example.zavira_movil.model.LoginRequest;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfiguracionFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configuracion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View rowCambiar = view.findViewById(R.id.rowCambiarContrasena);
        if (rowCambiar != null) rowCambiar.setOnClickListener(v -> mostrarDialogoCambio());

        View rowLogout = view.findViewById(R.id.btnCerrarSesion);
        if (rowLogout != null) rowLogout.setOnClickListener(v -> confirmarCerrarSesion());
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

    private void showLong(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
    }

    // ---------------------------------------------------------------------
    // Cambiar contraseña (con botones dentro del layout)
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

        TextInputEditText etActual    = content.findViewById(R.id.etActual);
        TextInputEditText etNueva     = content.findViewById(R.id.etNueva);
        TextInputEditText etConfirmar = content.findViewById(R.id.etConfirmar);

        // Botones del layout
        MaterialButton btnCancelar = content.findViewById(R.id.btnCancelar);
        MaterialButton btnGuardar  = content.findViewById(R.id.btnGuardar);

        final var dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cambiar Contraseña")
                .setView(content)
                // Importante: SIN setPositive/Negative para usar los del layout
                .create();

        // Cancelar -> cerrar
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        // Guardar -> validar + llamar API
        btnGuardar.setOnClickListener(v -> {
            String actual = etActual.getText() != null ? etActual.getText().toString().trim() : "";
            String nueva  = etNueva.getText()  != null ? etNueva.getText().toString().trim()  : "";
            String conf   = etConfirmar.getText()!= null ? etConfirmar.getText().toString().trim() : "";

            // Validaciones
            if (TextUtils.isEmpty(actual) || TextUtils.isEmpty(nueva) || TextUtils.isEmpty(conf)) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            if (nueva.length() < 6) { etNueva.setError("Mínimo 6 caracteres"); return; }
            if (!nueva.equals(conf)) { etConfirmar.setError("No coincide"); return; }

            Dialog loading = showLoading();

            ApiService api = RetrofitClient
                    .getInstance(requireContext())
                    .create(ApiService.class);

            CambiarPassword body = new CambiarPassword(actual, nueva);

            api.cambiarPasswordMovil(body).enqueue(new Callback<BasicResponse>() {
                @Override
                public void onResponse(Call<BasicResponse> call, Response<BasicResponse> resp) {
                    if (loading.isShowing()) loading.dismiss();

                    if (resp.isSuccessful() && resp.body() != null && resp.body().isOk()) {
                        // Verificar de una vez la nueva clave para asegurar que quedó bien
                        verificarLoginConNuevaClave(api, nueva, dialog);
                    } else {
                        String msg = "Error " + resp.code();
                        try { if (resp.errorBody() != null) msg += ": " + resp.errorBody().string(); } catch (Exception ignored) {}
                        showLong(msg);
                    }
                }

                @Override
                public void onFailure(Call<BasicResponse> call, Throwable t) {
                    if (loading.isShowing()) loading.dismiss();
                    showLong("Fallo de red: " + t.getMessage());
                }
            });
        });

        dialog.show();
    }

    /** Llama al perfil para obtener numero_documento y prueba login con la nueva contraseña. */
    private void verificarLoginConNuevaClave(ApiService api, String nuevaClave, Dialog dialogCambio) {
        // 1) Traer perfil para obtener el identificador de login
        api.getPerfilEstudiante().enqueue(new Callback<Estudiante>() {
            @Override
            public void onResponse(Call<Estudiante> call, Response<Estudiante> rPerfil) {
                if (!rPerfil.isSuccessful() || rPerfil.body() == null) {
                    showLong("Contraseña actualizada. No pude leer el perfil para verificar login.");
                    dialogCambio.dismiss();
                    return;
                }

                String numeroDoc = null;
                try {
                    // Ajusta este getter según tu modelo Estudiante
                    numeroDoc = rPerfil.body().getNumeroDocumento();
                } catch (Exception ignored) {}

                if (numeroDoc == null || numeroDoc.trim().isEmpty()) {
                    showLong("Contraseña actualizada. No pude obtener el documento del perfil.");
                    dialogCambio.dismiss();
                    return;
                }

                // 2) Probar login con la NUEVA clave
                LoginRequest loginReq = new LoginRequest(numeroDoc.trim(), nuevaClave);

                api.loginEstudiante(loginReq).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> rLogin) {
                        if (rLogin.isSuccessful()) {
                            showLong("Contraseña actualizada correctamente");
                            dialogCambio.dismiss();
                        } else {
                            String msg = "Falló al actualizar la contraseña (" + rLogin.code() + ")";
                            try { if (rLogin.errorBody()!=null) msg += ": " + rLogin.errorBody().string(); } catch (Exception ignored) {}
                            showLong(msg + " → Revisa persistencia/hash en el backend.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        showLong("No pude verificar login: " + t.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Call<Estudiante> call, Throwable t) {
                showLong("Contraseña actualizada. No pude leer el perfil: " + t.getMessage());
                dialogCambio.dismiss();
            }
        });
    }

    // ---------------------------------------------------------------------
    // Cerrar sesión
    // ---------------------------------------------------------------------
    private void confirmarCerrarSesion() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("¿Cerrar la sesión de tu cuenta?")
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .setPositiveButton("Cerrar sesión", (d, w) -> {
                    com.example.zavira_movil.local.TokenManager.clearAll(requireContext());
                    Intent i = new Intent(requireContext(), LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    requireActivity().finish();
                })
                .show();
    }
}
