package com.amicbuczek.whiteboardapp;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by buczek on 3/4/16.
 *
 * This object holds the information for every change
 * made on the whiteboard view whether it be adding
 * a photo or drawing a path.
 */
public class WhiteboardChange {
    public Paint paint;
    public Path path;
    public Bitmap backgroundImage;

    public WhiteboardChange(Paint paint, Path path, Bitmap backgroundImage){
        this.paint = paint;
        this.path = path;
        this.backgroundImage = backgroundImage;
    }
}
