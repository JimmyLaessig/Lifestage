package at.ac.tuwien.ims.lifestage.vibrotouch;

import android.content.Intent;
import android.content.res.ColorStateList;
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
    private SparkManager connectionManager;

    private FloatingActionButton fab;
    private Button button1, button2, button3, button4;
    private TextView text_connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        connectionManager=SparkManager.getInstance();

        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#004063")));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connectionManager.getStatus() == 3) {
                    connect();
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
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
        } else {
            connectionManager.disconnect();
            connectionManager.connectToCore(ip);
            Log.d(getClass().getName(), "connecting");
        }
        fab.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.wifi_off));
        text_connection.setText(getString(R.string.text_connected));
        text_connection.setTextColor(Color.parseColor("#719D98"));
    }

    /**
     * Disconnects the device with the SparkCore over Wifi.
     *
     */
    public void disconnect() {
        Log.d(getClass().getName(), "disconnect called");
        if(connectionManager!=null)
            if(connectionManager.getStatus() != 3) {
                connectionManager.disconnect();
                fab.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.wifi_on));
                text_connection.setText(getString(R.string.text_not_connected));
                text_connection.setTextColor(Color.parseColor("#BD4141"));
            }
    }
}
