package uth.cgyv.grupo.cuatro.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.HashSet;
import java.util.Set;

public class LoginFragment extends Fragment {

    private EditText editUsername, editPassword;
    private Button btnLogin, btnGoToRegister;
    private TextView textResult;
    private PreviewView previewView;
    private FaceDetector faceDetector;
    private ProcessCameraProvider cameraProvider;
    private boolean isFaceDetected = false;
    private SharedPreferences sharedPreferences;
    private static final int REQUEST_CAMERA_PERMISSION = 1001;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        editUsername = view.findViewById(R.id.editUsername);
        editPassword = view.findViewById(R.id.editPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnGoToRegister = view.findViewById(R.id.btnGoToRegister);
        textResult = view.findViewById(R.id.textResult);
        previewView = view.findViewById(R.id.previewView);

        btnLogin.setEnabled(false);
        sharedPreferences = requireActivity().getSharedPreferences("FacePrefs", Context.MODE_PRIVATE);

        // Cámara
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            startCamera();
        }

        btnLogin.setOnClickListener(v -> {
            String inputUser = editUsername.getText().toString().trim();
            String inputPass = editPassword.getText().toString().trim();

            Set<String> users = new HashSet<>(sharedPreferences.getStringSet("users", new HashSet<>()));
            if (!users.contains(inputUser)) {
                textResult.setText("Usuario no registrado.");
                return;
            }

            String storedPass = sharedPreferences.getString("pass_" + inputUser, "");
            if (!inputPass.equals(storedPass)) {
                textResult.setText("Contraseña incorrecta.");
                return;
            }

            if (!isFaceDetected) {
                textResult.setText("Asegúrate de que tu rostro esté visible.");
                return;
            }

            sharedPreferences.edit()
                    .putBoolean("isLoggedIn", true)
                    .putString("currentUser", inputUser)
                    .apply();

            textResult.setText("Bienvenido, " + inputUser);
            startActivity(new Intent(requireActivity(), MainActivity.class));
            requireActivity().finish();
        });

        btnGoToRegister.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new RegisterFragment())
                    .addToBackStack(null)
                    .commit();
        });


        return view;
    }

    @SuppressLint("MissingPermission")
    private void startCamera() {
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
                    InputImage image = InputImage.fromMediaImage(
                            imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees()
                    );

                    faceDetector.process(image)
                            .addOnSuccessListener(faces -> {
                                if (!faces.isEmpty() && !isFaceDetected) {
                                    isFaceDetected = true;
                                    btnLogin.setEnabled(true);
                                    textResult.setText("Rostro detectado.");
                                } else if (faces.isEmpty() && isFaceDetected) {
                                    isFaceDetected = false;
                                    btnLogin.setEnabled(false);
                                    textResult.setText("Esperando rostro...");
                                }
                                imageProxy.close();
                            })
                            .addOnFailureListener(e -> {
                                imageProxy.close();
                                textResult.setText("Error al procesar imagen.");
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
