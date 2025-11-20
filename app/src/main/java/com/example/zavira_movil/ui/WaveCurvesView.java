package com.example.zavira_movil.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class WaveCurvesView extends View {
    
    private Paint bluePaint;
    private Path topWavePath;
    private Path bottomWavePath;
    // Azul del botón de iniciar sesión (#004AAD)
    private int blueColor = Color.parseColor("#004AAD");
    
    public WaveCurvesView(Context context) {
        super(context);
        init();
    }
    
    public WaveCurvesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public WaveCurvesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Paint para el azul claro
        bluePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bluePaint.setColor(blueColor);
        bluePaint.setStyle(Paint.Style.FILL);
        
        topWavePath = new Path();
        bottomWavePath = new Path();
        
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
        
        // CURVA SUPERIOR (cóncava, baja hacia el centro) - más grande y prominente
        topWavePath.reset();
        
        // Empezar desde la esquina superior izquierda
        topWavePath.moveTo(0, 0);
        
        // Línea recta hasta el inicio de la curva (más abajo para hacerla más grande)
        float topCurveStartY = height * 0.25f; // La curva comienza a los 25% de la altura
        topWavePath.lineTo(0, topCurveStartY);
        
        // Curva Bezier cúbica suave y orgánica - más pronunciada
        // Primera mitad: izquierda a centro (baja más)
        float topControlX1 = width * 0.15f;
        float topControlY1 = topCurveStartY + (height * 0.12f);
        float topControlX2 = width * 0.50f;
        float topControlY2 = topCurveStartY + (height * 0.25f); // Punto más bajo, más pronunciado
        float topEndX = width * 0.50f;
        float topEndY = topCurveStartY + (height * 0.18f);
        
        topWavePath.cubicTo(topControlX1, topControlY1, topControlX2, topControlY2, topEndX, topEndY);
        
        // Segunda mitad: centro a derecha (sube, simétrica)
        float topControlX3 = width * 0.85f;
        float topControlY3 = topControlY1;
        float topControlX4 = width;
        float topControlY4 = topCurveStartY;
        float topEndX2 = width;
        float topEndY2 = topCurveStartY;
        
        topWavePath.cubicTo(topControlX3, topControlY3, topControlX4, topControlY4, topEndX2, topEndY2);
        
        // Completar hasta la esquina superior derecha
        topWavePath.lineTo(width, 0);
        topWavePath.close();
        
        // CURVA INFERIOR (convexa, sube hacia el centro) - más grande y prominente
        bottomWavePath.reset();
        
        // Empezar desde la esquina inferior izquierda
        bottomWavePath.moveTo(0, height);
        
        // Línea recta hasta el inicio de la curva (más arriba para hacerla más grande)
        float bottomCurveStartY = height * 0.75f; // La curva comienza a los 75% de la altura
        bottomWavePath.lineTo(0, bottomCurveStartY);
        
        // Curva Bezier cúbica suave y orgánica (inversa a la superior) - más pronunciada
        // Primera mitad: izquierda a centro (sube más)
        float bottomControlX1 = width * 0.15f;
        float bottomControlY1 = bottomCurveStartY - (height * 0.12f);
        float bottomControlX2 = width * 0.50f;
        float bottomControlY2 = bottomCurveStartY - (height * 0.25f); // Punto más alto, más pronunciado
        float bottomEndX = width * 0.50f;
        float bottomEndY = bottomCurveStartY - (height * 0.18f);
        
        bottomWavePath.cubicTo(bottomControlX1, bottomControlY1, bottomControlX2, bottomControlY2, bottomEndX, bottomEndY);
        
        // Segunda mitad: centro a derecha (baja, simétrica)
        float bottomControlX3 = width * 0.85f;
        float bottomControlY3 = bottomControlY1;
        float bottomControlX4 = width;
        float bottomControlY4 = bottomCurveStartY;
        float bottomEndX2 = width;
        float bottomEndY2 = bottomCurveStartY;
        
        bottomWavePath.cubicTo(bottomControlX3, bottomControlY3, bottomControlX4, bottomControlY4, bottomEndX2, bottomEndY2);
        
        // Completar hasta la esquina inferior derecha
        bottomWavePath.lineTo(width, height);
        bottomWavePath.close();
        
        // Dibujar ambas curvas azules
        canvas.drawPath(topWavePath, bluePaint);
        canvas.drawPath(bottomWavePath, bluePaint);
    }
}

