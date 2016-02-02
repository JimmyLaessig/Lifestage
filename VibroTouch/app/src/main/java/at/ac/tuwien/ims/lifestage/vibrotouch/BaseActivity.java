package at.ac.tuwien.ims.lifestage.vibrotouch;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
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
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.UserPreferences;
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
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progress = new ProgressDialog(this);
        progress.setTitle(getString(R.string.connecting));
        progress.setMessage(getString(R.string.wait));

        testcases=new ArrayList<>();
        connectionManager=SparkManager.getInstance();

        if(verifyStoragePermissions(this)) {
            updateTestcases();
            if(connectionManager.getStatus()!=SparkManager.CONNECTED) {
                try {
                    connectionManager.setIDandToken(XmlHelper.getIDandToken(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + XmlHelper.inputXMLPath));
                    connect();
                } catch (Exception e) {
                    Toast.makeText(BaseActivity.this, getString(R.string.xml_correct), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    protected void updateTestcases() {
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

    private class ConnectTask extends AsyncTask<String, Void, Boolean> {
        private boolean running=false;
        private long time=0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.show();
            running=true;
            time = System.currentTimeMillis();
        }

        protected Boolean doInBackground(String... params) {
            connectionManager.connectToCore(params[0]);
            boolean result=false;
            while (running) {
                if (System.currentTimeMillis() - time >= 100) {
                    final int status=connectionManager.getStatus();
                    if(status == SparkManager.CONNECTED) {
                        result=true;
                        running=false;
                    } else if (status == SparkManager.NOT_CONNECTED) {
                        result=false;
                        running=false;
                    }
                    time = System.currentTimeMillis();
                }
            }
            return result;
        }

        protected void onPostExecute(Boolean result) {
            progress.hide();
            if(!result)
                Toast.makeText(BaseActivity.this, "Couldn't connect to core...", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(BaseActivity.this, "Successfully connected to Core.", Toast.LENGTH_SHORT).show();
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
        String ip;
        if(!WifiUtil.localIPAdress.isEmpty()) {
            ip = WifiUtil.localIPAdress;
            Log.d(getClass().getName(), "using saved IP.");
        } else {
            ip = WifiUtil.getIPAddress();
            Log.d(getClass().getName(), "getting new IP.");
        }
        if (ip.equals("")) {
            Log.d(getClass().getName(), "invalid IP");
            return;
        }
        if (connectionManager.getStatus() == SparkManager.NOT_CONNECTED) {
            new ConnectTask().execute(ip);
            Log.d(getClass().getName(), "connecting");
        }
    }

    /**
     * Disconnects the device with the SparkCore over Wifi.
     *
     */
    public void disconnect() {
        Log.d(getClass().getName(), "disconnect called");
        if(connectionManager!=null) {
            connectionManager.disconnect();
            Toast.makeText(BaseActivity.this, "Disconnected Core.", Toast.LENGTH_SHORT).show();
        }
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
    public static boolean verifyStoragePermissions(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        final boolean granted = permission == PackageManager.PERMISSION_GRANTED;
        if (!granted) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        return granted;
    }
}
