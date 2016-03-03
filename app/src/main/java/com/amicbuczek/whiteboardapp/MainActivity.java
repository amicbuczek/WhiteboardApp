package com.amicbuczek.whiteboardapp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

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
}
