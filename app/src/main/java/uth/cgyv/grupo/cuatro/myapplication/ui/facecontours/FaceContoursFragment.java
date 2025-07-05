package uth.cgyv.grupo.cuatro.myapplication.ui.facecontours;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uth.cgyv.grupo.cuatro.myapplication.R;
import uth.cgyv.grupo.cuatro.myapplication.ui.facecontours.OverlayView;

public class FaceContoursFragment extends Fragment {

    private PreviewView previewView;
    private OverlayView overlayView;
    private ExecutorService cameraExecutor;
    private FaceDetector faceDetector;
    private boolean isFrontCamera = true;
    private int imageWidth = 0;
    private int imageHeight = 0;


    private static final int REQUEST_CAMERA_PERMISSION = 100;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_face_contours, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        previewView = view.findViewById(R.id.previewView);
        overlayView = view.findViewById(R.id.overlayView);

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            startCamera();
        }

        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .build();

        faceDetector = FaceDetection.getClient(options);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
                    @Override
                    public void analyze(@NonNull ImageProxy imageProxy) {
                        analyzeImage(imageProxy);
                    }
                });

                CameraSelector cameraSelector = isFrontCamera
                        ? CameraSelector.DEFAULT_FRONT_CAMERA
                        : CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    @OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
    private void analyzeImage(@NonNull ImageProxy imageProxy) {
        android.media.Image mediaImage = imageProxy.getImage();
        if (mediaImage == null) {
            imageProxy.close();
            return;
        }

        InputImage inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

        this.imageWidth = inputImage.getWidth();
        this.imageHeight = inputImage.getHeight();

        faceDetector.process(inputImage)
                .addOnSuccessListener(faces -> {
                    List<OverlayView.FaceContourPoints> contourPointsList = new ArrayList<>();

                    for (Face face : faces) {
                        contourPointsList.add(new OverlayView.FaceContourPoints(
                                getPoints(face.getContour(FaceContour.FACE)),
                                getPoints(face.getContour(FaceContour.LEFT_EYEBROW_TOP)),
                                getPoints(face.getContour(FaceContour.RIGHT_EYEBROW_TOP)),
                                getPoints(face.getContour(FaceContour.LEFT_EYE)),
                                getPoints(face.getContour(FaceContour.RIGHT_EYE)),
                                getPoints(face.getContour(FaceContour.UPPER_LIP_TOP)),
                                getPoints(face.getContour(FaceContour.LOWER_LIP_BOTTOM)),
                                getPoints(face.getContour(FaceContour.NOSE_BRIDGE)),
                                getPoints(face.getContour(FaceContour.NOSE_BOTTOM))
                        ));
                    }

                    overlayView.setFaceContours(contourPointsList, 0, 0, isFrontCamera);

                    imageProxy.close();
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    overlayView.clear();
                    imageProxy.close();
                });
    }


    private List<float[]> getPoints(FaceContour contour) {
        List<float[]> points = new ArrayList<>();
        if (contour == null) return points;

        float viewWidth = previewView.getWidth();
        float viewHeight = previewView.getHeight();
        float imageWidth = this.imageWidth;
        float imageHeight = this.imageHeight;

        for (PointF point : contour.getPoints()) {
            float x = isFrontCamera ? (imageWidth - point.x) : point.x;
            float px = x / imageWidth * viewWidth;
            float py = point.y / imageHeight * viewHeight;
            points.add(new float[]{px, py});
        }

        return points;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) cameraExecutor.shutdown();
        if (faceDetector != null) faceDetector.close();
    }
}
