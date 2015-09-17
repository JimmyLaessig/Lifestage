package at.ac.tuwien.mmi.gyropowerball;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class SampleActivity1 extends Activity{

    private AnimationView animationView;
    private Button btnStart, btnConnect;
    public SparkManager sparkManager;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_sample1);
	
	context = this;
	btnStart = (Button)findViewById(R.id.btnStartSample1);

	Display display = getWindowManager().getDefaultDisplay();
	Point size = new Point();
	display.getSize(size);

	animationView = (AnimationView)findViewById(R.id.animationView1);
	
	btnConnect = (Button)findViewById(R.id.btnConnectSample);
	btnConnect.setOnClickListener(new OnClickListener() {
	    
	    @Override
	    public void onClick(View v) {
		// TODO Auto-generated method stub
		if (sparkManager.getStatus() == SparkManager.NOT_CONNECTED) {
			if (!WifiUtil.isOnline(context)) {
			    Toast.makeText(context, "No Internet connection..", Toast.LENGTH_SHORT)
				    .show();
			    return;
			}
			String ip = WifiUtil.getIPAddress();
			if (ip == "") {
			    Toast.makeText(context, "invalid IP", Toast.LENGTH_SHORT)
			    .show();
			    return;
			}
			sparkManager.connectToCore(ip);

		    } else if (sparkManager.getStatus() == SparkManager.CONNECTED) {
			sparkManager.disconnect();
		    } else if (sparkManager.getStatus() == SparkManager.CONNECTING) {
			sparkManager.disconnect();
		    }
	    }
	});
	btnStart.setOnClickListener(new OnClickListener() {    
	    @Override
	    public void onClick(View v) {
		if (sparkManager.getStatus() == SparkManager.CONNECTED) {
		    animationView.start();
		} else{
		    Toast.makeText(getApplicationContext(), "not connected to phone", Toast.LENGTH_SHORT).show();  
		}
	    }
	});
	
	sparkManager = new SparkManager(this);
	animationView.setSparkManager(sparkManager);
	
	updateUI();
    }
    
    @Override
    protected void onStop() {
	super.onStop();
	sparkManager.disconnect();
    }
    
    public void updateUI() {
	runOnUiThread(new Runnable() {
	    @Override
	    public void run() {
		if (sparkManager.getStatus() == SparkManager.CONNECTED) {    
		    btnConnect.setText("disc.");
		    btnStart.setEnabled(true);
		} else if (sparkManager.getStatus() == SparkManager.CONNECTING) {
		    btnConnect.setText("canc.");
		    btnStart.setEnabled(false);
		} else if (sparkManager.getStatus() == SparkManager.NOT_CONNECTED) {
		    btnConnect.setText("conn.");
		    btnStart.setEnabled(false);
		}
	    }
	});
    }
}
