package com.example.zavira_movil;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.zavira_movil.Home.HomeActivity;
import com.example.zavira_movil.model.KolbRequest;
import com.example.zavira_movil.model.KolbResponse;
import com.example.zavira_movil.model.PreguntasKolb;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestActivity extends AppCompatActivity {

    private LinearLayout container;
    private Button btnEnviar;
    private ProgressBar progressBar;
    private TextView tvProgresoBloque;
    private TextView tvTituloBloque;
    private android.widget.ScrollView scrollView;

    private final List<PreguntasKolb> preguntas = new ArrayList<>();
    private final android.util.SparseIntArray respuestas = new android.util.SparseIntArray();
    private final List<CardView> tarjetasPreguntas = new ArrayList<>();
    private final Map<Integer, LinearLayout> opcionesLayouts = new LinkedHashMap<>();
    private int bloqueActual = 0;
    private static final int PREGUNTAS_POR_BLOQUE = 9;
    private static final int TOTAL_BLOQUES = 4;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_test);

        container = findViewById(R.id.questionsContainer);
        btnEnviar = findViewById(R.id.btnEnviar);
        progressBar = findViewById(R.id.progressBloque);
        tvProgresoBloque = findViewById(R.id.tvProgresoBloque);
        tvTituloBloque = findViewById(R.id.tvTituloBloque);
        
        // Obtener el ScrollView padre del contenedor
        View parent = container;
        if (parent instanceof android.widget.ScrollView) {
            scrollView = (android.widget.ScrollView) parent;
        }

        progressBar.setProgress(0);
        tvProgresoBloque.setText("Bloque 1 - Progreso: 0 / 9");

        btnEnviar.setOnClickListener(v -> validarBloqueActual());
        cargar();
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    private void cargar() {
        btnEnviar.setEnabled(false);
        btnEnviar.setText("Cargando...");

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.getPreguntas().enqueue(new Callback<List<PreguntasKolb>>() {
            @Override
            public void onResponse(Call<List<PreguntasKolb>> c, Response<List<PreguntasKolb>> r) {
                btnEnviar.setText("Enviar respuestas");
                if (!r.isSuccessful() || r.body() == null) {
                    toast("No se pudieron cargar las preguntas (" + r.code() + ")");
                    return;
                }
                preguntas.clear();
                preguntas.addAll(r.body());
                
                // Debug: ver primera pregunta (Log eliminado para evitar mensajes en Home)
                // if (!preguntas.isEmpty()) {
                //     PreguntasKolb primera = preguntas.get(0);
                //     android.util.Log.d("TestActivity", "Primera pregunta - id: " + primera.getId_pregunta_estilo_aprendizajes() + 
                //         ", tipo: '" + primera.getTipo_pregunta() + "', titulo: '" + primera.getTitulo() + "'");
                // }
                
                render();
                btnEnviar.setEnabled(true);
            }

            @Override
            public void onFailure(Call<List<PreguntasKolb>> c, Throwable t) {
                btnEnviar.setText("Enviar respuestas");
                toast("Error al cargar: " + t.getMessage());
            }
        });
    }

    private void render() {
        container.removeAllViews();
        tarjetasPreguntas.clear();
        LayoutInflater inflater = LayoutInflater.from(this);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);

        int totalPreguntas = Math.min(preguntas.size(), TOTAL_BLOQUES * PREGUNTAS_POR_BLOQUE);

        // Mostrar solo el bloque actual
        mostrarBloque(bloqueActual);
    }

    private void mostrarBloque(int bloque) {
        container.removeAllViews();
        tarjetasPreguntas.clear();
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);

        // ---- Calcular rango de preguntas del bloque ----
        int inicio = bloque * PREGUNTAS_POR_BLOQUE;
        int fin = Math.min(inicio + PREGUNTAS_POR_BLOQUE, preguntas.size());

        // Actualizar el título del bloque en el TextView del layout usando tipo_pregunta
        String tituloBloqueTexto = "Bloque " + (bloque + 1);
        if (inicio < preguntas.size()) {
            String tipoPregunta = preguntas.get(inicio).getTipo_pregunta();
            
            // Debug: mostrar valores
            // Log eliminado para evitar mensajes en Home
            // android.util.Log.d("TestActivity", "Bloque " + (bloque + 1) + " - tipo: '" + tipoPregunta + "'");
            
            if (tipoPregunta != null && !tipoPregunta.isEmpty()) {
                tituloBloqueTexto = tipoPregunta;
            }
        }
        
        // Actualizar el TextView del layout con el título correcto
        if (tvTituloBloque != null) {
            tvTituloBloque.setText(tituloBloqueTexto);
        }

        // ---- Bloque visual para las preguntas ----
        LinearLayout bloqueLayout = new LinearLayout(this);
        bloqueLayout.setOrientation(LinearLayout.VERTICAL);
        bloqueLayout.setPadding(24, 24, 24, 24);
        bloqueLayout.setBackgroundResource(R.drawable.bg_card);
        bloqueLayout.setElevation(6f);

        // ---- Agregar las 9 preguntas del bloque ----
        for (int i = inicio; i < fin; i++) {
            PreguntasKolb p = preguntas.get(i);

            CardView card = new CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 12, 0, 12);
            card.setLayoutParams(cardParams);
            card.setRadius(18f);
            card.setCardElevation(4f);
            card.setUseCompatPadding(true);
            card.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white));

            LinearLayout inner = new LinearLayout(this);
            inner.setOrientation(LinearLayout.VERTICAL);
            inner.setPadding(24, 24, 24, 24);

            TextView enunciado = new TextView(this);
            enunciado.setText((i + 1) + ". " + p.getPregunta());
            enunciado.setTextSize(15);
            enunciado.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            enunciado.setPadding(0, 8, 0, 16);

            // Labels para las opciones (como en la imagen)
            String[] labels = {"1", "2", "3", "4"};
            String[] labelsSub = {"Menos", "Poco", "Mucho", "Mas"};

            // Contenedor para las opciones organizadas
            LinearLayout opcionesLayout = new LinearLayout(this);
            opcionesLayout.setOrientation(LinearLayout.HORIZONTAL);
            opcionesLayout.setPadding(0, 8, 0, 8);
            opcionesLayouts.put(i, opcionesLayout);

            final int idx = i; // Capturar índice para el lambda
            // Verificar si esta pregunta puede ser respondida (solo si las anteriores están respondidas)
            boolean puedeResponder = puedeResponderPregunta(i, inicio);
            
            for (int v = 1; v <= 4; v++) {
                // Cada opción es un botón personalizado
                android.widget.Button opcionBtn = new android.widget.Button(this);
                opcionBtn.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                ));
                opcionBtn.setTag(v);
                opcionBtn.setText(labels[v - 1] + "\n" + labelsSub[v - 1]);
                opcionBtn.setTextSize(14);
                opcionBtn.setPadding(8, 12, 8, 12);
                opcionBtn.setBackgroundResource(R.drawable.bg_option_button);
                opcionBtn.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                
                // Deshabilitar si no puede responder (hay preguntas anteriores sin responder)
                if (!puedeResponder && respuestas.get(i, 0) == 0) {
                    opcionBtn.setEnabled(false);
                    opcionBtn.setAlpha(0.5f);
                }
                
                // Listener para marcar selección
                opcionBtn.setOnClickListener(view -> {
                    // Verificar nuevamente si puede responder
                    if (!puedeResponderPregunta(idx, inicio) && respuestas.get(idx, 0) == 0) {
                        toast("Debes responder las preguntas anteriores en orden");
                        return;
                    }
                    
                    int valor = (int) view.getTag();
                    respuestas.put(idx, valor);
                    
                    // Resetear todos los botones del grupo
                    for (int j = 0; j < opcionesLayout.getChildCount(); j++) {
                        View child = opcionesLayout.getChildAt(j);
                        child.setSelected(false);
                        child.setBackgroundResource(R.drawable.bg_option_button);
                    }
                    
                    // Marcar el seleccionado
                    view.setSelected(true);
                    view.setBackgroundResource(R.drawable.bg_option_selected);
                    
                    // Habilitar la siguiente pregunta
                    habilitarSiguientePregunta(idx + 1, inicio, fin);
                    
                    actualizarProgreso();
                    card.setCardBackgroundColor(ContextCompat.getColor(TestActivity.this, android.R.color.white));
                    
                    // Remover borde rojo si estaba marcada
                    android.graphics.drawable.GradientDrawable normal = new android.graphics.drawable.GradientDrawable();
                    normal.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                    normal.setCornerRadius(18f);
                    normal.setColor(ContextCompat.getColor(this, android.R.color.white));
                    card.setBackground(null);
                });
                
                opcionesLayout.addView(opcionBtn);
                
                // Marcar si ya estaba seleccionado
                if (respuestas.get(i, 0) == v) {
                    opcionBtn.setSelected(true);
                    opcionBtn.setBackgroundResource(R.drawable.bg_option_selected);
                }
            }

            inner.addView(enunciado);
            inner.addView(opcionesLayout);
            card.addView(inner);
            bloqueLayout.addView(card);
            
            // Guardar referencia a la tarjeta para validación (en orden)
            tarjetasPreguntas.add(card);
        }

        container.addView(bloqueLayout);
        bloqueLayout.startAnimation(anim);
        
        // Actualizar texto del botón
        if (bloqueActual < TOTAL_BLOQUES - 1) {
            btnEnviar.setText("Continuar al siguiente bloque");
        } else {
            btnEnviar.setText("Enviar respuestas");
        }
        
        // Usar animación lenta cuando se carga un nuevo bloque (puede haber respuestas guardadas)
        actualizarProgreso(true);
        
        // Habilitar solo la primera pregunta al inicio
        habilitarSiguientePregunta(inicio, inicio, fin);
        
        // Hacer scroll al inicio del bloque al cargarlo
        if (scrollView != null) {
            scrollView.post(() -> {
                scrollView.smoothScrollTo(0, 0);
            });
        }
    }
    
    private boolean puedeResponderPregunta(int indicePregunta, int inicioBloque) {
        // La primera pregunta del bloque siempre puede ser respondida
        if (indicePregunta == inicioBloque) {
            return true;
        }
        
        // Verificar que todas las preguntas anteriores estén respondidas
        for (int i = inicioBloque; i < indicePregunta; i++) {
            if (respuestas.get(i, 0) == 0) {
                return false;
            }
        }
        return true;
    }
    
    private void habilitarSiguientePregunta(int indicePregunta, int inicioBloque, int finBloque) {
        if (indicePregunta >= finBloque) return;
        
        LinearLayout opcionesLayout = opcionesLayouts.get(indicePregunta);
        if (opcionesLayout != null) {
            for (int j = 0; j < opcionesLayout.getChildCount(); j++) {
                View child = opcionesLayout.getChildAt(j);
                if (child instanceof android.widget.Button) {
                    android.widget.Button btn = (android.widget.Button) child;
                    if (!btn.isSelected()) {
                        btn.setEnabled(true);
                        btn.setAlpha(1.0f);
                    }
                }
            }
        }
    }

    private void actualizarProgreso() {
        actualizarProgreso(false);
    }
    
    private void actualizarProgreso(boolean animacionLenta) {
        // Contar solo las respuestas del bloque actual
        int inicio = bloqueActual * PREGUNTAS_POR_BLOQUE;
        int fin = Math.min(inicio + PREGUNTAS_POR_BLOQUE, preguntas.size());
        int respondidasBloque = 0;
        
        for (int i = inicio; i < fin; i++) {
            if (respuestas.get(i, 0) != 0) respondidasBloque++;
        }

        int totalBloque = fin - inicio;
        int porcentaje = totalBloque > 0 ? (int) ((respondidasBloque / (float) totalBloque) * 100) : 0;

        if (animacionLenta && respondidasBloque > 1) {
            // Si hay varias respuestas ya respondidas, animar pregunta por pregunta
            animarProgresoPreguntaPorPregunta(respondidasBloque, totalBloque, porcentaje);
        } else {
            // Actualización normal cuando el usuario responde una pregunta nueva
            ObjectAnimator anim = ObjectAnimator.ofInt(progressBar, "progress", progressBar.getProgress(), porcentaje);
            anim.setDuration(800); // Duración más lenta para que se vea mejor
            anim.start();
            tvProgresoBloque.setText("Bloque " + (bloqueActual + 1) + " - Progreso: " + respondidasBloque + " / " + totalBloque);
        }
    }
    
    private void animarProgresoPreguntaPorPregunta(int respondidas, int total, int porcentajeFinal) {
        // Resetear la barra a 0 primero
        progressBar.setProgress(0);
        
        // Si no hay respuestas, solo mostrar 0
        if (respondidas == 0) {
            tvProgresoBloque.setText("Bloque " + (bloqueActual + 1) + " - Progreso: 0 / " + total);
            return;
        }
        
        // Calcular el porcentaje por pregunta
        int porcentajePorPregunta = 100 / total;
        int delay = 0;
        
        // Animar pregunta por pregunta con delay entre cada una
        for (int i = 1; i <= respondidas; i++) {
            final int preguntaNum = i;
            final int nuevoProgreso = i * porcentajePorPregunta;
            
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                ObjectAnimator anim = ObjectAnimator.ofInt(progressBar, "progress", progressBar.getProgress(), nuevoProgreso);
                anim.setDuration(600); // Duración más lenta para cada pregunta
                anim.start();
                tvProgresoBloque.setText("Bloque " + (bloqueActual + 1) + " - Progreso: " + preguntaNum + " / " + total);
            }, delay);
            
            delay += 400; // Delay de 400ms entre cada pregunta
        }
        
        // Al final, asegurar que esté en el porcentaje final exacto
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            ObjectAnimator animFinal = ObjectAnimator.ofInt(progressBar, "progress", progressBar.getProgress(), porcentajeFinal);
            animFinal.setDuration(300);
            animFinal.start();
            tvProgresoBloque.setText("Bloque " + (bloqueActual + 1) + " - Progreso: " + respondidas + " / " + total);
        }, delay);
    }

    private void validarBloqueActual() {
        int inicio = bloqueActual * PREGUNTAS_POR_BLOQUE;
        int fin = Math.min(inicio + PREGUNTAS_POR_BLOQUE, preguntas.size());
        boolean todasRespondidas = true;
        List<Integer> preguntasFaltantes = new ArrayList<>();

        // Validar que todas las preguntas del bloque actual estén respondidas
        for (int i = inicio; i < fin; i++) {
            if (respuestas.get(i, 0) == 0) {
                todasRespondidas = false;
                preguntasFaltantes.add(i - inicio); // Índice relativo al bloque
            }
        }

        if (!todasRespondidas) {
            // Encontrar la primera pregunta faltante
            int primeraFaltante = preguntasFaltantes.isEmpty() ? -1 : preguntasFaltantes.get(0);
            
            // Marcar en rojo solo el borde de las preguntas sin responder
            for (int idxRelativo : preguntasFaltantes) {
                if (idxRelativo >= 0 && idxRelativo < tarjetasPreguntas.size()) {
                    CardView card = tarjetasPreguntas.get(idxRelativo);
                    card.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
                    // Agregar borde rojo
                    android.graphics.drawable.GradientDrawable border = new android.graphics.drawable.GradientDrawable();
                    border.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                    border.setCornerRadius(18f);
                    border.setStroke(3, ContextCompat.getColor(this, R.color.rojo));
                    border.setColor(ContextCompat.getColor(this, android.R.color.white));
                    card.setBackground(border);
                }
            }
            
            // Hacer scroll a la primera pregunta faltante
            if (primeraFaltante >= 0 && primeraFaltante < tarjetasPreguntas.size() && scrollView != null) {
                CardView cardFaltante = tarjetasPreguntas.get(primeraFaltante);
                scrollView.post(() -> {
                    // Obtener la posición de la tarjeta dentro del contenedor
                    int[] location = new int[2];
                    cardFaltante.getLocationOnScreen(location);
                    int[] containerLocation = new int[2];
                    container.getLocationOnScreen(containerLocation);
                    
                    // Calcular el scroll necesario relativo al contenedor
                    int scrollY = location[1] - containerLocation[1] + scrollView.getScrollY() - 200;
                    
                    // Hacer scroll suave a la posición
                    scrollView.smoothScrollTo(0, Math.max(0, scrollY));
                });
            }
            
            toast("Debes responder todas las preguntas del bloque " + (bloqueActual + 1) + " para continuar");
            return;
        }

        // Si es el último bloque, enviar
        if (bloqueActual >= TOTAL_BLOQUES - 1) {
            enviar();
        } else {
            // Avanzar al siguiente bloque
            bloqueActual++;
            mostrarBloque(bloqueActual);
        }
    }

    private void enviar() {
        // Verificar que todas las preguntas estén respondidas
        int total = Math.min(preguntas.size(), TOTAL_BLOQUES * PREGUNTAS_POR_BLOQUE);
        for (int i = 0; i < total; i++) {
            if (respuestas.get(i, 0) == 0) {
                toast("Responde todas las preguntas antes de enviar");
                return;
            }
        }

        List<KolbRequest.Item> items = new ArrayList<>(total);
        for (int i = 0; i < total; i++) {
            PreguntasKolb p = preguntas.get(i);
            int idPregunta = p.getId_pregunta_estilo_aprendizajes();
            int valorElegido = respuestas.get(i);
            items.add(new KolbRequest.Item(idPregunta, valorElegido));
        }

        btnEnviar.setEnabled(false);
        btnEnviar.setText("Enviando...");

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        KolbRequest body = new KolbRequest(items);

        api.guardarRespuestas(body).enqueue(new Callback<KolbResponse>() {
            @Override
            public void onResponse(Call<KolbResponse> c, Response<KolbResponse> r) {
                btnEnviar.setEnabled(true);
                btnEnviar.setText("Enviar respuestas");

                if (!r.isSuccessful() || r.body() == null) {
                    toast("Error " + r.code());
                    return;
                }

                KolbResponse k = r.body();
                Intent i = new Intent(TestActivity.this, ResultActivity.class);
                i.putExtra("estilo", k.getEstilo() != null ? k.getEstilo() : k.getEstiloDominante());
                i.putExtra("descripcion", k.getDescripcion());
                i.putExtra("caracteristicas", k.getCaracteristicas());
                i.putExtra("recomendaciones", k.getRecomendaciones());
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }

            @Override
            public void onFailure(Call<KolbResponse> c, Throwable t) {
                btnEnviar.setEnabled(true);
                btnEnviar.setText("Enviar respuestas");
                toast("Fallo: " + t.getMessage());
            }
        });
    }

    private void goToHome() {
        Intent i = new Intent(this, HomeActivity.class);
        startActivity(i);
        finish();
    }
}
