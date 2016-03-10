package com.amicbuczek.whiteboardapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
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

    private final ArrayList<WhiteboardChange> allWhiteboardChanges;
    private ArrayList<WhiteboardChange> allUndoneWhiteboardChanges;

    private Bitmap bitmap;

    public boolean isDrawingShape;
    private float lastX;
    private float lastY;
    private final ScaleGestureDetector scaleGestureDetector;

    public WhiteboardView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        allWhiteboardChanges = new ArrayList<>();
        allUndoneWhiteboardChanges = new ArrayList<>();

        isDrawingShape = false;
        lastX = lastY = 0;

        this.setDrawingCacheEnabled(true);
        setSaveEnabled(true);

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());

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
            bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);

            //Scale all paths
            for(int i = 0; i < allWhiteboardChanges.size(); i++){
                WhiteboardChange whiteboardChange = allWhiteboardChanges.remove(i);

                RectF rectF = new RectF();
                if(whiteboardChange.path != null) {
                    whiteboardChange.path.computeBounds(rectF, true);

                    Matrix scaleMatrix = new Matrix();

                    float width = ((float) w / oldw);
                    float height = ((float) h / oldh);

                    scaleMatrix.setScale(width, height);

                    whiteboardChange.path.transform(scaleMatrix);
                }else{
                    whiteboardChange.backgroundImage = Bitmap.createScaledBitmap(whiteboardChange.backgroundImage, w, h, true);
                }

                allWhiteboardChanges.add(i, whiteboardChange);
            }
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

        for (WhiteboardChange whiteboardChange : allWhiteboardChanges) {

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

        //Remove all previous undone paths.
        allUndoneWhiteboardChanges = new ArrayList<>();

        if(isDrawingShape){
            //Save the path for consistent scaling

            // Let the ScaleGestureDetector inspect all events.
            scaleGestureDetector.onTouchEvent(event);

            if(event.getAction() == MotionEvent.ACTION_MOVE) {

                float differenceX = touchX - lastX;
                float differenceY = touchY - lastY;

                WhiteboardChange whiteboardChange = allWhiteboardChanges.remove(allWhiteboardChanges.size() - 1);

                Matrix translateMatrix = new Matrix();
                translateMatrix.setTranslate(differenceX, differenceY);
                whiteboardChange.path.transform(translateMatrix);
                tempPaint.setStyle(whiteboardChange.paint.getStyle());
                whiteboardChange.paint = tempPaint;
                allWhiteboardChanges.add(whiteboardChange);
            }

            lastX = touchX;
            lastY = touchY;
            invalidate();
            return true;
        }


        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            path = new Path();
            allWhiteboardChanges.add(new WhiteboardChange(tempPaint, path, null));

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

            allWhiteboardChanges.get(allWhiteboardChanges.size() - 1);
        }else{
            //Something unexpected happened, do not draw the path
            Log.e("WhiteboardView", "onTouchEvent sent an unexpected event -- " + event.getAction());
            return false;
        }

        //refresh the view
        invalidate();
        return true;
    }

    /**
     * This listener is only called when drawing a shape.
     * The shape is the scaled based on the user's
     * gesture scale for a pinch/spread.
     */
    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();

            WhiteboardChange whiteboardChange = allWhiteboardChanges.remove(allWhiteboardChanges.size() - 1);

            RectF rectF = new RectF();
            whiteboardChange.path.computeBounds(rectF, true);

            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(scaleFactor, scaleFactor, rectF.centerX(), rectF.centerY());

            whiteboardChange.path.transform(scaleMatrix);
            allWhiteboardChanges.add(whiteboardChange);

            invalidate();
            return true;
        }
    }

    /**
     * Undo pops off the last path added to the whiteboard
     * and adds it to the undone list.
     * @return true if there was a path to undo, false if no paths
     */
    public boolean undo(){
        if (allWhiteboardChanges.size() == 0)
            return false;

        WhiteboardChange whiteboardChange = allWhiteboardChanges.remove(allWhiteboardChanges.size() - 1);
        allUndoneWhiteboardChanges.add(whiteboardChange);

        invalidate();
        return true;
    }

    /**
     * Redo adds the most recently undone path back
     * to the whiteboard.
     * @return true if there was a path to redo,
     * false if no paths.
     */
    public boolean redo(){
        if (allUndoneWhiteboardChanges.size() == 0)
            return false;

        WhiteboardChange whiteboardChange = allUndoneWhiteboardChanges.remove(allUndoneWhiteboardChanges.size() - 1);
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

        if(isDrawingShape){
            Paint tempPaint = new Paint(drawPaint);
            WhiteboardChange whiteboardChange = allWhiteboardChanges.remove(allWhiteboardChanges.size() - 1);
            tempPaint.setStyle(whiteboardChange.paint.getStyle());
            whiteboardChange.paint = tempPaint;
            allWhiteboardChanges.add(whiteboardChange);
        }

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

        if(isDrawingShape){
            allWhiteboardChanges.get(allWhiteboardChanges.size() - 1).paint = new Paint(drawPaint);
            invalidate();
        }
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
        allWhiteboardChanges.add(new WhiteboardChange(paint, path, null));

        invalidate();
    }

    /**
     * This is a helper method to create a correctly sized bitmap
     * to fill the whiteboard view and adds the image to the
     * change list.
     */
    public void changeBackground(Bitmap bitmap) {
        bitmap = Bitmap.createScaledBitmap(bitmap, getWidth(), getHeight(), true);
        allWhiteboardChanges.add(new WhiteboardChange(null, null, bitmap));
    }

    /**
     * This method is called by the MainActivity to draw the chosen
     * shape for the first time on the canvas.
     */
    public void drawNewShape(DrawShape shapeToDraw){
        //Remove all previous undone paths.
        allUndoneWhiteboardChanges = new ArrayList<>();

        int x = getWidth() / 2;
        int y = getHeight() / 2;
        int height = 500;
        int width = 500;

        Path path = new Path();

        if(shapeToDraw == DrawShape.TRIANGLE) {
            //Starting at the top corner
            path.moveTo(x, y - height / 2);
            path.lineTo(x + width / 2, y + height / 2);
            path.lineTo(x - width / 2, y + height / 2);
            path.lineTo(x, y - height / 2);
            path.close();
        }else if(shapeToDraw == DrawShape.RECTANGLE){
            //Starting at top left corner
            path.moveTo(x - width / 2, y - height / 2);
            RectF rectangleRect = new RectF(x - width / 2, y - height / 2, x + width / 2, y + height / 2);
            path.addRect(rectangleRect, Path.Direction.CW);
        }else if(shapeToDraw == DrawShape.OVAL){
            //Starting at the top center
            path.moveTo(x, y - height / 2);
            RectF ovalRect = new RectF(x - width / 2, y - height / 2, x + width / 2, y + height / 2);
            path.addOval(ovalRect, Path.Direction.CW);
        }

        isDrawingShape = true;
        allWhiteboardChanges.add(new WhiteboardChange(new Paint(drawPaint), path, null));
        invalidate();
    }

    /**
     * This method is called to change the style
     * of the shape. If the style is fill, then
     * the style will be changed to a stroke.
     * Stroke will be changed to fill.
     */
    public void fillShape(){
        if(isDrawingShape){
            WhiteboardChange whiteboardChange = allWhiteboardChanges.remove(allWhiteboardChanges.size() - 1);
            Paint.Style style = whiteboardChange.paint.getStyle();

            if(style == Paint.Style.FILL){
                whiteboardChange.paint.setStyle(Paint.Style.STROKE);
            }else{
                whiteboardChange.paint.setStyle(Paint.Style.FILL);
            }

            allWhiteboardChanges.add(whiteboardChange);
            invalidate();
        }
    }
}
