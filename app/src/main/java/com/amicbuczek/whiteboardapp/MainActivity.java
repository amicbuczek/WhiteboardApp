package com.amicbuczek.whiteboardapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private WhiteboardView whiteboardView;
    private ImageButton selectedPaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        whiteboardView = (WhiteboardView)findViewById(R.id.whiteboard);
        selectedPaint = (ImageButton)findViewById(R.id.button_blue_paint);
    }

    /**
     * This method is called whenever the user selects one
     * of the paint colors. The method checks to make sure the user
     * selected a new paint color and updates the selection and the
     * whiteboard view with the new color.
     * @param view the selected paint ImageButton
     */
    public void onPaintClicked(View view) {
        ImageButton imageButton = (ImageButton) view;
        if (imageButton == selectedPaint){
            return;
        }

        imageButton.setBackgroundColor(Color.BLACK);
        selectedPaint.setBackgroundColor(Color.TRANSPARENT);

        switch (imageButton.getId()) {
            case R.id.button_choose_color:
                //TODO Allow the user to choose an ARGB color
                break;
            case R.id.button_erase:
            case R.id.button_white_paint:
                whiteboardView.setPaintColor(Color.WHITE);
                break;
            case R.id.button_blue_paint:
                whiteboardView.setPaintColor(Color.rgb(255, 0, 29));
                break;
            case R.id.button_red_paint:
                whiteboardView.setPaintColor(Color.rgb(255, 0, 29));
                break;
            case R.id.button_green_paint:
                whiteboardView.setPaintColor(Color.rgb(0, 255, 57));
                break;
            case R.id.button_orange_paint:
                whiteboardView.setPaintColor(Color.rgb(255, 152, 44));
                break;
            case R.id.button_yellow_paint:
                whiteboardView.setPaintColor(Color.rgb(255, 255, 65));
                break;
            case R.id.button_light_gray_paint:
                whiteboardView.setPaintColor(Color.rgb(204, 204, 204));
                break;
            case R.id.button_dark_gray_paint:
                whiteboardView.setPaintColor(Color.rgb(68, 68, 68));
                break;
            case R.id.button_black_paint:
                whiteboardView.setPaintColor(Color.BLACK);
                imageButton.setBackgroundColor(Color.WHITE);
                break;
            default:

        }
        selectedPaint = imageButton;
    }

    public void onNewButtonSelected(View view) {
        whiteboardView.clearCanvas();
    }

    /**
     * This method saves a screenshot of the whiteboard view
     * to internal storage and shows the sharing dialog.
     *
     * NOTE: Due to this image not being stored externally,
     * this image will be deleted when the app is uninstalled.
     * This allows for the app to bypass asking for read/write
     * external storage permissions. This image is also not accessible
     * by any other app allowing for additional security.
     *
     * NOTE: This code was found and edited from StackOverflow answer:
     * http://stackoverflow.com/a/30172792/2291915
     *
     */
    public void onSaveButtonClicked(View view) {
        whiteboardView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(whiteboardView.getDrawingCache());
        whiteboardView.setDrawingCacheEnabled(false);

        try {

            File cachePath = new File(this.getCacheDir(), "images");
            if(!cachePath.mkdirs()){
                Log.e("MainActivity", "An error occured in creating the directory for the cache path.");
            }
            FileOutputStream stream = new FileOutputStream(cachePath + "/image.png"); // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            File newFile = new File(cachePath, "image.png");
            Uri contentUri = FileProvider.getUriForFile(this, "com.amicbuczek.whiteboardapp.fileprovider", newFile);

            if (contentUri != null) {

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, "Choose an app"));

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
