package uth.cgyv.grupo.cuatro.myapplication.ui.filter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
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
import com.google.mlkit.vision.face.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uth.cgyv.grupo.cuatro.myapplication.R;
import uth.cgyv.grupo.cuatro.myapplication.ui.filter.overlays.FaceOverlayView;

@ExperimentalGetImage
public class FiltrosCamaraXFragment extends Fragment {

    private PreviewView previewView;
    private FaceOverlayView overlayView;
    private ExecutorService cameraExecutor;
    private int currentFilter = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filtros_camara_x, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        previewView = view.findViewById(R.id.fullscreen_content);
        overlayView = view.findViewById(R.id.face_overlay);
        Button filterButton = view.findViewById(R.id.dummy_button);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            startCamera();
        }

        filterButton.setOnClickListener(v -> {
            currentFilter = (currentFilter + 1) % 3;
            applyFilter(currentFilter);
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraExecutor = Executors.newSingleThreadExecutor();

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .build();

                FaceDetector detector = FaceDetection.getClient(options);

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, image -> {
                    try {
                        if (image == null) return;

                        Image mediaImage = image.getImage();
                        if (mediaImage == null) {
                            image.close();
                            return;
                        }

                        InputImage inputImage = InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());

                        detector.process(inputImage)
                                .addOnSuccessListener(faces -> {
                                    requireActivity().runOnUiThread(() -> overlayView.setFaces(faces, inputImage.getWidth(), inputImage.getHeight()));
                                })
                                .addOnFailureListener(Throwable::printStackTrace)
                                .addOnCompleteListener(task -> image.close()); // Cerrar en onComplete
                    } catch (Exception e) {
                        e.printStackTrace();
                        image.close();
                    }
                });

                cameraProvider.unbindAll();
                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void applyFilter(int filter) {
        switch (filter) {
            case 0:
                previewView.setAlpha(1.0f); // Sin filtro
                break;
            case 1:
                previewView.setAlpha(0.7f); // Filtro claro
                break;
            case 2:
                previewView.setAlpha(0.4f); // Filtro oscuro
                break;
        }
        Toast.makeText(requireContext(), "Filtro " + (filter + 1), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}
