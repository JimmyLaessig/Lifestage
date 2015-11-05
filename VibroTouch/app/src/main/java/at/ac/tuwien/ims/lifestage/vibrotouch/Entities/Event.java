package at.ac.tuwien.ims.lifestage.vibrotouch.Entities;

/**
 * Event used for Patterns.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class Event {
    private int acId;
    private int intensity;
    private int targetIntensity;
    private long duration;
    private long pauseAfter;

    public Event(int acId, int intensity, int targetIntensity, long duration, long pauseAfter){
        this.acId=acId;
        this.intensity=intensity;
        this.targetIntensity = targetIntensity;
        this.duration = duration;
        this.pauseAfter = pauseAfter;
    }

    public int getAcId() {
        return acId;
    }

    public int getIntensity() {
        return intensity;
    }

    public int getTargetIntensity() {
        return targetIntensity;
    }

    public long getDuration() {
        return duration;
    }

    public long getPauseAfter() {
        return pauseAfter;
    }
}
