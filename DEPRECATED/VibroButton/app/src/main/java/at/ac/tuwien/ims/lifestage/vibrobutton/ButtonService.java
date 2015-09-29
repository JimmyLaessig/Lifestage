package at.ac.tuwien.ims.lifestage.vibrobutton;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

/**
 * A Service that sends the state of the hardware Button as a Broadcast every 1000 milliseconds.
 * <p/>
 * Application: Vibro
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class ButtonService extends Service {
    private static boolean buttonPressed=false;
    private final Handler handler = new Handler();

    private Runnable sendData = new Runnable() {
        public void run() {
            Intent sendIntent = new Intent();
            sendIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION|Intent.FLAG_FROM_BACKGROUND|Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            sendIntent.setAction("at.ac.tuwien.ims.lifestage.vibro.button");
            sendIntent.putExtra("at.ac.tuwien.ims.lifestage.vibro.button.state", buttonPressed);
            sendBroadcast(sendIntent);
            handler.removeCallbacks(this);
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        buttonPressed=false;
        handler.removeCallbacks(sendData);
        handler.postDelayed(sendData, 1000);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void setButtonPressed(boolean btnPressed) {
        buttonPressed=btnPressed;
    }
}