package uth.cgyv.grupo.cuatro.myapplication.ui.emotions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class OverlayEmotions extends View{
    private Paint emotionTextPaint;
    // private Paint faceRectPaint; // Opcional: si quieres el cuadro delimitador

    private List<String> detectedEmotions;
    private List<Rect> detectedFaceRects;

    public OverlayEmotions(Context context) { // Constructor renombrado
        super(context);
        init();
    }

    public OverlayEmotions(Context context, AttributeSet attrs) { // Constructor renombrado
        super(context, attrs);
        init();
    }

    public OverlayEmotions(Context context, AttributeSet attrs, int defStyleAttr) { // Constructor renombrado
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        emotionTextPaint = new Paint();
        emotionTextPaint.setColor(Color.YELLOW);
        emotionTextPaint.setTextSize(48f);
        emotionTextPaint.setAntiAlias(true);
        emotionTextPaint.setTextAlign(Paint.Align.CENTER);
        emotionTextPaint.setFakeBoldText(true);
        emotionTextPaint.setShadowLayer(5f, 0f, 0f, Color.BLACK);

        // Opcional: Configuración de la pintura para el cuadro delimitador
        // faceRectPaint = new Paint();
        // faceRectPaint.setColor(Color.GREEN);
        // faceRectPaint.setStyle(Paint.Style.STROKE);
        // faceRectPaint.setStrokeWidth(8f);
        // faceRectPaint.setAntiAlias(true);

        detectedEmotions = new ArrayList<>();
        detectedFaceRects = new ArrayList<>();
    }

    /**
     * Establece las listas de rectángulos de caras y las emociones para dibujar.
     * @param faceRects La lista de rectángulos de caras.
     * @param emotions Una lista de Strings con la emoción detectada para cada cara.
     */
    public void setDetectedFacesAndEmotions(List<Rect> faceRects, List<String> emotions) {
        this.detectedFaceRects = faceRects;
        this.detectedEmotions = emotions;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Opcional: Dibuja los rectángulos de las caras si aún los necesitas
        // for (Rect faceRect : detectedFaceRects) {
        //     canvas.drawRect(faceRect, faceRectPaint);
        // }

        // Dibuja el texto de la emoción para cada cara
        for (int i = 0; i < detectedEmotions.size(); i++) {
            if (i < detectedFaceRects.size()) {
                Rect faceRect = detectedFaceRects.get(i);
                String emotion = detectedEmotions.get(i);
                canvas.drawText(emotion, faceRect.centerX(), faceRect.top - 40f, emotionTextPaint);
            }
        }
    }
}