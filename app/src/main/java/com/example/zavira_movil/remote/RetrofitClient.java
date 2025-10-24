package com.example.zavira_movil.remote;

import android.content.Context;

import com.example.zavira_movil.local.TokenManager;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Cliente único de Retrofit con:
 * - Header Authorization: Bearer <token> (lee de TokenManager)
 * - Headers JSON por defecto
 * - Logging de peticiones (BODY)
 *
 * Helpers expuestos:
 *   RetrofitClient.init(context)
 *   RetrofitClient.getClient() / getInstance()
 *   RetrofitClient.getApiService()
 */
public final class RetrofitClient {

    private static final String BASE_URL =
            "https://overvaliantly-discourseless-delilah.ngrok-free.dev/";

    private static Retrofit retrofit;
    private static Context appContext; // para leer el token

    private RetrofitClient() { }

    /** Llama esto una vez (p. ej. en Application.onCreate). */
    public static void init(Context context) {
        if (context != null) appContext = context.getApplicationContext();
    }

    /** Alias conveniente. */
    public static Retrofit getClient() {
        return getInstance();
    }

    /** Compatibilidad por si llamas con contexto (inicializa token reader). */
    public static Retrofit getInstance(Context context) {
        init(context);
        return getInstance();
    }

    /** Obtiene/crea el singleton de Retrofit. */
    public static Retrofit getInstance() {
        if (retrofit == null) {

            // Logs de red (útil en desarrollo)
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Interceptor para Authorization + headers JSON
            Interceptor authInterceptor = chain -> {
                Request original = chain.request();
                Request.Builder builder = original.newBuilder();

                if (appContext != null) {
                    String token = TokenManager.getToken(appContext);
                    if (token != null && !token.trim().isEmpty()) {
                        builder.header("Authorization", "Bearer " + token);
                    }
                }

                builder.header("Accept", "application/json");
                builder.header("Content-Type", "application/json");
                return chain.proceed(builder.build());
            };

            OkHttpClient ok = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(authInterceptor)
                    .addInterceptor(log)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(ok)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /** Helper directo para obtener tu ApiService sin repetir .create(...) */
    public static ApiService getApiService() {
        return getInstance().create(ApiService.class);
    }
}
