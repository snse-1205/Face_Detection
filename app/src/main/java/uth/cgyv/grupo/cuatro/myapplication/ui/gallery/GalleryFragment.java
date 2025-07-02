package uth.cgyv.grupo.cuatro.myapplication.ui.gallery;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.PointF; // Importar PointF
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView; // Aunque no se usa, mantener si es parte de tu plantilla
import android.widget.Toast;

import androidx.camera.core.ImageProxy;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark; // Importar FaceLandmark

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uth.cgyv.grupo.cuatro.myapplication.R;
import uth.cgyv.grupo.cuatro.myapplication.databinding.FragmentGalleryBinding;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;

    private PreviewView previewView;
    Button btnFoto;

    ImageCapture captura;

    private ImageAnalysis imageAnalysis;
    private ExecutorService cameraExecutor;
    private OverlayView overlayView;
    private FaceDetector faceDetector;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        previewView = root.findViewById(R.id.previewCamara);
        btnFoto = root.findViewById(R.id.btnFoto);
        overlayView = root.findViewById(R.id.overlayView);

        cameraExecutor = Executors.newSingleThreadExecutor();

        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
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
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                captura = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, new FaceAnalyzer());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                try {
                    cameraProvider.unbindAll();
                    cameraProvider.bindToLifecycle(
                            this,
                            cameraSelector,
                            preview,
                            captura,
                            imageAnalysis
                    );
                } catch (Exception exc) {
                    Toast.makeText(requireContext(), "Error al iniciar la cámara: " + exc.getMessage(), Toast.LENGTH_SHORT).show();
                }

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error al obtener el proveedor de la cámara", e);
                Toast.makeText(requireContext(), "Error al iniciar la cámara", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
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
            @Nullable Bitmap bitmap = imageProxy.toBitmap();
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
                        List<List<PointF>> allGeneralLandmarks = new ArrayList<>();
                        List<List<PointF>> allLeftMouthLandmarks = new ArrayList<>();
                        List<List<PointF>> allRightMouthLandmarks = new ArrayList<>();
                        List<List<PointF>> allLeftEyeLandmarks = new ArrayList<>();
                        List<List<PointF>> allRightEyeLandmarks = new ArrayList<>();
                        List<List<PointF>> allNoseLandmarks = new ArrayList<>();
                        List<List<PointF>> allLeftEarLandmarks = new ArrayList<>();
                        List<List<PointF>> allRightEarLandmarks = new ArrayList<>();

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

                            List<PointF> currentGeneralLandmarks = new ArrayList<>();
                            List<PointF> currentLeftMouthLandmarks = new ArrayList<>();
                            List<PointF> currentRightMouthLandmarks = new ArrayList<>();
                            List<PointF> currentLeftEyeLandmarks = new ArrayList<>();
                            List<PointF> currentRightEyeLandmarks = new ArrayList<>();
                            List<PointF> currentNoseMarks = new ArrayList<>();

                            List<PointF> currentLeftEarMarks = new ArrayList<>();
                            List<PointF> currentRightEarMarks = new ArrayList<>();

                            // Ojos
                            FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);
                            if (leftEye != null) {
                                currentLeftEyeLandmarks.add(new PointF(leftEye.getPosition().x * scaleX, leftEye.getPosition().y * scaleY));
                            }
                            FaceLandmark rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE);
                            if (rightEye != null) {
                                currentRightEyeLandmarks.add(new PointF(rightEye.getPosition().x * scaleX, rightEye.getPosition().y * scaleY));
                            }

                            // Nariz
                            FaceLandmark noseBase = face.getLandmark(FaceLandmark.NOSE_BASE);
                            if (noseBase != null) {
                                currentNoseMarks.add(new PointF(noseBase.getPosition().x * scaleX, noseBase.getPosition().y * scaleY));
                            }
                            // Boca
                            FaceLandmark mouthLeft = face.getLandmark(FaceLandmark.MOUTH_LEFT);
                            if (mouthLeft != null) {
                                currentLeftMouthLandmarks.add(new PointF(mouthLeft.getPosition().x * scaleX, mouthLeft.getPosition().y * scaleY));
                            }
                            FaceLandmark mouthRight = face.getLandmark(FaceLandmark.MOUTH_RIGHT);
                            if (mouthRight != null) {
                                currentRightMouthLandmarks.add(new PointF(mouthRight.getPosition().x * scaleX, mouthRight.getPosition().y * scaleY));
                            }
                            FaceLandmark mouthBottom = face.getLandmark(FaceLandmark.MOUTH_BOTTOM);
                            if (mouthBottom != null) {
                                currentGeneralLandmarks.add(new PointF(mouthBottom.getPosition().x * scaleX, mouthBottom.getPosition().y * scaleY));
                            }

                            FaceLandmark leftEar = face.getLandmark(FaceLandmark.LEFT_EAR);
                            if (leftEar != null) {
                                currentLeftEarMarks.add(new PointF(leftEar.getPosition().x * scaleX, leftEar.getPosition().y * scaleY));
                            }
                            FaceLandmark rightEar = face.getLandmark(FaceLandmark.RIGHT_EAR);
                            if (rightEar != null) {
                                currentRightEarMarks.add(new PointF(rightEar.getPosition().x * scaleX, rightEar.getPosition().y * scaleY));
                            }

                            allGeneralLandmarks.add(currentGeneralLandmarks);
                            allLeftMouthLandmarks.add(currentLeftMouthLandmarks);
                            allRightMouthLandmarks.add(currentRightMouthLandmarks);
                            allLeftEyeLandmarks.add(currentLeftEyeLandmarks);
                            allRightEyeLandmarks.add(currentRightEyeLandmarks);
                            allNoseLandmarks.add(currentNoseMarks);

                            allLeftEarLandmarks.add(currentLeftEarMarks);
                            allRightEarLandmarks.add(currentRightEarMarks);

                        }

                        overlayView.setDetectedLandmarks(allGeneralLandmarks,allLeftMouthLandmarks,allRightMouthLandmarks, allLeftEyeLandmarks,allRightEyeLandmarks, allNoseLandmarks, allLeftEarLandmarks, allRightEarLandmarks);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error en la detección facial: " + e.getMessage(), e);

                        overlayView.setDetectedLandmarks(new ArrayList<>(), new ArrayList<>(),new ArrayList<>(), new ArrayList<>(),new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),  new ArrayList<>());
                    })
                    .addOnCompleteListener(task -> {
                        imageProxy.close();
                    });
        }
    }
}