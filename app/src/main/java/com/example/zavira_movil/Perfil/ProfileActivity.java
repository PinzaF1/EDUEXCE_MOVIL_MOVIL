package com.example.zavira_movil.Perfil;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.LoginActivity;
import com.example.zavira_movil.R;
import com.example.zavira_movil.local.TokenManager;
import com.google.android.material.appbar.MaterialToolbar;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Si no hay token -> Login
        if (TokenManager.getToken(this) == null) {
            goLogin();
            return;
        }

        setContentView(R.layout.acrivity_profile);

        // Toolbar
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Perfil");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        topAppBar.setNavigationOnClickListener(v -> finish());

        // Cargar el fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new PerfilFragment())
                    .commit();
        }
    }

    private void goLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
