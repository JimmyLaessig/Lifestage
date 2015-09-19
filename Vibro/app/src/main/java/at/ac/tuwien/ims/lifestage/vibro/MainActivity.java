package at.ac.tuwien.ims.lifestage.vibro;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import at.ac.tuwien.ims.lifestage.vibro.Entity.Event;
import at.ac.tuwien.ims.lifestage.vibro.Entity.Pattern;
import at.ac.tuwien.ims.lifestage.vibro.Util.WifiUtil;

/**
 * TODO Needs to be changed depending on how communication works from Unity to Android.
 * <p/>
 * Application: Vibro
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class MainActivity extends Activity {
    private SparkManager sparkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        disconnect();
    }
    @Override
    protected void onResume() {
        super.onResume();
        connect();
    }

    /**
     * Returns the status between the device and the SparkCore.
     *
     * CONNECTED = 1;
     * CONNECTING = 2;
     * NOT_CONNECTED = 3;
     */
    public int getStatus() {
        return sparkManager.getStatus();
    }

    /**
     * Disconnects the device with the SparkCore over Wifi.
     *
     */
    public void disconnect() {
        ((TextView)findViewById(R.id.textView)).setText("not connected");
        sparkManager.disconnect();
    }

    /**
     * Connects the device with the SparkCore over Wifi.
     *
     */
    public void connect() {
        //todo read from file?
        sparkManager=new SparkManager("48ff71065067555011472387", "20e1ee31e3f0b0ecace2820c73ab71f5966acbf6");

        if (sparkManager.getStatus() == SparkManager.NOT_CONNECTED) {
            if (!WifiUtil.isOnline(this)) {
                Log.d(getClass().getName(), "no Internet connection.. ");
                return;
            }
            String ip = WifiUtil.getIPAddress();
            if (ip.equals("")) {
                Log.d(getClass().getName(), "invalid IP");
                return;
            }
            sparkManager.connectToCore(ip);
            ((TextView)findViewById(R.id.textView)).setText("connected");
        } else if (sparkManager.getStatus() == SparkManager.CONNECTED) {
            sparkManager.disconnect();
            ((TextView)findViewById(R.id.textView)).setText("not connected");
            Log.d(getClass().getName(), "canceled connecting");
        } else if (sparkManager.getStatus() == SparkManager.CONNECTING) {
            sparkManager.disconnect();
            ((TextView)findViewById(R.id.textView)).setText("not connected");
            Log.d(getClass().getName(), "canceled connecting");
        }
    }

    /**
     * Sends pattern to Spark Core.
     *
     * @return true if pattern was sent else false
     */
    public boolean send() {
        //todo get pattern data from Unity
        Pattern p=new Pattern(1,0,true,2);
        p.addEvent(new Event(0, 0, 100, 1750, 250));
        p.addEvent(new Event(1, 100, 0, 1750, 250));

        Event e;
        String command="";
        if(p.active){
            Log.e("active: ", ""+p.ID);
            command+="_";
            command+=""+p.eventCount+"_";
            for(int j=0;j<p.eventCount;j++){
                e=p.eventList.get(j);
                command+=e.acId;
                command+="_"+e.intensity;
                command+="_"+e.targetIntensity;
                command+="_"+e.duration;
                command+="_"+e.pauseAfter;
                command+="_";
            }
            command+=p.repeat;
        }
        command+="_";
        Log.e("command", command);
        if(p.active){
            sparkManager.sendCommand_executePattern(command);
            Log.d(getClass().getName(), "Pattern: " + p.ID);
            return true;
        }
        return false;
    }
}
