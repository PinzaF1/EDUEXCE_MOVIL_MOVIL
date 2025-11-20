package com.example.zavira_movil.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class WhiteCurvedHeaderView extends View {
    
    private Paint whitePaint;
    private Paint bluePaint;
    private Path whitePath;
    private Path bluePath;
    // Azul del botón de iniciar sesión (#004AAD)
    private int blueColor = Color.parseColor("#004AAD");
    private int whiteColor = Color.parseColor("#FFFFFF");
    
    // Radio de las esquinas superiores redondeadas
    private float cornerRadius = 40f;
    
    public WhiteCurvedHeaderView(Context context) {
        super(context);
        init();
    }
    
    public WhiteCurvedHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public WhiteCurvedHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Paint para el blanco
        whitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        whitePaint.setColor(whiteColor);
        whitePaint.setStyle(Paint.Style.FILL);
        
        // Paint para el azul
        bluePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bluePaint.setColor(blueColor);
        bluePaint.setStyle(Paint.Style.FILL);
        
        whitePath = new Path();
        bluePath = new Path();
        
        // Asegurar que el View no tenga fondo
        setBackground(null);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        
        if (width == 0 || height == 0) {
            return;
        }
        
        // Altura del elemento blanco (aproximadamente 30-35% de la pantalla, más prominente)
        float whiteHeight = height * 0.32f;
        
        // Punto donde comienza la curva cóncava (más arriba para que la curva sea más visible)
        float curveStartY = whiteHeight * 0.65f;
        
        // Punto más bajo de la curva cóncava (centro)
        float curveLowestY = whiteHeight * 0.92f;
        
        // ELEMENTO BLANCO SUPERIOR con esquinas redondeadas arriba y curva cóncava abajo
        whitePath.reset();
        
        // Empezar desde la esquina superior izquierda (después del radio)
        whitePath.moveTo(cornerRadius, 0);
        
        // Arco superior izquierdo
        RectF topLeftArc = new RectF(0, 0, cornerRadius * 2, cornerRadius * 2);
        whitePath.arcTo(topLeftArc, 180, 90, false);
        
        // Línea recta vertical izquierda hasta el inicio de la curva
        whitePath.lineTo(0, curveStartY);
        
        // CURVA CÓNCAVA INFERIOR (baja en el centro, sube hacia los lados) - más suave y orgánica
        // Primera mitad: izquierda a centro (baja suavemente)
        float curveControlX1 = width * 0.15f;
        float curveControlY1 = curveStartY + (whiteHeight * 0.10f);
        float curveControlX2 = width * 0.50f;
        float curveControlY2 = curveLowestY; // Punto más bajo de la curva
        float curveEndX = width * 0.50f;
        float curveEndY = curveLowestY - (whiteHeight * 0.02f);
        
        whitePath.cubicTo(curveControlX1, curveControlY1, curveControlX2, curveControlY2, curveEndX, curveEndY);
        
        // Segunda mitad: centro a derecha (sube suavemente, simétrica)
        float curveControlX3 = width * 0.85f;
        float curveControlY3 = curveControlY1;
        float curveControlX4 = width;
        float curveControlY4 = curveStartY;
        float curveEndX2 = width;
        float curveEndY2 = curveStartY;
        
        whitePath.cubicTo(curveControlX3, curveControlY3, curveControlX4, curveControlY4, curveEndX2, curveEndY2);
        
        // Línea hasta el arco superior derecho
        whitePath.lineTo(width - cornerRadius, 0);
        
        // Arco superior derecho
        RectF topRightArc = new RectF(width - (cornerRadius * 2), 0, width, cornerRadius * 2);
        whitePath.arcTo(topRightArc, 270, 90, false);
        
        // Cerrar el path
        whitePath.close();
        
        // ELEMENTO AZUL INFERIOR que sigue exactamente la curva del blanco
        bluePath.reset();
        
        // Empezar desde la esquina inferior izquierda
        bluePath.moveTo(0, height);
        
        // Subir hasta el inicio de la curva (izquierda)
        bluePath.lineTo(0, curveStartY);
        
        // Seguir EXACTAMENTE la misma curva cóncava del blanco
        bluePath.cubicTo(curveControlX1, curveControlY1, curveControlX2, curveControlY2, curveEndX, curveEndY);
        bluePath.cubicTo(curveControlX3, curveControlY3, curveControlX4, curveControlY4, curveEndX2, curveEndY2);
        
        // Continuar hasta la esquina inferior derecha
        bluePath.lineTo(width, height);
        
        // Cerrar el path
        bluePath.close();
        
        // Dibujar primero el azul (fondo)
        canvas.drawPath(bluePath, bluePaint);
        
        // Dibujar el blanco encima (para que se vea como si estuviera "sobre" el azul)
        canvas.drawPath(whitePath, whitePaint);
    }
}

