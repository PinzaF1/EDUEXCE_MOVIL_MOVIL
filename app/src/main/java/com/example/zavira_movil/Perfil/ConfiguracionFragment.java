package com.example.zavira_movil.Perfil;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.zavira_movil.LoginActivity;
import com.example.zavira_movil.R;
import com.example.zavira_movil.local.TokenManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

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

        // --- Cambiar contraseña
        View rowCambiar = view.findViewById(R.id.rowCambiarContrasena);
        if (rowCambiar != null) {
            rowCambiar.setOnClickListener(v -> mostrarDialogoCambio());
        }

        // --- Cerrar sesión (soporta dos posibles IDs en tu layout)
        View rowLogout = view.findViewById(R.id.btnCerrarSesion);
        if (rowLogout == null) {
            rowLogout = view.findViewById(R.id.btnCerrarSesion);
        }
        if (rowLogout != null) {
            rowLogout.setOnClickListener(v -> confirmarCerrarSesion());
        }
    }

    // ====================== Cambiar contraseña ======================

    private void mostrarDialogoCambio() {
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialogo_cambiar_contrasena, null, false);

        TextInputEditText etActual    = content.findViewById(R.id.etActual);
        TextInputEditText etNueva     = content.findViewById(R.id.etNueva);
        TextInputEditText etConfirmar = content.findViewById(R.id.etConfirmar);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cambiar Contraseña")
                .setView(content)
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .setPositiveButton("Guardar", null); // lo interceptamos para validar

        final var dialog = builder.create();
        dialog.setOnShowListener(d -> dialog.getButton(
                androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE
        ).setOnClickListener(v -> {
            String actual = etActual.getText() != null ? etActual.getText().toString().trim() : "";
            String nueva  = etNueva.getText() != null ? etNueva.getText().toString().trim() : "";
            String conf   = etConfirmar.getText() != null ? etConfirmar.getText().toString().trim() : "";

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

            // TODO: Llamar a tu backend para cambiar la contraseña.
            Toast.makeText(requireContext(), "Contraseña actualizada", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }));
        dialog.show();
    }

    // ====================== Cerrar sesión ======================

    private void confirmarCerrarSesion() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("¿Cerrar la sesión de tu cuenta?")
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .setPositiveButton("Cerrar sesión", (d, w) -> {
                    // Limpia credenciales y vuelve al Login
                    TokenManager.clearAll(requireContext());
                    Intent i = new Intent(requireContext(), LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    requireActivity().finish();
                })
                .show();
    }
}
