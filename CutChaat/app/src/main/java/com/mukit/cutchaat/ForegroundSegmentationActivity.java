package com.mukit.cutchaat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
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
import java.text.SimpleDateFormat;
import java.util.Date;


public class ForegroundSegmentationActivity extends AppCompatActivity {

    static final int REQUEST_TAKE_PHOTO = 1, REQUEST_IMAGE_CAPTURE = 1;
    private ImageView imageView;
    private FrameLayout frameLayoutParent;
    private ZoomableViewGroup zoomableViewGroup;
    String currentPhotoPath;
    Bitmap takenImgBitmap;
    LinearLayoutManager layoutManager;
    String imageFileName;

    private RecyclerView recyclerView;
    private int[] images = {R.drawable.doctor, R.drawable.business_professional, R.drawable.engineer,
            R.drawable.fashion_designer, R.drawable.journalist, R.drawable.lawyer, R.drawable.painter,
            R.drawable.photographer, R.drawable.pilot, R.drawable.singer};
    private String[] image_names = {"Doctor", "Business", "Engineer",
            "Fashion", "Journalist", "Lawyer", "Painter",
            "Photographer", "Pilot", "Singer"};

    ProgressDialog pd;

    private RecyclerViewAdapter adapter;

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
        setContentView(R.layout.activity_foreground_segmentation);
        imageView = findViewById(R.id.fg);
        zoomableViewGroup = findViewById(R.id.zoomControl);
        frameLayoutParent = findViewById(R.id.frameLayoutParent);
        pd = new ProgressDialog(this);
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new RecyclerViewAdapter(this, images, image_names, zoomableViewGroup);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        dispatchTakePictureIntent();


    }

    public Bitmap imgProcessing(Bitmap inputImgBitmap) {

        Mat inputImageMat = new Mat(inputImgBitmap.getHeight(), inputImgBitmap.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(inputImgBitmap, inputImageMat, true); //change the bitmap to mat to pass the image as argument
        Mat outputImgMat = new Mat();
        imageFromJNI(inputImageMat.getNativeObjAddr(), outputImgMat.getNativeObjAddr());//call to native method

        Bitmap outputImgBitmap = Bitmap.createBitmap((int) outputImgMat.size().width,
                (int) outputImgMat.size().height,
                Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(outputImgMat, outputImgBitmap, true);
        return outputImgBitmap;
    }

    public native void imageFromJNI(long inputImage, long outputImage);

    private void setUpActivity(Bitmap takenImgBitmap) {
        Bitmap res = takenImgBitmap;
        imageView.setImageBitmap(res);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }


    public void onClickSaveBtn(View v) throws IOException {
        pd.setMessage("Saving img");
        pd.show();

        File sdcard = Environment.getExternalStorageDirectory();
        File f = new File(sdcard, imageFileName+".jpg");
        FileOutputStream out = null;
        out = new FileOutputStream(f);

        zoomableViewGroup.setDrawingCacheEnabled(true);
        Bitmap bitmap = zoomableViewGroup.getDrawingCache(true).copy(Bitmap.Config.ARGB_8888, false);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        out.close();

        zoomableViewGroup.setDrawingCacheEnabled(false);

        Toast.makeText(this,"Image saved", Toast.LENGTH_SHORT).show();
        pd.cancel();
    }


    public Bitmap getBitmap() {

        zoomableViewGroup.setDrawingCacheEnabled(true);
        zoomableViewGroup.buildDrawingCache();
        Bitmap bitmap = zoomableViewGroup.getDrawingCache();
        zoomableViewGroup.setDrawingCacheEnabled(false);
        return bitmap;

    }

    public void onClickMagicBtn(View view) {
        pd.setMessage("Removing BG");
        pd.show();

        Bitmap res = imgProcessing(takenImgBitmap);
        imageView.setImageBitmap(res);

        pd.cancel();
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.mukit.cutchaat.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "CutChaat_" + timeStamp ;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File file = new File(currentPhotoPath);
            takenImgBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            takenImgBitmap = RotateBitmap(takenImgBitmap, 90);
            setUpActivity(takenImgBitmap);

        }
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

}
