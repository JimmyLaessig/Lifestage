package at.ac.tuwien.ims.lifestage.vibrotouch.Scenario;

import android.content.Context;
import android.graphics.Color;

import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Object;
import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Testcase;

/**
 * Handler for Playground Scenario.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class ObjectHandlerPlayground extends ObjectHandler {
    public ObjectHandlerPlayground(Context context, Testcase testcase){
        super(context, testcase);
    }

    void placeObjects() {
        Object o1=new Object(mmToPixels(10), mmToPixels(34.5f), mmToPixels(20), mmToPixels(10), mmToPixels(30), Color.RED);
        Object o2=new Object(mmToPixels(20), mmToPixels(29.5f), mmToPixels(55), mmToPixels(10), mmToPixels(30), Color.BLUE);
        Object o3=new Object(mmToPixels(30), mmToPixels(24.5f), mmToPixels(90), mmToPixels(10), mmToPixels(30), Color.BLACK);

        testcase.addObject(o1);
        testcase.addObject(o2);
        testcase.addObject(o3);
    }

    void finishTestcase() {
        //nothing to do here
    }

    void layDownObject(float x, float y) {
        Object picked=pickedUpObjects.pop();

        for (Object object : testcase.getObjects())
            if(picked.equals(object)) {
                picked.setX(x - (picked.getSize() / 2));
                picked.setY(y - (picked.getSize() / 2));
                object.changeObjectState();
            }
    }
}
