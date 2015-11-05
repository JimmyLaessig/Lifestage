package at.ac.tuwien.ims.lifestage.vibrotouch.Scenario;

import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Event;
import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Object;
import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Testcase;
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.SparkManager;

/**
 * ObjectHandler containing objects that are drawn and interacted with.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class ObjectHandler {
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

    private List<Object> allObjects;
    private Stack<Object> pickedUpObjects;
    private SparkManager connectionManager;

    private UsedVibro usedVibro;

    private long pauseBetweenVibes=1000L;
    private long pauseAfterVibe=400L;
    private long durationOfVibe=100L;

    private volatile boolean vibroThreadRunning = true;
    private volatile boolean buttonThreadRunning = true;

    private float minWidth, maxWidth, minHeight, maxHeight;

    public ObjectHandler(Testcase testcase) {
        allObjects =new ArrayList<>();
        pickedUpObjects =new Stack<>();
        connectionManager=SparkManager.getInstance();
        usedVibro=UsedVibro.Vibro0;

        //TODO testobects spawnable => xml getScreenWidthAndHeight()
        minWidth=150;
        maxWidth=250;
        minHeight=150;
        maxHeight=250;
        allObjects.add(new Object(700, 200, 250, Color.RED, minWidth, maxWidth, minHeight, maxHeight));
        allObjects.add(new Object(450, 200, 200, Color.GREEN, minWidth, maxWidth, minHeight, maxHeight));
        allObjects.add(new Object(150, 200, 150, Color.BLUE, minWidth, maxWidth, minHeight, maxHeight));

        buttonThread();
        vibroThread();
        Log.d(getClass().getName(), "begin handling objects");
    }

    public void draw(Canvas canvas) {
        for(Object object : allObjects)
            if(object.getObjectState()== ObjectState.OnScreen)
                object.draw(canvas);
    }

    public void handleScale(float scaleFactor, float xFocus, float yFocus) {
        if (scaleFactor < 1.0f) {
            for (Object object : allObjects)
                if (object.getObjectState()==ObjectState.OnScreen && object.contains(xFocus, yFocus)) {
                    pickUpObject(object);
                }
        } else if (scaleFactor > 1.0f) {
            if(!pickedUpObjects.isEmpty())
                layDownObject(xFocus, yFocus);
        }
    }

    public void handleMove(float x, float y, float dx, float dy) {
        for(Object object : allObjects)
            if (object.getObjectState()==ObjectState.OnScreen && object.contains(x, y))
                object.move(dx, dy);
    }

    private void layDownObject(float x, float y) {
        Object picked=pickedUpObjects.pop();

        for (Object object : allObjects)
            if(picked.equals(object)) {
                picked.setX(x - (picked.getWidth() / 2));
                picked.setY(y - (picked.getHeight() / 2));
                object.changeObjectState();
            }
    }

    private void pickUpObject(Object object) {
        object.changeObjectState();
        pickedUpObjects.push(object);
    }

    private void vibroThread() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (vibroThreadRunning) {
                    try {
                        if (!pickedUpObjects.isEmpty()) {
                            ArrayList<Event> events = new ArrayList<>();
                            for (Object o : pickedUpObjects) {
                                events.add(new Event(usedVibro.getID(), 0, o.getIntensity(), durationOfVibe, pauseAfterVibe));
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

    private void buttonThread() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (buttonThreadRunning) {
                    try {
                        //TODO test
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
