package at.ac.tuwien.ims.lifestage.vibro.Entity;

/**
 * Event entity.
 * <p/>
 * Application: Vibro
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class Event {
    public int acId;
    public int intensity;
    public int targetIntensity;
    public int duration;
    public int pauseAfter;

    public Event(int acId, int intensity, int targetIntensity, int duration, int pauseAfter){
        this.acId=acId;
        this.intensity=intensity;
        this.targetIntensity = targetIntensity;
        this.duration = duration;
        this.pauseAfter = pauseAfter;
    }

}
