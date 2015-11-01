package at.ac.tuwien.ims.lifestage.vibrotouch.Scene;

import android.graphics.Canvas;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import at.ac.tuwien.ims.lifestage.vibrotouch.Util.SparkManager;

/**
 * ObjectHandler containing objects that are drawn and interacted with.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class ObjectHandler {
    private List<Object> allObjects;
    private Stack<Object> pickedUpObjects;
    private SparkManager connectionManager;

    public ObjectHandler() {
        allObjects =new ArrayList<>();
        pickedUpObjects =new Stack<>();
        connectionManager=SparkManager.getInstance();
    }

    public void add(Object object) {
        allObjects.add(object);
    }

    public void draw(Canvas canvas) {
        for(Object object : allObjects)
            if(object.getObjectState()==ObjectState.OnScreen)
                object.draw(canvas);
    }

    public void handleScale(float scaleFactor, float xFocus, float yFocus) {
        if (scaleFactor < 1.0f) {
            for (Object object : allObjects)
                if (object.getObjectState()==ObjectState.OnScreen && object.contains(xFocus, yFocus)) {
                    object.setObjectState(ObjectState.PickedUpVibro0);
                    pickUpObject(object);
                }
        } else if (scaleFactor > 1.0f) {
            if(!pickedUpObjects.isEmpty())
                layDownObject(xFocus, yFocus);
        }
    }

    public void handleMove(float x, float y, float dx, float dy) {
        for(Object object : allObjects)
            if (object.getObjectState()==ObjectState.OnScreen && object.contains(x, y))
                object.move(dx, dy);
    }

    private void layDownObject(float x, float y) {
        Object picked= pickedUpObjects.pop();

        for (Object object : allObjects)
            if(picked.equals(object)) {
                picked.setX(x - (picked.getWidth() / 2));
                picked.setY(y - (picked.getHeight() / 2));
                object.setObjectState(ObjectState.OnScreen);
            }
    }

    private void pickUpObject(Object object) {
        pickedUpObjects.push(object);
    }

    /**
     * Sends a command to Spark Core.
     *
     * @param command command string to send
     * @return true if command was sent else false
     */
    public boolean sendCommand(String command) {
        if(connectionManager==null || command.isEmpty()) {
            Log.e(getClass().getName(), "sendevent failed");
            return false;
        }
        if(connectionManager.getStatus() != 1) {
            Log.e(getClass().getName(), "sendevent failed, not connected");
            return false;
        }

        Log.d(getClass().getName(), "sent command: " + command);
        connectionManager.sendCommand_executePattern(command);
        return true;
    }
}
