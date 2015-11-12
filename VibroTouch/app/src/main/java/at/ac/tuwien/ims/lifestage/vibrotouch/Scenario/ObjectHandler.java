package at.ac.tuwien.ims.lifestage.vibrotouch.Scenario;

import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Event;
import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Object;
import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Testcase;
import at.ac.tuwien.ims.lifestage.vibrotouch.R;
import at.ac.tuwien.ims.lifestage.vibrotouch.ScenarioActivity;
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.SparkManager;
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.UserPreferences;
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.XmlHelper;

/**
 * ObjectHandler containing objects that are drawn and interacted with.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public abstract class ObjectHandler {
    private enum UsedVibro {
        Vibro0(0),
        Vibro1(1);
        private final int mask;
        UsedVibro(int mask) {
            this.mask = mask;
        }
        public int getID() {
            return mask;
        }
    }

    protected ScenarioActivity context;
    protected Button button;

    protected Stack<Object> pickedUpObjects;
    private SparkManager connectionManager;

    private UsedVibro usedVibro;
    private final long pauseBetweenVibes=750L;
    private final long pauseAfterVibe=250L;
    private final long durationOfVibe=250L;

    private volatile boolean vibroThreadRunning = true;
    private volatile boolean buttonThreadRunning = true;

    protected Testcase testcase;
    protected float screenWidthInPX, screenHeightInPX, screenWidthInMM, screenHeightInMM;

    protected long time;

    protected String userID;

    public ObjectHandler(ScenarioActivity context, Testcase testcase) {
        this.context=context;
        pickedUpObjects=new Stack<>();
        connectionManager=SparkManager.getInstance();
        usedVibro=UsedVibro.Vibro0;

        button=(Button)context.findViewById(R.id.button_finish);

        this.testcase=testcase;

        float[] screen={-1,-1};
        try {
            screen=XmlHelper.getScreenWidthAndHeight(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + XmlHelper.inputXMLPath);
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getMessage());
        }
        screenWidthInMM=screen[0];
        screenHeightInMM=screen[1];

        Display mdisp = context.getWindowManager().getDefaultDisplay();
        Point mdispSize = new Point();
        mdisp.getSize(mdispSize);
        screenWidthInPX = mdispSize.x;
        screenHeightInPX = mdispSize.y;

        userID=UserPreferences.getCurrentUserID(context);

        Log.d(getClass().getName(), "begin handling objects");
    }

    abstract void placeObjects();
    abstract void finishTestcase();
    public abstract void handleThreeFingerTap(float xFocus, float yFocus);
    public abstract void handleScale(float scale, float xFocus, float yFocus);

    protected void startTestCase() {
        if(testcase.isButtonOn())
            startButtonThread();
        startVibroThread();

        time=System.currentTimeMillis();
    }

    protected void stopTime() {
        long begin=time;
        time=System.currentTimeMillis()-begin;
    }

    protected float pixelsToMM(float pixels) {
        return pixels/(screenWidthInPX/screenWidthInMM);
    }

    protected float mmToPixels(float mm) {
        return mm/(screenWidthInMM/screenWidthInPX);
    }

    public void draw(Canvas canvas) {
        for(Object object : testcase.getObjects())
            if(object.getObjectState()== ObjectState.OnScreen)
                object.draw(canvas);
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

    void pickUpObject(Object object) {
        object.changeObjectState();
        pickedUpObjects.push(object);
    }

    /*public void handleMove(float xFocus, float yFocus, float dx, float dy) {
        for(Object object : testcase.getObjects())
            if (object.getObjectState()==ObjectState.OnScreen && object.contains(xFocus, yFocus))
                object.move(dx, dy);
    }*/

    private void startVibroThread() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (vibroThreadRunning) {
                    try {
                        if (!pickedUpObjects.isEmpty()) {
                            ArrayList<Event> events = new ArrayList<>();
                            for (Object o : pickedUpObjects) {
                                //min 250, max 750
                                /*long durationOfVibe=o.calculateDuration(250, 750);
                                events.add(
                                        new Event(
                                                usedVibro.getID(),
                                                100 ,
                                                100 ,
                                                durationOfVibe,
                                                pauseAfterVibe));*/
                                events.add(
                                        new Event(
                                                usedVibro.getID(),
                                                o.getIntensity(testcase.getMinIntensity(),testcase.getMaxIntensity()),
                                                o.getIntensity(testcase.getMinIntensity(), testcase.getMaxIntensity()),
                                                durationOfVibe,
                                                pauseAfterVibe));
                            }
                            Collections.reverse(events);
                            while(!events.isEmpty()) {
                                sendEvent(events.get(0));
                                long dur=events.get(0).getDuration();
                                long pause=events.get(0).getPauseAfter();
                                events.remove(0);
                                Thread.sleep(dur+pause+1L);
                            }
                            Thread.sleep(pauseBetweenVibes);
                        }
                    } catch (Exception e) {
                        Log.e(getClass().getName(), e.getMessage());
                        vibroThreadRunning = false;
                    }
                }
            }
        });
        thread.start();
    }

    private void startButtonThread() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (buttonThreadRunning) {
                    try {
                        if(connectionManager.getStatus()==SparkManager.NOT_CONNECTED) {
                            Toast.makeText(context, context.getResources().getString(R.string.connectionLost), Toast.LENGTH_SHORT).show();
                        }
                        if (connectionManager.getButtonStateChanged()) {
                            if(usedVibro==UsedVibro.Vibro0) {
                                usedVibro=UsedVibro.Vibro1;
                            } else {
                                usedVibro=UsedVibro.Vibro0;
                            }
                            Log.d(getClass().getName(), "button pressed");
                            connectionManager.setButtonStateChangedRecognized();
                        }
                    } catch (Exception e) {
                        Log.e(getClass().getName(), e.getMessage());
                        buttonThreadRunning = false;
                    }
                }
            }
        });
        thread.start();
    }

    public void stopThreads() {
        Log.d(getClass().getName(), "stopping object handling threads");
        buttonThreadRunning=false;
        vibroThreadRunning=false;
    }

    private void sendEvent(Event e) {
        if(connectionManager.getStatus()!=1) {
            Log.e(getClass().getName(), "send vibrate failed, not connected");
            return;
        }
        if(e==null) {
            return;
        }

        String command="";
        command+="_1";
        command+="_"+ e.getAcId();
        command+="_"+e.getIntensity();
        command+="_"+e.getTargetIntensity();
        command+="_"+e.getDuration();
        command+="_"+e.getPauseAfter();
        command+="_";
        command+="1_";
        connectionManager.sendCommand_executePattern(command);
    }
}
