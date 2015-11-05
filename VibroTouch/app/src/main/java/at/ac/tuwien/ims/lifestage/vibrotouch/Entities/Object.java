package at.ac.tuwien.ims.lifestage.vibrotouch.Entities;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import at.ac.tuwien.ims.lifestage.vibrotouch.Scenario.ObjectState;

/**
 * Object that will be drawn and interacted with.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class Object {
    private Paint paint;
    private float x, y, width, height, minArea, maxArea;
    private ObjectState objectState;

    public Object(float x, float y, float size, int color, float minWidth, float maxWidth, float minHeight, float maxHeight) {
        paint = new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);

        this.x = x;
        this.y = y;
        this.width = size;
        this.height = size;
        this.minArea=minWidth*minHeight;
        this.maxArea=maxWidth*maxHeight;
        this.objectState=ObjectState.OnScreen;
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(new RectF(x, y, x + width, y + height), paint);
    }

    public void move(float dx, float dy) {
        setX(getX() + dx);
        setY(getY() + dy);
    }

    public boolean contains(float x, float y) {
        if (x>getX() && x<(getX()+getWidth()) && y>getY() && y<(getY()+getHeight())) {
            return true;
        }
        return false;
    }

    public int getIntensity() {
        float currArea=width*height;
        float result = (float)10 + (((float)100 - (float)10) / (maxArea - minArea)) * (currArea - minArea);
        return Math.round(result);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public ObjectState getObjectState() {
        return objectState;
    }

    public void changeObjectState() {
        if(this.objectState==ObjectState.OnScreen)
            this.objectState = ObjectState.PickedUp;
        else
            this.objectState = ObjectState.OnScreen;
    }
}
