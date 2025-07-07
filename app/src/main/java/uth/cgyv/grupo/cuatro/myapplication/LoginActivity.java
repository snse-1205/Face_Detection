package uth.cgyv.grupo.cuatro.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

public class LoginActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (tienePermisosRequeridos()) {
            continuarLogin();
        } else {
            solicitarPermisos();
        }
    }
    private boolean tienePermisosRequeridos() {
        boolean cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        boolean storagePermission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Para Android 13 (Tiramisu) y superior, se usa READ_MEDIA_IMAGES
            storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            // Para versiones anteriores, se usa READ_EXTERNAL_STORAGE
            storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }

        return cameraPermission && storagePermission;
    }

    private void solicitarPermisos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_MEDIA_IMAGES
                    },
                    REQUEST_PERMISSIONS);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            boolean cameraPermissionGranted = false;
            boolean storagePermissionGranted = false;

            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.CAMERA)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        cameraPermissionGranted = true;
                    }
                }
                else if (permissions[i].equals(Manifest.permission.READ_MEDIA_IMAGES) ||
                        permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        storagePermissionGranted = true;
                    }
                }
            }

            if (cameraPermissionGranted && storagePermissionGranted) {
                continuarLogin();
            } else if (cameraPermissionGranted && !storagePermissionGranted) {
                boolean shouldShowRationaleForStorage = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    shouldShowRationaleForStorage = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES);
                } else {
                    shouldShowRationaleForStorage = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);
                }

                if (!shouldShowRationaleForStorage) {
                    Toast.makeText(this, "El permiso de almacenamiento fue denegado permanentemente. Por favor, habilítelo manualmente en la configuración de la aplicación para usar todas las funcionalidades.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "El permiso de almacenamiento fue denegado. Algunas funcionalidades podrían no estar disponibles.", Toast.LENGTH_LONG).show();
                }
                continuarLogin();
            } else {
                Toast.makeText(this, "Se requiere el permiso de la cámara para usar la aplicación. La aplicación se cerrará.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void continuarLogin() {
        setContentView(R.layout.activity_login);

        SharedPreferences prefs = getSharedPreferences("FacePrefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragmentContainer, new LoginFragment());
            ft.commit();
        }
    }

}