package at.ac.tuwien.ims.lifestage.vibro;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.unity3d.player.UnityPlayerActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    private final static String filename="SparkAuth.txt";
    public static Context context;
    private SparkManager connectionManager;
    private OrientationTracker orientationTracker;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        context = this;
        orientationTracker=new OrientationTracker(this);

        File file = new File(Environment.getExternalStorageDirectory(), filename);
        String id="";
        String token="";
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                if(line.startsWith("id"))
                    id=line.substring(line.indexOf('=')+1);
                if(line.startsWith("token"))
                    token=line.substring(line.indexOf('=')+1);
            }
            br.close();
        } catch (IOException e) {}

        if(!id.equals("") && !token.equals(""))
            connectionManager=new SparkManager(id, token);
        else
            Log.d(getClass().getName(), "token and id can not be read from file");

        Log.d(getClass().getName(), "class created");
    }

    /** TODO
     * Returns the state of the button that is connected with the spark core.
     *
     * @return true if button is pressed else false
     */
    public boolean getButtonState() {
        return false;
    }

    /**
     * Returns the orientation matrix of the smartphone.
     *
     * @return the orientation matrix as float array
     */
    public float[] getOrientationMatrix() {
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
    public void connect() {
        if (!WifiUtil.isOnline(this)) {
            Log.d(getClass().getName(), "no Internet connection.. ");
            return;
        }
        String ip = WifiUtil.getIPAddress();
        if (ip.equals("")) {
            Log.d(getClass().getName(), "invalid IP");
            return;
        }

        if (connectionManager.getStatus() == SparkManager.NOT_CONNECTED) {
            connectionManager.connectToCore(ip);
            Log.d(getClass().getName(), "connecting");
        } else if (connectionManager.getStatus() == SparkManager.CONNECTING) {
            connectionManager.disconnect();
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
