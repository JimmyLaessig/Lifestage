package at.ac.tuwien.mmi.gyropowerball;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

    private TextView txtvLog, txtvStatus;
    private Button btnConnect, btnRight, btnLeft, btnNext;
    private OnClickListener clickListener;
    public OnCheckedChangeListener chkListener;

    public ScrollView patternLayout;
    
    private Context context;
    public SparkManager sparkManager;
    private String log_text;
    //private SeekBar seekBar_position, seekBar_speed;

    private XmlLoader xmlLoader;
    public boolean[] activePatterns;
    public static ArrayList<Pattern> patterns;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		context = MainActivity.this;
	
		txtvLog = (TextView) findViewById(R.id.txtvLog);
		txtvStatus = (TextView) findViewById(R.id.txtvStatus);
		btnConnect = (Button) findViewById(R.id.btnConnect);
		btnLeft = (Button) findViewById(R.id.btnLeft);
		btnRight = (Button) findViewById(R.id.btnRight);
		btnNext = (Button) findViewById(R.id.btnNext);
		//seekBar_speed = (SeekBar) findViewById(R.id.seekBar_speed);
		//seekBar_position = (SeekBar) findViewById(R.id.seekBar);
		log_text = "";
	
		//load xml
		xmlLoader = new XmlLoader(this.getResources().getXml(R.xml.patterns));
		
		sparkManager = new SparkManager(this);
		sparkManager.DEVICE_ID=xmlLoader.devID;
		sparkManager.ACCESTOKEN=xmlLoader.accToken;
		initUI();
		
	

		
		
		
		activePatterns = new boolean[xmlLoader.patternCount];
		for(int i=0;i<xmlLoader.patternCount;i++) activePatterns[i]=false;
		
		patternLayout = createPatternLayout(this);
    }

    
    @Override
    protected void onStop() {
	super.onStop();
	sparkManager.disconnect();
    }

    private void initUI() {
    	
    chkListener = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			int a = (Integer)buttonView.getTag();
			//Log.e("main", "CheckboxTag: " + a + ", tag: " + buttonView.getTag());
			//activePatterns[a]=isChecked;
			patterns.get(a-1).active=isChecked;
		}
	};
    	
	clickListener = new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		if (v.getId() == R.id.btnConnect) {
		    if (sparkManager.getStatus() == SparkManager.NOT_CONNECTED) {
			if (!WifiUtil.isOnline(context)) {
			    Toast.makeText(context, "No Internet connection..", Toast.LENGTH_SHORT)
				    .show();
			    addToLog("no Internet connection.. ");
			    return;
			}
			String ip = WifiUtil.getIPAddress();
			if (ip == "") {
			    addToLog("invalid IP");
			    return;
			}
			sparkManager.connectToCore(ip);

		    } else if (sparkManager.getStatus() == SparkManager.CONNECTED) {
			sparkManager.disconnect();
		    } else if (sparkManager.getStatus() == SparkManager.CONNECTING) {
			sparkManager.disconnect();
			addToLog("canceled connecting");
		    } 
		} else if (v.getId() == R.id.btnLeft) {
			//command: anzPatterns_anzEvents_acId.intensity.targetIntensity.duration.pauseAfter_nextEvent_repeat_anzEvents_acId...
			
			Pattern p=new Pattern(0,0,false,0);
			Event e;
			String command="";
			int anzEvents=0;
			for(int i=0;i<patterns.size();i++){
				p=patterns.get(i);
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
						anzEvents++;
					}
					command+=p.repeat;
				i=patterns.size()+1;	
				}
			}
			
			command+="_";
			Log.e("command", command);
			if(p.active){
				sparkManager.sendCommand_executePattern(command);
				addToLog("Pattern: "+p.ID);
			}
		} else if (v.getId() == R.id.btnRight) {
			patternLayout = createPatternLayout(context);
			setContentView(patternLayout);
			
			
		    //sparkManager.sendCommand_RightRotation();
		} else if(v.getTag() == "btnOKpattern"){
			
			setContentView(R.layout.activity_main);
			context = MainActivity.this;
			
			txtvLog = (TextView) findViewById(R.id.txtvLog);
			txtvStatus = (TextView) findViewById(R.id.txtvStatus);
			btnConnect = (Button) findViewById(R.id.btnConnect);
			btnLeft = (Button) findViewById(R.id.btnLeft);
			btnRight = (Button) findViewById(R.id.btnRight);
			btnNext = (Button) findViewById(R.id.btnNext);
			
			
			log_text = "";
			initUI();
		}else if(v.getId()==R.id.btnNext){
			Pattern p=new Pattern(0,0,false,0);
			Event e;
			String command="";
			int anzEvents=0;
			for(int i=0;i<patterns.size();i++){
				p=patterns.get(i);
				if(p.active && (i+1)<patterns.size()){
					p.active=false;
					p=patterns.get(i+1);
					p.active=true;
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
						anzEvents++;
					}
					command+=p.repeat;
				i=patterns.size()+1;	
				}
			}
			
			command+="_";
			Log.e("command", command);
			
			sparkManager.sendCommand_executePattern(command);
			addToLog("Pattern: "+p.ID);
		}
	    }
	};


	btnLeft.setEnabled(true);
	btnRight.setEnabled(true);

	btnRight.setOnClickListener(clickListener);
	btnLeft.setOnClickListener(clickListener);
	btnConnect.setOnClickListener(clickListener);
	btnNext.setOnClickListener(clickListener);

	txtvLog.setMovementMethod(new ScrollingMovementMethod());

	txtvLog.setOnLongClickListener(new OnLongClickListener() {

	    @Override
	    public boolean onLongClick(View v) {
		clearLog();
		return false;
	    }
	});
