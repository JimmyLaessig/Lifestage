package at.ac.tuwien.ims.lifestage.vibrotouch.Scenario;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Object;
import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Testcase;
import at.ac.tuwien.ims.lifestage.vibrotouch.R;
import at.ac.tuwien.ims.lifestage.vibrotouch.ScenarioActivity;
import at.ac.tuwien.ims.lifestage.vibrotouch.SelectionActivity;
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.XmlHelper;

/**
 * Handler for Scenario 1.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class ObjectHandlerScenario1 extends ObjectHandler {
    private NiftyDialogBuilder dialogBuilder;
    private int errors=0;
    private int screenPlacements=0;

    //integer=id of corresponding object
    private HashMap<Integer, RectF> targets;

    private RectF blackRect;
    private Paint blackPaint;
    private Paint whitePaint;

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

    public void draw(Canvas canvas) {
        canvas.drawRect(blackRect, blackPaint);
        for(Map.Entry<Integer, RectF> entry : targets.entrySet()) {
            canvas.drawRect(entry.getValue(), whitePaint);
        }
        super.draw(canvas);
    }

    void placeObjects() {
        blackRect=new RectF(0, screenHeightInPX/2, screenWidthInPX, screenHeightInPX);
        blackPaint=new Paint();
        blackPaint.setColor(Color.BLACK);

        Random rnd = new Random();
        targets=new HashMap<>();
        for(Object object : testcase.getObjects()) {
            if((object.getY()+(object.getSize()/2))>screenHeightInMM/2) {
                Toast.makeText(context, context.getResources().getString(R.string.scenario1_half), Toast.LENGTH_SHORT).show();
                context.startActivity(new Intent(context, SelectionActivity.class));
                context.finish();
            }
            object.setSize(mmToPixels(object.getSize()));
            object.setX(mmToPixels(object.getX()));
            object.setY(mmToPixels(object.getY()));
            object.setMinSize(mmToPixels(object.getMinSize()));
            object.setMaxSize(mmToPixels(object.getMaxSize()));
            object.setPaint(Color.rgb(rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255)));

            //TODO only works when objs are in a line
            float x=0, y = rnd.nextFloat() * ((screenHeightInPX - object.getSize()) - (screenHeightInPX / 2)) + (screenHeightInPX / 2);
            if(targets.isEmpty()) {
                x=rnd.nextFloat() * (screenWidthInPX - object.getSize()-1);
            } else {
                boolean collision=true;
                while(collision) {
                    x=rnd.nextFloat() * (screenWidthInPX - object.getSize());
                    int i=0;
                    for (Map.Entry<Integer, RectF> entry : targets.entrySet()) {
                        RectF rect=entry.getValue();
                        if((x+object.getSize()+1)<rect.left || x+1>rect.right)
                            i++;
                    }
                    if(i==targets.size())
                        collision=false;
                }
            }
            targets.put(object.getId(), new RectF(x, y, x + object.getSize(), y + object.getSize()));
        }
        whitePaint=new Paint();
        whitePaint.setColor(Color.WHITE);
        whitePaint.setStyle(Paint.Style.STROKE);
    }

    void finishTestcase() {
        stopTime();
        float timeEnd=time/1000f;

        Map<Integer, Float> accuracyDeviation=new TreeMap<>();
        for(int i=0; i<testcase.getObjects().size(); i++) {
            Object object=testcase.getObjects().get(i);
            RectF target=targets.get(object.getId());
            accuracyDeviation.put(object.getId(), pixelsToMM(object.distTo(target)));
        }

        try {
            XmlHelper.writeTestCase1Result(
                    Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + XmlHelper.outputtXMLPath,
                    userID,
                    testcase.getId(),
                    testcase.getScenario(),
                    timeEnd,
                    errors,
                    screenPlacements,
                    accuracyDeviation
            );
        } catch (Exception e) {
            Toast.makeText(context, context.getResources().getString(R.string.saveFailed), Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(context, context.getResources().getString(R.string.saved), Toast.LENGTH_SHORT).show();
        context.startActivity(new Intent(context, SelectionActivity.class));
        context.finish();
    }

    public void handleThreeFingerTap(float xFocus, float yFocus) {
        for (Object object : testcase.getObjects())
            if (object.getObjectState()==ObjectState.OnScreen && object.contains(xFocus, yFocus)) {
                pickUpObject(object);
                return;
            }

        if(!pickedUpObjects.isEmpty()) {
            int white=0, black=0;
            for (Object object : testcase.getObjects())
                if(object.getObjectState()==ObjectState.OnScreen)
                    if((object.getY()+object.getSize()/2f)<screenHeightInPX/2)
                        white++;
                    else
                        black++;

            if(yFocus<screenHeightInPX/2) { //white part
                if(black==0) {
                    layDownObject(xFocus, yFocus);
                    screenPlacements++;
                }
            } else { //black part
                if(white==0) {
                    Object obj=pickedUpObjects.peek();
                    RectF rect=targets.get(obj.getId());
                    float devX=rect.centerX()-xFocus;
                    float devY=rect.centerY()-yFocus;
                    float dist=(float)Math.sqrt(Math.pow(devX, 2) + Math.pow(devY, 2));
                    if(dist > obj.getSize()/2f) { //TODO how much of a difference?
                        errors++;
                        return;
                    }
                    layDownObject(xFocus, yFocus);
                    if(pickedUpObjects.isEmpty()) {
                        finishTestcase();
                    }
                }
            }
        }
    }

    public void handleScale(float scale, float xFocus, float yFocus) {
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
