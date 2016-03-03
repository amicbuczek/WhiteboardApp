package com.amicbuczek.whiteboardapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by buczek on 3/3/16.
 *
 * This view is used to create a custom drawing
 * area for the white board app.
 *
 * Note some of this code was found/edited from
 * this tutorial on drawing:
 * http://code.tutsplus.com/tutorials/android-sdk-create-a-drawing-app-interface-creation--mobile-19021
 */
public class WhiteboardView extends View {

    private Path path;
    private Paint drawPaint, canvasPaint;
    private int paintColor = Color.BLUE;
    private Canvas canvas;
    private Bitmap bitmap;

    public WhiteboardView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        setUpWhiteboard();
    }

    /**
     * Set up the drawing area for the user interaction
     */
    private void setUpWhiteboard() {
        path = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);

        //Smooths out the edges of what is being drawn
        drawPaint.setAntiAlias(true);

        drawPaint.setStrokeWidth(getResources().getDimension(R.dimen.brush_medium));
        drawPaint.setStyle(Paint.Style.STROKE);

        //This will create a smoother circular arc when outer edges meet
        drawPaint.setStrokeJoin(Paint.Join.ROUND);

        //This will end the paint path in a semicircle
        //Alternatively BUTT -- stroke ends with path
        //Square -- ends as a square shape
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    /**
     * This method is called when the view is assigned a size.
     * Instantiating the canvas and bitmap with the view size.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    /**
     * This method allows the custom class to function as a custom
     * drawing view.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(bitmap, 0, 0, canvasPaint);
        canvas.drawPath(path, drawPaint);
    }

    /**
     * Detecting anytime the user is touching the whiteboard
     * view. Determine the touch location, the current touch action,
     * and draw the path created by the touch.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            //The touch has just begun, start the path at this location
            path.moveTo(touchX, touchY);
        } else if(event.getAction() == MotionEvent.ACTION_MOVE) {
            //Continue the path along this line
            path.lineTo(touchX, touchY);
        } else if(event.getAction() == MotionEvent.ACTION_UP ||
                event.getAction() == MotionEvent.ACTION_CANCEL) {
            //The drawing is complete, draw the path on the canvas
            //with the current paint color, and reset the path.

            //ACTION_CANCEL occurs when the user touches up
            //outside of the whiteboard view.

            canvas.drawPath(path, drawPaint);
            path.reset();
        }else{
            //Something unexpected happened, do not draw the path
            return false;
        }

        //refresh the view
        invalidate();
        return true;
    }

    /**
     * This method is called everytime a new
     * color is selected or when the eraser
     * is selected.
     */
    public void setPaintColor(int color) {
        drawPaint.setColor(color);
        invalidate();
    }

    public void clearCanvas(){
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        invalidate();
    }
}
