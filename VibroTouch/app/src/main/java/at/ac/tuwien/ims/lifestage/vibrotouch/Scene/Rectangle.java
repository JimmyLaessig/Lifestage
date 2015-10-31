package at.ac.tuwien.ims.lifestage.vibrotouch.Scene;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Rectangle that will be drawn and interacted with.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class Rectangle {
    private Paint paint;
    private float x, y, width, height;
    private boolean visible;

    public Rectangle(float x, float y, float width, float height, int color) {
        paint = new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        visible=true;
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(new RectF(x, y, x+width, y+height), paint);
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

    public float getArea() {
        return width*height;
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

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
