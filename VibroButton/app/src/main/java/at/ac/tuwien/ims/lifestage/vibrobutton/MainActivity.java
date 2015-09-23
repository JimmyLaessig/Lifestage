package at.ac.tuwien.ims.lifestage.vibrobutton;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import at.ac.tuwien.ims.lifestage.vibrobutton.Util.WifiUtil;

/**
 * Starts the ButtonService.
 * <p/>
 * Application: Vibro
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class MainActivity extends Activity {
    public static Context context;
    private SparkManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        startService(new Intent(this, ButtonService.class));
        connect();

        ButtonService.setButtonPressed(false); //todo get this from spark
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
        //todo read id and token from file?
        connectionManager =new SparkManager("48ff71065067555011472387", "20e1ee31e3f0b0ecace2820c73ab71f5966acbf6");

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
}
