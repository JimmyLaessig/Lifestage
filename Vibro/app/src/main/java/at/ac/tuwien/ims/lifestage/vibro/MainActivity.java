package at.ac.tuwien.ims.lifestage.vibro;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;

import com.unity3d.player.UnityPlayerActivity;

import java.util.ArrayList;
import java.util.TimerTask;

import at.ac.tuwien.ims.lifestage.vibro.Entity.Event;
import at.ac.tuwien.ims.lifestage.vibro.Entity.Pattern;
import at.ac.tuwien.ims.lifestage.vibro.Util.WifiUtil;

/**
 * Methods used by Unity.
 * <p/>
 * Application: Vibro
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class MainActivity extends UnityPlayerActivity {
    public static Context context;
    public static String id="48ff71065067555011472387";
    public static String token="20e1ee31e3f0b0ecace2820c73ab71f5966acbf6";

    private SparkManager connectionManager;
    private OrientationTracker orientationTracker;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle bundle) {
        Log.d(getClass().getName(), "oncreate called");
        super.onCreate(bundle);
        context = this;
        orientationTracker=new OrientationTracker(this);
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    protected void onDestroy () {
        disconnect();
        super.onDestroy();
    }

    /**
     * Returns the state of the button that is connected with the spark core.
     *
     * @return true if button is pressed else false
     */
    public boolean getButtonState() {
        if(connectionManager==null)
            return false;
        return connectionManager.getButtonState();
    }

    /**
     * Returns the orientation matrix of the smartphone.
     *
     * @return the orientation matrix as float array
     */
    public float[] getOrientationMatrix() {
        if(orientationTracker==null)
            return null;
        return orientationTracker.getOrientationMatrix();
    }

    /**
     * Returns the status between the device and the SparkCore.
     *
     * CONNECTED = 1;
     * CONNECTING = 2;
     * NOT_CONNECTED = 3;
     */
    public int getConnectionState() {
        Log.d(getClass().getName(), "getConnectionState called");
        if(connectionManager ==null)
            return 3;
        return connectionManager.getStatus();
    }

    /**
     * Disconnects the device with the SparkCore over Wifi.
     *
     */
    public void disconnect() {
        Log.d(getClass().getName(), "disconnect called");
        if(connectionManager !=null)
            if(getConnectionState() != 3)
                connectionManager.disconnect();
    }

    /**
     * Connects the device with the SparkCore over Wifi.
     *
     */
    public void connect(String id, String token) {
        Log.d(getClass().getName(), "connect called");

        if(id.equals("") || token.equals("")) {
            Log.d(getClass().getName(), "invalid id or token");
            return;
        }
        if (!WifiUtil.isOnline(this)) {
            Log.d(getClass().getName(), "no Internet connection.. ");
            return;
        }
        String ip = WifiUtil.getIPAddress();
        if (ip.equals("")) {
            Log.d(getClass().getName(), "invalid IP");
            return;
        }
        connectionManager=new SparkManager(id, token);

        if (connectionManager.getStatus() == SparkManager.CONNECTING) {
            connectionManager.disconnect();
            connectionManager.connectToCore(ip);
            Log.d(getClass().getName(), "connecting");
        } else {
            connectionManager.connectToCore(ip);
            Log.d(getClass().getName(), "connecting");
        }
    }

    /**
     * Sends a command to Spark Core.
     *
     * @param command command string to send
     * @return true if command was sent else false
     */
    public boolean sendCommand(String command) {
        if(connectionManager==null || command.isEmpty()) {
            Log.e(getClass().getName(), "sendevent failed");
            return false;
        }
        if(getConnectionState() != 1) {
            Log.e(getClass().getName(), "sendevent failed, not connected");
            return false;
        }

        Log.d(getClass().getName(), "sent command: " + command);
        connectionManager.sendCommand_executePattern(command);
        return true;
    }

    /**
     * Sends an event to Spark Core.
     *
     * @return true if event was sent else false
     */
    public boolean sendEvent(int acId, int intensity, int targetIntensity, int duration, int pauseAfter) {
        Log.d(getClass().getName(), "sendevent called with parameters: " +acId+", "+intensity+", "+targetIntensity+", "+duration+", "+ pauseAfter);
        if(connectionManager==null || acId<0 || intensity<0 || targetIntensity<0 || duration<0 || pauseAfter<0) {
            Log.e(getClass().getName(), "sendevent failed");
            return false;
        }
        if(getConnectionState()!=1) {
            Log.e(getClass().getName(), "sendevent failed, not connected");
            return false;
        }

        String command="";
        command+="_1";
        command+="_"+acId;
        command+="_"+intensity;
        command+="_"+targetIntensity;
        command+="_"+duration;
        command+="_"+pauseAfter;
        command+="_";
        command+="1_";

        Log.d(getClass().getName(), "sent event command: " + command);
        connectionManager.sendCommand_executePattern(command);
        return true;
    }

    /**
     * Sends an event to Spark Core.
     *
     * @return true if event was sent else false
     */
    public boolean sendEvent(Event e) {
        Log.d(getClass().getName(), "sendevent called");

        if(connectionManager==null || e==null)
            return false;

        if(getConnectionState()!=1) {
            Log.e(getClass().getName(), "sendevent failed, not connected");
            return false;
        }

        String command="";
        command+="_1";
        command+="_"+e.acId;
        command+="_"+e.intensity;
        command+="_"+e.targetIntensity;
        command+="_"+e.duration;
        command+="_"+e.pauseAfter;
        command+="_";
        command+="1_";
        Log.d(getClass().getName(), "sent event command: " + command);

        connectionManager.sendCommand_executePattern(command);
        return true;
    }

    /**
     * Sends a pattern to Spark Core.
     *
     * @return true if pattern was sent else false
     */
    public boolean sendPattern(Pattern pattern) {
        Log.d(getClass().getName(), "sendpattern called");

        if(connectionManager==null || pattern==null)
            return false;

        if(getConnectionState()!=1) {
            Log.e(getClass().getName(), "sendpattern failed, not connected");
            return false;
        }

        String command="";
        command+="_";
        command+=""+pattern.eventList.size()+"_";
        Event e;
        for(int j=0; j<pattern.eventList.size(); j++) {
            e=pattern.eventList.get(j);
            command+=e.acId;
            command+="_"+e.intensity;
            command+="_"+e.targetIntensity;
            command+="_"+e.duration;
            command+="_"+e.pauseAfter;
            command+="_";
        }
        command+=pattern.repeat;
        command+="_";
        Log.d(getClass().getName(), "sent pattern command: " + command);

        connectionManager.sendCommand_executePattern(command);
        return true;
    }

    /**
     * Sends a test pattern to Spark Core.
     *
     */
    public void sendTestPattern() {
        Log.d(getClass().getName(), "send test pattern called");
        if(connectionManager ==null)
            return;

        ArrayList<Event> events=new ArrayList<>();
        events.add(new Event(0, 0, 100, 1750, 250));
        events.add(new Event(1, 100, 0, 1750, 250));
        Pattern p=new Pattern(0, events);

        String command="";
        command+="_";
        command+=""+p.eventList.size()+"_";
        Event e;
        for(int j=0; j<p.eventList.size(); j++) {
            e=p.eventList.get(j);
            command+=e.acId;
            command+="_"+e.intensity;
            command+="_"+e.targetIntensity;
            command+="_"+e.duration;
            command+="_"+e.pauseAfter;
            command+="_";
        }
        command+=p.repeat;
        command+="_";
        Log.d("sent command: ", command);

        connectionManager.sendCommand_executePattern(command);
    }

    /**
     * Checks if device has a vibrator.
     *
     * @return true if device has vibrator
     */
    public boolean hasVibrator() {
        Log.d(getClass().getName(), "hasvibrator called");
        return vibrator.hasVibrator();
    }

    /**
     * Vibrates device for given amount of ms.
     *
     * @param milliseconds time in ms to vibrate
     */
    public void vibrate(long milliseconds) {
        Log.d(getClass().getName(), "vibrate called");
        vibrator.vibrate(milliseconds);
    }

    /**
     * Vibrates the device with a given pattern for a give amount of times.
     *
     * @param pattern pattern that used for vibration
     * @param repeat times that the pattern will be repeated (0=repeat indefinitely, -1=no repeat)
     */
    public void vibrate(long[] pattern, int repeat) {
        Log.d(getClass().getName(), "vibrate called");
        vibrator.vibrate(pattern, repeat);
    }

    /**
     * Cancels current vibration.
     *
     */
    public void cancelVibration() {
        vibrator.cancel();
    }
}
