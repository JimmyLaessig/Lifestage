package at.ac.tuwien.mmi.gyropowerball;

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
