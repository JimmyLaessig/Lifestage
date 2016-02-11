package at.ac.tuwien.ims.lifestage.vibrotouch.Scenario;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.*;
import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Object;
import at.ac.tuwien.ims.lifestage.vibrotouch.R;
import at.ac.tuwien.ims.lifestage.vibrotouch.ScenarioActivity;
import at.ac.tuwien.ims.lifestage.vibrotouch.SelectionActivity;
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.UserPreferences;
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.XmlHelper;

/**
 * Handler for Scenario 2.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class ObjectHandlerScenario2 extends ObjectHandler {
    private boolean pickedUp;
    private Button button;

    private int testCasePosition;

    public ObjectHandlerScenario2(final ScenarioActivity context, Testcase testcase, int testCasePosition){
        super(context, testcase);
        this.testCasePosition=testCasePosition;
        pickedUp=false;
        placeObjects();


        if (UserPreferences.getShowTestcaseInfo(context, 2)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(context.getResources().getString(R.string.scenario2welcome))
                    .setTitle(context.getResources().getString(R.string.scenario2))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startTestCase();
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show();
            UserPreferences.setShowTestcaseInfo(context, 2, false);
        }

        button=(Button)context.findViewById(R.id.button_finish);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pickedUp)
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
        object.setPaint(Color.parseColor("#FFA07A"));
    }

    void finishTestcase() {
        stopTime();
        float deviation=Math.abs(testcase.getObjects().get(0).getSize()-testcase.getObjects().get(1).getSize());
        float timeEnd=time/1000f;
        try {
            XmlHelper.writeTestCase2Result(
                    Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + XmlHelper.outputtXMLPath,
                    userID,
                    testcase.getId(),
                    testcase.getScenario(),
                    timeEnd,
                    pixelsToMM(deviation)
            );
        } catch (Exception e) {
            toast.setText(context.getResources().getString(R.string.saveFailed));
            toast.show();
        }
        toast.setText(context.getResources().getString(R.string.saved));
        toast.show();

        UserPreferences.setJustFinishedTestcase(context, true);
        UserPreferences.setCurrentTestcasePositionInList(context, testCasePosition);

        Intent myIntent=new Intent(context, SelectionActivity.class);
        context.startActivity(myIntent);
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
                        Color.parseColor("#98FB98"));
                object.setX(screenWidthInPX / 2 - object.getSize() / 2);
                object.setY(screenHeightInPX / 2 - object.getSize() / 2);
                testcase.addObject(object);
                button.setVisibility(View.VISIBLE);
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
