package uth.cgyv.grupo.cuatro.myapplication.ui.filter.overlays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceLandmark;

import java.util.ArrayList;
import java.util.List;

import uth.cgyv.grupo.cuatro.myapplication.R;

public class FaceOverlayView extends View {

    private List<Face> faces = new ArrayList<>();
    private Bitmap glassesBitmap;
    private int imageWidth = 0;
    private int imageHeight = 0;

    public FaceOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        glassesBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img);
    }

    public void setFaces(List<Face> faces, int imageWidth, int imageHeight) {
        this.faces = faces;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (imageWidth == 0 || imageHeight == 0) return;

        for (Face face : faces) {
            FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);

            if (leftEye != null) {
                float eyeX = leftEye.getPosition().x;
                float eyeY = leftEye.getPosition().y;

                eyeX = imageWidth - eyeX;

                float scaleX = (float) getWidth() / (float) imageWidth;
                float scaleY = (float) getHeight() / (float) imageHeight;

                eyeX = eyeX * scaleX;
                eyeY = eyeY * scaleY;

                float size = 200;

                RectF dst = new RectF(eyeX - size/2, eyeY - size/2, eyeX + size/2, eyeY + size/2);
                canvas.drawBitmap(glassesBitmap, null, dst, null);
            }
        }
    }
}