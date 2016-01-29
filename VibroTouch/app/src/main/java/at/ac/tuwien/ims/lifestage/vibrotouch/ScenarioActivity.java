package at.ac.tuwien.ims.lifestage.vibrotouch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Testcase;
import at.ac.tuwien.ims.lifestage.vibrotouch.Scenario.ObjectHandler;
import at.ac.tuwien.ims.lifestage.vibrotouch.Scenario.ObjectHandlerPlayground;
import at.ac.tuwien.ims.lifestage.vibrotouch.Scenario.ObjectHandlerScenario1;
import at.ac.tuwien.ims.lifestage.vibrotouch.Scenario.ObjectHandlerScenario2;

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
        Testcase testcase=null;
        try {
            int id=getIntent().getIntExtra("testcase", 0);
            for(Testcase t : testcases)
                if(t.getId()==id)
                    testcase = t;
        } catch(Exception e){
            e.printStackTrace();
        }
        if(testcase!=null) {
            Log.d(getClass().getName(), "Scenario with Testcase " + testcase.getId());
            switch (testcase.getScenario()) {
                case 1:
                    objectHandler = new ObjectHandlerScenario1(this, testcase);
                    break;
                case 2:
                    objectHandler = new ObjectHandlerScenario2(this, testcase);
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
