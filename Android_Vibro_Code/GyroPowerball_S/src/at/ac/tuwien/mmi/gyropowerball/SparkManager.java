package at.ac.tuwien.mmi.gyropowerball;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.util.Log;

public class SparkManager {

    public static final int SERVERPORT = 9000;
    public static String ACCESTOKEN = "";
    public static String DEVICE_ID = "";
    
    public static int CONNECTED = 1;
    public static int CONNECTING = 2;
    public static int NOT_CONNECTED = 3;

    private ServerSocket serverSocket;
    private ServerThread serverThread;
    private CommunicationThread commThread;
    private MainActivity mActivity;
    private int status;
    private int rotation_speed; // rotation speed of servo
    private SampleActivity1 sActivity;

    public SparkManager(MainActivity mActivity) {
	status = NOT_CONNECTED;
	this.mActivity = mActivity;
	rotation_speed = 5;
	sActivity = null;
    }

    public SparkManager(SampleActivity1 sActivity) {
	status = NOT_CONNECTED;
	this.sActivity = sActivity;
	rotation_speed = 5;
    }

    /*
     * tries to establish a local wlan connection with the core
     * 
     * ip: local ip from smartphone
     */
    public void connectToCore(final String ip) {
	addToLog("connecting to Core...");
	
	status = CONNECTING;
	if(sActivity == null){
	    mActivity.updateUI();
	}else{
	    sActivity.updateUI();
	}

	this.serverThread = new ServerThread();
	new Thread(serverThread).start(); // ServerThread creates a ServerSocket
					  // and waits for a incoming connection
					  // requests (from sparkcore)

	Thread thread = new Thread(new Runnable() {
	    // Internet connections have to be handled in a separate thread
	    @Override
	    public void run() {
		BufferedReader bufferedReader = null;
		try {
		    StringBuffer stringBuffer = new StringBuffer("");

		    /*
		     * CHECK IF CORE IS ONLINE
		     */

		    HttpClient httpClient = new DefaultHttpClient();
		    HttpGet httpGet = new HttpGet();

		    URI uri = new URI("https://api.spark.io/v1/devices/" + DEVICE_ID + "?access_token=" + ACCESTOKEN);
		    //addToLog("Created URI:"+uri);

		    httpGet.setURI(uri);
		    HttpResponse httpResponse = httpClient.execute(httpGet);
		    // sends Httpget to cloud

		    InputStream inputStream = httpResponse.getEntity().getContent();
		    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

		    String readLine = bufferedReader.readLine();
		    
		    // receive response from spark cloud
		    // (in JSON Format)
		    while (readLine != null) {
			stringBuffer.append(readLine);
			stringBuffer.append("\n");
			readLine = bufferedReader.readLine();
		    }
		    addToLog(stringBuffer.toString());

		    Boolean isOnline = false;
		    JSONObject jsonObject = new JSONObject(stringBuffer.toString());
		    Log.v("XX", jsonObject.toString());
		    isOnline = jsonObject.getBoolean("connected");
		    if (isOnline == false) {
			mActivity.addToLog("Core is not online!");
			status = NOT_CONNECTED;
			return;
		    }
		    /*
		     * SEND LOCAL IP TO CORE after the core received the phones
		     * ip address a local wlan connection will be established
		     */
		    stringBuffer = new StringBuffer("");

		    httpClient = new DefaultHttpClient();
		    HttpPost httpPost = new HttpPost();

		    uri = new URI("https://api.spark.io/v1/devices/" + DEVICE_ID + "/connect");
		    // connect is a Internet accessible function on core

		    httpPost.setURI(uri);

		    ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		    nameValuePairs.add(new BasicNameValuePair("access_token", ACCESTOKEN));
		    nameValuePairs.add(new BasicNameValuePair("params", ip));

		    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		    httpResponse = httpClient.execute(httpPost);
		    inputStream = httpResponse.getEntity().getContent();
		    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		    // receive response from spark cloud N
		    // (in JSON Format)
		    readLine = "";

		    readLine = bufferedReader.readLine();
		    while (readLine != null) {
			stringBuffer.append(readLine);
			stringBuffer.append("\n");
			readLine = bufferedReader.readLine();
		    }

		    jsonObject = new JSONObject(stringBuffer.toString());
		    int return_value = jsonObject.getInt("return_value");
		    
		    if (return_value != 1) {
			addToLog("Core couldn't connect to phone!");
			status = NOT_CONNECTED;
		    }
		    Log.v("XX", "received from spark cloud: "+
		     stringBuffer.toString());
		} catch (Exception e) {
		    addToLog("Couldn't connect to core:"+"https://api.spark.io/v1/devices/" + DEVICE_ID);
		    addToLog("" + e.toString());
		    status = NOT_CONNECTED;
		} finally {
		    if (bufferedReader != null) {
			try {
			    bufferedReader.close();
			} catch (IOException e) {
			}
		    }
		    if(sActivity == null){
		    mActivity.updateUI();
		    }else{
			sActivity.updateUI();
		    }
		}
	    }
	});

	thread.start();
    }

