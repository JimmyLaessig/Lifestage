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
    private float x, y, size, minSize, maxSize;
    private ObjectState objectState;

    public Object(float size, float x, float y, float minSize, float maxSize,  int color) {
        paint = new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);

        this.x = x;
        this.y = y;
        this.size = size;
        this.minSize =minSize;
        this.maxSize =maxSize;
        this.objectState=ObjectState.OnScreen;
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(new RectF(x, y, x + size, y + size), paint);
    }

    public void move(float dx, float dy) {
        setX(getX() + dx);
        setY(getY() + dy);
    }

    public boolean contains(float x, float y) {
        if (x>getX() && x<(getX()+ getSize()) && y>getY() && y<(getY()+getSize())) {
            return true;
        }
        return false;
    }

    public int getIntensity(int minIntesity, int maxIntesity) {
        float s=getSize()>maxSize? maxSize : getSize();
        s=getSize()<minSize? minSize : getSize();
        float result = (float)minIntesity + (((float)maxIntesity - (float)minIntesity) / (maxSize - minSize)) * (s - minSize);
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

    public float getSize() {
        return size;
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
