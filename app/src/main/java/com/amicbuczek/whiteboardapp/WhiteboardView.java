package com.amicbuczek.whiteboardapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

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
    private Paint drawPaint;
    private Canvas canvas;
    private ArrayList<WhiteboardChanges> allWhiteboardChanges;
    private ArrayList<WhiteboardChanges> allUndoneWhiteboardChanges;
    private Bitmap bitmap;

    public WhiteboardView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        allWhiteboardChanges = new ArrayList<>();
        allUndoneWhiteboardChanges = new ArrayList<>();

        this.setDrawingCacheEnabled(true);
        setSaveEnabled(true);


        setUpWhiteboard();
    }

    /**
     * Set up the drawing area for the user interaction
     */
    private void setUpWhiteboard() {
        path = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(Color.BLUE);

        //Smooths out the edges of what is being drawn
        drawPaint.setAntiAlias(true);

        drawPaint.setStrokeWidth(15);
        drawPaint.setStyle(Paint.Style.STROKE);

        //This will create a smoother circular arc when outer edges meet
        drawPaint.setStrokeJoin(Paint.Join.ROUND);

        //This will end the paint path in a semicircle
        //Alternatively BUTT -- stroke ends with path
        //Square -- ends as a square shape
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    /**
     * This method is called when the view is assigned a size.
     * Instantiating the canvas and bitmap with the view size.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if(bitmap == null) {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createScaledBitmap(bitmap, w, h, false);
        }

        canvas = new Canvas(bitmap);
    }

    /**
     * This method allows the custom class to function as a custom
     * drawing view.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (WhiteboardChanges whiteboardChange : allWhiteboardChanges) {

            if(whiteboardChange.backgroundImage != null) {
                canvas.drawBitmap(whiteboardChange.backgroundImage, 0, 0, null);
            }else {
                canvas.drawPath(whiteboardChange.path, whiteboardChange.paint);
            }
            
        }
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

        Paint tempPaint = new Paint(drawPaint);

        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            //The touch has just begun, start the path at this location

            allUndoneWhiteboardChanges = new ArrayList<>();

            path = new Path();
            allWhiteboardChanges.add(new WhiteboardChanges(tempPaint, path, null));

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

            path.lineTo(touchX, touchY);
            canvas.drawPath(path, tempPaint);
        }else{
            //Something unexpected happened, do not draw the path
            Log.e("WhiteboardView", "onTouchEvent sent an unexpected event -- " + event.getAction());
            return false;
        }

        //refresh the view
        invalidate();
        return true;
    }

    public boolean undo(){
        if (allWhiteboardChanges.size() == 0)
            return false;

        WhiteboardChanges whiteboardChange = allWhiteboardChanges.remove(allWhiteboardChanges.size() - 1);
        allUndoneWhiteboardChanges.add(whiteboardChange);

        invalidate();
        return true;
    }

    public boolean redo(){
        if (allUndoneWhiteboardChanges.size() == 0)
            return false;

        WhiteboardChanges whiteboardChange = allUndoneWhiteboardChanges.remove(allUndoneWhiteboardChanges.size() - 1);
        allWhiteboardChanges.add(whiteboardChange);

        invalidate();
        return true;
    }

    /**
     * These two methods get and set the current
     * paint color for the WhiteBoard view.
     */
    public void setPaintColor(int color) {
        drawPaint.setColor(color);
        invalidate();
    }

    public int getPaintColor() {
        return  drawPaint.getColor();
    }

    /**
     * These two methods get and set the current
     * brush size used for in drawing on the
     * whiteboard view.
     */
    public void setBrushSize(float size){
        drawPaint.setStrokeWidth(size);
    }

    public float getBrushSize() {
        return drawPaint.getStrokeWidth();
    }

    /**
     * Clears the canvas, creating a "new" view.
     * Adds all allWhiteboardChanges currently visible to the undone array.
     */
    public void newPage(){
        allUndoneWhiteboardChanges = new ArrayList<>();

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(10);

        Path path = new Path();
        path.moveTo(0, 0);
        path.lineTo(0, this.getHeight());
        path.lineTo(this.getWidth(), this.getHeight());
        path.lineTo(this.getWidth(), 0);
        path.lineTo(0, 0);

        path.close();

        canvas.drawPath(path, paint);
        allWhiteboardChanges.add(new WhiteboardChanges(paint, path, null));

        invalidate();
    }

    public void changeBackground(Bitmap bitmap) {
        bitmap = Bitmap.createScaledBitmap(bitmap, getWidth(), getHeight(), true);
        allWhiteboardChanges.add(new WhiteboardChanges(null, null, bitmap));
    }

}
