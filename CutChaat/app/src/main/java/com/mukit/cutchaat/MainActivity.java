package com.mukit.cutchaat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private int STORAGE_PERMISSION_CODE = 1;
    private int REQUEST_GROUP_PERMISSION = 111;

    private boolean haveCameraPermission = false;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestAllPermission();

    }

    private void requestAllPermission() {
        ArrayList<String> permissionNeeded = new ArrayList<>();
        ArrayList<String> permissionAvailable = new ArrayList<>();
        permissionAvailable.add(Manifest.permission.CAMERA);
        permissionAvailable.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissionAvailable.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        for (String permission : permissionAvailable) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                permissionNeeded.add(permission);
        }

        requestGroupPermission(permissionNeeded);
    }

    private void requestGroupPermission(ArrayList<String> permissions) {
        if (permissions.size() > 0) {
            String[] permissionList = new String[permissions.size()];
            permissions.toArray(permissionList);
            ActivityCompat.requestPermissions(MainActivity.this, permissionList, REQUEST_GROUP_PERMISSION);
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
//    public native String stringFromJNI();
    public void OpenCameraActivity(View view) {
        Intent intent = new Intent(this, ForegroundSegmentationActivity.class);
        startActivity(intent);

//        Intent intent = new Intent(this, CameraActivity.class);
//        startActivity(intent);
    }


}

