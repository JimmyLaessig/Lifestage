package at.ac.tuwien.ims.lifestage.vibrotouch.Scenario;

import android.content.Context;

import java.util.List;

import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.*;
import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Object;

/**
 * Handler for Scenario 1.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class ObjectHandlerScenario1 extends ObjectHandler {
    public ObjectHandlerScenario1(Context context, Testcase testcase){
        super(context, testcase);
    }

    void placeObjects() {
        //TODO
    }

    void finishTestcase() {
        stopTime();
        //TODO
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
