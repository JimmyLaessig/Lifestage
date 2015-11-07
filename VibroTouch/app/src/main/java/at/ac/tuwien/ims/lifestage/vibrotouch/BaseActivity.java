package at.ac.tuwien.ims.lifestage.vibrotouch;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Testcase;
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.SparkManager;
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.WifiUtil;
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.XmlHelper;

/**
 * BaseActivity that all Activities use.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class BaseActivity extends AppCompatActivity {
    protected SparkManager connectionManager;
    protected ArrayList<Testcase> testcases;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyStoragePermissions(this);

        connectionManager=SparkManager.getInstance();
        try {
            testcases=XmlHelper.getTestcases(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + XmlHelper.inputXMLPath);
        } catch (Exception e) {
            Toast.makeText(BaseActivity.this, "Please make sure your XML Files are correct.", Toast.LENGTH_SHORT).show();
        }
        if(testcases==null || testcases.isEmpty()) {
            Toast.makeText(BaseActivity.this, "Please add Testcases in the XML file.", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    //============================================================================================//
    // SparkCore Methods
    //============================================================================================//

    /**
     * Connects the device with the SparkCore over Wifi.
     *
     */
    public void connect() {
        Log.d(getClass().getName(), "connect called");

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
        }
    }

    /**
     * Disconnects the device with the SparkCore over Wifi.
     *
     */
    public void disconnect() {
        Log.d(getClass().getName(), "disconnect called");
        if(connectionManager!=null)
            connectionManager.disconnect();
    }

    //============================================================================================//
    // Permissions
    //============================================================================================//

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}