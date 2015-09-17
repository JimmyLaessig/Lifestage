package at.tuwien.ims.mobilebrdf.tracker;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import at.tuwien.ims.mobilebrdf.tracker.OrientationTrackerService_Complementary.LocalBinder_Complementary;
import at.tuwien.ims.mobilebrdf.tracker.OrientationTrackerService_Simple.LocalBinder_Simple;
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

public class OrientationTrackerActivity extends Activity {
	private final static String LOGTAG = "OrientationTracker";
	
    private GLSurfaceView mGLSurfaceView;
    private MyGLRenderer mRenderer;

    // service stuff -> can be used to call public service methods
    // --> not used in this demo
    private OrientationTrackerService_Simple mTrackerService_simple;
    private OrientationTrackerService_Complementary mTrackerService_complementary;
    
    // 4x4 rotation matrix to show current rotation on cube
    private static float[] mRotationMatrix = new float[16];   
    
    private static boolean mBound_simple = false;
    private static boolean mBound_complementary = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orientation_tracker);
        
		// register message receiver to get local broadcasts sent by tracking service
        LocalBroadcastManager.getInstance(this).registerReceiver(
        		mUpdateRotationReceiver, new IntentFilter("orientationTracker_update"));
        
    	/*
    	 * GUI GENERATION
    	 */
        
        // Create our Preview view and set it as the content of our Activity
        mRenderer = new MyGLRenderer();
        mGLSurfaceView = new GLSurfaceView(this); 
        mGLSurfaceView.setRenderer(mRenderer);
        setContentView(mGLSurfaceView);
        
        LinearLayout ll = new LinearLayout(this);
        
        Button bnt_simpleOrientTracking = new Button(this);
        bnt_simpleOrientTracking.setText("Simple");

        Button bnt_complementaryOrientTracking = new Button(this);
        bnt_complementaryOrientTracking.setText("Complementary");
        
        Button bnt_stopOrientTracking = new Button(this);
        bnt_stopOrientTracking.setText("Stop");
        
        ll.addView(bnt_simpleOrientTracking);
        ll.addView(bnt_complementaryOrientTracking);
        ll.addView(bnt_stopOrientTracking);
        
        ll.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        this.addContentView(ll, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
          
        // button listeners
        bnt_simpleOrientTracking.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v) 
            {
          	  	//int num = mTrackerService_simple.getRandomNumber();
  
                mGLSurfaceView.onResume();
                
            	if (mBound_complementary)
            	{             
            		unbindService(mTrackerConnection_complementary);
            		mBound_complementary = false;
            		Log.d(LOGTAG,"SERVICE UNBOUND: Complementary Filter.");   
            	}

            	if (!mBound_simple)
            	{
	                Intent intent = new Intent(OrientationTrackerActivity.this, OrientationTrackerService_Simple.class);
	                bindService(intent, mTrackerConnection_simple, Context.BIND_AUTO_CREATE);
            		Log.d(LOGTAG,"SERVICE BOUND: Simple Filter.");   
            	}
            	else 
            	{
            		Log.d(LOGTAG,"SERVICE ALREADY BOUND: Simple Filter.");   
            	}
            }
        });
        
        bnt_complementaryOrientTracking.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v) 
            {
                mGLSurfaceView.onResume();
                
            	if (mBound_simple)
            	{                
            		unbindService(mTrackerConnection_simple);
            		mBound_simple = false;
            		Log.d(LOGTAG,"SERVICE UNBOUND: Simple Filter.");   
            	}

            	if (!mBound_complementary)
            	{
	                Intent intent = new Intent(OrientationTrackerActivity.this, OrientationTrackerService_Complementary.class);
	                bindService(intent, mTrackerConnection_complementary, Context.BIND_AUTO_CREATE);
            		Log.d(LOGTAG,"SERVICE BOUND: Complementary Filter.");   
            	}
            	else 
            	{
            		Log.d(LOGTAG,"SERVICE ALREADY BOUND: Complementary Filter.");   
            	}
            	
            }
        });
        
        bnt_stopOrientTracking.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v) 
            {
                mGLSurfaceView.onPause();
                
            	if (mBound_simple)
            	{                
            		unbindService(mTrackerConnection_simple);
            		mBound_simple = false;
            		Log.d(LOGTAG,"SERVICE UNBOUND: Simple Filter.");   
            	}

            	if (mBound_complementary)
            	{
            		unbindService(mTrackerConnection_complementary);
            		mBound_complementary = false;
            		Log.d(LOGTAG,"SERVICE UNBOUND: Complementary Filter.");   
            	}
            }
        });
    }
   

   /*
    @Override
    protected void onStart() 
    {
        super.onStart();
        
        // Bind to OrientationTrackerService_Simple if activity starts
        Intent intent = new Intent(this, OrientationTrackerService_Simple.class);
        bindService(intent, mTrackerConnection_simple, Context.BIND_AUTO_CREATE);
    }
   */
    
    @Override
    protected void onPause() 
    {
        // when the activity looses focus so something appropriate
        super.onPause();
        
        // Unbind from the service
        if (mBound_simple) 
        {
            unbindService(mTrackerConnection_simple);
            mBound_simple = false;
    		Log.d(LOGTAG,"Activity paused: Simple Service unbound!");   
        }
        
        if (mBound_complementary) 
        {
            unbindService(mTrackerConnection_complementary);
            mBound_complementary = false;
    		Log.d(LOGTAG,"Activity paused: Complementary Service unbound!");   
        }

        mGLSurfaceView.onPause();
    } 
    

    @Override
    protected void onResume() 
    {
        super.onResume();
        mGLSurfaceView.onResume();
    }
    
    @Override
    protected void onStop() 
    {
        super.onStop();
        
        // Unbind from the current bound service
        if (mBound_simple) 
        {
            unbindService(mTrackerConnection_simple);
            mBound_simple = false;
    		Log.d(LOGTAG,"Activity stopped: Simple Service unbound!");   
        }
        
        if (mBound_complementary) 
        {
            unbindService(mTrackerConnection_complementary);
            mBound_complementary = false;
    		Log.d(LOGTAG,"Activity stopped: Complementary Service unbound!");   
        }
        
        mGLSurfaceView.onPause();
    }

    /*
     * INTER COMPONENT COMMUNICATION
     */
    
    // Register braodcast receiver to get updates from rotation tracker services
	private BroadcastReceiver mUpdateRotationReceiver = new BroadcastReceiver() {
	
	    @Override
	    public void onReceive(Context context, Intent intent) {
	
	        //String action = intent.getAction(); // is the name of the created
	    	// intent in the service (= trackingOrientationUpdate)
	        //Log.d(LOGTAG,"message received " + action);   
	
	        // set updated rotation matrix to private class member variable
	        mRotationMatrix = intent.getFloatArrayExtra("affineRotationMatrix");
	    }
	};

	
    /*
     * DEPRECATED: dirty hack (but maybe faster than message broadcasting???)
     * Call this function from Service to update the private mRotationMatrix array
     
    public static void updateRotationMatrix(float[] matrix) 
    {
		Log.d(LOGTAG,"updateRotationMatrix() called.");   
		
    	if (mBound_simple)
    	{
    		mRotationMatrix = matrix;
    	}
    	
    	if (mBound_complementary)
    	{
    		mRotationMatrix = matrix;
    	}
    }
    */



    /*
     * SERVICE BINDING
     */
    
    // Defines callbacks for service binding, passed to bindService() 
    private ServiceConnection mTrackerConnection_simple = new ServiceConnection() 
    {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) 
        {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder_Simple binder = (LocalBinder_Simple) service;
            mTrackerService_simple = binder.getService();
            mBound_simple = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) 
        {
            mBound_simple = false;
        }
    };
    
    private ServiceConnection mTrackerConnection_complementary = new ServiceConnection() 
    {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) 
        {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
        	LocalBinder_Complementary binder = (LocalBinder_Complementary) service;
            mTrackerService_complementary = binder.getService();
            mBound_complementary = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) 
        {
        	mBound_complementary = false;
        }
    };
    

	/*
	 * OPENGL RENDERING
	 */
    class MyGLRenderer implements GLSurfaceView.Renderer {
        
    	private Cube mCube;

        public MyGLRenderer() {
            mCube = new Cube();
            // initialize the rotation matrix to identity
            mRotationMatrix[ 0] = 1;
            mRotationMatrix[ 4] = 1;
            mRotationMatrix[ 8] = 1;
            mRotationMatrix[12] = 1;
        }

        public void onDrawFrame(GL10 gl) {
            // clear screen
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

            // set-up modelview matrix
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glTranslatef(0, 0, -3.0f);
            gl.glMultMatrixf(mRotationMatrix, 0);

            // draw our object
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

            mCube.draw(gl);
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            // set view-port
            gl.glViewport(0, 0, width, height);
            // set projection matrix
            float ratio = (float) width / height;
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // dither is enabled by default, we don't need it
            gl.glDisable(GL10.GL_DITHER);
            // clear screen in white
            gl.glClearColor(1,1,1,1);
        }

        class Cube {
            // initialize our cube
            private FloatBuffer mVertexBuffer;
            private FloatBuffer mColorBuffer;
            private ByteBuffer  mIndexBuffer;

            public Cube() {
                final float vertices[] = {
                        -1, -1, -1,		 1, -1, -1,
                         1,  1, -1,	    -1,  1, -1,
                        -1, -1,  1,      1, -1,  1,
                         1,  1,  1,     -1,  1,  1,
                };

                final float colors[] = {
                        0,  0,  0,  1,  1,  0,  0,  1,
                        1,  1,  0,  1,  0,  1,  0,  1,
                        0,  0,  1,  1,  1,  0,  1,  1,
                        1,  1,  1,  1,  0,  1,  1,  1,
                };

                final byte indices[] = {
                        0, 4, 5,    0, 5, 1,
                        1, 5, 6,    1, 6, 2,
                        2, 6, 7,    2, 7, 3,
                        3, 7, 4,    3, 4, 0,
                        4, 7, 6,    4, 6, 5,
                        3, 0, 1,    3, 1, 2
                };

                ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
                vbb.order(ByteOrder.nativeOrder());
                mVertexBuffer = vbb.asFloatBuffer();
                mVertexBuffer.put(vertices);
                mVertexBuffer.position(0);

                ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length*4);
                cbb.order(ByteOrder.nativeOrder());
                mColorBuffer = cbb.asFloatBuffer();
                mColorBuffer.put(colors);
                mColorBuffer.position(0);

                mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
                mIndexBuffer.put(indices);
                mIndexBuffer.position(0);
            }

            public void draw(GL10 gl) {
                gl.glEnable(GL10.GL_CULL_FACE);
                gl.glFrontFace(GL10.GL_CW);
                gl.glShadeModel(GL10.GL_SMOOTH);
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
                gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
                gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
            }            
        }
    }
}
