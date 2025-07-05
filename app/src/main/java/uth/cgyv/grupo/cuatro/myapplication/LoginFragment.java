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
import androidx.fragment.app.FragmentTransaction;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

public class LoginFragment extends Fragment {

    private EditText editUsername, editPassword;
    private Button btnLogin, btnGoToRegister;
    private TextView textResult;
    private PreviewView previewView;
    private ProcessCameraProvider cameraProvider;
    private boolean isFaceDetected = false;
    private FaceDetector faceDetector;
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

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            startCamera();
        }

        btnLogin.setOnClickListener(v -> {
            String inputUser = editUsername.getText().toString();
            String inputPass = editPassword.getText().toString();
            String storedUser = sharedPreferences.getString("username", "");
            String storedPass = sharedPreferences.getString("password", "");

            if (inputUser.equals(storedUser) && inputPass.equals(storedPass) && isFaceDetected) {
                textResult.setText("Bienvenido, " + inputUser);
                Intent intent = new Intent(requireActivity(), MainActivity.class);
                startActivity(intent);
                requireActivity().finish(); // Cierra la actividad actual con el LoginFragment

            } else {
                textResult.setText("Credenciales incorrectas o sin rostro.");
            }

        });

        btnGoToRegister.setOnClickListener(v -> {
            FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragmentContainer, new RegisterFragment());
            ft.addToBackStack(null);
            ft.commit();
        });

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                textResult.setText("Permiso de camara denegado. No se puede continuar.");
                btnLogin.setEnabled(false);
            }
        }
    }


    private void startCamera() {
        faceDetector = FaceDetection.getClient(
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .build());

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();  // Guarda la referencia aquí

                Preview preview = new Preview.Builder().build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), imageProxy -> {
                    @SuppressLint("UnsafeOptInUsageError")
                    InputImage image = InputImage.fromMediaImage(
                            imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

                    faceDetector.process(image)
                            .addOnSuccessListener(faces -> {
                                if (!faces.isEmpty()) {
                                    if (!isFaceDetected) {
                                        isFaceDetected = true;
                                        btnLogin.setEnabled(true);
                                        textResult.setText("Rostro detectado.");
                                    }
                                } else {
                                    if (isFaceDetected) {
                                        isFaceDetected = false;
                                        btnLogin.setEnabled(false);
                                        textResult.setText("Esperando rostro...");
                                    }
                                }
                                imageProxy.close();
                            })
                            .addOnFailureListener(e -> {
                                imageProxy.close();
                                textResult.setText("Error al procesar imagen.");
                            });
                });

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageAnalysis);
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
        if (cameraProvider != null) {
            cameraProvider.unbindAll();  // Libera la cámara para evitar errores de buffer abandonado
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();  // También libera aquí para seguridad
        }
        if (faceDetector != null) {
            faceDetector.close();  // Cierra el detector ML Kit para liberar recursos
        }
    }
}
