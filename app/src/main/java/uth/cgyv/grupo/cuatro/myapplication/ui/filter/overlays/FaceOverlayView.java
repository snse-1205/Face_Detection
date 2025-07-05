package uth.cgyv.grupo.cuatro.myapplication.ui.filter.overlays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceLandmark;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import uth.cgyv.grupo.cuatro.myapplication.R;

public class FaceOverlayView extends View {

    private List<Face> faces = new ArrayList<>();
    private int imageWidth = 0;
    private int imageHeight = 0;

    private int currentFilter = 0;

    private final Bitmap glassesBitmap;
    private final Bitmap mustacheBitmap;
    private Movie gifSmileMovie;
    private Movie gifNeutralMovie;
    private Movie gifEyesClosedMovie;
    private long movieStart = 0;



    public FaceOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        glassesBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img);
        mustacheBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_1);
        InputStream isSmile = context.getResources().openRawResource(R.raw.smile_gif);
        gifSmileMovie = Movie.decodeStream(isSmile);

        InputStream isNeutral = context.getResources().openRawResource(R.raw.neutral_gif);
        gifNeutralMovie = Movie.decodeStream(isNeutral);

        InputStream isEyesClosed = context.getResources().openRawResource(R.raw.eyes_closed_gif);
        gifEyesClosedMovie = Movie.decodeStream(isEyesClosed);


    }

    public void setFaces(List<Face> faces, int imageWidth, int imageHeight) {
        this.faces = faces;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        invalidate();
    }

    public void setCurrentFilter(int filter) {
        this.currentFilter = filter;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (imageWidth == 0 || imageHeight == 0) return;

        for (Face face : faces) {
            float scaleX = (float) getWidth() / imageWidth;
            float scaleY = (float) getHeight() / imageHeight;

            switch (currentFilter) {
                case 0:
                    //drawGlasses(canvas, face, scaleX, scaleY);
                    break;
                case 1:
                    drawMustache(canvas, face, scaleX, scaleY);
                    break;
                case 2:
                    drawHat(canvas, face, scaleX, scaleY);
                    break;
            }
        }
    }

//    private void drawGlasses(Canvas canvas, Face face, float scaleX, float scaleY) {
//        FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);
//        FaceLandmark rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE);
//
//        if (leftEye != null && rightEye != null) {
//
//            float leftX = mirror ? (imageWidth - leftEye.getPosition().x) : leftEye.getPosition().x;
//            float leftY = leftEye.getPosition().y;
//
//            float rightX = mirror ? (imageWidth - rightEye.getPosition().x) : rightEye.getPosition().x;
//            float rightY = rightEye.getPosition().y;
//
//
//            float leftScaledX = (leftX * scaleX) - 300;
//            float leftScaledY = (leftY * scaleY) - 300;
//
//            float rightScaledX = (rightX * scaleX) + 100;
//            float rightScaledY = (rightY * scaleY) - 300;
//
//            float centerX = (leftScaledX + rightScaledX) / 2;
//            float centerY = (leftScaledY + rightScaledY) / 2;
//
//            float eyeDistance = Math.abs(rightScaledX - leftScaledX);
//
//            // Obtener ángulo cabeza en eje Y (rotación lateral)
//            float headAngleY = Math.abs(face.getHeadEulerAngleY()); // siempre positivo
//
//            // Mapear ángulo a un factor entre 1 (frontal) y 0.5 (girado)
//            // Ajusta valores según necesites suavidad o rango
//            float scaleFactor = 1f - Math.min(headAngleY / 45f, 0.5f); // máximo 0.5 de reducción
//
//            // Aplicar reducción al ancho
//            float width = eyeDistance * 2.2f * scaleFactor;
//            float height = width / 2.5f;
//
//            // Destino para dibujar gafas
//            RectF dest = new RectF(
//                    centerX - width / 2f,
//                    centerY - height / 2f,
//                    centerX + width / 2f,
//                    centerY + height / 2f
//            );
//
//            canvas.drawBitmap(glassesBitmap, null, dest, null);
//        }
//    }
//
//


    private void drawMustache(Canvas canvas, Face face, float scaleX, float scaleY) {
        FaceLandmark noseBase = face.getLandmark(FaceLandmark.NOSE_BASE);
        if (noseBase != null) {
            float noseX = mirror ? (imageWidth - noseBase.getPosition().x) : noseBase.getPosition().x;
            float noseY = noseBase.getPosition().y;

            float centerX = (noseX * scaleX) -100;
            float centerY = (noseY * scaleY) -300;

            float headAngleY = Math.abs(face.getHeadEulerAngleY());

            float scaleFactor = 1f - Math.min(headAngleY / 45f, 0.5f);

            float headWidth = face.getBoundingBox().width() * scaleX;

            float width = headWidth * scaleFactor;
            float height = width / 3f;

            float left = centerX - width / 2f;
            float top = centerY + 20;
            float right = centerX + width / 2f;
            float bottom = top + height;

            RectF dest = new RectF(left, top, right, bottom);

            canvas.drawBitmap(mustacheBitmap, null, dest, null);
        }
    }

    private void drawHat(Canvas canvas, Face face, float scaleX, float scaleY) {
        float centerX = mirror ?
                (imageWidth - face.getBoundingBox().centerX()) :
                face.getBoundingBox().centerX();

        float x = centerX * scaleX;
        float y = face.getBoundingBox().top * scaleY;

        float width = face.getBoundingBox().width() * scaleX * 1.2f;
        float height = width / 1.5f;

        Float smilingProb = face.getSmilingProbability();
        Float leftEyeOpenProb = face.getLeftEyeOpenProbability();
        Float rightEyeOpenProb = face.getRightEyeOpenProbability();

        boolean eyesClosed = (leftEyeOpenProb != null && leftEyeOpenProb < 0.1f)
                && (rightEyeOpenProb != null && rightEyeOpenProb < 0.1f);

        Movie selectedGif = null;

        if (eyesClosed && gifEyesClosedMovie != null) {
            selectedGif = gifEyesClosedMovie;
        } else if (smilingProb != null && smilingProb > 0.3f && gifSmileMovie != null) {
            selectedGif = gifSmileMovie;
        } else if (gifNeutralMovie != null) {
            selectedGif = gifNeutralMovie;
        }

        if (selectedGif != null) {
            long now = android.os.SystemClock.uptimeMillis();

            if (movieStart == 0) {
                movieStart = now;
            }

            int relTime = (int) ((now - movieStart) % selectedGif.duration());
            selectedGif.setTime(relTime);

            canvas.save();
            canvas.translate(x - width / 2, y - height);

            float scaleW = width / selectedGif.width();
            float scaleH = height / selectedGif.height();
            canvas.scale(scaleW, scaleH);

            selectedGif.draw(canvas, 0, 0);
            canvas.restore();

            postInvalidateOnAnimation();
        } else {
            RectF dest = new RectF(x - width / 2, y - height, x + width / 2, y);
            canvas.drawBitmap(glassesBitmap, null, dest, null);
        }
    }

    private boolean mirror = false;

    public void setMirror(boolean mirror) {
        this.mirror = mirror;
    }


}