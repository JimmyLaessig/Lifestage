package at.ac.tuwien.ims.lifestage.vibrotouch.Scene;

import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Container containing objects that are drawn and interacted with.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class Container {
    private List<Rectangle> allRectangles;
    private Stack<Rectangle> pickedUpRectangles;

    public Container() {
        allRectangles =new ArrayList<>();
        pickedUpRectangles =new Stack<>();
    }

    public void add(Rectangle rectangle) {
        allRectangles.add(rectangle);
    }

    public void draw(Canvas canvas) {
        for(Rectangle rectangle : allRectangles)
            if(rectangle.isVisible())
                rectangle.draw(canvas);
    }

    public void handleScale(float scaleFactor, float xFocus, float yFocus) {
        if (scaleFactor < 1.0f) {
            for (Rectangle rectangle : allRectangles)
                if (rectangle.isVisible() && rectangle.contains(xFocus, yFocus)) {
                    rectangle.setVisible(false);
                    pickUpObject(rectangle);
                }
        } else if (scaleFactor > 1.0f) {
            if(!pickedUpRectangles.isEmpty())
                layDownObject(xFocus, yFocus);
        }
    }

    public void handleMove(float x, float y, float dx, float dy) {
        for(Rectangle rectangle : allRectangles)
            if (rectangle.isVisible() && rectangle.contains(x, y))
                rectangle.move(dx, dy);
    }

    private void layDownObject(float x, float y) {
        Rectangle picked= pickedUpRectangles.pop();

        for (Rectangle rectangle : allRectangles)
            if(picked.equals(rectangle)) {
                picked.setX(x - (picked.getWidth() / 2));
                picked.setY(y - (picked.getHeight() / 2));
                rectangle.setVisible(true);
            }
    }

    private void pickUpObject(Rectangle rectangle) {
        pickedUpRectangles.push(rectangle);
    }
}
