package uth.cgyv.grupo.cuatro.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.HashSet;
import java.util.Set;

public class RegisterFragment extends Fragment {

    private EditText editUsername, editPassword;
    private Button btnRegister;
    private TextView textResult;
    private PreviewView previewView;
    private FaceDetector faceDetector;
    private boolean isFaceDetected = false;
    private SharedPreferences sharedPreferences;
    private ProcessCameraProvider cameraProvider;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        editUsername = view.findViewById(R.id.editUsername);
        editPassword = view.findViewById(R.id.editPassword);
        btnRegister = view.findViewById(R.id.btnRegister);
        textResult = view.findViewById(R.id.textResult);
        previewView = view.findViewById(R.id.previewView);

        btnRegister.setEnabled(false);
        sharedPreferences = requireActivity().getSharedPreferences("FacePrefs", Context.MODE_PRIVATE);

        btnRegister.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                textResult.setText("Completa todos los campos.");
                return;
            }
            if (!isFaceDetected) {
                textResult.setText("Asegúrate de que tu rostro esté visible.");
                return;
            }

            // 1) Recupera o crea el set de usuarios
            Set<String> users = new HashSet<>(sharedPreferences.getStringSet("users", new HashSet<>()));

            if (users.contains(username)) {
                textResult.setText("El usuario ya existe.");
                return;
            }

            // 2) Añade el usuario y su contraseña
            users.add(username);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet("users", users);
            editor.putString("pass_" + username, password);
            editor.apply();

            textResult.setText("Registro exitoso: " + username);

            // 3) Vuelve al login
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new LoginFragment())
                    .addToBackStack(null)
                    .commit();
        });

        initFaceDetection();
        return view;
    }

    private void initFaceDetection() {
        faceDetector = FaceDetection.getClient(
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .build()
        );

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                CameraSelector selector = CameraSelector.DEFAULT_FRONT_CAMERA;
                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                analysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), imageProxy -> {
                    @SuppressLint("UnsafeOptInUsageError")
                    InputImage image = InputImage.fromMediaImage(
                            imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees()
                    );

                    faceDetector.process(image)
                            .addOnSuccessListener(faces -> {
                                if (!faces.isEmpty() && !isFaceDetected) {
                                    isFaceDetected = true;
                                    btnRegister.setEnabled(true);
                                    textResult.setText("Rostro detectado. Ya puedes registrarte.");
                                } else if (faces.isEmpty() && isFaceDetected) {
                                    isFaceDetected = false;
                                    btnRegister.setEnabled(false);
                                    textResult.setText("Sin rostro. Esperando...");
                                }
                                imageProxy.close();
                            })
                            .addOnFailureListener(e -> {
                                imageProxy.close();
                                textResult.setText("Error al procesar rostro.");
                            });
                });

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), selector, preview, analysis);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
            } catch (Exception e) {
                textResult.setText("Error al iniciar cámara.");
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (cameraProvider != null) cameraProvider.unbindAll();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraProvider != null) cameraProvider.unbindAll();
        if (faceDetector != null) faceDetector.close();
    }
}