/**
	seekBar_position.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
	    @Override
	    public void onStopTrackingTouch(SeekBar seekBar) {
		sparkManager.sendCommand_SetServoPosition(seekBar.getProgress() + 10);
	    }

	    @Override
	    public void onStartTrackingTouch(SeekBar seekBar) {
	    }

	    @Override
	    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	    }
	});

	seekBar_speed.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
	    @Override
	    public void onStopTrackingTouch(SeekBar seekBar) {
		sparkManager.setSpeed(seekBar.getProgress());
		addToLog("selected speed: " + seekBar.getProgress());
	    }

	    @Override
	    public void onStartTrackingTouch(SeekBar seekBar) {
	    }

	    @Override
	    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	    }
	});
/**/
	updateUI();
	addToLog("UI initialised");
    }

    public void addToLog(String text) {
	Date now = new Date();
	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	String formattedTime = sdf.format(now);

	log_text = (formattedTime + ":   " + text + "\r\n") + log_text;

	String[] splitted_log_text = log_text.split("\r\n");

	//delete old log entries
	if (splitted_log_text.length > 150) {
	    log_text = "";
	    int counter = 0;
	    for (String s : splitted_log_text) {
		if (counter < 30) {
		    log_text += s + "\r\n";
		} else {
		    break;
		}
		counter++;
	    }

	}
	runOnUiThread(new Runnable() {
	    @Override
	    public void run() {
		txtvLog.setText(log_text);
	    }
	});

    }

    private void clearLog() {
	log_text = "";
	txtvLog.setText(log_text);
	Toast.makeText(context, "Log cleared!", Toast.LENGTH_SHORT).show();
    }

    public void updateUI() {
	runOnUiThread(new Runnable() {
	    @Override
	    public void run() {
		//seekBar_speed.setProgress(sparkManager.getSpeed());
		if (sparkManager.getStatus() == SparkManager.CONNECTED) {
		    txtvStatus.setText("connected");
		    btnConnect.setText("disconnect");
		    btnLeft.setEnabled(true);
		    btnRight.setEnabled(true);
		   // seekBar_position.setEnabled(true);
		    //seekBar_speed.setEnabled(true);
		    
		} else if (sparkManager.getStatus() == SparkManager.CONNECTING) {
		    txtvStatus.setText("connecting..");
		    btnConnect.setText("cancel");
		    //btnLeft.setEnabled(false);
		    //btnRight.setEnabled(false);
		 //   seekBar_position.setEnabled(false);
		 //   seekBar_speed.setEnabled(false);

		} else if (sparkManager.getStatus() == SparkManager.NOT_CONNECTED) {
		    txtvStatus.setText("not connected");
		    btnConnect.setText("connect");
		    //btnLeft.setEnabled(false);
		    //btnRight.setEnabled(false);
		//    seekBar_position.setEnabled(false);
		//    seekBar_speed.setEnabled(false);
		}
	    }
	});
    }

    public void updateSeekBar(final int pos) {
	//seekBar_position.setProgress(pos - 10);
    }
    
    @SuppressWarnings("deprecation")
	public ScrollView createPatternLayout(Context context){
    	context = this;
    	
    	ScrollView wrapLayout = new ScrollView(context);
    	
    	TableLayout PatternLayout = new TableLayout(context);
    	PatternLayout.setGravity(android.view.Gravity.RIGHT);
    	PatternLayout.setVerticalScrollBarEnabled(true);
    	PatternLayout.setMinimumWidth(ViewGroup.LayoutParams.FILL_PARENT);
    	PatternLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.FILL_PARENT));
    	PatternLayout.setStretchAllColumns(true);
        TableRow row;
        Button btn;
        CheckBox chk;
        TextView txt;
        String btnText;
        RelativeLayout chkLayout;
    	/**
        int cnt = xmlLoader.patternCount;
        
        for(int i=0;i<cnt;i++){
        	row=new TableRow(context);
        	chk=new CheckBox(context);
        	txt = new TextView(context);
        	txt.setText("Pattern " + xmlLoader.pId[i]);
        	chk.setGravity(android.view.Gravity.RIGHT);
        	chk.setText("");
        	chk.setTag(xmlLoader.pId[i]);
        	//Log.e("im aufbau", "xmlloader.pid: "+xmlLoader.pId[i]);
        	//Log.e("im aufbau", "xmlLoader.patternCount: "+xmlLoader.patternCount);
        	chk.setChecked(activePatterns[xmlLoader.pId[i]-1]);
        	chkLayout = new RelativeLayout(context);
        	chkLayout.setGravity(android.view.Gravity.RIGHT);
        	chkLayout.addView(chk);
        	
        	row.addView(txt);
        	row.addView(chkLayout);
        	
        	PatternLayout.addView(row);
        }
        /**/
     int cnt = patterns.size();
        
        for(int i=0;i<cnt;i++){
        	row=new TableRow(context);
        	chk=new CheckBox(context);
        	txt = new TextView(context);
        	txt.setText("Pattern " + patterns.get(i).ID);
        	chk.setGravity(android.view.Gravity.RIGHT);
        	chk.setText("");
        	chk.setTag(patterns.get(i).ID);
        	chk.setChecked(patterns.get(i).active);
        	chk.setOnCheckedChangeListener(chkListener);
        	//chk.setChecked(patterns.get(i).active);
        	chkLayout = new RelativeLayout(context);
        	chkLayout.setGravity(android.view.Gravity.RIGHT);
        	chkLayout.addView(chk);
        	
        	row.addView(txt);
        	row.addView(chkLayout);
        	
        	PatternLayout.addView(row);
        }
        
        // ADD OK BUTTON
        btn = new Button(context);
        btn.setText("OK");
        btn.setOnClickListener(clickListener);
        btn.setTag("btnOKpattern");
        
        row = new TableRow(context);
        
        row.addView(btn);
        
        PatternLayout.addView(row);
        
        wrapLayout.addView(PatternLayout);
        
        return wrapLayout;    
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	getMenuInflater().inflate(R.menu.main, menu);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	int itemId = item.getItemId();
	if (itemId == R.id.startSample) {
	    startActivity(new Intent(this, SampleActivity1.class));
	}else {
	    return super.onOptionsItemSelected(item);
	}
	return true;
    }
}