    /*
     * creates a Server Socket and waits for requests (spark core). after a
     * client (spark core) is accepted a Communication thread will be created,
     * which can be used for communicating with the core.
     */
    private class ServerThread implements Runnable {
	private boolean stopped = false;

	public void run() {
	    if (serverSocket != null) {
		try {
		    serverSocket.close();
		} catch (IOException e) {
		}
	    }
	    try {
		serverSocket = new ServerSocket(SERVERPORT);
	    } catch (IOException e) {
	    }

	    while (!Thread.currentThread().isInterrupted() || stopped) {
		try {
		    if (serverSocket.isClosed()) {
			stopped = true;
			break;
		    }
		    commThread = new CommunicationThread(serverSocket.accept());
		    new Thread(commThread).start();
		} catch (IOException e) {
		    stopped = true;
		    if (commThread != null) {
			commThread.closeThread();
		    }
		}
	    }
	    if (commThread != null) {
		commThread.closeThread();
	    }
	}

	public void stop() {
	    stopped = true;
	    if (commThread != null) {
		commThread.closeThread();
		commThread = null;
	    }

	    try {
		serverSocket.close();
	    } catch (IOException e) {
	    }
	    status = NOT_CONNECTED;
	    if(sActivity == null){
	    mActivity.updateUI();}else{
		sActivity.updateUI();
	    }
	    addToLog("Core disconnected");
	}
    }

    /*
     * can be used for communicating with the core (sending/receiving Strings
     * to/from the core)
     */
    private class CommunicationThread implements Runnable {
	private Socket clientSocket;
	private BufferedReader input;
	private OutputStream output;
	boolean closed = false;

	public CommunicationThread(Socket clientSocket) {
	    this.clientSocket = clientSocket;
	    try {
		this.output = clientSocket.getOutputStream();
		this.input = new BufferedReader(new InputStreamReader(
			this.clientSocket.getInputStream()));

		addToLog("Core is connected now!");
		status = CONNECTED;
		
		if(sActivity == null){
		mActivity.updateUI();}else{
		    sActivity.updateUI();
		}
	    } catch (IOException e) {
	    }
	}
	
	public void sendCommand_executePattern(String command){
		sendCommandToCore(command);
	}

	public void sendCommand_LeftRotation() {
	    String command = "l" + String.format("%03d", rotation_speed);
	    sendCommandToCore(command);
	}

	public void sendCommand_RightRotation() {
	    String command = "r" + String.format("%03d", rotation_speed);
	    sendCommandToCore(command);
	}

	public void setServoPosition(int pos) {
	    String command = "s" + String.format("%03d", pos)
		    + String.format("%03d", rotation_speed);
	    sendCommandToCore(command);
	}

	private void sendCommandToCore(String command) {
	    try {
		output.write(command.getBytes());
		output.flush();
		addToLog("send: " + command);
	    } catch (IOException e) {
	    }
	}

	public void run() {
	    while (!Thread.currentThread().isInterrupted() || closed) {
		try {
		    String read = input.readLine(); // receive strings from core
		    if (read == null) {
			closed = true;
			break;
		    }

		    if (read.startsWith("ACC")) {
			addToLog("received: " + read);
			int pos = Integer.valueOf(read.substring(4, read.length()));
			if(sActivity == null){
			mActivity.updateSeekBar(pos);}
		    }
		} catch (Exception e) {
		    closed = true;
		}
	    }
	}

	public void closeThread() {
	    try {
		closed = true;
		clientSocket.shutdownInput();
		clientSocket.shutdownOutput();
		clientSocket.close();
	    } catch (Exception e) {
	    }
	}
    }

    public int getStatus() {
	return status;
    }

    public void disconnect() {
	if (serverThread != null) {
	    serverThread.stop();
	}
    }

    public void sendCommand_LeftRotation() {
	commThread.sendCommand_LeftRotation();
    }

    public void sendCommand_RightRotation() {
	commThread.sendCommand_RightRotation();
    }

    public void sendCommand_SetServoPosition(int progress) {
	commThread.setServoPosition(progress);
    }

    public void setSpeed(int progress) {
	this.rotation_speed = progress;
    }

    public int getSpeed() {
	return rotation_speed;
    }

    private void addToLog(String text){
	if(sActivity == null){
	    mActivity.addToLog(text);
	}
    }

	public void sendCommand_executePattern(String command) {
		commThread.sendCommand_executePattern(command);
	}
}
