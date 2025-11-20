package com.example.zavira_movil;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.zavira_movil.Home.HomeActivity;
import com.example.zavira_movil.databinding.ActivityResultBinding;
import com.example.zavira_movil.model.KolbResultado;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultActivity extends AppCompatActivity {

    private ActivityResultBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnIrHome.setOnClickListener(v -> {
            Intent i = new Intent(ResultActivity.this, HomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });

        String extraFecha  = getIntent().getStringExtra("fecha");
        String extraEstilo = getIntent().getStringExtra("estilo");
        String extraDesc   = getIntent().getStringExtra("descripcion");
        String extraCarac  = getIntent().getStringExtra("caracteristicas");
        String extraRec    = getIntent().getStringExtra("recomendaciones");

        // Formatear nombre y fecha
        String nombre = getIntent().getStringExtra("nombre");
        if (nombre == null || nombre.isEmpty()) {
            binding.tvNombreCompleto.setText("Nombre: —");
        } else {
            binding.tvNombreCompleto.setText("Nombre: " + nombre.toLowerCase());
        }
        
        String fechaFormateada = formatearFechaFlexible(extraFecha);
        if (fechaFormateada.equals("-")) {
            // Si no hay fecha en el intent, usar fecha actual
            fechaFormateada = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        }
        binding.tvFecha.setText("Fecha: " + fechaFormateada);
        
        // Configurar estilo y descripción
        String estilo = safe(extraEstilo);
        binding.tvEstilo.setText(estilo);
        // Solo resumir la descripción, características y recomendaciones completas
        binding.tvDescripcion.setText(resumirTexto(limpiarTexto(extraDesc)));
        
        // Configurar icono del estilo
        configurarIconoEstilo(estilo);
        
        // Actualizar título de características
        if (estilo != null && !estilo.equals("-")) {
            String tituloCarac = "Características del Estilo " + estilo.split(":")[0];
            binding.tvTituloCaracteristicas.setText(tituloCarac);
        }
        
        // Parsear y mostrar características (completas, sin resumir)
        parsearYMostrarLista(limpiarTexto(extraCarac), binding.llCaracteristicas, true);
        
        // Parsear y mostrar recomendaciones (completas, sin resumir)
        parsearYMostrarLista(limpiarTexto(extraRec), binding.llRecomendaciones, false);

        boolean falta = isEmpty(extraFecha) || isEmpty(extraEstilo) || isEmpty(extraCarac) || isEmpty(extraRec);
        if (falta) {
            apiService = RetrofitClient.getInstance(this).create(ApiService.class);
            apiService.obtenerResultado().enqueue(new Callback<KolbResultado>() {
                @Override public void onResponse(Call<KolbResultado> call, Response<KolbResultado> r) {
                    if (!r.isSuccessful() || r.body() == null) {
                        if (r.code() == 404) {
                            Toast.makeText(ResultActivity.this, "No se encontró resultado del test de Kolb", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ResultActivity.this, "Error al obtener resultado: " + r.code(), Toast.LENGTH_SHORT).show();
                        }
                        Log.e("ResultActivity", "Error al obtener resultado: " + r.code());
                        return;
                    }

                    KolbResultado k = r.body();
                    
                    // Log para debug
                    Log.d("ResultActivity", "Estilo recibido: " + (k.getEstilo() != null ? k.getEstilo() : "null"));
                    Log.d("ResultActivity", "Fecha recibida: " + (k.getFecha() != null ? k.getFecha() : "null"));

                    // Formatear nombre y fecha
                    String nombreEstudiante = k.getEstudiante();
                    if (nombreEstudiante == null || nombreEstudiante.isEmpty()) {
                        binding.tvNombreCompleto.setText("Nombre: —");
                    } else {
                        binding.tvNombreCompleto.setText("Nombre: " + nombreEstudiante.toLowerCase());
                    }
                    
                    String fechaFormateada = formatearFechaFlexible(k.getFecha());
                    if (fechaFormateada.equals("-")) {
                        // Si no hay fecha, usar fecha actual
                        fechaFormateada = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                    }
                    binding.tvFecha.setText("Fecha: " + fechaFormateada);
                    
                    // Configurar estilo y descripción
                    String estilo = safe(k.getEstilo());
                    binding.tvEstilo.setText(estilo);
                    // Solo resumir la descripción, características y recomendaciones completas
                    binding.tvDescripcion.setText(resumirTexto(limpiarTexto(k.getDescripcion())));
                    
                    // Configurar icono del estilo
                    configurarIconoEstilo(estilo);
                    
                    // Actualizar título de características
                    if (estilo != null && !estilo.equals("-")) {
                        String tituloCarac = "Características del Estilo " + estilo.split(":")[0];
                        binding.tvTituloCaracteristicas.setText(tituloCarac);
                    }
                    
                    // Parsear y mostrar características (completas, sin resumir)
                    parsearYMostrarLista(limpiarTexto(k.getCaracteristicas()), binding.llCaracteristicas, true);
                    
                    // Parsear y mostrar recomendaciones (completas, sin resumir)
                    parsearYMostrarLista(limpiarTexto(k.getRecomendaciones()), binding.llRecomendaciones, false);
                }
                @Override public void onFailure(Call<KolbResultado> call, Throwable t) {
                    Log.e("ResultActivity", "Error de red al obtener resultado", t);
                    Toast.makeText(ResultActivity.this, "No se pudo completar el resultado", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private static boolean isEmpty(String s) { return s == null || s.trim().isEmpty(); }
    private static String safe(String s) { return isEmpty(s) ? "-" : s; }

    private String limpiarTexto(String s) {
        if (s == null) return "-";
        String out = s.replace("\\t", " ")
                .replace("\t", " ")
                .replace("\\n", "\n")
                .replace("\r", "")
                .trim();
        return out.isEmpty() ? "-" : out;
    }
    
    private String resumirTexto(String texto) {
        if (texto == null || texto.trim().isEmpty() || texto.equals("-")) {
            return texto;
        }
        
        // Si el texto es corto, devolverlo tal cual
        if (texto.length() <= 180) {
            return texto;
        }
        
        // Dividir en oraciones
        String[] oraciones = texto.split("[.!?]");
        StringBuilder resumen = new StringBuilder();
        int contador = 0;
        
        // Tomar las primeras 2 oraciones completas y significativas
        for (String oracion : oraciones) {
            oracion = oracion.trim();
            if (oracion.isEmpty() || oracion.length() < 20) continue;
            
            if (resumen.length() > 0) {
                resumen.append(". ");
            }
            resumen.append(oracion);
            contador++;
            
            // Tomar hasta 2 oraciones o hasta 180 caracteres
            if (contador >= 2 || resumen.length() >= 180) {
                break;
            }
        }
        
        // Si no encontramos oraciones buenas, tomar los primeros 180 caracteres
        if (resumen.length() < 50) {
            String corto = texto.substring(0, Math.min(180, texto.length())).trim();
            // Asegurar que termine en punto o cortar en un espacio
            int ultimoPunto = corto.lastIndexOf('.');
            int ultimoEspacio = corto.lastIndexOf(' ');
            if (ultimoPunto > 120) {
                return corto.substring(0, ultimoPunto + 1);
            } else if (ultimoEspacio > 120) {
                return corto.substring(0, ultimoEspacio) + "...";
            }
            return corto + "...";
        }
        
        return resumen.toString() + ".";
    }

    private String formatearFechaFlexible(String iso) {
        if (isEmpty(iso)) return "-";
        List<String> pats = Arrays.asList(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd"
        );
        for (String p : pats) {
            try {
                SimpleDateFormat in = new SimpleDateFormat(p, Locale.getDefault());
                if (p.contains("'Z'") || p.endsWith("XXX"))
                    in.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date d = in.parse(iso);
                if (d != null)
                    return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(d);
            } catch (ParseException ignored) {}
        }
        return iso;
    }
    
    private void configurarIconoEstilo(String estilo) {
        if (estilo == null || estilo.equals("-")) {
            binding.imgEstilo.setVisibility(View.GONE);
            return;
        }
        
        binding.imgEstilo.setVisibility(View.VISIBLE);
        String estiloLower = estilo.toLowerCase();
        
        if (estiloLower.contains("acomodador")) {
            binding.imgEstilo.setImageResource(R.drawable.acomodadorverde);
            binding.tvEstilo.setTextColor(Color.parseColor("#36C59D"));
        } else if (estiloLower.contains("divergente")) {
            binding.imgEstilo.setImageResource(R.drawable.convergentenaranja);
            binding.tvEstilo.setTextColor(Color.parseColor("#F5A623"));
        } else if (estiloLower.contains("convergente")) {
            binding.imgEstilo.setImageResource(R.drawable.asimiladorazul);
            binding.tvEstilo.setTextColor(Color.parseColor("#4A90E2"));
        } else if (estiloLower.contains("asimilador")) {
            binding.imgEstilo.setImageResource(R.drawable.divergentemorado);
            binding.tvEstilo.setTextColor(Color.parseColor("#A54ADC"));
        } else {
            binding.imgEstilo.setVisibility(View.GONE);
        }
    }
    
    private void parsearYMostrarLista(String texto, LinearLayout contenedor, boolean esCaracteristicas) {
        contenedor.removeAllViews();
        
        if (texto == null || texto.equals("-") || texto.trim().isEmpty()) {
            return;
        }
        
        // Dividir por saltos de línea
        String[] items = texto.split("\n");
        
        for (String item : items) {
            item = item.trim();
            if (item.isEmpty()) continue;
            
            // Remover viñetas o números al inicio si existen
            item = item.replaceFirst("^[•\\-\\*\\d+\\.]+\\s*", "");
            
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 8);
            itemLayout.setLayoutParams(params);
            
            // Icono (checkmark para características, target para recomendaciones)
            ImageView icono = new ImageView(this);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(20, 20);
            iconParams.setMargins(0, 0, 8, 0);
            icono.setLayoutParams(iconParams);
            if (esCaracteristicas) {
                icono.setImageResource(R.drawable.ic_checkmark);
            } else {
                icono.setImageResource(R.drawable.ic_target);
            }
            icono.setColorFilter(ContextCompat.getColor(this, R.color.nav_blue));
            
            // Texto
            TextView textoItem = new TextView(this);
            textoItem.setText(item);
            textoItem.setTextColor(Color.parseColor("#000000"));
            textoItem.setTextSize(13);
            textoItem.setTypeface(null, android.graphics.Typeface.BOLD);
            textoItem.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ));
            
            itemLayout.addView(icono);
            itemLayout.addView(textoItem);
            contenedor.addView(itemLayout);
        }
    }
}
