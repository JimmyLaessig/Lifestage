package at.ac.tuwien.ims.lifestage.vibro.Entity;

import java.util.ArrayList;

/**
 * Pattern entity.
 * <p/>
 * Application: Vibro
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class Pattern {
    public int repeat;
    public ArrayList<Event> eventList;

    public Pattern(int repeat, ArrayList<Event> eventList){
        this.repeat=repeat;
        this.eventList = eventList;
    }
}