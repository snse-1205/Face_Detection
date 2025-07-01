package uth.cgyv.grupo.cuatro.myapplication.ui.gallery;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class OverlayView extends View {

    private Paint generalLandmarkPaint;
    private Paint LefteyeLandmarkPaint;
    private Paint RighteyeLandmarkPaint;
    private Paint noseLandmarkPaint;
    private Paint textPaint;

    private List<List<PointF>> detectedGeneralLandmarks;
    private List<List<PointF>> detecteLeftdEyeLandmarks;
    private List<List<PointF>> detectedRightEyeLandmarks;
    private List<List<PointF>> detectedNoseLandmarks;



    public OverlayView(Context context) {
        super(context);
        init();
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        generalLandmarkPaint = new Paint();
        generalLandmarkPaint.setColor(Color.RED);
        generalLandmarkPaint.setStyle(Paint.Style.FILL);
        generalLandmarkPaint.setStrokeWidth(5f);
        generalLandmarkPaint.setAntiAlias(true);


        LefteyeLandmarkPaint = new Paint();
        LefteyeLandmarkPaint.setColor(Color.BLUE);
        LefteyeLandmarkPaint.setStyle(Paint.Style.FILL);
        LefteyeLandmarkPaint.setStrokeWidth(5f);
        LefteyeLandmarkPaint.setAntiAlias(true);

        RighteyeLandmarkPaint = new Paint();
        RighteyeLandmarkPaint.setColor(Color.BLUE);
        RighteyeLandmarkPaint.setStyle(Paint.Style.FILL);
        RighteyeLandmarkPaint.setStrokeWidth(5f);
        RighteyeLandmarkPaint.setAntiAlias(true);


        noseLandmarkPaint = new Paint();
        noseLandmarkPaint.setColor(Color.GREEN);
        noseLandmarkPaint.setStyle(Paint.Style.FILL);
        noseLandmarkPaint.setStrokeWidth(5f);
        noseLandmarkPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24f);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);




        detectedGeneralLandmarks = new ArrayList<>();
        detecteLeftdEyeLandmarks = new ArrayList<>();
        detectedRightEyeLandmarks = new ArrayList<>();
        detectedNoseLandmarks = new ArrayList<>(); // Inicializa la nueva lista

    }


    public void setDetectedLandmarks(List<List<PointF>> generalLandmarks, List<List<PointF>> LefteyeLandmarks, List<List<PointF>> RighteyeLandmarks , List<List<PointF>> noseLandmarks) {
        this.detectedGeneralLandmarks = generalLandmarks;
        this.detecteLeftdEyeLandmarks = LefteyeLandmarks;
        this.detectedRightEyeLandmarks = RighteyeLandmarks;
        this.detectedNoseLandmarks = noseLandmarks;

        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);




        for (List<PointF> facePoints : detectedGeneralLandmarks) {
            for (PointF point : facePoints) {
                canvas.drawCircle(point.x, point.y, 8f, generalLandmarkPaint);

                canvas.drawText("Boca", point.x + 40f, point.y, textPaint);
            }
        }


        for (List<PointF> facePoints : detecteLeftdEyeLandmarks) {
            for (PointF point : facePoints) {
                canvas.drawCircle(point.x, point.y, 8f, LefteyeLandmarkPaint);

                canvas.drawText("Ojo Izquierdo", point.x + 40f, point.y, textPaint);
            }
        }

        for (List<PointF> facePoints : detectedRightEyeLandmarks) {
            for (PointF point : facePoints) {
                canvas.drawCircle(point.x, point.y, 8f, RighteyeLandmarkPaint);

                canvas.drawText("Ojo Derecho", point.x + 40f, point.y, textPaint);
            }
        }


        for (List<PointF> facePoints : detectedNoseLandmarks) {
            for (PointF point : facePoints) {
                canvas.drawCircle(point.x, point.y, 8f, noseLandmarkPaint);

                canvas.drawText("Nariz", point.x + 40f, point.y, textPaint);
            }
        }


    }

}