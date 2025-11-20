package com.example.zavira_movil;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
    private android.widget.ScrollView scrollView;
    
    // Indicador de progreso visual
    private View circle1, circle2, circle3, circle4;
    private TextView number1, number2, number3, number4;
    private TextView tvTituloBloqueActual;
    private View line1, line2, line3;

    private final List<PreguntasKolb> preguntas = new ArrayList<>();
    private final android.util.SparseIntArray respuestas = new android.util.SparseIntArray();
    private final List<LinearLayout> tarjetasPreguntas = new ArrayList<>();
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
        
        // Inicializar indicador de progreso visual
        try {
            circle1 = findViewById(R.id.circle1);
            circle2 = findViewById(R.id.circle2);
            circle3 = findViewById(R.id.circle3);
            circle4 = findViewById(R.id.circle4);
            number1 = findViewById(R.id.number1);
            number2 = findViewById(R.id.number2);
            number3 = findViewById(R.id.number3);
            number4 = findViewById(R.id.number4);
            tvTituloBloqueActual = findViewById(R.id.tvTituloBloqueActual);
            line1 = findViewById(R.id.line1);
            line2 = findViewById(R.id.line2);
            line3 = findViewById(R.id.line3);
            
            // Inicializar indicador de progreso solo si todos los elementos existen
            if (circle1 != null && circle2 != null && circle3 != null && circle4 != null &&
                number1 != null && number2 != null && number3 != null && number4 != null &&
                tvTituloBloqueActual != null && line1 != null && line2 != null && line3 != null) {
                // Se actualizará después de cargar las preguntas
            } else {
                android.util.Log.w("TestActivity", "Algunos elementos del indicador de progreso no se encontraron");
            }
        } catch (Exception e) {
            android.util.Log.e("TestActivity", "Error al inicializar indicador de progreso", e);
        }
        
        // Obtener el ScrollView desde el layout - buscar en la jerarquía
        android.view.ViewParent parent = container.getParent();
        while (parent != null && !(parent instanceof android.widget.ScrollView)) {
            parent = parent.getParent();
        }
        if (parent instanceof android.widget.ScrollView) {
            scrollView = (android.widget.ScrollView) parent;
        }

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
                btnEnviar.setText("Siguiente");
                if (!r.isSuccessful() || r.body() == null) {
                    toast("No se pudieron cargar las preguntas (" + r.code() + ")");
                    return;
                }
                preguntas.clear();
                preguntas.addAll(r.body());
                
                // Actualizar etiquetas de los bloques con los tipos de pregunta
                actualizarEtiquetasBloques();
                
                // Inicializar indicador de progreso después de cargar las preguntas
                actualizarIndicadorProgreso();
                
                render();
                btnEnviar.setEnabled(true);
            }

            @Override
            public void onFailure(Call<List<PreguntasKolb>> c, Throwable t) {
                btnEnviar.setText("Siguiente");
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

        // Ya no se usa tvTituloBloque, los nombres están en los círculos

        // ---- Bloque visual para las preguntas ----
        LinearLayout bloqueLayout = new LinearLayout(this);
        bloqueLayout.setOrientation(LinearLayout.VERTICAL);
        bloqueLayout.setPadding(0, 0, 0, 0);

        // ---- Agregar las 9 preguntas del bloque ----
        for (int i = inicio; i < fin; i++) {
            PreguntasKolb p = preguntas.get(i);

            // Contenedor sin card, directamente LinearLayout
            LinearLayout inner = new LinearLayout(this);
            inner.setOrientation(LinearLayout.VERTICAL);
            inner.setPadding(12, 12, 12, 12);
            LinearLayout.LayoutParams innerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            innerParams.setMargins(0, 8, 0, 8);
            inner.setLayoutParams(innerParams);

            // Label "Pregunta X" en la parte superior
            TextView labelPregunta = new TextView(this);
            labelPregunta.setText("Pregunta " + (i + 1));
            labelPregunta.setTextSize(11);
            labelPregunta.setTextColor(Color.parseColor("#60A5FA")); // Azul claro
            labelPregunta.setPadding(10, 5, 10, 5);
            labelPregunta.setBackgroundResource(R.drawable.bg_label_pregunta);
            labelPregunta.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            ((LinearLayout.LayoutParams) labelPregunta.getLayoutParams()).setMargins(0, 0, 0, 8);

            TextView enunciado = new TextView(this);
            enunciado.setText(p.getPregunta());
            enunciado.setTextSize(14);
            enunciado.setTextColor(ContextCompat.getColor(this, android.R.color.black)); // Negro
            enunciado.setPadding(0, 0, 0, 12);
            enunciado.setTypeface(null, android.graphics.Typeface.BOLD);

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
                // FrameLayout para contener el botón y el checkmark
                FrameLayout frameOption = new FrameLayout(this);
                frameOption.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                ));
                ((LinearLayout.LayoutParams) frameOption.getLayoutParams()).setMargins(4, 0, 4, 0);
                
                // Cada opción es un botón personalizado
                android.widget.Button opcionBtn = new android.widget.Button(this);
                opcionBtn.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ));
                opcionBtn.setTag(v);
                opcionBtn.setText(labels[v - 1] + "\n" + labelsSub[v - 1]);
                opcionBtn.setTextSize(12);
                opcionBtn.setPadding(6, 8, 6, 8);
                opcionBtn.setBackgroundResource(R.drawable.bg_option_button);
                opcionBtn.setTextColor(Color.parseColor("#60A5FA")); // Texto azul claro y sutil
                opcionBtn.setGravity(android.view.Gravity.CENTER);
                
                // ImageView para el checkmark (inicialmente invisible)
                ImageView checkmark = new ImageView(this);
                FrameLayout.LayoutParams checkParams = new FrameLayout.LayoutParams(
                    28, 28
                );
                checkParams.gravity = android.view.Gravity.TOP | android.view.Gravity.END;
                checkParams.setMargins(0, -4, -4, 0); // Posicionar en la esquina superior derecha, ligeramente fuera
                checkmark.setLayoutParams(checkParams);
                checkmark.setImageResource(R.drawable.ic_check_circle);
                checkmark.setVisibility(View.GONE);
                checkmark.setTag("checkmark_" + v);
                
                frameOption.addView(opcionBtn);
                frameOption.addView(checkmark);
                
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
                        if (child instanceof FrameLayout) {
                            FrameLayout frame = (FrameLayout) child;
                            // Buscar el botón dentro del FrameLayout
                            for (int k = 0; k < frame.getChildCount(); k++) {
                                View childFrame = frame.getChildAt(k);
                                if (childFrame instanceof android.widget.Button) {
                                    android.widget.Button btn = (android.widget.Button) childFrame;
                                    btn.setSelected(false);
                                    btn.setBackgroundResource(R.drawable.bg_option_button);
                                    btn.setTextColor(Color.parseColor("#60A5FA")); // Texto azul claro y sutil
                                } else if (childFrame instanceof ImageView) {
                                    // Ocultar checkmark
                                    childFrame.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                    
                    // Marcar el seleccionado
                    View parentFrame = (View) view.getParent();
                    if (parentFrame instanceof FrameLayout) {
                        FrameLayout frame = (FrameLayout) parentFrame;
                    view.setSelected(true);
                        if (view instanceof android.widget.Button) {
                            android.widget.Button btn = (android.widget.Button) view;
                            btn.setBackgroundResource(R.drawable.bg_option_selected);
                            btn.setTextColor(ContextCompat.getColor(TestActivity.this, android.R.color.white)); // Texto blanco sobre fondo azul
                            
                            // Mostrar checkmark
                            for (int k = 0; k < frame.getChildCount(); k++) {
                                View childFrame = frame.getChildAt(k);
                                if (childFrame instanceof ImageView) {
                                    childFrame.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                    
                    // Habilitar la siguiente pregunta
                    habilitarSiguientePregunta(idx + 1, inicio, fin);
                    
                    actualizarProgreso();
                    
                    // Remover borde rojo si estaba marcada
                    View preguntaView = (View) view.getParent().getParent().getParent(); // FrameLayout -> LinearLayout (opciones) -> LinearLayout (inner)
                    if (preguntaView instanceof LinearLayout) {
                        preguntaView.setBackground(null);
                    }
                });
                
                opcionesLayout.addView(frameOption);
                
                // Marcar si ya estaba seleccionado
                if (respuestas.get(i, 0) == v) {
                    opcionBtn.setSelected(true);
                    opcionBtn.setBackgroundResource(R.drawable.bg_option_selected);
                    opcionBtn.setTextColor(ContextCompat.getColor(this, android.R.color.white)); // Texto blanco sobre fondo azul
                    checkmark.setVisibility(View.VISIBLE);
                }
            }

            inner.addView(labelPregunta);
            inner.addView(enunciado);
            inner.addView(opcionesLayout);
            bloqueLayout.addView(inner);
            
            // Guardar referencia a la pregunta para validación (en orden)
            tarjetasPreguntas.add(inner);
        }

        container.addView(bloqueLayout);
        bloqueLayout.startAnimation(anim);
        
        // Actualizar texto del botón
        if (bloqueActual < TOTAL_BLOQUES - 1) {
            btnEnviar.setText("Siguiente");
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
                // Los botones ahora están dentro de FrameLayouts
                if (child instanceof FrameLayout) {
                    FrameLayout frame = (FrameLayout) child;
                    for (int k = 0; k < frame.getChildCount(); k++) {
                        View childFrame = frame.getChildAt(k);
                        if (childFrame instanceof android.widget.Button) {
                            android.widget.Button btn = (android.widget.Button) childFrame;
                            if (!btn.isSelected()) {
                                btn.setEnabled(true);
                                btn.setAlpha(1.0f);
                            }
                        }
                    }
                } else if (child instanceof android.widget.Button) {
                    // Compatibilidad con el código anterior
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
        // Ya no hay barra de progreso, solo actualizamos el indicador visual de bloques
        // El progreso se muestra visualmente en los círculos del indicador
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
                    LinearLayout preguntaLayout = tarjetasPreguntas.get(idxRelativo);
                    // Agregar borde rojo
                    android.graphics.drawable.GradientDrawable border = new android.graphics.drawable.GradientDrawable();
                    border.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                    border.setCornerRadius(12f);
                    border.setStroke(2, ContextCompat.getColor(this, R.color.rojo));
                    border.setColor(ContextCompat.getColor(this, android.R.color.white));
                    preguntaLayout.setBackground(border);
                }
            }
            
            // Hacer scroll a la primera pregunta faltante
            if (primeraFaltante >= 0 && primeraFaltante < tarjetasPreguntas.size() && scrollView != null) {
                LinearLayout preguntaFaltante = tarjetasPreguntas.get(primeraFaltante);
                scrollView.post(() -> {
                    // Obtener la posición de la tarjeta dentro del contenedor
                    int[] location = new int[2];
                    preguntaFaltante.getLocationOnScreen(location);
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
            actualizarIndicadorProgreso();
            mostrarBloque(bloqueActual);
        }
    }
    
    private void actualizarEtiquetasBloques() {
        if (tvTituloBloqueActual == null) {
            android.util.Log.w("TestActivity", "tvTituloBloqueActual no encontrado");
            return;
        }
        
        if (preguntas.isEmpty()) {
            android.util.Log.w("TestActivity", "No hay preguntas cargadas aún");
            return;
        }
        
        try {
            // Obtener el tipo_pregunta del bloque actual
            int inicio = bloqueActual * PREGUNTAS_POR_BLOQUE;
            if (inicio < preguntas.size()) {
                String tipoPregunta = preguntas.get(inicio).getTipo_pregunta();
                android.util.Log.d("TestActivity", "Bloque actual " + (bloqueActual + 1) + " - tipo_pregunta: '" + tipoPregunta + "'");
                if (tipoPregunta != null && !tipoPregunta.isEmpty()) {
                    tvTituloBloqueActual.setText(tipoPregunta);
                } else {
                    tvTituloBloqueActual.setText("Bloque " + (bloqueActual + 1));
                }
            } else {
                tvTituloBloqueActual.setText("Bloque " + (bloqueActual + 1));
            }
        } catch (Exception e) {
            android.util.Log.e("TestActivity", "Error al actualizar título del bloque", e);
            e.printStackTrace();
        }
    }
    
    private String formatearEnDosLineas(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }
        
        // Dividir por espacios
        String[] palabras = texto.trim().split("\\s+");
        
        if (palabras.length == 1) {
            // Si solo hay una palabra, dividirla por la mitad si es larga
            if (palabras[0].length() > 8) {
                int mitad = palabras[0].length() / 2;
                return palabras[0].substring(0, mitad) + "\n" + palabras[0].substring(mitad);
            }
            return palabras[0];
        } else if (palabras.length == 2) {
            // Dos palabras: una arriba, otra abajo
            return palabras[0] + "\n" + palabras[1];
        } else {
            // Más de dos palabras: primera arriba, resto abajo
            StringBuilder arriba = new StringBuilder(palabras[0]);
            StringBuilder abajo = new StringBuilder();
            for (int i = 1; i < palabras.length; i++) {
                if (i > 1) abajo.append(" ");
                abajo.append(palabras[i]);
            }
            return arriba.toString() + "\n" + abajo.toString();
        }
    }
    
    private void actualizarIndicadorProgreso() {
        // Validar que todos los elementos existan antes de actualizar
        if (circle1 == null || circle2 == null || circle3 == null || circle4 == null ||
            number1 == null || number2 == null || number3 == null || number4 == null ||
            line1 == null || line2 == null || line3 == null) {
            android.util.Log.w("TestActivity", "No se puede actualizar indicador: elementos faltantes");
            return;
        }
        
        try {
            int blueColor = ContextCompat.getColor(this, R.color.nav_blue);
            int grayColor = 0xFF9E9E9E;
            int whiteColor = ContextCompat.getColor(this, android.R.color.white);
            
            // Actualizar bloque 1
            if (bloqueActual >= 0) {
                if (bloqueActual == 0) {
                    // Bloque activo
                    circle1.setBackgroundResource(R.drawable.bg_progress_circle_active);
                    number1.setTextColor(whiteColor);
                } else {
                    // Bloque completado
                    circle1.setBackgroundResource(R.drawable.bg_progress_circle_completed);
                    number1.setTextColor(whiteColor);
                }
                line1.setBackgroundColor(bloqueActual > 0 ? blueColor : 0xFFE0E0E0);
            } else {
                circle1.setBackgroundResource(R.drawable.bg_progress_circle_inactive);
                number1.setTextColor(grayColor);
            }
            
            // Actualizar bloque 2
            if (bloqueActual >= 1) {
                if (bloqueActual == 1) {
                    // Bloque activo
                    circle2.setBackgroundResource(R.drawable.bg_progress_circle_active);
                    number2.setTextColor(whiteColor);
                } else {
                    // Bloque completado
                    circle2.setBackgroundResource(R.drawable.bg_progress_circle_completed);
                    number2.setTextColor(whiteColor);
                }
                line1.setBackgroundColor(blueColor);
                line2.setBackgroundColor(bloqueActual > 1 ? blueColor : 0xFFE0E0E0);
            } else {
                circle2.setBackgroundResource(R.drawable.bg_progress_circle_inactive);
                number2.setTextColor(grayColor);
                line1.setBackgroundColor(0xFFE0E0E0);
            }
            
            // Actualizar bloque 3
            if (bloqueActual >= 2) {
                if (bloqueActual == 2) {
                    // Bloque activo
                    circle3.setBackgroundResource(R.drawable.bg_progress_circle_active);
                    number3.setTextColor(whiteColor);
                } else {
                    // Bloque completado
                    circle3.setBackgroundResource(R.drawable.bg_progress_circle_completed);
                    number3.setTextColor(whiteColor);
                }
                line2.setBackgroundColor(blueColor);
                line3.setBackgroundColor(bloqueActual > 2 ? blueColor : 0xFFE0E0E0);
            } else {
                circle3.setBackgroundResource(R.drawable.bg_progress_circle_inactive);
                number3.setTextColor(grayColor);
                line2.setBackgroundColor(0xFFE0E0E0);
            }
            
            // Actualizar bloque 4
            if (bloqueActual >= 3) {
                // Bloque activo
                circle4.setBackgroundResource(R.drawable.bg_progress_circle_active);
                number4.setTextColor(whiteColor);
                line3.setBackgroundColor(blueColor);
            } else {
                circle4.setBackgroundResource(R.drawable.bg_progress_circle_inactive);
                number4.setTextColor(grayColor);
                line3.setBackgroundColor(0xFFE0E0E0);
            }
            
            // Actualizar el título del bloque actual
            actualizarEtiquetasBloques();
        } catch (Exception e) {
            android.util.Log.e("TestActivity", "Error al actualizar indicador de progreso", e);
            e.printStackTrace();
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
                btnEnviar.setText("Siguiente");

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
                btnEnviar.setText("Siguiente");
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
