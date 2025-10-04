package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.zavira_movil.local.TokenManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TokenManager tokenManager = new TokenManager();
        String token = tokenManager.getToken(this);

        if (token != null && !token.isEmpty()) {
            // Si hay token → va al Home
            startActivity(new Intent(this, HomeActivity.class));
        } else {
            // Si no hay token → va al Login
            startActivity(new Intent(this, LoginActivity.class));
        }

        // Cierra MainActivity para que no regrese con "back"
        finish();
    }
}