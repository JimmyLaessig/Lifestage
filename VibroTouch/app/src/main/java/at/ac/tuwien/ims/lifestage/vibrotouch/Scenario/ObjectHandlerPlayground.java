package at.ac.tuwien.ims.lifestage.vibrotouch.Scenario;

import android.content.Context;
import android.graphics.Color;

import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Object;
import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Testcase;
import at.ac.tuwien.ims.lifestage.vibrotouch.ScenarioActivity;

/**
 * Handler for Playground Scenario.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class ObjectHandlerPlayground extends ObjectHandler {
    public ObjectHandlerPlayground(ScenarioActivity context, Testcase testcase){
        super(context, testcase);
        startTestCase();
    }

    void placeObjects() {
        Object o1=new Object(mmToPixels(10), mmToPixels(34.5f), mmToPixels(20), mmToPixels(10), mmToPixels(30), Color.RED);
        Object o2=new Object(mmToPixels(20), mmToPixels(29.5f), mmToPixels(55), mmToPixels(10), mmToPixels(30), Color.BLUE);
        Object o3=new Object(mmToPixels(30), mmToPixels(24.5f), mmToPixels(90), mmToPixels(10), mmToPixels(30), Color.BLACK);

        testcase.addObject(o3);
        testcase.addObject(o2);
        testcase.addObject(o1);
    }

    void finishTestcase() {
        //nothing to do here
    }

    public void handleThreeFingerTap(float xFocus, float yFocus) {
        for (Object object : testcase.getObjects())
            if (object.getObjectState()==ObjectState.OnScreen && object.contains(xFocus, yFocus)) {
                pickUpObject(object);
                return;
            }
        if(!pickedUpObjects.isEmpty())
            layDownObject(xFocus, yFocus);
    }

    public void handleScale(float scale, float xFocus, float yFocus) {
        for (Object object : testcase.getObjects())
            if (object.getObjectState()==ObjectState.OnScreen && object.contains(xFocus, yFocus)) {
                float oldSize=object.getSize();

                float newSize=oldSize*scale;
                if (newSize>object.getMaxSize())
                    newSize=object.getMaxSize();
                if (newSize<object.getMinSize())
                    newSize=object.getMinSize();
                object.setSize(newSize);

                float diff=newSize-oldSize;
                object.setX(object.getX()-diff/2f);
                object.setY(object.getY() - diff / 2f);
            }
    }
}
