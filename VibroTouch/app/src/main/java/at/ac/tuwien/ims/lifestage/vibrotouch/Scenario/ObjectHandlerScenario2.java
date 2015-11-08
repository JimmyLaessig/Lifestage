package at.ac.tuwien.ims.lifestage.vibrotouch.Scenario;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;

import java.io.File;
import java.util.Random;

import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.*;
import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Object;
import at.ac.tuwien.ims.lifestage.vibrotouch.R;
import at.ac.tuwien.ims.lifestage.vibrotouch.ScenarioActivity;
import at.ac.tuwien.ims.lifestage.vibrotouch.SelectionActivity;
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.XmlHelper;

/**
 * Handler for Scenario 2.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class ObjectHandlerScenario2 extends ObjectHandler {
    private NiftyDialogBuilder dialogBuilder;
    private boolean pickedUp;

    public ObjectHandlerScenario2(ScenarioActivity context, Testcase testcase){
        super(context, testcase);
        pickedUp=false;
        placeObjects();

        dialogBuilder=NiftyDialogBuilder.getInstance(context);
        dialogBuilder.setCancelable(false);
        dialogBuilder
                .withEffect(Effectstype.Fadein)
                .isCancelableOnTouchOutside(false)
                .withTitle(context.getResources().getString(R.string.scenario2))
                .withMessage(context.getResources().getString(R.string.scenario2welcome))
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
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pickedUp)
                    finishTestcase();
            }
        });
    }

    void placeObjects() {
        Object object=testcase.getObjects().get(0);
        object.setSize(mmToPixels(object.getSize()));
        object.setX(screenWidthInPX / 2 - object.getSize() / 2);
        object.setY(screenHeightInPX / 2 - object.getSize() / 2);
        object.setMinSize(mmToPixels(object.getMinSize()));
        object.setMaxSize(mmToPixels(object.getMaxSize()));
        Random rnd = new Random();
        object.setPaint(Color.rgb(rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255)));
    }

    void finishTestcase() {
        stopTime();
        float deviation=Math.abs(testcase.getObjects().get(0).getSize()-testcase.getObjects().get(1).getSize());
        float timeEnd=time/1000f;
        try {
            XmlHelper.writeTestCase2Result(
                    Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + XmlHelper.outputtXMLPath,
                    2, //TODO
                    testcase.getId(),
                    testcase.getScenario(),
                    timeEnd,
                    pixelsToMM(deviation)
            );
        } catch (Exception e) {
            Toast.makeText(context, context.getResources().getString(R.string.saveFailed), Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(context, context.getResources().getString(R.string.saved), Toast.LENGTH_SHORT).show();
        context.startActivity(new Intent(context, SelectionActivity.class));
        context.finish();
    }

    public void handleThreeFingerTap(float xFocus, float yFocus) {
        if(!pickedUp)
            if (testcase.getObjects().get(0).getObjectState() == ObjectState.OnScreen && testcase.getObjects().get(0).contains(xFocus, yFocus)) {
                pickedUp=true;
                pickUpObject(testcase.getObjects().get(0));
                Random rnd = new Random();
                float min=testcase.getObjects().get(0).getMinSize();
                float max=testcase.getObjects().get(0).getMaxSize();
                Object object=new Object(rnd.nextFloat() * (max - min) + min,
                        0, 0,
                        min,
                        max,
                        Color.rgb(rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255)));
                object.setX(screenWidthInPX / 2 - object.getSize() / 2);
                object.setY(screenHeightInPX / 2 - object.getSize() / 2);
                testcase.addObject(object);
            }
    }

    public void handleScale(float scale, float xFocus, float yFocus) {
        if(pickedUp) {
            Object object=testcase.getObjects().get(1);
            if (object.getObjectState() == ObjectState.OnScreen && object.contains(xFocus, yFocus)) {
                float oldSize = object.getSize();

                float newSize = oldSize * scale;
                object.setSize(newSize);

                float diff = newSize - oldSize;
                object.setX(object.getX() - diff / 2f);
                object.setY(object.getY() - diff / 2f);
            }
        }
    }
}
