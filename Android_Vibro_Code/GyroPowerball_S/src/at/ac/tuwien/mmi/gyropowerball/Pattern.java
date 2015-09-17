package at.ac.tuwien.mmi.gyropowerball;

import java.util.ArrayList;
import java.util.List;

public class Pattern {

	public int ID;
	public int eventCount;
	public int repeat;
	
	public boolean active;
	
	public ArrayList<Event> eventList;
	
	public Pattern(int ID, int repeat, boolean active, int anzEvent){
		this.active = active;
		this.ID=ID;
		this.repeat=repeat;
		this.eventCount=anzEvent;
		this.eventList = new ArrayList<Event>(anzEvent);
		
	}
	
	public void addEvent(Event event){
		eventList.add(event);
	}
	
	
}
