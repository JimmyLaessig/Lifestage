package at.ac.tuwien.ims.lifestage.vibrotouch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Testcase;
import at.ac.tuwien.ims.lifestage.vibrotouch.Scenario.ObjectHandler;
import at.ac.tuwien.ims.lifestage.vibrotouch.Scenario.ObjectHandlerPlayground;
import at.ac.tuwien.ims.lifestage.vibrotouch.Scenario.ObjectHandlerScenario1;
import at.ac.tuwien.ims.lifestage.vibrotouch.Scenario.ObjectHandlerScenario2;
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.UserPreferences;

/**
 * Activity where the different scenarios are drawn.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class ScenarioActivity extends BaseActivity {
    private ObjectHandler objectHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scenario);
        int position=getIntent().getIntExtra("testcasePosition", -1);
        if(position>=0 && position<testcases.size()) {
            Testcase testcase = testcases.get(position);
            Log.d(getClass().getName(), "Scenario with Testcase " + testcase.getId());
            switch (testcase.getScenario()) {
                case 1:
                    objectHandler = new ObjectHandlerScenario1(this, testcase, position);
                    break;
                case 2:
                    objectHandler = new ObjectHandlerScenario2(this, testcase, position);
                    break;
                default:
                    objectHandler = new ObjectHandlerPlayground(this, testcase);
            }
            ((DrawView) findViewById(R.id.drawview)).setObjectHandler(objectHandler);
        } else {
            Log.d(getClass().getName(), "ScenarioActivity without Testcase...");
            Intent myIntent = new Intent(ScenarioActivity.this, SelectionActivity.class);
            startActivity(myIntent);
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(objectHandler!=null)
            objectHandler.stopThreads();
        finish();
    }
}
