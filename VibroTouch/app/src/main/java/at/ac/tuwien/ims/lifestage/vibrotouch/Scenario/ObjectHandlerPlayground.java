package at.ac.tuwien.ims.lifestage.vibrotouch.Scenario;

import android.graphics.Color;
import android.graphics.RectF;

import java.util.Random;

import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Object;
import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Testcase;
import at.ac.tuwien.ims.lifestage.vibrotouch.ScenarioActivity;

/**
 * Handler for Playground Scenario.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class ObjectHandlerPlayground extends ObjectHandler { //keine skalierung, pickup mit skalierung
    public ObjectHandlerPlayground(ScenarioActivity context, Testcase testcase){
        super(context, testcase);
        placeObjects();
        startTestCase();
    }

    void placeObjects() {
        Random rnd = new Random();
        for(Object object : testcase.getObjects()) {
            object.setSize(mmToPixels(object.getSize()));
            object.setX(mmToPixels(object.getX()));
            object.setY(mmToPixels(object.getY()));
            object.setMinSize(mmToPixels(object.getMinSize()));
            object.setMaxSize(mmToPixels(object.getMaxSize()));
            object.setPaint(Color.rgb(rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255)));
        }
    }

    void finishTestcase() {
        //nothing to do here
    }

    public void handleThreeFingerTap(float xFocus, float yFocus) {
        /*for (Object object : testcase.getObjects())
            if (object.getObjectState()==ObjectState.OnScreen && object.contains(xFocus, yFocus)) {
                pickUpObject(object);
                return;
            }
        if(!pickedUpObjects.isEmpty())
            layDownObject(xFocus, yFocus);*/
    }

    public void handleScale(float scale, float xFocus, float yFocus) {
        if (scale<1.0) {
            for (Object object : testcase.getObjects())
                if (object.getObjectState()==ObjectState.OnScreen && object.contains(xFocus, yFocus)) {
                    pickUpObject(object);
                    return;
                }
        } else {
            if(!pickedUpObjects.isEmpty()) {
                int i=0;
                for (Object object : testcase.getObjects()) {
                    Object object1=pickedUpObjects.peek();
                    RectF rectF = new RectF(xFocus-object1.getSize()/2, yFocus-object1.getSize()/2, xFocus+object1.getSize()/2, yFocus+object1.getSize()/2);
                    if (object.getObjectState() == ObjectState.OnScreen && object.intersects(rectF)) {
                        i++;
                    }
                }
                if(i==0)
                    layDownObject(xFocus, yFocus);
            }
        }

        /*for (Object object : testcase.getObjects())
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
            }*/
    }
}
