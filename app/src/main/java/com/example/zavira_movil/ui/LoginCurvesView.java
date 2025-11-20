package com.example.zavira_movil.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class LoginCurvesView extends View {
    
    private Paint whitePaint;
    private Paint bluePaint;
    private Path topWhitePath;
    private Path bottomWhitePath;
    private Path bluePath;
    // Azul del botón de iniciar sesión (#004AAD)
    private int blueColor = Color.parseColor("#004AAD");
    private int whiteColor = Color.parseColor("#FFFFFF");
    
    public LoginCurvesView(Context context) {
        super(context);
        init();
    }
    
    public LoginCurvesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public LoginCurvesView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        
        topWhitePath = new Path();
        bottomWhitePath = new Path();
        bluePath = new Path();
        
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
        
        // CURVA BLANCA SUPERIOR (cóncava, baja hacia el centro)
        topWhitePath.reset();
        
        // Empezar desde la esquina superior izquierda
        topWhitePath.moveTo(0, 0);
        
        // Línea recta hasta el inicio de la curva (aproximadamente 15% de la altura)
        float topCurveStartY = height * 0.15f;
        topWhitePath.lineTo(0, topCurveStartY);
        
        // Curva Bezier cúbica suave y orgánica (cóncava)
        float topControlX1 = width * 0.20f;
        float topControlY1 = topCurveStartY + (height * 0.08f);
        float topControlX2 = width * 0.50f;
        float topControlY2 = topCurveStartY + (height * 0.18f); // Punto más bajo
        float topEndX = width * 0.50f;
        float topEndY = topCurveStartY + (height * 0.12f);
        
        topWhitePath.cubicTo(topControlX1, topControlY1, topControlX2, topControlY2, topEndX, topEndY);
        
        // Segunda mitad: centro a derecha (sube, simétrica)
        float topControlX3 = width * 0.80f;
        float topControlY3 = topControlY1;
        float topControlX4 = width;
        float topControlY4 = topCurveStartY;
        float topEndX2 = width;
        float topEndY2 = topCurveStartY;
        
        topWhitePath.cubicTo(topControlX3, topControlY3, topControlX4, topControlY4, topEndX2, topEndY2);
        
        // Completar hasta la esquina superior derecha
        topWhitePath.lineTo(width, 0);
        topWhitePath.close();
        
        // CURVA BLANCA INFERIOR (convexa, sube hacia el centro)
        bottomWhitePath.reset();
        
        // Empezar desde la esquina inferior izquierda
        bottomWhitePath.moveTo(0, height);
        
        // Línea recta hasta el inicio de la curva (aproximadamente 85% de la altura)
        float bottomCurveStartY = height * 0.85f;
        bottomWhitePath.lineTo(0, bottomCurveStartY);
        
        // Curva Bezier cúbica suave y orgánica (convexa, inversa a la superior)
        float bottomControlX1 = width * 0.20f;
        float bottomControlY1 = bottomCurveStartY - (height * 0.08f);
        float bottomControlX2 = width * 0.50f;
        float bottomControlY2 = bottomCurveStartY - (height * 0.18f); // Punto más alto
        float bottomEndX = width * 0.50f;
        float bottomEndY = bottomCurveStartY - (height * 0.12f);
        
        bottomWhitePath.cubicTo(bottomControlX1, bottomControlY1, bottomControlX2, bottomControlY2, bottomEndX, bottomEndY);
        
        // Segunda mitad: centro a derecha (baja, simétrica)
        float bottomControlX3 = width * 0.80f;
        float bottomControlY3 = bottomControlY1;
        float bottomControlX4 = width;
        float bottomControlY4 = bottomCurveStartY;
        float bottomEndX2 = width;
        float bottomEndY2 = bottomCurveStartY;
        
        bottomWhitePath.cubicTo(bottomControlX3, bottomControlY3, bottomControlX4, bottomControlY4, bottomEndX2, bottomEndY2);
        
        // Completar hasta la esquina inferior derecha
        bottomWhitePath.lineTo(width, height);
        bottomWhitePath.close();
        
        // ÁREA AZUL CENTRAL (entre las curvas blancas)
        bluePath.reset();
        
        // Empezar desde donde termina la curva blanca superior (izquierda)
        bluePath.moveTo(0, topCurveStartY);
        
        // Seguir la curva superior blanca (la misma curva)
        bluePath.cubicTo(topControlX1, topControlY1, topControlX2, topControlY2, topEndX, topEndY);
        bluePath.cubicTo(topControlX3, topControlY3, topControlX4, topControlY4, topEndX2, topEndY2);
        
        // Continuar hasta donde comienza la curva blanca inferior (derecha)
        bluePath.lineTo(width, bottomCurveStartY);
        
        // Seguir la curva inferior blanca (la misma curva)
        bluePath.cubicTo(bottomControlX4, bottomControlY4, bottomControlX3, bottomControlY3, bottomEndX2, bottomEndY2);
        bluePath.cubicTo(bottomControlX2, bottomControlY2, bottomControlX1, bottomControlY1, 0, bottomCurveStartY);
        
        // Cerrar el path
        bluePath.close();
        
        // Dibujar primero el azul (fondo)
        canvas.drawPath(bluePath, bluePaint);
        
        // Dibujar las curvas blancas encima
        canvas.drawPath(topWhitePath, whitePaint);
        canvas.drawPath(bottomWhitePath, whitePaint);
    }
}

