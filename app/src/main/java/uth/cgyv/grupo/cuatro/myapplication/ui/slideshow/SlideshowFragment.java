package uth.cgyv.grupo.cuatro.myapplication.ui.slideshow;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import uth.cgyv.grupo.cuatro.myapplication.R;

public class SlideshowFragment extends Fragment {

    private static final int REQUEST_IMAGE_GALLERY = 1;
    private static final int REQUEST_IMAGE_CAMERA = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    private ImageView imageView;
    private TextView resultTextView;
    private Button btnGallery;
    private Button btnCamera;

    private FaceDetector detector;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_slideshow, container, false);

        imageView = root.findViewById(R.id.imageView);
        resultTextView = root.findViewById(R.id.text_slideshow);
        btnGallery = root.findViewById(R.id.btn_gallery);
        btnCamera = root.findViewById(R.id.btn_camera);

        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build();

        detector = FaceDetection.getClient(options);

        btnGallery.setOnClickListener(v -> openGallery());
        btnCamera.setOnClickListener(v -> checkCameraPermissionAndTakePhoto());

        resultTextView.setText("Seleccione una imagen para detectar sonrisas");

        return root;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    private void checkCameraPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            takePhoto();
        }
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            } else {
                Toast.makeText(getContext(), "Permiso de cámara requerido", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            Bitmap bitmap = null;

            try {
                if (requestCode == REQUEST_IMAGE_GALLERY) {
                    Uri imageUri = data.getData();
                    InputStream inputStream = requireActivity().getContentResolver().openInputStream(imageUri);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                } else if (requestCode == REQUEST_IMAGE_CAMERA) {
                    Bundle extras = data.getExtras();
                    bitmap = (Bitmap) extras.get("data");
                }

                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    detectSmiles(bitmap);
                }
            } catch (FileNotFoundException e) {
                Log.e("SlideshowFragment", "Error al cargar imagen", e);
                Toast.makeText(getContext(), "Error al cargar imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void detectSmiles(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        detector.process(image)
                .addOnSuccessListener(faces -> {
                    if (faces.isEmpty()) {
                        resultTextView.setText("No se detectaron rostros en la imagen");
                        return;
                    }

                    StringBuilder result = new StringBuilder();
                    result.append("Análisis de sonrisas:\n\n");

                    for (int i = 0; i < faces.size(); i++) {
                        Face face = faces.get(i);
                        float smilingProbability = face.getSmilingProbability() != null ?
                                face.getSmilingProbability() : 0.0f;

                        String smileState = getSmileState(smilingProbability);

                        if (faces.size() > 1) {
                            result.append("Rostro ").append(i + 1).append(": ");
                        }

                        result.append(smileState)
                                .append(" (").append(String.format("%.1f", smilingProbability * 100))
                                .append("% probabilidad de sonrisa)");

                        if (i < faces.size() - 1) {
                            result.append("\n");
                        }
                    }

                    resultTextView.setText(result.toString());
                })
                .addOnFailureListener(e -> {
                    Log.e("SlideshowFragment", "Error en detección de sonrisas", e);
                    resultTextView.setText("Error al procesar la imagen");
                });
    }

    private String getSmileState(float smilingProbability) {
        if (smilingProbability < 0.3f) {
            return "Serio 😐";
        } else if (smilingProbability < 0.7f) {
            return "Neutro 😊";
        } else {
            return "Sonriente 😄";
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (detector != null) {
            detector.close();
        }
    }
}