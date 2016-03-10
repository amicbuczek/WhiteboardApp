package com.amicbuczek.whiteboardapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WhiteboardView whiteboardView;
    private ImageButton selectedPaint;
    private boolean firstTimeCreatingShape;

    private static final int PHOTO_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firstTimeCreatingShape = true;

        whiteboardView = (WhiteboardView) findViewById(R.id.whiteboard);
        selectedPaint = (ImageButton) findViewById(R.id.button_blue_paint);
    }

    /**
     * This method is called whenever the user selects one
     * of the paint colors. The method checks to make sure the user
     * selected a new paint color and updates the selection and the
     * whiteboard view with the new color.
     *
     * @param view the selected paint ImageButton
     */
    public void onPaintClicked(View view) {
        final ImageButton imageButton = (ImageButton) view;
        if (imageButton == selectedPaint && imageButton.getId() != R.id.button_choose_color) {
            return;
        }

        switch (imageButton.getId()) {
            case R.id.button_choose_color:
                final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                LayoutInflater layoutInflater = this.getLayoutInflater();
                final View layout = layoutInflater.inflate(R.layout.dialog_custom_color, (ViewGroup) findViewById(R.id.root_linear_layout));


                final SeekBar redBar = (SeekBar) layout.findViewById(R.id.seek_bar_red_color);
                final SeekBar blueBar = (SeekBar) layout.findViewById(R.id.seek_bar_blue_color);
                final SeekBar greenBar = (SeekBar) layout.findViewById(R.id.seek_bar_green_color);
                final SeekBar alphaBar = (SeekBar) layout.findViewById(R.id.seek_bar_alpha_color);

                dialog.setView(layout)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                whiteboardView.setPaintColor(Color.argb(alphaBar.getProgress(), redBar.getProgress(), greenBar.getProgress(), blueBar.getProgress()));

                                selectedPaint.setBackgroundColor(Color.TRANSPARENT);
                                imageButton.setBackgroundColor(Color.BLACK);
                                selectedPaint = imageButton;
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });

                setUpColorChangerLayout(layout, alphaBar, redBar, blueBar, greenBar);

                dialog.create();
                dialog.show();

                return;
            case R.id.button_erase:
                stopDrawingShape();
            case R.id.button_white_paint:
                whiteboardView.setPaintColor(Color.WHITE);
                break;
            case R.id.button_blue_paint:
                whiteboardView.setPaintColor(Color.argb(255, 0, 0, 255));
                break;
            case R.id.button_red_paint:
                whiteboardView.setPaintColor(Color.argb(255, 255, 0, 29));
                break;
            case R.id.button_green_paint:
                whiteboardView.setPaintColor(Color.argb(255, 0, 255, 57));
                break;
            case R.id.button_orange_paint:
                whiteboardView.setPaintColor(Color.argb(255, 255, 152, 44));
                break;
            case R.id.button_yellow_paint:
                whiteboardView.setPaintColor(Color.argb(255, 255, 255, 65));
                break;
            case R.id.button_light_gray_paint:
                whiteboardView.setPaintColor(Color.argb(255, 204, 204, 204));
                break;
            case R.id.button_dark_gray_paint:
                whiteboardView.setPaintColor(Color.argb(255, 68, 68, 68));
                break;
            case R.id.button_black_paint:
                whiteboardView.setPaintColor(Color.BLACK);
                imageButton.setBackgroundColor(Color.WHITE);
                break;
            default:

        }

        selectedPaint.setBackgroundColor(Color.TRANSPARENT);
        imageButton.setBackgroundColor(Color.BLACK);
        selectedPaint = imageButton;
    }

    /**
     * This is a helper method for the dialog.
     * This method sets up and handles the SeekBar
     * OnChangeListeners and updates the ImageView's
     * final chosen color
     *
     * @param layout - the dialog's layout
     */
    private void setUpColorChangerLayout(View layout, final SeekBar alphaBar, final SeekBar redBar, final SeekBar blueBar, final SeekBar greenBar) {
        final ImageView chosenColor = (ImageView) layout.findViewById(R.id.final_chosen_color);

        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                chosenColor.setBackgroundColor(Color.argb(alphaBar.getProgress(), redBar.getProgress(), greenBar.getProgress(), blueBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };

        redBar.setOnSeekBarChangeListener(seekBarChangeListener);
        blueBar.setOnSeekBarChangeListener(seekBarChangeListener);
        greenBar.setOnSeekBarChangeListener(seekBarChangeListener);
        alphaBar.setOnSeekBarChangeListener(seekBarChangeListener);

        //Update the choices with the current color
        int initialColor = whiteboardView.getPaintColor();

        chosenColor.setBackgroundColor(initialColor);
        alphaBar.setProgress(Color.alpha(initialColor));
        redBar.setProgress(Color.red(initialColor));
        greenBar.setProgress(Color.green(initialColor));
        blueBar.setProgress(Color.blue(initialColor));

    }

    /**
     * This method is triggered when the plus button (new)
     * is selected. This will clear the whiteboard view.
     */
    public void onNewButtonSelected(View view) {
        stopDrawingShape();

        ImageButton eraseButton = (ImageButton)findViewById(R.id.button_erase);
        if(eraseButton == selectedPaint){
            onPaintClicked(findViewById(R.id.button_blue_paint));
        }

        whiteboardView.newPage();
    }

    /**
     * This method is triggered when the brush button
     * is selected. This will produce a dialog with a SeekBar
     * allowing the user to create a custom brush size.
     * An image will increase/decrease with the SeekBar to
     * accurately show the current brush size.
     */
    public void onBrushButtonSelected(View view) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = this.getLayoutInflater();
        final View layout = layoutInflater.inflate(R.layout.dialog_custom_brush_size, (ViewGroup) findViewById(R.id.root_linear_layout));

        final SeekBar brushBar = (SeekBar) layout.findViewById(R.id.seek_bar_brush_size);
        brushBar.setProgress((int) whiteboardView.getBrushSize());


        final ImageView brushSizeImage = (ImageView) layout.findViewById(R.id.image_brush_size);
        brushSizeImage.getLayoutParams().height = (brushBar.getProgress() + 1) * 2;
        brushSizeImage.getLayoutParams().width = (brushBar.getProgress() + 1) * 2;

        brushSizeImage.setBackgroundColor(whiteboardView.getPaintColor());
        if(whiteboardView.getPaintColor() == Color.WHITE){
            brushSizeImage.setBackgroundResource(R.drawable.white_brush_image);
        }

        brushBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                brushSizeImage.getLayoutParams().height = (brushBar.getProgress() + 1) * 2;
                brushSizeImage.getLayoutParams().width = (brushBar.getProgress() + 1) * 2;
                brushSizeImage.requestLayout();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        dialog.setView(layout)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        whiteboardView.setBrushSize(brushBar.getProgress());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        dialog.create();
        dialog.show();
    }

    /**
     * This method saves a screenshot of the whiteboard view
     * to internal storage and shows the sharing dialog.
     * <p/>
     * NOTE: Due to this image not being stored externally,
     * this image will be deleted when the app is uninstalled.
     * This allows for the app to bypass asking for read/write
     * external storage permissions. This image is also not accessible
     * by any other app allowing for additional security.
     * <p/>
     * NOTE: This code was found and edited from StackOverflow answer:
     * http://stackoverflow.com/a/30172792/2291915
     */
    public void onSaveButtonClicked(View view) {
        whiteboardView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(whiteboardView.getDrawingCache());
        whiteboardView.setDrawingCacheEnabled(false);

        try {

            File cachePath = new File(this.getCacheDir(), "images");
            if (!cachePath.mkdirs()) {
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

    /**
     * This method is called anytime the user selects
     * the undo or redo button. It calls the
     * appropriate method in the whiteboard view.
     */
    public void onUndoRedoClicked(View view) {
        stopDrawingShape();

        if (view.getTag() == null) {
            Log.e("MainActivity", "There was an error in the onUndoRedoClicked for tag " + view.getTag());
        }
        if (view.getTag().equals("redo")) {
            if (!whiteboardView.redo()) {
                Toast.makeText(this, "Nothing to redo", Toast.LENGTH_SHORT).show();
            }
        } else if (view.getTag().equals("undo")) {
            if (!whiteboardView.undo()) {
                Toast.makeText(this, "Nothing to undo", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("MainActivity", "There was an error in the onUndoRedoClicked for tag " + view.getTag());
        }
    }

    /**
     * This method is called when the new photo
     * button is selected. This will prompt the user to
     * select an image (or take an image) to be drawn on.
     * <p/>
     * NOTE: if an image is taken, the image is stored internally
     * and can not be found with other Photos. This is to allow
     * for additional security and to allow for the no additional permissions
     * of saving the photo externally.
     */
    public void onSelectImage(View view) {
        stopDrawingShape();

        List<Intent> cameraIntents = new ArrayList<>();
        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntents.add(captureIntent);

        // Filesystem
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(pickIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, PHOTO_REQUEST_CODE);
    }

    /**
     * This method is called when the user completes the intent.
     * The photo is then added as a new layer to the
     * whiteboard view.
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == PHOTO_REQUEST_CODE) {
            Uri selectedImageUri = data.getData();
            Bitmap bitmap = null;
            if (selectedImageUri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);

                    Drawable d = Drawable.createFromStream(inputStream, selectedImageUri.toString());
                    bitmap = ((BitmapDrawable) d).getBitmap();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                bitmap = (Bitmap) data.getExtras().get("data");

            }

            whiteboardView.changeBackground(bitmap);
        }
    }

    /**
     * This method is called anytime the user selects the shape
     * button (triangle in this case). The user is prompted
     * with a dialog message to select the desired shape and size
     * of the shape. The user will then be prompted to tap the whiteboard
     * view where the center of the shape will be placed.
     */
    public void onShapeButtonSelected(View view) {

        ImageButton erasePaint = (ImageButton)findViewById(R.id.button_erase);
        if(erasePaint == selectedPaint) {
            View bluePaint = findViewById(R.id.button_blue_paint);
            onPaintClicked(bluePaint);
        }

        if(firstTimeCreatingShape) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    firstTimeCreatingShape = false;
                }
            });
            dialog.setMessage("Drag your new shape to the desired location. Pinch and spread to change the size of the shape. Select the check button to save the location and size of your shape and continue to draw.");
            dialog.create();
            dialog.show();
        }

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.FAB);
        floatingActionButton.setVisibility(View.VISIBLE);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopDrawingShape();
            }
        });

        if (view.getId() == R.id.button_oval) {
            whiteboardView.drawNewShape(DrawShape.OVAL);
        } else if (view.getId() == R.id.button_triangle) {
            whiteboardView.drawNewShape(DrawShape.TRIANGLE);
        } else if (view.getId() == R.id.button_rectangle) {
            whiteboardView.drawNewShape(DrawShape.RECTANGLE);
        }

        ImageButton fillButton = (ImageButton)findViewById(R.id.button_fill);
        fillButton.setVisibility(View.VISIBLE);
    }

    /**
     * This method is called when the user selects
     * the fill button. The fill button should
     * only be visible while drawing a shape.
     */
    public void onFillClicked(View view) {
        whiteboardView.fillShape();
    }

    /**
     * This method is a helper method to update the view
     * when the user is no longer drawing a shape.
     */
    private void stopDrawingShape(){
        whiteboardView.isDrawingShape = false;
        findViewById(R.id.FAB).setVisibility(View.GONE);
        findViewById(R.id.button_fill).setVisibility(View.GONE);
    }
}
