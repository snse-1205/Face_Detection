package uth.cgyv.grupo.cuatro.myapplication.ui.headangle;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;
import java.util.concurrent.ExecutionException;

import uth.cgyv.grupo.cuatro.myapplication.databinding.FragmentHeadAngleBinding;

public class HeadAngle extends Fragment {


    private FragmentHeadAngleBinding binding;
    private FaceDetector faceDetector;
    private boolean isFrontCamera = true;
    private ProcessCameraProvider cameraProvider;
    private Preview preview;
    private ImageAnalysis imageAnalysis;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHeadAngleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupFaceDetector();
        startCamera();
    }

    private void setupFaceDetector() {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .enableTracking()
                .build();

        faceDetector = FaceDetection.getClient(options);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                if (preview == null) {
                    preview = new Preview.Builder().build();
                }
                preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

                if (imageAnalysis == null) {
                    imageAnalysis = new ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build();
                    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), this::processImageProxy);
                }

                CameraSelector cameraSelector = isFrontCamera
                        ? CameraSelector.DEFAULT_FRONT_CAMERA
                        : CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    public void toggleCamera() {
        isFrontCamera = !isFrontCamera;
        startCamera();
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @OptIn(markerClass = ExperimentalGetImage.class)
    private void processImageProxy(ImageProxy imageProxy) {
        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        InputImage inputImage = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        faceDetector.process(inputImage)
                .addOnSuccessListener(faces -> {
                    for (Face face : faces) {
                        float headAngleY = face.getHeadEulerAngleY();
                        String position;

                        if (headAngleY < -15) {
                            position = "Mirando a la izquierda";
                        } else if (headAngleY > 15) {
                            position = "Mirando a la derecha";
                        } else {
                            position = "De frente";
                        }

                        String result = String.format("Ángulo: %s (%.2f°)", position, headAngleY);
                        binding.statusTextView.setText(result);
                        break;
                    }
                })
                .addOnFailureListener(e -> {
                    binding.statusTextView.setText("Error al detectar rostro");
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (faceDetector != null) {
            faceDetector.close();
        }
        binding = null;
    }
}
