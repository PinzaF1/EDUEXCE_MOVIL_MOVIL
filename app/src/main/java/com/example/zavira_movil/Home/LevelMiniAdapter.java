package com.example.zavira_movil.Home;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.niveleshome.LivesManager;
import com.example.zavira_movil.niveleshome.ProgressLockManager;
import com.example.zavira_movil.local.TokenManager;
import com.example.zavira_movil.model.Level;
import com.example.zavira_movil.model.Subject;
import com.example.zavira_movil.niveleshome.QuizActivity;
import com.example.zavira_movil.niveleshome.SimulacroActivity;
import com.example.zavira_movil.niveleshome.SubjectAdapter;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Adapter para mostrar los niveles dentro de un Subject (ej: Matemáticas).
 * Incluye los 5 niveles normales + el Examen Final (25 preguntas).
 *
 * Estilos añadidos (sin cambiar tu estructura):
 * - Nivel desbloqueado: borde del color del área + ripple del área.
 * - Nivel bloqueado: borde gris (#B6B9C2) + alpha 0.5 (como ya tenías).
 */
public class LevelMiniAdapter extends RecyclerView.Adapter<LevelMiniAdapter.Holder> {

    private final List<Level> niveles;
    private final Subject subject;
    private final SubjectAdapter.OnStartActivity launcher;

    public LevelMiniAdapter(List<Level> niveles, Subject subject, SubjectAdapter.OnStartActivity launcher) {
        this.niveles = niveles;
        this.subject = subject;
        this.launcher = launcher;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_level, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        // CRÍTICO: Usar TokenManager en lugar de UserSession para consistencia
        int userIdInt = TokenManager.getUserId(h.itemView.getContext());
        if (userIdInt <= 0) {
            android.util.Log.e("LevelMiniAdapter", "ERROR: userId inválido");
            h.itemView.setEnabled(false);
            h.itemView.setAlpha(0.5f);
            return;
        }
        
        String userId = String.valueOf(userIdInt);

        @ColorInt int areaColor = colorFor(subject.title);
        @ColorInt int grayLocked = Color.parseColor("#B6B9C2");

        if (position < niveles.size()) {
            // ---------------- Niveles normales ----------------
            Level nivel = niveles.get(position);
            int nivelNumero = position + 1;

            h.txtLevel.setText(nivel.getTitle() != null ? nivel.getTitle() : "Nivel " + nivelNumero);

            boolean enabled = ProgressLockManager.isLevelUnlocked(
                    h.itemView.getContext(),
                    userId,
                    subject.title,
                    nivelNumero
            );

            h.itemView.setEnabled(enabled);
            h.itemView.setAlpha(enabled ? 1f : 0.5f);

            // 🎨 Borde + ripple según estado (sin romper si root no es MaterialCardView)
            applyCardStyle(h.itemView, enabled ? areaColor : grayLocked, areaColor);

            // Mostrar vidas SOLO para el nivel ACTUAL (el más alto desbloqueado)
            // Los niveles anteriores NO deben mostrar vidas
            int nivelMaximoDesbloqueado = ProgressLockManager.getUnlockedLevel(
                    h.itemView.getContext(), userId, subject.title);
            
            // Solo mostrar vidas si:
            // 1. Es nivel 2 o superior
            // 2. Está desbloqueado
            // 3. Es el nivel ACTUAL (el más alto desbloqueado)
            // 4. Las vidas están inicializadas (no es -1)
            if (nivelNumero > 1 && enabled && nivelNumero == nivelMaximoDesbloqueado) {
                int vidas = LivesManager.getLives(h.itemView.getContext(), userId, subject.title, nivelNumero);
                // Solo mostrar vidas si están inicializadas (no es -1)
                if (vidas >= 0) {
                    // Obtener el color correcto del área usando ContextCompat
                    int colorCorrecto = obtenerColorArea(h.itemView.getContext(), subject.title);
                    mostrarVidas(h, vidas, colorCorrecto);
                } else {
                    // Vidas no inicializadas aún, ocultar hasta que se sincronicen
                    ocultarVidas(h);
                }
            } else {
                ocultarVidas(h);
            }

            h.itemView.setOnClickListener(v -> {
                if (!enabled || launcher == null) return;

                Intent i = new Intent(v.getContext(), QuizActivity.class);
                i.putExtra(QuizActivity.EXTRA_AREA, subject.title);
                String subtema = (nivel.subtopics != null && !nivel.subtopics.isEmpty())
                        ? nivel.subtopics.get(0).title
                        : "";
                i.putExtra(QuizActivity.EXTRA_SUBTEMA, subtema);
                i.putExtra(QuizActivity.EXTRA_NIVEL, nivelNumero);
                launcher.launch(i);
            });

        } else {
            // ---------------- Examen Final (25 preguntas) ----------------
            h.txtLevel.setText("Examen Final");

            int nivelMaximoDesbloqueado = ProgressLockManager.getUnlockedLevel(
                    h.itemView.getContext(),
                    userId,
                    subject.title
            );
            
            // El examen final se desbloquea cuando el nivel máximo es >= 6
            boolean unlocked = nivelMaximoDesbloqueado >= 6;

            h.itemView.setEnabled(unlocked);
            h.itemView.setAlpha(unlocked ? 1f : 0.5f);

            // 🎨 Borde + ripple para examen final
            applyCardStyle(h.itemView, unlocked ? areaColor : grayLocked, areaColor);

            // Mostrar vidas si está desbloqueado (nivel 6 = examen final) y están inicializadas
            if (unlocked) {
                int vidas = LivesManager.getLives(h.itemView.getContext(), userId, subject.title, 6);
                // Solo mostrar vidas si están inicializadas (no es -1)
                if (vidas >= 0) {
                    // Obtener el color correcto del área usando ContextCompat
                    int colorCorrecto = obtenerColorArea(h.itemView.getContext(), subject.title);
                    mostrarVidas(h, vidas, colorCorrecto);
                } else {
                    // Vidas no inicializadas aún, ocultar hasta que se sincronicen
                    ocultarVidas(h);
                }
            } else {
                ocultarVidas(h);
            }

            h.itemView.setOnClickListener(v -> {
                if (!unlocked || launcher == null) return;
                Intent i = new Intent(v.getContext(), SimulacroActivity.class);
                i.putExtra("area", subject.title);
                launcher.launch(i);
            });
        }
    }

    @Override
    public int getItemCount() {
        // 5 niveles + 1 examen final
        return (niveles == null ? 0 : niveles.size()) + 1;
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView txtLevel;

        public Holder(@NonNull View itemView) {
            super(itemView);
            txtLevel = itemView.findViewById(R.id.txtLevel);
        }
    }

    // ===== Métodos para mostrar/ocultar vidas =====

    private void mostrarVidas(Holder h, int vidas, @ColorInt int areaColor) {
        LinearLayout llVidas = h.itemView.findViewById(R.id.llVidas);
        if (llVidas == null) return;

        llVidas.removeAllViews();
        llVidas.setVisibility(View.VISIBLE);

        // Agregar corazones
        for (int i = 0; i < 3; i++) {
            ImageView ivCorazon = new ImageView(h.itemView.getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dp(h.itemView.getContext(), 20), dp(h.itemView.getContext(), 20)
            );
            params.setMargins(0, 0, dp(h.itemView.getContext(), 4), 0);
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
    }

    private void ocultarVidas(Holder h) {
        LinearLayout llVidas = h.itemView.findViewById(R.id.llVidas);
        if (llVidas != null) {
            llVidas.setVisibility(View.GONE);
        }
    }

    private int dp(android.content.Context context, int px) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(px * density);
    }

    // ===== Helpers de estilo =====

    /**
     * Aplica stroke y ripple al root si es un MaterialCardView.
     * @param itemView la vista raíz del item (item_level)
     * @param strokeColor color del borde (gris si bloqueado, color de área si desbloqueado)
     * @param rippleColor color para el efecto ripple (color de área)
     */
    private void applyCardStyle(View itemView, @ColorInt int strokeColor, @ColorInt int rippleColor) {
        if (itemView instanceof MaterialCardView) {
            MaterialCardView card = (MaterialCardView) itemView;
            card.setStrokeWidth(3);
            card.setStrokeColor(strokeColor);
            card.setRippleColor(ColorStateList.valueOf(rippleColor));
        }
        // Si no es MaterialCardView, no hacemos nada (respetamos tu layout actual)
    }

    /**
     * Mapa de color por área (usando recursos de color).
     */
    @ColorInt
    private int colorFor(String title) {
        if (title == null) return Color.parseColor("#B6B9C2");
        String t = title.toLowerCase().trim();

        if (t.contains("matem"))                                   return Color.parseColor("#E53935"); // rojo
        if (t.contains("lectura") || t.contains("lenguaje") || t.contains("espa") || t.contains("critica"))
            return Color.parseColor("#2C92EB"); // azul - usar el color del resource
        if (t.contains("social") || t.contains("ciudad"))          return Color.parseColor("#FB8C00"); // naranja
        if (t.contains("cien") || t.contains("biolo") || t.contains("fis") || t.contains("quim"))
            return Color.parseColor("#43A047"); // verde
        if (t.contains("ingl"))                                    return Color.parseColor("#8E24AA"); // morado

        return Color.parseColor("#B6B9C2");
    }
    
    /**
     * Obtiene el color del área usando ContextCompat (para usar recursos de color).
     */
    @ColorInt
    private int obtenerColorArea(android.content.Context context, String area) {
        if (area == null) return Color.parseColor("#B6B9C2");
        String a = area.toLowerCase().trim();
        
        try {
            // Isla del Conocimiento / Todas las áreas - Amarillo
            if (a.contains("conocimiento") || a.contains("isla") || 
                (a.contains("todas") && (a.contains("area") || a.contains("área")))) {
                return ContextCompat.getColor(context, R.color.area_conocimiento);
            }
            
            if (a.contains("matem")) return ContextCompat.getColor(context, R.color.area_matematicas);
            if (a.contains("lengua") || a.contains("lectura") || a.contains("espa") || a.contains("critica")) 
                return ContextCompat.getColor(context, R.color.area_lenguaje);
            if (a.contains("social") || a.contains("ciudad")) 
                return ContextCompat.getColor(context, R.color.area_sociales);
            if (a.contains("cien") || a.contains("biolo") || a.contains("fis") || a.contains("quim")) 
                return ContextCompat.getColor(context, R.color.area_ciencias);
            if (a.contains("ingl")) 
                return ContextCompat.getColor(context, R.color.area_ingles);
        } catch (Exception e) {
            // Si falla, usar colorFor como fallback
            return colorFor(area);
        }
        
        return Color.parseColor("#B6B9C2");
    }
}
