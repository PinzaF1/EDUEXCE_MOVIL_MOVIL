package com.example.zavira_movil.niveleshome;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.databinding.ActivitySubjectDetailBinding;
import com.example.zavira_movil.local.TokenManager;
import com.example.zavira_movil.model.Level;
import com.example.zavira_movil.model.Subject;
import com.example.zavira_movil.niveleshome.LivesManager;
import com.example.zavira_movil.niveleshome.ProgressLockManager;

import java.util.ArrayList;
import java.util.List;

public class SubjectDetailActivity extends AppCompatActivity {

    private ActivitySubjectDetailBinding binding;
    private LevelAdapter adapter;
    private Subject subject;

    private ActivityResultLauncher<Intent> launcher;
    private android.content.BroadcastReceiver syncReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySubjectDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        subject = (Subject) getIntent().getSerializableExtra("subject");
        if (subject == null) { finish(); return; }

        binding.tvSubjectTitle.setText(subject.title);
        binding.rvLevels.setLayoutManager(new LinearLayoutManager(this));

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && adapter != null) {
                        adapter.notifyDataSetChanged(); // refresca niveles desbloqueados
                    }
                }
        );

        // CRÍTICO: Obtener userId desde TokenManager (fuente única de verdad)
        int userId = TokenManager.getUserId(this);
        if (userId <= 0) {
            android.util.Log.e("SubjectDetailActivity", "ERROR: userId inválido (" + userId + "). No se puede mostrar niveles.");
            finish();
            return;
        }
        
        android.util.Log.d("SubjectDetailActivity", "=== INICIALIZANDO ACTIVITY ===");
        android.util.Log.d("SubjectDetailActivity", "UserId obtenido: " + userId);
        android.util.Log.d("SubjectDetailActivity", "Área: " + subject.title);
        
        // Verificar sincronización antes de mostrar la UI
        verificarYSincronizar(userId);
    }
    
    /**
     * Verifica si hay datos sincronizados y sincroniza si es necesario
     */
    private void verificarYSincronizar(int userId) {
        android.util.Log.d("SubjectDetailActivity", "Verificando sincronización...");
        
        // Obtener timestamp de última sincronización
        long lastSync = com.example.zavira_movil.sincronizacion.ProgresoSincronizador.getInstance()
                .getLastSyncTimestamp(this);
        long now = System.currentTimeMillis();
        long timeSinceLastSync = now - lastSync;
        long fiveMinutes = 5 * 60 * 1000;
        
        // Verificar si hay datos locales para esta área
        String userIdStr = String.valueOf(userId);
        int nivelLocal = ProgressLockManager.getUnlockedLevel(this, userIdStr, subject.title);
        
        android.util.Log.d("SubjectDetailActivity", "Nivel local actual: " + nivelLocal);
        android.util.Log.d("SubjectDetailActivity", "Última sincronización: " + 
                (lastSync == 0 ? "nunca" : (timeSinceLastSync / 1000) + " segundos atrás"));
        
        // Si no hay sincronización reciente (menos de 5 minutos) o nunca se ha sincronizado, sincronizar
        if (lastSync == 0 || timeSinceLastSync > fiveMinutes) {
            android.util.Log.d("SubjectDetailActivity", "Sincronizando desde backend...");
            
            // Registrar receiver para cuando termine la sincronización
            syncReceiver = new android.content.BroadcastReceiver() {
                @Override
                public void onReceive(android.content.Context context, android.content.Intent intent) {
                    if (intent != null && "com.example.zavira_movil.SYNC_COMPLETED".equals(intent.getAction())) {
                        android.util.Log.d("SubjectDetailActivity", "Sincronización completada, inicializando adapter");
                        runOnUiThread(() -> {
                            inicializarAdapter();
                            desregistrarReceiver();
                        });
                    }
                }
            };
            
            try {
                android.content.IntentFilter filter = new android.content.IntentFilter("com.example.zavira_movil.SYNC_COMPLETED");
                registerReceiver(syncReceiver, filter);
                android.util.Log.d("SubjectDetailActivity", "Receiver registrado para sincronización");
            } catch (Exception e) {
                android.util.Log.e("SubjectDetailActivity", "Error al registrar receiver", e);
                syncReceiver = null;
            }
            
            // Iniciar sincronización
            com.example.zavira_movil.sincronizacion.ProgresoSincronizador.getInstance()
                    .sincronizarDesdeBackend(this, userIdStr);
            
            // Timeout: Si después de 3 segundos no hay respuesta, mostrar UI con datos locales
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                android.util.Log.w("SubjectDetailActivity", "Timeout de sincronización, mostrando UI con datos locales");
                if (adapter == null) {
                    inicializarAdapter();
                    desregistrarReceiver();
                }
            }, 3000);
        } else {
            // Ya hay datos sincronizados recientemente, mostrar UI directamente
            android.util.Log.d("SubjectDetailActivity", "Datos sincronizados recientemente, mostrando UI directamente");
            inicializarAdapter();
        }
    }
    
    /**
     * Desregistra el receiver de sincronización de forma segura
     */
    private void desregistrarReceiver() {
        if (syncReceiver != null) {
            try {
                unregisterReceiver(syncReceiver);
                syncReceiver = null;
                android.util.Log.d("SubjectDetailActivity", "Receiver desregistrado correctamente");
            } catch (Exception e) {
                android.util.Log.e("SubjectDetailActivity", "Error al desregistrar receiver", e);
            }
        }
    }
    
    /**
     * Inicializa el adapter con los datos disponibles
     */
    private void inicializarAdapter() {
        if (adapter == null) {
            adapter = new LevelAdapter(subject.levels, subject, intent -> launcher.launch(intent));
            binding.rvLevels.setAdapter(adapter);
            android.util.Log.d("SubjectDetailActivity", "Adapter inicializado");
        } else {
            adapter.notifyDataSetChanged();
            android.util.Log.d("SubjectDetailActivity", "Adapter actualizado");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // CRÍTICO: Actualizar adapter en onResume para reflejar cambios en ProgressLockManager
        // Esto asegura que si el usuario volvió desde otra pantalla, los niveles se actualicen
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            android.util.Log.d("SubjectDetailActivity", "Adapter actualizado en onResume()");
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Desregistrar receiver si existe
        desregistrarReceiver();
    }

    /** Lista de niveles 1..5 + Examen Final */
    static class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.VH> {
        private final List<Level> levels;
        private final Subject subject;
        private final OnStartActivity onStartActivity;

        interface OnStartActivity {
            void launch(Intent i);
        }

        LevelAdapter(List<Level> levels, Subject subject, OnStartActivity onStartActivity) {
            this.levels = levels;
            this.subject = subject;
            this.onStartActivity = onStartActivity;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_level_row, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            if (pos < (levels == null ? 0 : levels.size())) {
                // ---------------- Niveles 1..5 ----------------
                Level l = levels.get(pos);
                int nivelNumero = pos + 1;

                h.tvName.setText("Nivel " + nivelNumero);
                String sub = (l.subtopics != null && !l.subtopics.isEmpty())
                        ? l.subtopics.get(0).title
                        : "—";
                h.tvSubtopic.setText(sub);

                // CRÍTICO: Usar TokenManager en lugar de UserSession para consistencia
                int userIdInt = TokenManager.getUserId(h.itemView.getContext());
                if (userIdInt <= 0) {
                    android.util.Log.e("SubjectDetailActivity", "ERROR: userId inválido en onBindViewHolder");
                    h.btnStart.setEnabled(false);
                    h.btnStart.setText("Error");
                    return;
                }
                
                String userId = String.valueOf(userIdInt);
                boolean unlocked = ProgressLockManager.isLevelUnlocked(
                        h.itemView.getContext(), userId, subject.title, nivelNumero);

                android.util.Log.d("SubjectDetailActivity", "=== CONFIGURANDO NIVEL " + nivelNumero + " ===");
                android.util.Log.d("SubjectDetailActivity", "Área: " + subject.title);
                android.util.Log.d("SubjectDetailActivity", "Usuario: " + userId);
                android.util.Log.d("SubjectDetailActivity", "Desbloqueado: " + unlocked);
                android.util.Log.d("SubjectDetailActivity", "Es nivel 2+: " + (nivelNumero > 1));
                
                // CRÍTICO: Mostrar vidas para niveles 2+ que estén desbloqueados
                // IMPORTANTE: Siempre intentar mostrar el contenedor, incluso si está oculto inicialmente
                LinearLayout llVidas = h.itemView.findViewById(R.id.llVidas);
                if (llVidas != null) {
                    if (nivelNumero > 1 && unlocked) {
                        int vidas = LivesManager.getLives(h.itemView.getContext(), userId, subject.title, nivelNumero);
                        android.util.Log.d("SubjectDetailActivity", "Nivel " + nivelNumero + " - Vidas obtenidas: " + vidas + " (área: " + subject.title + ", userId: " + userId + ")");
                        
                        // CRÍTICO: Si las vidas son -1 (no inicializadas), inicializarlas con 3
                        if (vidas == -1) {
                            android.util.Log.d("SubjectDetailActivity", "Vidas no inicializadas para nivel " + nivelNumero + ", inicializando con 3");
                            LivesManager.resetLives(h.itemView.getContext(), userId, subject.title, nivelNumero);
                            vidas = LivesManager.getLives(h.itemView.getContext(), userId, subject.title, nivelNumero);
                            android.util.Log.d("SubjectDetailActivity", "Vidas inicializadas: " + vidas);
                        }
                        
                        // CRÍTICO: Si las vidas son 0, el nivel está bloqueado y debe retrocederse
                        if (vidas == 0 && nivelNumero > 1) {
                            android.util.Log.w("SubjectDetailActivity", "⚠ Nivel " + nivelNumero + " bloqueado (vidas = 0). Retrocediendo automáticamente.");
                            // Retroceder al nivel anterior
                            int nivelRetrocedido = Math.max(1, nivelNumero - 1);
                            ProgressLockManager.retrocederPorFalloAndSync(h.itemView.getContext(), userId, subject.title, nivelNumero);
                            // Reiniciar vidas para el nivel retrocedido
                            LivesManager.resetLivesAndSync(h.itemView.getContext(), userId, subject.title, nivelRetrocedido);
                            // Ocultar vidas para este nivel bloqueado
                            ocultarVidas(h);
                            // Actualizar el estado de desbloqueo (verificar nuevamente después del retroceso)
                            boolean unlockedDespuesRetroceso = ProgressLockManager.isLevelUnlocked(h.itemView.getContext(), userId, subject.title, nivelNumero);
                            android.util.Log.d("SubjectDetailActivity", "Nivel retrocedido a: " + nivelRetrocedido + ", unlocked después retroceso: " + unlockedDespuesRetroceso);
                            // Actualizar unlocked para reflejar el estado después del retroceso
                            unlocked = unlockedDespuesRetroceso;
                        } else {
                            // Mostrar vidas normalmente
                            mostrarVidas(h, vidas, subject.title);
                        }
                    } else {
                        android.util.Log.d("SubjectDetailActivity", "Ocultando vidas (nivel 1 o bloqueado)");
                        // Ocultar vidas si es nivel 1 o está bloqueado
                        ocultarVidas(h);
                    }
                } else {
                    android.util.Log.e("SubjectDetailActivity", "ERROR CRÍTICO: llVidas es NULL en el layout item_level_row.xml");
                }

                // CRÍTICO: Crear una copia final de unlocked para usar en el lambda
                final boolean unlockedFinal = unlocked;
                
                h.btnStart.setEnabled(unlockedFinal);
                h.btnStart.setAlpha(unlockedFinal ? 1f : 0.5f);
                h.btnStart.setText(unlockedFinal ? "Comenzar" : "Bloqueado");

                h.btnStart.setOnClickListener(v -> {
                    if (!unlockedFinal) return;
                    Intent i = new Intent(v.getContext(), QuizActivity.class);
                    // Enviamos textos de UI tal cual (el mapping a API se hace en QuizActivity)
                    i.putExtra(QuizActivity.EXTRA_AREA, subject.title);
                    i.putExtra(QuizActivity.EXTRA_SUBTEMA, sub);
                    i.putExtra(QuizActivity.EXTRA_NIVEL, nivelNumero);
                    onStartActivity.launch(i);
                });

            } else {
                // ---------------- Examen Final (Simulacro) ----------------
                h.tvName.setText("Examen Final");
                h.tvSubtopic.setText("Simulacro de " + subject.title);

                // CRÍTICO: Usar TokenManager en lugar de UserSession para consistencia
                int userIdInt = TokenManager.getUserId(h.itemView.getContext());
                if (userIdInt <= 0) {
                    android.util.Log.e("SubjectDetailActivity", "ERROR: userId inválido en onBindViewHolder (Examen Final)");
                    h.btnStart.setEnabled(false);
                    h.btnStart.setText("Error");
                    return;
                }
                
                String userId = String.valueOf(userIdInt);
                // IMPORTANTE: ahora sólo se habilita si unlockedLevel >= 6 (aprobó Nivel 5)
                boolean unlocked = ProgressLockManager.getUnlockedLevel(
                        h.itemView.getContext(), userId, subject.title) >= 6;

                h.btnStart.setEnabled(unlocked);
                h.btnStart.setAlpha(unlocked ? 1f : 0.5f);
                h.btnStart.setText(unlocked ? "Comenzar" : "Bloqueado");
                
                // Mostrar vidas si está desbloqueado (nivel 6 = examen final) y están inicializadas
                if (unlocked) {
                    int vidas = LivesManager.getLives(h.itemView.getContext(), userId, subject.title, 6);
                    // Solo mostrar vidas si están inicializadas (no es -1)
                    if (vidas >= 0) {
                        mostrarVidas(h, vidas, subject.title);
                    } else {
                        // Vidas no inicializadas aún, ocultar hasta que se sincronicen
                        ocultarVidas(h);
                    }
                } else {
                    ocultarVidas(h);
                }

                h.btnStart.setOnClickListener(v -> {
                    if (!unlocked) return;

                    // Construye la lista real de subtemas desde DemoData/levels
                    ArrayList<String> subs = new ArrayList<>();
                    if (subject.levels != null) {
                        for (Level L : subject.levels) {
                            String st = (L.subtopics != null && !L.subtopics.isEmpty())
                                    ? L.subtopics.get(0).title : null;
                            if (st != null && !st.trim().isEmpty()) subs.add(st);
                        }
                    }

                    Intent i = new Intent(v.getContext(), SimulacroActivity.class);
                    // Enviamos textos UI; mapping a API se hace en SimulacroActivity
                    i.putExtra("area", subject.title);
                    i.putStringArrayListExtra("subtemas", subs);
                    onStartActivity.launch(i);
                });
            }
        }

        @Override public int getItemCount() {
            return (levels == null ? 0 : levels.size()) + 1;
        }

        private void mostrarVidas(VH h, int vidas, String area) {
            android.util.Log.d("SubjectDetailActivity", "mostrarVidas() llamado - vidas: " + vidas + ", área: " + area);
            
            // Obtener el contenedor de vidas del layout
            LinearLayout llVidas = h.itemView.findViewById(R.id.llVidas);
            if (llVidas == null) {
                android.util.Log.e("SubjectDetailActivity", "ERROR: llVidas no encontrado en el layout!");
                android.util.Log.e("SubjectDetailActivity", "Buscando en itemView: " + h.itemView.getClass().getSimpleName());
                android.util.Log.e("SubjectDetailActivity", "IDs encontrados en itemView:");
                if (h.itemView instanceof ViewGroup) {
                    ViewGroup vg = (ViewGroup) h.itemView;
                    for (int i = 0; i < vg.getChildCount(); i++) {
                        View child = vg.getChildAt(i);
                        android.util.Log.e("SubjectDetailActivity", "  Child " + i + ": " + child.getClass().getSimpleName() + " (id: " + child.getId() + ")");
                    }
                }
                return;
            }
            
            android.util.Log.d("SubjectDetailActivity", "llVidas encontrado! Visibility: " + llVidas.getVisibility());
            
            llVidas.removeAllViews();
            llVidas.setVisibility(View.VISIBLE);
            
            android.util.Log.d("SubjectDetailActivity", "llVidas configurado como VISIBLE");
            
            // Obtener color del área
            int areaColor = obtenerColorArea(h.itemView.getContext(), area);
            
            android.util.Log.d("SubjectDetailActivity", "Color del área: " + String.format("#%06X", (0xFFFFFF & areaColor)));
            
            // Agregar corazones
            for (int i = 0; i < 3; i++) {
                ImageView ivCorazon = new ImageView(h.itemView.getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dp(h.itemView.getContext(), 24), dp(h.itemView.getContext(), 24)
                );
                params.setMargins(0, 0, dp(h.itemView.getContext(), 6), 0);
                ivCorazon.setLayoutParams(params);
                ivCorazon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                
                if (i < vidas) {
                    // Corazón lleno del color del área
                    ivCorazon.setImageResource(R.drawable.ic_heart_filled);
                    ivCorazon.setColorFilter(areaColor, android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    // Corazón vacío
                    ivCorazon.setImageResource(R.drawable.ic_heart_empty);
                    ivCorazon.setColorFilter(Color.parseColor("#CCCCCC"), android.graphics.PorterDuff.Mode.SRC_IN);
                }
                llVidas.addView(ivCorazon);
            }
            
            android.util.Log.d("SubjectDetailActivity", "Corazones agregados. Total hijos en llVidas: " + llVidas.getChildCount());
        }
        
        private void ocultarVidas(VH h) {
            LinearLayout llVidas = h.itemView.findViewById(R.id.llVidas);
            if (llVidas != null) {
                llVidas.setVisibility(View.GONE);
            }
        }
        
        private int obtenerColorArea(Context context, String area) {
            if (area == null) return Color.parseColor("#B6B9C2");
            String a = area.toLowerCase().trim();
            
            if (a.contains("matem")) return ContextCompat.getColor(context, R.color.area_matematicas);
            if (a.contains("lengua") || a.contains("lectura") || a.contains("espa")) 
                return ContextCompat.getColor(context, R.color.area_lenguaje);
            if (a.contains("social") || a.contains("ciudad")) 
                return ContextCompat.getColor(context, R.color.area_sociales);
            if (a.contains("cien") || a.contains("biolo") || a.contains("fis") || a.contains("quim")) 
                return ContextCompat.getColor(context, R.color.area_ciencias);
            if (a.contains("ingl")) 
                return ContextCompat.getColor(context, R.color.area_ingles);
            
            return Color.parseColor("#B6B9C2");
        }
        
        private int dp(Context context, int px) {
            return (int) (px * context.getResources().getDisplayMetrics().density);
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvSubtopic;
            com.google.android.material.button.MaterialButton btnStart;
            VH(@NonNull View v) {
                super(v);
                tvName = v.findViewById(R.id.tvLevelName);
                tvSubtopic = v.findViewById(R.id.tvLevelSubtopic);
                btnStart = v.findViewById(R.id.btnStart);
            }
        }
    }
}
