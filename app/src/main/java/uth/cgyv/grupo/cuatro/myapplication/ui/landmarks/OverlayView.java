package uth.cgyv.grupo.cuatro.myapplication.ui.landmarks;

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
    private Paint LeftmouthLandmarkPaint;
    private Paint RightmouthLandmarkPaint;
    private Paint LefteyeLandmarkPaint;
    private Paint RighteyeLandmarkPaint;
    private Paint noseLandmarkPaint;
    private Paint LeftearLandmarkPaint;
    private Paint RightearLandmarkPaint;
    private Paint textPaint;

    private List<List<PointF>> detectedGeneralLandmarks;
    private List<List<PointF>> detectedLeftMouthLandmarks;
    private List<List<PointF>> detectedRightMouthLandmarks;
    private List<List<PointF>> detecteLeftdEyeLandmarks;
    private List<List<PointF>> detectedRightEyeLandmarks;
    private List<List<PointF>> detectedNoseLandmarks;
    private List<List<PointF>> detectedLeftEarLandmarks;
    private List<List<PointF>> detectedRightEarLandmarks;





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

        LeftmouthLandmarkPaint = new Paint();
        LeftmouthLandmarkPaint.setColor(Color.RED);
        LeftmouthLandmarkPaint.setStyle(Paint.Style.FILL);
        LeftmouthLandmarkPaint.setStrokeWidth(5f);
        LeftmouthLandmarkPaint.setAntiAlias(true);



        RightmouthLandmarkPaint = new Paint();
        RightmouthLandmarkPaint.setColor(Color.RED);
        RightmouthLandmarkPaint.setStyle(Paint.Style.FILL);
        RightmouthLandmarkPaint.setStrokeWidth(5f);
        RightmouthLandmarkPaint.setAntiAlias(true);

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

        LeftearLandmarkPaint = new Paint();
        LeftearLandmarkPaint.setColor(Color.BLACK);
        LeftearLandmarkPaint.setStyle(Paint.Style.FILL);
        LeftearLandmarkPaint.setStrokeWidth(5f);
        LeftearLandmarkPaint.setAntiAlias(true);

        RightearLandmarkPaint = new Paint();
        RightearLandmarkPaint.setColor(Color.BLACK);
        RightearLandmarkPaint.setStyle(Paint.Style.FILL);
        RightearLandmarkPaint.setStrokeWidth(5f);
        RightearLandmarkPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24f);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);




        detectedGeneralLandmarks = new ArrayList<>();
        detectedLeftMouthLandmarks = new ArrayList<>();
        detectedRightMouthLandmarks = new ArrayList<>();
        detecteLeftdEyeLandmarks = new ArrayList<>();
        detectedRightEyeLandmarks = new ArrayList<>();
        detectedNoseLandmarks = new ArrayList<>();
        detectedLeftEarLandmarks = new ArrayList<>();
        detectedRightEarLandmarks = new ArrayList<>();

    }


    public void setDetectedLandmarks(List<List<PointF>> generalLandmarks, List<List<PointF>> LeftmouthLandmarks,List<List<PointF>> RightmouthLandmarks, List<List<PointF>> LefteyeLandmarks, List<List<PointF>> RighteyeLandmarks , List<List<PointF>> noseLandmarks, List<List<PointF>> LeftEarLandmarks, List<List<PointF>> RightEarLandmarks) {
        this.detectedGeneralLandmarks = generalLandmarks;
        this.detecteLeftdEyeLandmarks = LefteyeLandmarks;
        this.detectedRightEyeLandmarks = RighteyeLandmarks;
        this.detectedNoseLandmarks = noseLandmarks;
        this.detectedLeftMouthLandmarks = LeftmouthLandmarks;
        this.detectedRightMouthLandmarks = RightmouthLandmarks;
        this.detectedLeftEarLandmarks = LeftEarLandmarks;
        this.detectedRightEarLandmarks = RightEarLandmarks;

        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);






        for (List<PointF> facePoints : detectedLeftMouthLandmarks) {
            for (PointF point : facePoints) {
                canvas.drawCircle(point.x, point.y, 8f, LeftmouthLandmarkPaint);

                canvas.drawText("Boca Izquierda", point.x + 40f, point.y, textPaint);
            }
        }

        for (List<PointF> facePoints : detectedRightMouthLandmarks) {
            for (PointF point : facePoints) {
                canvas.drawCircle(point.x, point.y, 8f, RightmouthLandmarkPaint);

                canvas.drawText("Boca derecha", point.x + 40f, point.y, textPaint);
            }
        }


        for (List<PointF> facePoints : detectedGeneralLandmarks) {
            for (PointF point : facePoints) {
                canvas.drawCircle(point.x, point.y, 8f, generalLandmarkPaint);

                canvas.drawText("Boca inferior", point.x + 40f, point.y, textPaint);
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

        for (List<PointF> facePoints : detectedLeftEarLandmarks) {
            for (PointF point : facePoints) {
                canvas.drawCircle(point.x, point.y, 8f, LeftearLandmarkPaint);

                canvas.drawText("Oreja izquierda", point.x + 40f, point.y, textPaint);
            }
        }

        for (List<PointF> facePoints : detectedRightEarLandmarks) {
            for (PointF point : facePoints) {
                canvas.drawCircle(point.x, point.y, 8f, RightearLandmarkPaint);

                canvas.drawText("Oreja derecha", point.x + 40f, point.y, textPaint);
            }
        }


    }

}