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

    /**
     * Callback para el resultado de la solicitud de permisos.
     * Se invoca después de que el usuario interactúa con el diálogo de permisos.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            boolean cameraPermissionGranted = false;
            boolean storagePermissionGranted = false;

            // Itera sobre los resultados de los permisos que fueron solicitados
            for (int i = 0; i < permissions.length; i++) {
                // Verifica el permiso de la cámara
                if (permissions[i].equals(Manifest.permission.CAMERA)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        cameraPermissionGranted = true;
                    }
                }
                // Verifica el permiso de almacenamiento (READ_MEDIA_IMAGES o READ_EXTERNAL_STORAGE)
                else if (permissions[i].equals(Manifest.permission.READ_MEDIA_IMAGES) ||
                        permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        storagePermissionGranted = true;
                    }
                }
            }

            // Evalúa el estado combinado de los permisos
            if (cameraPermissionGranted && storagePermissionGranted) {
                // Ambos permisos (cámara y almacenamiento) fueron concedidos
                continuarLogin();
            } else if (cameraPermissionGranted && !storagePermissionGranted) {
                // El permiso de la cámara fue concedido, pero el de almacenamiento fue denegado.
                // Aquí es donde el usuario no vio el diálogo de almacenamiento.
                // Verificamos si fue una denegación "permanente" (con "No volver a preguntar").

                boolean shouldShowRationaleForStorage = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    shouldShowRationaleForStorage = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES);
                } else {
                    shouldShowRationaleForStorage = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);
                }

                if (!shouldShowRationaleForStorage) {
                    // Si shouldShowRequestPermissionRationale es false y el permiso está denegado,
                    // significa que el usuario marcó "No volver a preguntar" o es una política del dispositivo.
                    Toast.makeText(this, "El permiso de almacenamiento fue denegado permanentemente. Por favor, habilítelo manualmente en la configuración de la aplicación para usar todas las funcionalidades.", Toast.LENGTH_LONG).show();
                } else {
                    // Si shouldShowRequestPermissionRationale es true, significa que el usuario simplemente lo denegó.
                    Toast.makeText(this, "El permiso de almacenamiento fue denegado. Algunas funcionalidades podrían no estar disponibles.", Toast.LENGTH_LONG).show();
                }
                // Continuamos con la aplicación, pero con funcionalidades que dependan del almacenamiento limitadas
                continuarLogin();
            } else {
                // El permiso de la cámara fue denegado (independientemente del estado del almacenamiento).
                // En este caso, la cámara es crítica, por lo que cerramos la aplicación.
                Toast.makeText(this, "Se requiere el permiso de la cámara para usar la aplicación. La aplicación se cerrará.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    /**
     * Continúa con el flujo de inicio de sesión de la aplicación.
     * Carga el layout, verifica el estado de login y navega a la actividad principal o al fragmento de login.
     */
    private void continuarLogin() {
        setContentView(R.layout.activity_login);

        SharedPreferences prefs = getSharedPreferences("FacePrefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            // Este fragmentContainer DEBE existir en activity_login.xml
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragmentContainer, new LoginFragment());
            ft.commit();
        }
    }

}