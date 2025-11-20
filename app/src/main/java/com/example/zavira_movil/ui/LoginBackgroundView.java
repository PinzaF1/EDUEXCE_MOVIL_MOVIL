package com.example.zavira_movil.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class LoginBackgroundView extends View {
    
    private Paint whitePaint;
    private Paint bluePaint;
    private Path topLeftShape;
    private Path bottomRightShape;
    // Azul de la aplicación (#2563EB)
    private int blueColor = Color.parseColor("#2563EB");
    private int whiteColor = Color.parseColor("#FFFFFF");
    
    public LoginBackgroundView(Context context) {
        super(context);
        init();
    }
    
    public LoginBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public LoginBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Paint para el blanco
        whitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        whitePaint.setColor(whiteColor);
        whitePaint.setStyle(Paint.Style.FILL);
        
        // Paint para el azul (aunque el fondo ya es azul, esto es por si acaso)
        bluePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bluePaint.setColor(blueColor);
        bluePaint.setStyle(Paint.Style.FILL);
        
        topLeftShape = new Path();
        bottomRightShape = new Path();
        
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
        
        // FORMA BLANCA SUPERIOR IZQUIERDA (tipo nube suave y orgánica, muy grande)
        topLeftShape.reset();
        
        // Empezar desde la esquina superior izquierda
        topLeftShape.moveTo(0, 0);
        
        // Bajar por el borde izquierdo (más abajo para hacerla más grande)
        topLeftShape.lineTo(0, height * 0.50f);
        
        // Crear una forma tipo "nube" o "blob" orgánico que se extiende hacia el centro
        // Primera protuberancia: se extiende hacia abajo y derecha (forma de burbuja)
        topLeftShape.cubicTo(
            width * 0.05f, height * 0.48f,   // Control point 1 (suave entrada)
            width * 0.15f, height * 0.45f,   // Control point 2 (extiende)
            width * 0.25f, height * 0.42f    // End point (primer bulto)
        );
        
        // Segunda protuberancia: continúa extendiéndose más hacia el centro
        topLeftShape.cubicTo(
            width * 0.35f, height * 0.40f,   // Control point 1
            width * 0.45f, height * 0.38f,   // Control point 2 (más hacia el centro)
            width * 0.50f, height * 0.40f    // End point (punto más extendido)
        );
        
        // Tercera protuberancia: curva hacia arriba (forma de nube)
        topLeftShape.cubicTo(
            width * 0.42f, height * 0.35f,   // Control point 1
            width * 0.35f, height * 0.32f,   // Control point 2
            width * 0.28f, height * 0.30f    // End point
        );
        
        // Cuarta protuberancia: continúa hacia arriba y izquierda
        topLeftShape.cubicTo(
            width * 0.20f, height * 0.28f,   // Control point 1
            width * 0.12f, height * 0.30f,   // Control point 2
            width * 0.08f, height * 0.32f    // End point
        );
        
        // Quinta protuberancia: sube más (forma de nube suave)
        topLeftShape.cubicTo(
            width * 0.05f, height * 0.28f,   // Control point 1
            width * 0.03f, height * 0.22f,   // Control point 2
            width * 0.02f, height * 0.18f    // End point
        );
        
        // Sexta protuberancia: cierra suavemente hacia la esquina superior
        topLeftShape.cubicTo(
            width * 0.01f, height * 0.12f,   // Control point 1
            width * 0.005f, height * 0.06f,  // Control point 2
            0, height * 0.05f                // End point (cerca de la esquina)
        );
        
        // Cerrar el path
        topLeftShape.lineTo(0, 0);
        topLeftShape.close();
        
        // FORMA BLANCA INFERIOR DERECHA (tipo nube suave y orgánica, muy grande)
        bottomRightShape.reset();
        
        // Empezar desde la esquina inferior derecha
        bottomRightShape.moveTo(width, height);
        
        // Subir por el borde derecho (más arriba para hacerla más grande)
        bottomRightShape.lineTo(width, height * 0.50f);
        
        // Crear una forma tipo "nube" o "blob" orgánico que se extiende hacia el centro
        // Primera protuberancia: se extiende hacia arriba e izquierda (forma de burbuja)
        bottomRightShape.cubicTo(
            width * 0.95f, height * 0.52f,   // Control point 1 (suave entrada)
            width * 0.85f, height * 0.55f,   // Control point 2 (extiende)
            width * 0.75f, height * 0.58f    // End point (primer bulto)
        );
        
        // Segunda protuberancia: continúa extendiéndose más hacia el centro
        bottomRightShape.cubicTo(
            width * 0.65f, height * 0.60f,   // Control point 1
            width * 0.55f, height * 0.62f,   // Control point 2 (más hacia el centro)
            width * 0.50f, height * 0.60f    // End point (punto más extendido)
        );
        
        // Tercera protuberancia: curva hacia abajo (forma de nube)
        bottomRightShape.cubicTo(
            width * 0.58f, height * 0.65f,   // Control point 1
            width * 0.65f, height * 0.68f,   // Control point 2
            width * 0.72f, height * 0.70f    // End point
        );
        
        // Cuarta protuberancia: continúa hacia abajo y derecha
        bottomRightShape.cubicTo(
            width * 0.80f, height * 0.72f,   // Control point 1
            width * 0.88f, height * 0.70f,   // Control point 2
            width * 0.92f, height * 0.68f    // End point
        );
        
        // Quinta protuberancia: baja más (forma de nube suave)
        bottomRightShape.cubicTo(
            width * 0.95f, height * 0.72f,   // Control point 1
            width * 0.97f, height * 0.78f,   // Control point 2
            width * 0.98f, height * 0.82f    // End point
        );
        
        // Sexta protuberancia: cierra suavemente hacia la esquina inferior
        bottomRightShape.cubicTo(
            width * 0.99f, height * 0.88f,   // Control point 1
            width * 0.995f, height * 0.94f,  // Control point 2
            width, height * 0.95f            // End point (cerca de la esquina)
        );
        
        // Cerrar el path
        bottomRightShape.lineTo(width, height);
        bottomRightShape.close();
        
        // Dibujar las formas blancas
        canvas.drawPath(topLeftShape, whitePaint);
        canvas.drawPath(bottomRightShape, whitePaint);
    }
}

