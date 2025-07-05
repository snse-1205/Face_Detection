package uth.cgyv.grupo.cuatro.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import uth.cgyv.grupo.cuatro.myapplication.databinding.ActivityMainBinding;
import uth.cgyv.grupo.cuatro.myapplication.ui.emotions.FaceEmotions;
import uth.cgyv.grupo.cuatro.myapplication.ui.eyesclosed.EyeState;
import uth.cgyv.grupo.cuatro.myapplication.ui.filter.FiltrosCamaraXFragment;
import uth.cgyv.grupo.cuatro.myapplication.ui.headangle.HeadAngle;
import uth.cgyv.grupo.cuatro.myapplication.ui.landmarks.FaceLandmarks;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(R.id.fab).show();
            }
        });


        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        binding.appBarMain.fab.setBackgroundColor(
                ContextCompat.getColor(this, R.color.verde_salvia_oscuro)
        );

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_gallery,
                R.id.nav_slideshow,
                R.id.nav_filtros,
                R.id.nav_face_emotions,
                R.id.nav_face_landmarks,
                R.id.nav_face_closed_eyes,
                R.id.nav_face_angle
        ).setOpenableLayout(drawer).build();


        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();

            if (id == R.id.nav_face_landmarks) {
                binding.appBarMain.fab.setImageResource(R.drawable.ic_camera_flip);
                binding.appBarMain.fab.show();
                binding.appBarMain.fab.setOnClickListener(v -> {
                    Fragment currentFragment = getSupportFragmentManager()
                            .findFragmentById(R.id.nav_host_fragment_content_main)
                            .getChildFragmentManager()
                            .getPrimaryNavigationFragment();

                    if (currentFragment instanceof FaceLandmarks) {
                        ((FaceLandmarks) currentFragment).toggleCamera();
                    }
                });
            } else if (id == R.id.nav_face_angle) {
                binding.appBarMain.fab.setImageResource(R.drawable.ic_camera_flip);
                binding.appBarMain.fab.show();
                binding.appBarMain.fab.setOnClickListener(v -> {
                    Fragment currentFragment = getSupportFragmentManager()
                            .findFragmentById(R.id.nav_host_fragment_content_main)
                            .getChildFragmentManager()
                            .getPrimaryNavigationFragment();

                    if (currentFragment instanceof HeadAngle) {
                        ((HeadAngle) currentFragment).toggleCamera();
                    }});
            } else if (id == R.id.nav_filtros) {
                binding.appBarMain.fab.setImageResource(R.drawable.ic_camera_flip);
                binding.appBarMain.fab.show();
                binding.appBarMain.fab.setOnClickListener(v -> {
                    FiltrosCamaraXFragment fragment = (FiltrosCamaraXFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.nav_host_fragment_content_main)
                            .getChildFragmentManager()
                            .getFragments()
                            .get(0); // obtener el fragmento hijo real

                    if (fragment != null) {
                        fragment.toggleCamera();
                    }
                });
            } else if (id == R.id.nav_face_closed_eyes) {
                binding.appBarMain.fab.setImageResource(R.drawable.ic_camera_flip); // cambia el ícono
                binding.appBarMain.fab.show();

                binding.appBarMain.fab.setOnClickListener(v -> {
                    Fragment currentFragment = getSupportFragmentManager()
                            .findFragmentById(R.id.nav_host_fragment_content_main)
                            .getChildFragmentManager()
                            .getPrimaryNavigationFragment();

                    if (currentFragment instanceof EyeState) {
                        ((EyeState) currentFragment).toggleCamera();
                    }
                });
            }
            else if(id == R.id.nav_face_emotions){
                binding.appBarMain.fab.setImageResource(R.drawable.ic_camera_flip);
                binding.appBarMain.fab.show();
                binding.appBarMain.fab.setOnClickListener(v -> {
                    Fragment currentFragment = getSupportFragmentManager()
                            .findFragmentById(R.id.nav_host_fragment_content_main)
                            .getChildFragmentManager()
                            .getPrimaryNavigationFragment();

                    if (currentFragment instanceof FaceEmotions) {
                        ((FaceEmotions) currentFragment).toggleCamera();
                    }
                });
            } else{
                    // Por defecto, puedes ocultarlo si no se necesita
                    binding.appBarMain.fab.hide();
            }

        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Se necesita permiso de cámara para usar esta función", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}