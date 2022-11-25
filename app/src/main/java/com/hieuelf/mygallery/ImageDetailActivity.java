package com.hieuelf.mygallery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import android.os.StrictMode;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.hieuelf.mygallery.adapter.RecyclerViewAdapter;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

public class ImageDetailActivity extends AppCompatActivity {

    // creating a string variable, image view variable
    // and a variable for our scale gesture detector class.
    File imgFile;
    String imgPath,fileName;
    private ImageView imageView;
    private ScaleGestureDetector scaleGestureDetector;

    private RecyclerViewAdapter recyclerViewAdapter;
    // on below line we are defining our scale factor.
    private float mScaleFactor = 1.0f;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.image_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        mapping();

        // on below line getting data which we have passed from our adapter class.
        imgPath = getIntent().getStringExtra("imgPath");
        int imgId = getIntent().getIntExtra("imgID",0);

        fileName = imgPath.substring(imgPath.lastIndexOf("/")+1);

        // on below line we are initializing our scale gesture detector for zoom in and out for our image.
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        // on below line we are getting our image file from its path.
        imgFile = new File(imgPath);
        // if the file exists then we are loading that image in our image view.
        if (imgFile.exists()) {
            Picasso.get().load(imgFile).placeholder(R.drawable.loading).into(imageView);
        }
    }

    private void mapping() {
        imageView = findViewById(R.id.idIVImage);
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.mnuReName:
                final AlertDialog.Builder renameDialog = new AlertDialog.Builder(ImageDetailActivity.this);
                renameDialog.setTitle("Rename To:");
                final EditText input = new EditText(ImageDetailActivity.this);
                //final String renamePath = imagePaths.get(imgPath);
                input.setText(imgPath.substring(imgPath.lastIndexOf("/")+1));
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                renameDialog.setView(input);
                renameDialog.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String s = imgFile.getParent()+"/" +input.getText();
                        File newFile = new File(s);
                        try {
                            imgFile.renameTo(newFile);
                            Toast.makeText(ImageDetailActivity.this,"Rename to: "+s,Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            Log.e("Fail", "Fail: "+e.toString());
                        }
                    }
                });
                renameDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                renameDialog.show();

                return true;
            case R.id.mnuInfo:
                final AlertDialog.Builder infoDialog = new AlertDialog.Builder(ImageDetailActivity.this);
                infoDialog.setTitle("Image Info");
                final File imageFile = new File(imgPath);
                final Date lastModified = new Date(imageFile.lastModified());
                String imgDate = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.MEDIUM).format(lastModified).toString();

                final long fileLenght = imageFile.length()/1024;

                BitmapFactory.Options bitMapOption=new BitmapFactory.Options();
                bitMapOption.inJustDecodeBounds=true;
                BitmapFactory.decodeFile(imgPath, bitMapOption);
                int imageWidth=bitMapOption.outWidth;
                int imageHeight=bitMapOption.outHeight;
                String imgResolusion = Integer.toString(imageWidth)+"*"+Integer.toString(imageHeight)+" pixels";

                String fileLenghtString;
                if (fileLenght>1024){
                    fileLenghtString = String.valueOf(fileLenght/1024)+" MB";
                }else {
                    fileLenghtString = String.valueOf(fileLenght)+" KB";
                }

                String info = "Location: "+imgPath + "\n" +
                        "Date: "+imgDate + "\n" +
                        "Size: "+fileLenghtString + "\n" +
                        "Resolusion: "+imgResolusion;


                infoDialog.setMessage(info);
                infoDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                infoDialog.show();

                return true;
            case R.id.mnuDelete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("bạn có chắc chắn muốn xoá ảnh này")
                        .setTitle("Xoá ảnh")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    imgFile.delete();
                                    if (imgFile.exists()){
                                        imgFile.getCanonicalFile().delete();
                                        if (imgFile.exists()){
                                            getApplicationContext().deleteFile(imgFile.getName());
                                        }
                                    }
                                }catch (Exception e){
                                    Log.e("DeleteError","Lỗi: "+e.toString());
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            case R.id.mnuShare:
                final Uri imageUri = Uri.parse("file://"+imgPath);
                final Intent intent = new Intent(Intent.ACTION_SEND);
                if (imgPath.endsWith(".png")){
                    intent.setType("image/png");
                }else {
                    intent.setType("image/jpeg");
                }
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_STREAM,imageUri);
                startActivity(Intent.createChooser(intent,"Share to:"));
                return true;
            case R.id.mnuXoayPhai:
                imageView.setRotation(imageView.getRotation() + 90);
                return true;
            case R.id.mnuXoayTrai:
                imageView.setRotation(imageView.getRotation() - 90);
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // inside on touch event method we are calling on
        // touch event method and passing our motion event to it.
        scaleGestureDetector.onTouchEvent(motionEvent);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        // on below line we are creating a class for our scale
        // listener and extending it with gesture listener.
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {

            // inside on scale method we are setting scale
            // for our image in our image view.
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

            // on below line we are setting
            // scale x and scale y to our image view.
            imageView.setScaleX(mScaleFactor);
            imageView.setScaleY(mScaleFactor);
            return true;
        }
    }
}
