package at.ac.tuwien.ims.lifestage.vibrotouch.Scenario;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Environment;
import android.util.Log;
import android.view.Display;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Event;
import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Object;
import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Testcase;
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.SparkManager;
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

    protected Stack<Object> pickedUpObjects;
    private SparkManager connectionManager;

    private UsedVibro usedVibro;
    private final long pauseBetweenVibes=1000L;
    private final long pauseAfterVibe=400L;
    private final long durationOfVibe=100L;

    private volatile boolean vibroThreadRunning = true;
    private volatile boolean buttonThreadRunning = true;

    protected Testcase testcase;
    protected float screenWidthInPX, screenHeightInPX, screenWidthInMM, screenHeightInMM;

    protected int attempts=0;
    protected long time;

    public ObjectHandler(Context context, Testcase testcase) {
        pickedUpObjects=new Stack<>();
        connectionManager=SparkManager.getInstance();
        usedVibro=UsedVibro.Vibro0;

        this.testcase=testcase;

        float[] screen={-1,-1};
        try {
            screen=XmlHelper.getScreenWidthAndHeight(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + XmlHelper.inputXMLPath);
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getMessage());
        }
        screenWidthInMM=screen[0];
        screenHeightInMM=screen[1];

        Display mdisp = ((Activity)context).getWindowManager().getDefaultDisplay();
        Point mdispSize = new Point();
        mdisp.getSize(mdispSize);
        screenWidthInPX = mdispSize.x;
        screenHeightInPX = mdispSize.y;

        placeObjects();

        if(testcase.isButtonOn())
            startButtonThread();
        startVibroThread();

        time=System.currentTimeMillis();
        Log.d(getClass().getName(), "begin handling objects");
    }

    abstract void placeObjects();
    abstract void finishTestcase();
    abstract void layDownObject(float x, float y);

    protected void stopTime() {
        long begin=time;
        time=System.currentTimeMillis()-begin;
    }

    protected float pixelsToMM(float pixels) {
        return pixels/(screenHeightInPX/screenHeightInMM);
    }

    protected float mmToPixels(float mm) {
        return mm/(screenHeightInMM/screenHeightInPX);
    }

    public void draw(Canvas canvas) {
        for(Object object : testcase.getObjects())
            if(object.getObjectState()== ObjectState.OnScreen)
                object.draw(canvas);
    }

    public void handleScale(float scaleFactor, float xFocus, float yFocus) {
        if (scaleFactor < 1.0f) {
            for (Object object : testcase.getObjects())
                if (object.getObjectState()==ObjectState.OnScreen && object.contains(xFocus, yFocus)) {
                    pickUpObject(object);
                }
        } else if (scaleFactor > 1.0f) {
            if(!pickedUpObjects.isEmpty())
                layDownObject(xFocus, yFocus);
        }
    }

    public void handleMove(float x, float y, float dx, float dy) {
        for(Object object : testcase.getObjects())
            if (object.getObjectState()==ObjectState.OnScreen && object.contains(x, y))
                object.move(dx, dy);
    }

    private void pickUpObject(Object object) {
        object.changeObjectState();
        pickedUpObjects.push(object);
    }

    private void startVibroThread() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (vibroThreadRunning) {
                    try {
                        if (!pickedUpObjects.isEmpty()) {
                            ArrayList<Event> events = new ArrayList<>();
                            for (Object o : pickedUpObjects) {
                                events.add(new Event(usedVibro.getID(), 0, o.getIntensity(testcase.getMinIntensity(), testcase.getMaxIntensity()), durationOfVibe, pauseAfterVibe));
                            }
                            Collections.reverse(events);
                            while(!events.isEmpty()) {
                                sendEvent(events.get(0));
                                events.remove(0);
                                Thread.sleep(durationOfVibe+pauseAfterVibe+1L);
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
