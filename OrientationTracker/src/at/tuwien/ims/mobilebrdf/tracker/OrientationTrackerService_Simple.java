package at.tuwien.ims.mobilebrdf.tracker;
import java.util.Random;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

/* =====================================================================================
 * 3DOF Orientation Tracker
 * ====================================================================================
 * Copyright (c) 2012-2015
 * 
 * Annette Mossel
 * mossel@ims.tuwien.ac.at
 * Interactive Media Systems Group, Vienna University of Technology, Austria
 * www.ims.tuwien.ac.at
 * 
 * Usage information
 *  - Uses TYPE_ROTATION_VECTOR for Sensor Fusion
 *  - It has much more jitter and wrong calculation when translating than the Complementary Filter
 *  - Gyros are only used with Android 4.x and if phone provides it
 * 
 * ====================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ************************************************************************************/

/*

 */
public class OrientationTrackerService_Simple extends Service implements SensorEventListener 
{

	private final static String LOGTAG = "OrientationTrackerService_Simple";
	
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder_Simple();
    
    // Random number generator
    private final Random mGenerator = new Random();

    
	// --------- sensor stuff --------- 
    private SensorManager mSensorManager = null;
    private Sensor mRotationVectorSensor = null;
    private static float[] mRotationMatrix_16 = new float[16];

	/*
	 * SERVICE BINDING AND LIFE CYCLE
	 */

    // Class used for the client Binder.  Because we know this service always
    // runs in the same process as its clients, we don't need to deal with IPC.
    public class LocalBinder_Simple extends Binder 
    {
    	OrientationTrackerService_Simple getService() 
    	{
            // Return this instance of LocalService so clients can call public methods
            return OrientationTrackerService_Simple.this;
        }
    }
    
    
    @Override
    public IBinder onBind(Intent intent) 
    {
    	Toast.makeText(this, "SERVICE BOUND: Simple Filter", Toast.LENGTH_SHORT).show();
    	
        // initialize the rotation matrix to identity
        mRotationMatrix_16[ 0] = 1;
        mRotationMatrix_16[ 4] = 1;
        mRotationMatrix_16[ 8] = 1;
        mRotationMatrix_16[12] = 1;
        
        // Get an instance of the SensorManager
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        // find the rotation-vector sensor
        mRotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        
        // enable our sensor when the activity is bound, ask for 10 ms updates.
        mSensorManager.registerListener(this, mRotationVectorSensor, 10000);

        return mBinder;
    }
    

    public void onDestroy() 
    {
		// make sure to turn our sensor off when the activity is paused
		mSensorManager.unregisterListener(this);
		Toast.makeText(this, "SERVICE DESTROYED: Simple Filter", Toast.LENGTH_SHORT).show();
    }
    
    /*
     * INTER COMPONENT COMMUNICATION
     * Every component of the app (i.e. Activity) can receive this updates
     */
    private void broadcastUpdatedOrientationTracking() 
    {     
    	//Log.d(LOGTAG,"Broadcast sent!");   
    	
		Intent intent = new Intent("orientationTracker_update");
    	sendOrientationTrackerBroadcast(intent);
	}

	private void sendOrientationTrackerBroadcast(Intent intent)
	{
    	//Log.d(LOGTAG,"Broadcast sent!");   
    	
        intent.putExtra("affineRotationMatrix", mRotationMatrix_16);
	    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

    /*
     * SENSOR READOUT 
     */
    public void onSensorChanged(SensorEvent event) 
    {
        // we received a sensor event. 
    	// it is a good practice to check that we received the proper event
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) 
        {
            // convert the rotation-vector to a 4x4 matrix. the matrix
            // is interpreted by Open GL as the inverse of the
            // rotation-vector, which is what we want.
            SensorManager.getRotationMatrixFromVector(mRotationMatrix_16 , event.values);
            
            //OrientationTrackerActivity.updateRotationMatrix(mRotationMatrix_16);
            broadcastUpdatedOrientationTracking();
        }
    }


    public void onAccuracyChanged(Sensor sensor, int accuracy) 
    {
    }
    
	
    /*
     * PUBLIC METHODS - CALLABLE BY CLIENTS
     */
    public int getRandomNumber() 
    {
    	// just a sample
    	// can be called in activity by (OrientationTrackerService_Simple)mTrackerService_simple.getRandomNumber();
    	return mGenerator.nextInt(100);
    }
}

/*
public class OrientationTrackerService extends Service {

	// threading stuff
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;

	// sensor fusion stuff
	private SimpleOrientationTracker mTracker;
    private SensorManager mSensorManager;
    
	// Handler that receives messages from the thread
	private final class ServiceHandler extends Handler {
	  
	      public ServiceHandler(Looper looper) {
	          super(looper);
	      }
	      
	      @Override
	      public void handleMessage(Message msg) {
	          // Normally we would do some work here, like download a file.
	          // For our sample, we just sleep for 5 seconds.
	          long endTime = System.currentTimeMillis() + 5*1000;
	          while (System.currentTimeMillis() < endTime) {
	              synchronized (this) {
	                  try {
	                      wait(endTime - System.currentTimeMillis());
	                  } catch (Exception e) {
	                  }
	              }
	          }
	          // Stop the service using the startId, so that we don't stop
	          // the service in the middle of handling another job
	          stopSelf(msg.arg1);
	      }
	  }

	  @Override
	  public void onCreate() {
	    // Start up the thread running the service.  Note that we create a
	    // separate thread because the service normally runs in the process's
	    // main thread, which we don't want to block.  We also make it
	    // background priority so CPU-intensive work will not disrupt our UI.
		  
		// ------------ multi threading stuff creation ------------ 
	    HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
	    thread.start();

	    // Get the HandlerThread's Looper and use it for our Handler
	    mServiceLooper = thread.getLooper();
	    mServiceHandler = new ServiceHandler(mServiceLooper);
	    
	    // ------------ sensor fusion stuff creation ------------ 
        // Get an instance of the SensorManager
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mTracker = new SimpleOrientationTracker(mSensorManager);

	  }

	  @Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
	      Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

	      // sensor fusion stuff
	      mTracker.start();
	      
	      // For each start request, send a message to start a job and deliver the
	      // start ID so we know which request we're stopping when we finish the job
	      Message msg = mServiceHandler.obtainMessage();
	      msg.arg1 = startId;
	      mServiceHandler.sendMessage(msg);


	      // If we get killed, after returning from here, restart
	      return START_STICKY;
	  }

	  @Override
	  public IBinder onBind(Intent intent) {
	      // We don't provide binding, so return null
	      return null;
	  }

	  @Override
	  public void onDestroy() 
	  {
		mTracker.stop();
	    Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
	  }

	@Override
	protected void onHandleIntent(Intent arg0) {
	}
}
*/