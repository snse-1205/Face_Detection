package uth.cgyv.grupo.cuatro.myapplication.ui.gallery;

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

public class GalleryFragment extends Fragment {

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
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        imageView = root.findViewById(R.id.imageView);
        resultTextView = root.findViewById(R.id.text_gallery);
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

        resultTextView.setText("Seleccione una imagen para detectar rostros");

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
                    detectFaces(bitmap);
                }
            } catch (FileNotFoundException e) {
                Log.e("GalleryFragment", "Error al cargar imagen", e);
                Toast.makeText(getContext(), "Error al cargar imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void detectFaces(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        detector.process(image)
                .addOnSuccessListener(faces -> {
                    int faceCount = faces.size();
                    String result;

                    if (faceCount == 0) {
                        result = "No se detectaron rostros en la imagen";
                    } else if (faceCount == 1) {
                        result = "Se detectó 1 rostro en la imagen";
                    } else {
                        result = "Se detectaron " + faceCount + " rostros en la imagen";
                    }

                    resultTextView.setText(result);
                })
                .addOnFailureListener(e -> {
                    Log.e("GalleryFragment", "Error en detección de rostros", e);
                    resultTextView.setText("Error al procesar la imagen");
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (detector != null) {
            detector.close();
        }
    }
}