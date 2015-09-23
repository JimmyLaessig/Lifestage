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
    private SparkManager connectionManager;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        context = this;
    }

    /**
     * Returns the status between the device and the SparkCore.
     *
     * CONNECTED = 1;
     * CONNECTING = 2;
     * NOT_CONNECTED = 3;
     */
    public int getStatus() {
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
     * @return true if connecting else false
     */
    public boolean connect() {
        initSpark();

        if (!WifiUtil.isOnline(this)) {
            Log.d(getClass().getName(), "no Internet connection.. ");
            return false;
        }
        String ip = WifiUtil.getIPAddress();
        if (ip.equals("")) {
            Log.d(getClass().getName(), "invalid IP");
            return false;
        }

        if (connectionManager.getStatus() == SparkManager.NOT_CONNECTED) {
            connectionManager.connectToCore(ip);
            Log.d(getClass().getName(), "connecting");
        } else if (connectionManager.getStatus() == SparkManager.CONNECTING) {
            connectionManager.disconnect();
            connectionManager.connectToCore(ip);
        }
        return true;
    }

    private void initSpark() {
        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard,"SparkAuth.txt");
        String id="";
        String token="";

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                if(line.startsWith("id")) {
                    id=line.substring(line.indexOf('=')+1);
                }
                if(line.startsWith("token")) {
                    token=line.substring(line.indexOf('=')+1);
                }
            }
            br.close();
        } catch (IOException e) {}

        if(!id.equals("") && !token.equals(""))
            connectionManager=new SparkManager(id, token);
        //connectionManager=new SparkManager("48ff71065067555011472387", "20e1ee31e3f0b0ecace2820c73ab71f5966acbf6");
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
        Pattern p=new Pattern(1,0,true,2);
        p.addEvent(new Event(0, 0, 100, 1750, 250));
        p.addEvent(new Event(1, 100, 0, 1750, 250));

        Event e;
        String command="";

        if(p.active) {
            Log.e("active: ", ""+p.ID);
            command+="_";
            command+=""+p.eventCount+"_";
            for(int j=0;j<p.eventCount;j++) {
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

            Log.e("command", command);

            connectionManager.sendCommand_executePattern(command);
            Log.d(getClass().getName(), "Pattern: " + p.ID);
            return true;
        }
        return false;
    }
}
