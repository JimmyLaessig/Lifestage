package at.ac.tuwien.ims.lifestage.androidtounityinterface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receives Broatcasts from OrientationTracker (Rotation Matrix) Service and VibroService (Hardware Button).
 * <p>
 * Application: Lifestage
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class AndroidToUnityReceiver extends BroadcastReceiver {
    private static AndroidToUnityReceiver instance;
    public static float[] orientation = new float[0];
    public static boolean buttonPressed = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals("orientationTracker_update")) {
                float[] intentOrientation = intent.getFloatArrayExtra("affineRotationMatrix");
                if (intentOrientation != null) {
                    orientation = intentOrientation;
                }
            } else if (intent.getAction().equals("at.ac.tuwien.ims.lifestage.vibro.button"))
                buttonPressed = intent.getBooleanExtra("at.ac.tuwien.ims.lifestage.vibro.button.state", false);
        }
    }

    public static void createInstance() {
        if(instance ==  null) {
            instance = new AndroidToUnityReceiver();
        }
    }
}
