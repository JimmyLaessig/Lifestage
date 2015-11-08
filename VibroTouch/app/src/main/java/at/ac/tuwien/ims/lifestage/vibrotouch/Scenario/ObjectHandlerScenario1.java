package at.ac.tuwien.ims.lifestage.vibrotouch.Scenario;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;

import java.util.Random;

import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Object;
import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Testcase;
import at.ac.tuwien.ims.lifestage.vibrotouch.R;
import at.ac.tuwien.ims.lifestage.vibrotouch.ScenarioActivity;

/**
 * Handler for Scenario 1.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class ObjectHandlerScenario1 extends ObjectHandler {
    private NiftyDialogBuilder dialogBuilder;

    public ObjectHandlerScenario1(ScenarioActivity context, Testcase testcase){
        super(context, testcase);
        placeObjects();

        dialogBuilder= NiftyDialogBuilder.getInstance(context);
        dialogBuilder.setCancelable(false);
        dialogBuilder
                .withEffect(Effectstype.Fadein)
                .isCancelableOnTouchOutside(false)
                .withTitle(context.getResources().getString(R.string.scenario1))
                .withMessage(context.getResources().getString(R.string.scenario1welcome))
                .withTitleColor("#000000")
                .withMessageColor("#000000")
                .withDialogColor("#f9f9f9")
                .withButton1Text(context.getResources().getString(R.string.ok))
                .setButton1Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startTestCase();
                        dialogBuilder.dismiss();
                    }
                })
                .show();
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
        stopTime();
        //TODO
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
