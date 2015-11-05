package at.ac.tuwien.ims.lifestage.vibrotouch.Entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Testcase Entity.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class Testcase {
    private int id;
    private int scenario;
    private boolean buttonOn;
    private ArrayList<Object> objects;
    private int minIntensity;
    private int maxIntensity;

    public Testcase() {
        this.objects = new ArrayList<>();
    }

    public Testcase(int id, int scenario, boolean buttonOn, int minIntensity, int maxIntensity) {
        this.scenario = scenario;
        this.buttonOn = buttonOn;
        this.objects = new ArrayList<>();
        this.id = id;
        this.minIntensity=minIntensity;
        this.maxIntensity=maxIntensity;
    }

    public int getScenario() {
        return scenario;
    }

    public void setScenario(int scenario) {
        this.scenario = scenario;
    }

    public boolean isButtonOn() {
        return buttonOn;
    }

    public void setButtonOn(boolean buttonOn) {
        this.buttonOn = buttonOn;
    }

    public List<Object> getObjects() {
        return objects;
    }

    public void addObject(Object object) {
        this.objects.add(object);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getMinIntensity() {
        return minIntensity;
    }

    public void setMinIntensity(int minIntensity) {
        this.minIntensity = minIntensity;
    }

    public int getMaxIntensity() {
        return maxIntensity;
    }

    public void setMaxIntensity(int maxIntensity) {
        this.maxIntensity = maxIntensity;
    }
}
