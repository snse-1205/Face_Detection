package uth.cgyv.grupo.cuatro.myapplication.ui.emotions;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.camera.core.ImageProxy;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;

import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uth.cgyv.grupo.cuatro.myapplication.R;

import uth.cgyv.grupo.cuatro.myapplication.databinding.FragmentFaceEmotionsBinding;
import uth.cgyv.grupo.cuatro.myapplication.ui.emotions.OverlayEmotions;

public class FaceEmotions extends Fragment {

    private FragmentFaceEmotionsBinding binding;
    private PreviewView previewView;
    Button btnFoto;

    ImageCapture captura;

    private ImageAnalysis imageAnalysis;
    private ExecutorService cameraExecutor;
    private OverlayEmotions overlayView;
    private FaceDetector faceDetector;
    private boolean isFrontCamera = false;
    private ProcessCameraProvider cameraProvider;
    private Preview preview;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentFaceEmotionsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        previewView = root.findViewById(R.id.previewCamara2);
        btnFoto = root.findViewById(R.id.btnFoto2);
        overlayView = root.findViewById(R.id.overlayView2);

        cameraExecutor = Executors.newSingleThreadExecutor();

        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build();
        faceDetector = FaceDetection.getClient(options);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            iniciarCamara();
        }

        btnFoto.setOnClickListener(v -> takePhoto());
    }

    private void iniciarCamara() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                if (preview == null) {
                    preview = new Preview.Builder().build();
                }
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                captura = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                if (imageAnalysis == null) {
                    imageAnalysis = new ImageAnalysis.Builder()
                            .setTargetResolution(new Size(640, 480))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build();

                    imageAnalysis.setAnalyzer(cameraExecutor, new FaceAnalyzer());
                }

                CameraSelector cameraSelector = isFrontCamera
                        ? CameraSelector.DEFAULT_FRONT_CAMERA
                        : CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();

                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        captura,
                        imageAnalysis
                );

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(requireContext(), "Error al iniciar la cámara: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    public void toggleCamera() {
        isFrontCamera = !isFrontCamera;
        iniciarCamara();
    }

    private void takePhoto() {
        if (captura == null) {
            Toast.makeText(requireContext(), "Cámara no inicializada", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(System.currentTimeMillis());

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Images");
        }

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(
                requireContext().getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
        ).build();

        captura.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        String msg = "Foto tomada con éxito: " + outputFileResults.getSavedUri();
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, msg);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exc) {
                        Log.e(TAG, "Error al tomar foto: " + exc.getMessage(), exc);
                        Toast.makeText(requireContext(), "Error al tomar foto: " + exc.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (faceDetector != null) {
            faceDetector.close();
        }
    }

    private class FaceAnalyzer implements ImageAnalysis.Analyzer {
        @Override
        public void analyze(@NonNull ImageProxy imageProxy) {
            @Nullable Bitmap bitmap = imageProxyToBitmap(imageProxy);
            if (bitmap == null) {
                imageProxy.close();
                return;
            }

            int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
            Matrix matrix = new Matrix();
            matrix.postRotate(rotationDegrees);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            InputImage inputImage = InputImage.fromBitmap(rotatedBitmap, 0);

            faceDetector.process(inputImage)
                    .addOnSuccessListener(faces -> {
                        List<Rect> faceRects = new ArrayList<>();
                        List<String> allEmotions = new ArrayList<>();

                        float scaleX = (float) previewView.getWidth() / rotatedBitmap.getWidth();
                        float scaleY = (float) previewView.getHeight() / rotatedBitmap.getHeight();

                        for (Face face : faces) {
                            Rect boundingBox = face.getBoundingBox();
                            faceRects.add(new Rect(
                                    (int) (boundingBox.left * scaleX),
                                    (int) (boundingBox.top * scaleY),
                                    (int) (boundingBox.right * scaleX),
                                    (int) (boundingBox.bottom * scaleY)
                            ));

                            String emotion = "";
                            if (face.getSmilingProbability() != null) {
                                float smileProb = face.getSmilingProbability();
                                if (smileProb > 0.8) {
                                    emotion = "Feliz 😊";
                                } else if (smileProb >= 0.4 && smileProb < 0.8) {
                                    emotion = "Neutral 🙂";
                                } else if(smileProb >= 0.0 && smileProb < 0.4){emotion="Triste 😢";}else{emotion="";}
                            }


                            if (face.getLeftEyeOpenProbability() != null && face.getRightEyeOpenProbability() != null) {
                                float leftEyeProb = face.getLeftEyeOpenProbability();
                                float rightEyeProb = face.getRightEyeOpenProbability();

                                if (leftEyeProb < 0.2 && rightEyeProb < 0.2) {
                                    emotion = "Ojos Cerrados 😴";
                                } else if (leftEyeProb < 0.2) {
                                    emotion = "Guiño Izquierdo 😉";
                                } else if (rightEyeProb < 0.2) {
                                    emotion = "Guiño Derecho 😉";
                                }
                            }
                            allEmotions.add(emotion);
                        }
                        overlayView.setDetectedFacesAndEmotions(faceRects, allEmotions);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error en la detección facial: " + e.getMessage(), e);
                        overlayView.setDetectedFacesAndEmotions(new ArrayList<>(), new ArrayList<>());
                    })
                    .addOnCompleteListener(task -> {
                        imageProxy.close();
                    });
        }
    }

    private Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();
        if (planes.length < 3) return null;

        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];

        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(
                nv21,
                ImageFormat.NV21,
                imageProxy.getWidth(),
                imageProxy.getHeight(),
                null
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(
                new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()),
                100,
                out
        );

        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }
}