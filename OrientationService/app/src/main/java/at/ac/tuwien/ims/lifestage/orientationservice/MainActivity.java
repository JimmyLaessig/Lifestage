package at.ac.tuwien.ims.lifestage.orientationservice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Starts the service.
 * <p/>
 * Application: OrientationService
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, OrientationTrackerService_Complementary.class));
    }
}
