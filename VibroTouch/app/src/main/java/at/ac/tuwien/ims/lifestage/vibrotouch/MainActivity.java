package at.ac.tuwien.ims.lifestage.vibrotouch;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import at.ac.tuwien.ims.lifestage.vibrotouch.Util.SparkManager;
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.WifiUtil;

/**
 * Main Activity handling connection to spark core.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class MainActivity extends AppCompatActivity {
    public static String id="48ff71065067555011472387";
    public static String token="20e1ee31e3f0b0ecace2820c73ab71f5966acbf6";

    private SparkManager connectionManager;
    private FloatingActionButton fab;
    private Button button1, button2, button3, button4;
    private TextView text_connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getConnectionState() == 3) {
                    connect(id, token);
                } else {
                    disconnect();
                }
            }
        });

        button1=(Button)findViewById(R.id.button_1);
        button1.setOnClickListener(btnListener);
        button2=(Button)findViewById(R.id.button_2);
        button2.setOnClickListener(btnListener);
        button3=(Button)findViewById(R.id.button_3);
        button3.setOnClickListener(btnListener);
        button4=(Button)findViewById(R.id.button_4);
        button4.setOnClickListener(btnListener);

        text_connection=(TextView)findViewById(R.id.text_connection);
    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;
            if(v==button1) {
                intent=new Intent(MainActivity.this, ScenarioActivity.class);
                startActivity(intent);
            } else if(v==button2) {
                //TODO
            } else if(v==button3) {
                //TODO
            } else if(v==button4) {
                //TODO
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //TODO do we need settings?
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy () {
        disconnect();
        super.onDestroy();
    }

    //============================================================================================//
    // SparkCore Methods
    //============================================================================================//

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
            if(getConnectionState() != 3) {
                connectionManager.disconnect();
                fab.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.wifi_on));
                text_connection.setText(getString(R.string.text_not_connected));
                text_connection.setTextColor(Color.parseColor("#BD4141"));
            }
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
        fab.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.wifi_off));
        text_connection.setText(getString(R.string.text_connected));
        text_connection.setTextColor(Color.parseColor("#719D98"));
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
}
