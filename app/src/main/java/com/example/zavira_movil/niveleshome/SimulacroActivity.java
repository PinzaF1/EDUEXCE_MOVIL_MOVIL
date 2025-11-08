package com.example.zavira_movil.niveleshome;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.HislaConocimiento.IslaModalityActivity;
import com.example.zavira_movil.HislaConocimiento.IslaPreguntasActivity;
import com.example.zavira_movil.HislaConocimiento.GsonHolder;
import com.example.zavira_movil.model.SimulacroRequest;
import com.example.zavira_movil.niveleshome.MapeadorArea;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SimulacroActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar si viene del examen final (tiene extra "area")
        String area = getIntent() != null ? getIntent().getStringExtra("area") : null;
        
        if (area != null && !area.isEmpty()) {
            // Es examen final - usar endpoint movil/simulacro
            iniciarExamenFinal(area);
        } else {
            // Es Isla del Conocimiento - redirigir a modalidad
            Intent i = new Intent(this, IslaModalityActivity.class);
            if (getIntent() != null && getIntent().getExtras() != null) {
                i.putExtras(getIntent().getExtras());
            }
            startActivity(i);
            finish();
        }
    }
    
    private void iniciarExamenFinal(String area) {
        // CRÍTICO: Normalizar el área al formato canónico del backend antes de enviar
        // El backend espera: "Matematicas", "Lenguaje", "Ciencias", "Sociales", "Ingles"
        String areaCanonica = MapeadorArea.toApiArea(area);
        if (areaCanonica == null || areaCanonica.isEmpty()) {
            // Fallback: usar el área tal cual
            areaCanonica = area;
        }
        
        android.util.Log.d("SimulacroActivity", "Iniciando examen final - area UI: " + area + ", area canonica: " + areaCanonica);
        
        // Crear request para el examen final (solo necesita area)
        SimulacroRequest request = new SimulacroRequest(areaCanonica, null);
        
        ApiService api = RetrofitClient.getInstance(getApplicationContext()).create(ApiService.class);
        api.crearSimulacro(request).enqueue(new Callback<SimulacroResponse>() {
            @Override
            public void onResponse(Call<SimulacroResponse> call, Response<SimulacroResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(SimulacroActivity.this, 
                        "No se pudo iniciar el examen final (" + response.code() + ")", 
                        Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                
                // Convertir SimulacroResponse a formato compatible con IslaPreguntasActivity
                SimulacroResponse simulacroResponse = response.body();
                com.example.zavira_movil.HislaConocimiento.IslaSimulacroResponse islaResponse = 
                    convertirASimulacroIsla(simulacroResponse, area);
                
                // Iniciar actividad de preguntas
                String payload = GsonHolder.gson().toJson(islaResponse);
                Intent i = new Intent(SimulacroActivity.this, IslaPreguntasActivity.class);
                i.putExtra("modalidad", "estandar"); // Examen final no tiene modalidad fácil/difícil
                i.putExtra("payload", payload);
                i.putExtra("area", area);
                i.putExtra("es_examen_final", true); // Marcar como examen final
                startActivity(i);
                finish();
            }
            
            @Override
            public void onFailure(Call<SimulacroResponse> call, Throwable t) {
                Toast.makeText(SimulacroActivity.this, 
                    "Error de red: " + t.getMessage(), 
                    Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    /**
     * Convierte SimulacroResponse a IslaSimulacroResponse para reutilizar IslaPreguntasActivity
     * Usa Gson para crear el JSON directamente ya que los campos son privados
     */
    private com.example.zavira_movil.HislaConocimiento.IslaSimulacroResponse convertirASimulacroIsla(
            SimulacroResponse simulacro, String area) {
        
        Gson gson = GsonHolder.gson();
        JsonObject jsonRoot = new JsonObject();
        
        // Construir sesión
        if (simulacro.sesion != null) {
            JsonObject sesionJson = new JsonObject();
            sesionJson.addProperty("idSesion", simulacro.sesion.idSesion != null ? simulacro.sesion.idSesion : "");
            sesionJson.addProperty("area", area.toLowerCase());
            sesionJson.addProperty("subtema", "todos los subtemas");
            sesionJson.addProperty("nivelOrden", 6);
            sesionJson.addProperty("modo", "estandar");
            sesionJson.addProperty("usaEstiloKolb", false);
            sesionJson.addProperty("totalPreguntas", simulacro.preguntas != null ? simulacro.preguntas.size() : 0);
            jsonRoot.add("sesion", sesionJson);
        }
        
        // Construir preguntas
        if (simulacro.preguntas != null && !simulacro.preguntas.isEmpty()) {
            JsonArray preguntasArray = new JsonArray();
            
            for (ApiQuestion pregunta : simulacro.preguntas) {
                JsonObject preguntaJson = new JsonObject();
                preguntaJson.addProperty("id_pregunta", pregunta.id_pregunta != null ? String.valueOf(pregunta.id_pregunta) : "0");
                preguntaJson.addProperty("area", pregunta.area != null ? pregunta.area : area);
                preguntaJson.addProperty("subtema", pregunta.subtema != null ? pregunta.subtema : "todos los subtemas");
                preguntaJson.addProperty("enunciado", pregunta.enunciado != null ? pregunta.enunciado : "");
                
                // Agregar opciones
                JsonArray opcionesArray = new JsonArray();
                if (pregunta.opciones != null) {
                    for (String opcion : pregunta.opciones) {
                        opcionesArray.add(opcion != null ? opcion : "");
                    }
                }
                preguntaJson.add("opciones", opcionesArray);
                
                preguntasArray.add(preguntaJson);
            }
            
            jsonRoot.add("preguntas", preguntasArray);
            jsonRoot.addProperty("totalPreguntas", simulacro.preguntas.size());
        }
        
        // Deserializar el JSON a IslaSimulacroResponse
        return gson.fromJson(jsonRoot, com.example.zavira_movil.HislaConocimiento.IslaSimulacroResponse.class);
    }
}
