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
    private static int ID_COUNTER=0;
    private int id;
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
        this.id=ID_COUNTER;
        ID_COUNTER++;
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

    public boolean intersects(RectF object) {
        RectF ra=new RectF(getX(), getY(), getX()+getSize(), getY()+getSize());
        return ra.intersect(object);
    }

    public int getIntensity(int minIntesity, int maxIntesity) {
        float s=getSize();
        if(s>maxSize) {
            s=maxSize;
        }
        if(s<minSize) {
            s=minSize;
        }
        float result = (float)minIntesity + (((float)maxIntesity - (float)minIntesity) / (maxSize - minSize)) * (s - minSize);
        return Math.round(result);
    }

    public long calculateDuration(long minTime, long maxTime) {
        float s=getSize();
        if(s>maxSize) {
            s=maxSize;
        }
        if(s<minSize) {
            s=minSize;
        }
        float result = (float)minTime + (((float)maxTime - (float)minTime) / (maxSize - minSize)) * (s - minSize);
        return Math.round(result);
    }

    public void changeObjectState() {
        if(this.objectState==ObjectState.OnScreen)
            this.objectState = ObjectState.PickedUp;
        else
            this.objectState = ObjectState.OnScreen;
    }

    public float getCenterX() {
        return x+(getSize()/2f);
    }

    public float getCenterY() {
        return y+(getSize()/2f);
    }

    public float distTo(Object object) {
        float devX=getCenterX()-object.getCenterX();
        float devY=getCenterY()-object.getCenterY();
        return (float)Math.sqrt(Math.pow(devX, 2) + Math.pow(devY, 2));
    }

    public float distTo(RectF rect) {
        float devX=rect.centerX()-getCenterX();
        float devY=rect.centerY()-getCenterY();
        return (float)Math.sqrt(Math.pow(devX, 2) + Math.pow(devY, 2));
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

    public void setSize(float size) {
        this.size = size;
    }

    public float getSize() {
        return size;
    }

    public ObjectState getObjectState() {
        return objectState;
    }

    public float getMinSize() {
        return minSize;
    }

    public void setMinSize(float minSize) {
        this.minSize = minSize;
    }

    public float getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(float maxSize) {
        this.maxSize = maxSize;
    }

    public void setPaint(int color) {
        this.paint.setColor(color);
    }

    public int getId() {
        return id;
    }
}
