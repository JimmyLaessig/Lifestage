package at.ac.tuwien.ims.lifestage.vibrotouch.Scenario;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

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
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.RectanglePacker;
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.UserPreferences;
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.XmlHelper;

/**
 * Handler for Scenario 1.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class ObjectHandlerScenario1 extends ObjectHandler { //pickup mit skalierung
    private AlertDialog.Builder builder;
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

        builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getResources().getString(R.string.scenario1welcome))
                .setTitle(context.getResources().getString(R.string.scenario1))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startTestCase();
                    }
                })
                .setCancelable(false)
                .create()
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
                toast.setText(context.getResources().getString(R.string.scenario1_half));
                toast.show();
                context.startActivity(new Intent(context, SelectionActivity.class));
                context.finish();
            }
            object.setSize(mmToPixels(object.getSize()));
            object.setX(mmToPixels(object.getX()));
            object.setY(mmToPixels(object.getY()));
            object.setMinSize(mmToPixels(object.getMinSize()));
            object.setMaxSize(mmToPixels(object.getMaxSize()));
            object.setPaint(Color.rgb(rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255)));
        }
        Log.d(getClass().getName(), "Done placing top Rectangles.");

        RectanglePacker r= new RectanglePacker((int)screenWidthInPX, (int)(screenHeightInPX/2), 10);
        boolean running=true;
        while(running) {
            int i=0;
            for (Object object : testcase.getObjects()) {
                RectanglePacker.Rectangle rec = r.insert((int) object.getSize(), (int) object.getSize(), object);
                if (rec == null) {
                    r.clear();
                    break;
                }
                i++;
            }
            if(i>=testcase.getObjects().size())
                running=false;
        }
        float minX=0, minY=0, maxX=0, maxY=0;
        for (Object object : testcase.getObjects()) {
            RectanglePacker.Rectangle rec=r.findRectangle(object);
            RectF rect=new RectF(rec.x, rec.y+r.getHeight(), rec.x + object.getSize(), rec.y + object.getSize()+r.getHeight());
            targets.put(object.getId(), rect);

            if(rect.top<minY)
                minY=rect.top;
            if(rect.bottom>maxY)
                maxY=rect.bottom;
            if(rect.left<minX)
                minX=rect.left;
            if(rect.right>maxX)
                maxX=rect.right;
        }

        float distTop=minY;
        float distBot=screenHeightInPX-maxY;
        float distL=minX;
        float distR=screenWidthInPX-maxX;
        float hDiff=(distL+distR)/2, vDiff=(distTop+distBot)/2;
        if (distTop-distBot>=0)
            vDiff*=(-1);
        if (distR-distL<0)
            hDiff*=(-1);

        for (Map.Entry<Integer, RectF> entry : targets.entrySet()) {
            entry.getValue().bottom+=vDiff;
            entry.getValue().top+=vDiff;
            entry.getValue().left+=hDiff;
            entry.getValue().right+=hDiff;
        }

        /*
        boolean running=true;
        long startTime=System.currentTimeMillis();
        while (running) {
            for (Object object : testcase.getObjects()) {
                float x = 0, y = rnd.nextFloat() * ((screenHeightInPX - object.getSize()) - (screenHeightInPX / 2)) + (screenHeightInPX / 2);
                if (targets.isEmpty()) {
                    x = rnd.nextFloat() * (screenWidthInPX - object.getSize() - 1);
                } else {
                    boolean collision = true;
                    while (collision) {
                        x = rnd.nextFloat() * (screenWidthInPX - object.getSize());
                        int i = 0;
                        for (Map.Entry<Integer, RectF> entry : targets.entrySet()) {
                            RectF rect = entry.getValue();
                            if ((x + object.getSize() + 1) < rect.left || x + 1 > rect.right)
                                i++;
                        }
                        if (i == targets.size())
                            collision = false;
                    }
                }
                if(System.currentTimeMillis()-startTime>1000) {
                    startTime=System.currentTimeMillis();
                    targets.clear();
                    break;
                }

                targets.put(object.getId(), new RectF(x, y, x + object.getSize(), y + object.getSize()));

                if(targets.size()==testcase.getObjects().size())
                    running=false;
                Log.d(getClass().getName(), "One loop.");
            }
            Log.d(getClass().getName(), "Another Round.");
        }*/

        Log.d(getClass().getName(), "Done placing bottom Rectangles.");

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
            toast.setText(context.getResources().getString(R.string.saveFailed));
            toast.show();
        }
        toast.setText(context.getResources().getString(R.string.saved));
        toast.show();

        UserPreferences.setCurrentTestcaseID(context, testcase.getId());
        UserPreferences.setJustFinishedTestcase(context, true);

        Intent myIntent=new Intent(context, SelectionActivity.class);
        context.startActivity(myIntent);
        context.finish();
    }

    public void handleThreeFingerTap(float xFocus, float yFocus) {
        /*for (Object object : testcase.getObjects())
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
                    if(dist > obj.getMinSize()) {
                        errors++;
                        Toast.makeText(context, context.getResources().getString(R.string.placeOnRightTarget), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    layDownObject(xFocus, yFocus);
                    if(pickedUpObjects.isEmpty()) {
                        finishTestcase();
                    }
                }
            }
        }*/
    }

    public void handleScale(float scale, float xFocus, float yFocus) {
        if (scale<=1.0) {
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
                if(i==0) {
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
                            if(dist > obj.getMinSize()) {
                                errors++;
                                toast.setText(context.getResources().getString(R.string.placeOnRightTarget));
                                toast.show();
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
