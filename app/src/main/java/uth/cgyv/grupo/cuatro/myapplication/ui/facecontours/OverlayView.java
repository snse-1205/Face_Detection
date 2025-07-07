package uth.cgyv.grupo.cuatro.myapplication.ui.facecontours;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OverlayView extends View {

    private List<FaceContourPoints> faceContours = new ArrayList<>();

    private final Paint paintFace = new Paint();
    private final Paint paintLeftEyebrow = new Paint();
    private final Paint paintRightEyebrow = new Paint();
    private final Paint paintLeftEye = new Paint();
    private final Paint paintRightEye = new Paint();
    private final Paint paintMouth = new Paint();
    private final Paint paintNose = new Paint();

    public OverlayView(@NonNull Context context) {
        super(context);
        init();
    }

    public OverlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paintFace.setColor(Color.GREEN);
        paintFace.setStyle(Paint.Style.STROKE);
        paintFace.setStrokeWidth(5f);

        paintLeftEyebrow.setColor(Color.RED);
        paintLeftEyebrow.setStyle(Paint.Style.STROKE);
        paintLeftEyebrow.setStrokeWidth(4f);

        paintRightEyebrow.setColor(Color.RED);
        paintRightEyebrow.setStyle(Paint.Style.STROKE);
        paintRightEyebrow.setStrokeWidth(4f);

        paintLeftEye.setColor(Color.BLUE);
        paintLeftEye.setStyle(Paint.Style.STROKE);
        paintLeftEye.setStrokeWidth(4f);

        paintRightEye.setColor(Color.BLUE);
        paintRightEye.setStyle(Paint.Style.STROKE);
        paintRightEye.setStrokeWidth(4f);

        paintMouth.setColor(Color.MAGENTA);
        paintMouth.setStyle(Paint.Style.STROKE);
        paintMouth.setStrokeWidth(5f);

        paintNose.setColor(Color.YELLOW);
        paintNose.setStyle(Paint.Style.STROKE);
        paintNose.setStrokeWidth(4f);
    }

    public void setFaceContours(List<FaceContourPoints> contours) {
        this.faceContours = contours;
        postInvalidate();
    }

    public void clear() {
        faceContours.clear();
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (faceContours.isEmpty()) return;

        for (FaceContourPoints contourPoints : faceContours) {
            drawContour(canvas, contourPoints.face, paintFace);
            drawContour(canvas, contourPoints.leftEyebrow, paintLeftEyebrow);
            drawContour(canvas, contourPoints.rightEyebrow, paintRightEyebrow);
            drawContour(canvas, contourPoints.leftEye, paintLeftEye);
            drawContour(canvas, contourPoints.rightEye, paintRightEye);
            drawContour(canvas, contourPoints.upperLip, paintMouth);
            drawContour(canvas, contourPoints.lowerLip, paintMouth);
            drawContour(canvas, contourPoints.noseBridge, paintNose);
            drawContour(canvas, contourPoints.noseBottom, paintNose);
        }
    }



    private void drawContour(Canvas canvas, List<float[]> points, Paint paint) {
        if (points == null || points.isEmpty()) return;

        for (int i = 0; i < points.size() - 1; i++) {
            float[] start = points.get(i);
            float[] end = points.get(i + 1);

            canvas.drawLine(start[0], start[1], end[0], end[1], paint);
        }

        for (float[] point : points) {
            canvas.drawCircle(point[0], point[1], 6f, paint);
        }
    }

    public static class FaceContourPoints {
        List<float[]> face;
        List<float[]> leftEyebrow;
        List<float[]> rightEyebrow;
        List<float[]> leftEye;
        List<float[]> rightEye;
        List<float[]> upperLip;
        List<float[]> lowerLip;
        List<float[]> noseBridge;
        List<float[]> noseBottom;

        public FaceContourPoints(List<float[]> face,
                                 List<float[]> leftEyebrow,
                                 List<float[]> rightEyebrow,
                                 List<float[]> leftEye,
                                 List<float[]> rightEye,
                                 List<float[]> upperLip,
                                 List<float[]> lowerLip,
                                 List<float[]> noseBridge,
                                 List<float[]> noseBottom) {
            this.face = face;
            this.leftEyebrow = leftEyebrow;
            this.rightEyebrow = rightEyebrow;
            this.leftEye = leftEye;
            this.rightEye = rightEye;
            this.upperLip = upperLip;
            this.lowerLip = lowerLip;
            this.noseBridge = noseBridge;
            this.noseBottom = noseBottom;
        }
    }
}
