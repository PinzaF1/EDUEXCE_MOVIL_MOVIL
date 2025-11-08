package com.example.zavira_movil.remote;

import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    public interface TokenProvider {
        @Nullable String getToken();
    }

    private final TokenProvider tokenProvider;

    public AuthInterceptor(TokenProvider tokenProvider) {
        this.tokenProvider= tokenProvider;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = tokenProvider.getToken();
        Log.d("INTERCEPTOR_TOKEN", "Token usado en interceptor: Bearer " + token);


        if (token != null && !token.trim().isEmpty()) {
            Request withAuth = original.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
            Log.d("INTERCEPTOR_TOKEN", "Header Authorization agregado con token");

            return chain.proceed(withAuth);
        }
        Log.d("INTERCEPTOR_TOKEN", "No hay token, enviando request original"); 

        return chain.proceed(original);
    }
}
