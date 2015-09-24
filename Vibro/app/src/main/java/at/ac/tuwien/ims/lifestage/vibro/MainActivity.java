package at.ac.tuwien.ims.lifestage.vibro;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.unity3d.player.UnityPlayerActivity;

import java.util.ArrayList;

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

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        context = this;
        orientationTracker=new OrientationTracker(this);
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
        if(connectionManager==null)
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
        if(connectionManager ==null)
            return 3;
        return connectionManager.getStatus();
    }

    /**
     * Disconnects the device with the SparkCore over Wifi.
     *
     */
    public void disconnect() {
        if(connectionManager !=null)
            connectionManager.disconnect();
    }

    /**
     * Connects the device with the SparkCore over Wifi.
     *
     */
    public void connect(String id, String token) {
        if(!id.equals("") && !token.equals("")) {
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
     * Sends pattern to Spark Core.
     *
     * @return true if pattern was sent else false
     */
    public boolean send() {
        if(connectionManager ==null)
            return false;

        //todo get pattern data from Unity
        ArrayList<Event> events=new ArrayList<>();
        events.add(new Event(0, 0, 100, 1750, 250));
        events.add(new Event(1, 100, 0, 1750, 250));
        Pattern p=new Pattern(1, 0, events);

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
        return true;
    }
}
