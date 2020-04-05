package com.mukit.cutchaat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraActivity extends AppCompatActivity {

    ImageView imageView;
    Button btn;
    ConstraintLayout view;

    static {
        System.loadLibrary("native-lib");
    }

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imageView = findViewById(R.id.imageView);
        btn = findViewById(R.id.imPro);
        view = findViewById(R.id.bg);
        final Bitmap inputImgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.a);
        imageView.setImageBitmap(inputImgBitmap);// display the decoded image
    }

    public void imgProcessing(View view) throws IOException {
        final Bitmap inputImgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.a);
        imageView.setImageBitmap(inputImgBitmap);// display the decoded image
//
//        Mat inputImageMat = new Mat(inputImgBitmap.getHeight(), inputImgBitmap.getWidth(), CvType.CV_8UC4);
//        Utils.bitmapToMat(inputImgBitmap, inputImageMat, true); //change the bitmap to mat to pass the image as argument
//        Mat outputImgMat = new Mat();
//        imgFromJNI(inputImageMat.getNativeObjAddr(), outputImgMat.getNativeObjAddr());//call to native method
//
//        Bitmap outputImgBitmap = Bitmap.createBitmap((int) outputImgMat.size().width,
//                (int) outputImgMat.size().height,
//                Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(outputImgMat, outputImgBitmap, true);
//
//        imageView.setImageBitmap(outputImgBitmap);
//        saveImage();
    }

    public void saveImage() throws IOException {

        File sdcard = Environment.getExternalStorageDirectory();
        File f = new File(sdcard, "temp.jpg");
        FileOutputStream out = null;
        out = new FileOutputStream(f);

        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = view.getDrawingCache(true).copy(Bitmap.Config.ARGB_8888,false);
        bitmap.compress(Bitmap.CompressFormat.JPEG,90,out);
        out.close();

        view.setDrawingCacheEnabled(false);
    }

    public Bitmap getBitmap()
    {
//        Bitmap bitmap= Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        Drawable bgDrawable = view.getBackground();
//        if(bgDrawable != null)
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap =view.getDrawingCache();
        view.setDrawingCacheEnabled(false);
        return bitmap;

    }

//    public native void imgFromJNI(long inputImage, long outputImage);
}
