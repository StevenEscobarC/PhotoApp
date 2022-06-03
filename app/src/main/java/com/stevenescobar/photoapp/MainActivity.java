package com.stevenescobar.photoapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    ActivityResultLauncher<Intent> actResLauncherTakePhoto;
    ActivityResultLauncher<Intent> actResLauncherSelectPhoto;

    private ImageView foto = null;
    private Button tomarFoto = null;
    private Button seleccionarFoto = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initEvents();
    }

    public void initViews(){
        foto = findViewById(R.id.imgPhoto);
        tomarFoto = findViewById(R.id.butTakePhoto);
        seleccionarFoto = findViewById(R.id.butSelectPhoto);
    }

    public void initEvents(){
        String root = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        String imageFolderPath = root;
        File imagesFolder = new File(imageFolderPath);

        if (imagesFolder.mkdirs()){
            Log.d("Take Photo", imagesFolder + "created.");//meter la foto en una carpeta
        } else {
            Log.d("Take Photo", imagesFolder + "NOT created.");//no la crea porque la carpeta ya se creo
        }
        File image = new File(imageFolderPath, "TempPhoto.jpg");
        //image.delete();
        Uri fileUri = FileProvider.getUriForFile(this, "com.stevenescobar.photoapp", image);
        final Bitmap bitmap2;
        try {
            bitmap2 = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);
            foto.setImageBitmap(bitmap2);
        } catch (IOException e) {
            e.printStackTrace();
        }


        actResLauncherTakePhoto = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            switch (result.getResultCode()){
                case RESULT_OK:
                    try{
                        final Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);
                        if (bitmap != null){
                            runOnUiThread(()->{
                                foto.setImageBitmap(null);
                                foto.setImageURI(fileUri);
                            });
                            break;
                        }else{
                            Thread.sleep(500);
                        }
                    }catch (Exception e){

                    }
                    break;

                case RESULT_CANCELED:
                    Log.e("Take Photo", "Result Cancel");
                    break;
                default:
                    Log.e("take Photo", "getResultCode" + result.getResultCode());
                    break;
            }
        });

        actResLauncherSelectPhoto = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            switch (result.getResultCode()){
                case RESULT_OK:
                    Log.d("Take Photo", "Select Photo");
                    Uri selectImageUri = result.getData().getData();
                    if(null != selectImageUri){

                        runOnUiThread(() -> {
                            foto.setImageBitmap(null);
                            foto.setImageURI(selectImageUri);
                        });
                    }
                    break;

            }
        });

        seleccionarFoto.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_PICK);

            if(intent.resolveActivity(getPackageManager()) != null){
                try {
                    actResLauncherSelectPhoto.launch(intent);
                }catch (Exception e){
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this, "There is no app that support this action", Toast.LENGTH_SHORT).show();
            }
        });


        tomarFoto.setOnClickListener(v -> {
            if (hasCameraHardware (this)) {
                if (image.delete()){
                    Log.d("image", "Borrado: ");
                }


                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    try {
                        actResLauncherTakePhoto.launch(intent);
                    } catch (Exception e) {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });
    }

    private boolean hasCameraPermissions(Context context) {
        if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.e("Take Photo", "PERMISSION DENIED");
            return false;
        }
        return true;
    }


    private boolean hasCameraHardware (Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            //this device has a camera
            return true;
        } else {
            //no camera on this device
            return false;
        }
    }



    }



