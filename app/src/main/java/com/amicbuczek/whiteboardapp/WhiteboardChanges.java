package com.amicbuczek.whiteboardapp;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;

/**
 * Created by buczek on 3/4/16.
 */
public class WhiteboardChanges {
    public Paint paint;
    public Path path;
    public Drawable backgroundImage;

    public WhiteboardChanges(Paint paint, Path path, Drawable backgroundImage){
        this.paint = paint;
        this.path = path;
        this.backgroundImage = backgroundImage;
    }
}
