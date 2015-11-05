package at.ac.tuwien.ims.lifestage.vibrotouch.Entities;

import java.util.ArrayList;

/**
 * Pattern used for vibration.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class Pattern {
    private int repeat;
    private ArrayList<Event> eventList;

    public Pattern(ArrayList<Event> eventList, int repeat){
        this.repeat=repeat;
        this.eventList = eventList;
    }

    public long getWaitingTime() {
        long time=0L;
        for(Event event : eventList)
            time+=event.getDuration()+event.getPauseAfter();
        return time;
    }

    public int getRepeat() {
        return repeat;
    }

    public ArrayList<Event> getEventList() {
        return eventList;
    }
}
