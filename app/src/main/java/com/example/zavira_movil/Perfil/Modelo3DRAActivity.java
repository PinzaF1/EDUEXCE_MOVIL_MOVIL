package com.example.zavira_movil.Perfil;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.R;
import com.google.android.material.appbar.MaterialToolbar;

public class Modelo3DRAActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modelo_3d_ra);

        // Toolbar
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Modelo 3D RA");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (topAppBar != null) {
            topAppBar.setNavigationOnClickListener(v -> finish());
        }
    }
}


