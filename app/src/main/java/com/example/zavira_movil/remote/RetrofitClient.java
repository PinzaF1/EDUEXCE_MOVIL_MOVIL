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

public final class RetrofitClient {

    // ✅ URL NGROK ACTUALIZADA (con soporte OpenAI/IA)
    private static final String BASE_URL = "https://unimparted-henrietta-uninspissated.ngrok-free.dev/";

    // DEBUG LOCAL (comentado hasta resolver firewall):
    // private static final String BASE_URL = "http://192.168.1.5:3333/";
    
    // EMULADOR: Descomentar si usas emulador
    // private static final String BASE_URL = "http://10.0.2.2:3333/";
    
    // DISPOSITIVO FÍSICO: Descomenta y reemplaza con IP de tu PC
    // private static final String BASE_URL = "http://192.168.X.X:3333/";
    
    private static Retrofit retrofit;
    private static Context appContext; // para leer el token

    private RetrofitClient() {}

    /** Llama esto una vez (por ejemplo en Application o en tu primera Activity) */
    public static void init(Context context) {
        if (context != null) appContext = context.getApplicationContext();
    }

    /** Compatibilidad: permite usar getInstance(this) como tú lo estabas haciendo */
    public static Retrofit getInstance(Context context) {
        init(context);
        return getInstance();
    }

    /** Usar cuando ya llamaste init(Context) antes */
    public static Retrofit getInstance() {
        if (retrofit == null) {
            // Interceptor de logs (útil en desarrollo)
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Interceptor para Authorization
            Interceptor authInterceptor = chain -> {
                Request original = chain.request();
                Request.Builder builder = original.newBuilder();

                if (appContext != null) {
                    String token = TokenManager.getToken(appContext);
                    if (token != null && !token.trim().isEmpty()) {
                        builder.header("Authorization", "Bearer " + token);
                    }
                }
                // JSON por defecto
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
}