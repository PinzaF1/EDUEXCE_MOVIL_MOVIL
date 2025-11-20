package com.example.zavira_movil.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class WaveHeaderView extends View {
    
    private Paint bluePaint;
    private Path wavePath;
    private int blueColor = Color.parseColor("#2563EB");
    
    public WaveHeaderView(Context context) {
        super(context);
        init();
    }
    
    public WaveHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public WaveHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Paint para el azul oscuro
        bluePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bluePaint.setColor(blueColor);
        bluePaint.setStyle(Paint.Style.FILL);
        
        wavePath = new Path();
        
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
        
        // Crear la curva suave y cóncava como en la imagen
        wavePath.reset();
        
        // Empezar desde la esquina superior izquierda
        wavePath.moveTo(0, 0);
        
        // Línea recta hasta el inicio de la curva (izquierda)
        // La curva comienza aproximadamente al 60% de la altura
        float curveStartY = height * 0.60f;
        wavePath.lineTo(0, curveStartY);
        
        // Usar curvas Bezier cúbicas para una curva suave y cóncava
        // La curva baja en el centro y sube hacia los lados
        // Primera mitad: izquierda a centro
        float controlX1 = width * 0.25f;
        float controlY1 = height * 0.68f;
        float controlX2 = width * 0.50f;
        float controlY2 = height * 0.88f; // Punto más bajo de la curva
        float endX = width * 0.50f;
        float endY = height * 0.78f;
        
        wavePath.cubicTo(controlX1, controlY1, controlX2, controlY2, endX, endY);
        
        // Segunda mitad: centro a derecha (simétrica)
        float controlX3 = width * 0.75f;
        float controlY3 = height * 0.68f;
        float controlX4 = width;
        float controlY4 = height * 0.68f;
        float endX2 = width;
        float endY2 = curveStartY;
        
        wavePath.cubicTo(controlX3, controlY3, controlX4, controlY4, endX2, endY2);
        
        // Completar el rectángulo hasta la esquina superior derecha
        wavePath.lineTo(width, 0);
        
        // Cerrar el path
        wavePath.close();
        
        // Dibujar el fondo azul oscuro
        canvas.drawPath(wavePath, bluePaint);
        
        // NO dibujar la línea azul claro - se ve mejor sin ella
    }
}

