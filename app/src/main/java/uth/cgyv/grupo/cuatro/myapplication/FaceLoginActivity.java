package uth.cgyv.grupo.cuatro.myapplication;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

public class FaceLoginActivity extends AppCompatActivity {

    private EditText editUsername;
    private Button btnRegister, btnLogin;
    private TextView textResult;
    private PreviewView previewView;

    private FaceDetector faceDetector;
    private SharedPreferences sharedPreferences;
    private boolean isRegistering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_login);

        editUsername = findViewById(R.id.editUsername);
        //btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);
        textResult = findViewById(R.id.textResult);
        previewView = findViewById(R.id.previewView);

        sharedPreferences = getSharedPreferences("FacePrefs", MODE_PRIVATE);

        faceDetector = FaceDetection.getClient(new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build());

        btnRegister.setOnClickListener(v -> {
            isRegistering = true;
            startCamera();
        });

        btnLogin.setOnClickListener(v -> {
            isRegistering = false;
            startCamera();
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this),
                        imageProxy -> {
                            @SuppressLint("UnsafeOptInUsageError")
                            InputImage image = InputImage.fromMediaImage(
                                    imageProxy.getImage(),
                                    imageProxy.getImageInfo().getRotationDegrees());

                            faceDetector.process(image)
                                    .addOnSuccessListener(faces -> {
                                        if (!faces.isEmpty()) {
                                            if (isRegistering) {
                                                saveUserFace(editUsername.getText().toString());
                                                textResult.setText("Rostro registrado.");
                                            } else {
                                                String storedUser = sharedPreferences.getString("username", "");
                                                if (!storedUser.isEmpty()) {
                                                    textResult.setText("Acceso concedido a " + storedUser);
                                                } else {
                                                    textResult.setText("Rostro detectado (modo recaptcha).");
                                                }
                                            }
                                        } else {
                                            textResult.setText("No se detectó rostro.");
                                        }
                                        imageProxy.close();
                                    })
                                    .addOnFailureListener(e -> {
                                        imageProxy.close();
                                        textResult.setText("Error al procesar imagen.");
                                    });
                        });

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageAnalysis);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

            } catch (Exception e) {
                textResult.setText("Error al iniciar cámara.");
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void saveUserFace(String username) {
        sharedPreferences.edit().putString("username", username).apply();
    }

}