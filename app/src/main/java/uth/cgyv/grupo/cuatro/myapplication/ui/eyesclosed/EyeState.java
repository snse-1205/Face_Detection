package uth.cgyv.grupo.cuatro.myapplication.ui.eyesclosed;

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

import java.util.concurrent.ExecutionException;

import uth.cgyv.grupo.cuatro.myapplication.databinding.FragmentEyeStateBinding;

public class EyeState extends Fragment {

    private FragmentEyeStateBinding binding;
    private FaceDetector faceDetector;
    private boolean isFrontCamera = true;
    private ProcessCameraProvider cameraProvider;
    private Preview preview;
    private ImageAnalysis imageAnalysis;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEyeStateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupFaceDetector();
        startCamera();
    }

    public void toggleCamera() {
        isFrontCamera = !isFrontCamera;
        startCamera(); // Reiniciar con nueva selección
    }

    private void setupFaceDetector() {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
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

                preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

                imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), this::processImageProxy);

                CameraSelector cameraSelector = isFrontCamera
                        ? CameraSelector.DEFAULT_FRONT_CAMERA
                        : CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        getViewLifecycleOwner(),
                        cameraSelector,
                        preview,
                        imageAnalysis
                );

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    @SuppressLint("SetTextI18n")
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
                        Float leftProb = face.getLeftEyeOpenProbability();
                        Float rightProb = face.getRightEyeOpenProbability();

                        String state;
                        if (leftProb != null && rightProb != null) {
                            if (leftProb > 0.5f && rightProb > 0.5f) {
                                state = "Ojos abiertos";
                            } else {
                                state = "Ojos cerrados";
                            }
                        } else {
                            state = "No se pudo determinar";
                        }

                        binding.statusTextView.setText("Estado de ojos: " + state);
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
