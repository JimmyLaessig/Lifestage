package at.ac.tuwien.mmi.gyropowerball;

import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.res.XmlResourceParser;
import android.util.Log;

public class XmlLoader {
	
	public String devID="";
	public String accToken="";
	
	public int patternCount=0;
	public int[] eventCount;
	public int[] pId;
	public int[][] acId;
	public int[][] intensity;
	public int[][] targetIntensity;
	public int[][] duration;
	public int[][] pauseAfter;
	public int[] repeat;
	
	public int PID;
	public int EventCount;
	public int ACID;
	public int Intensity;
	public int TargetIntensity;
	public int Duration;
	public int PauseAfter;
	public int Repeat;	
	
	Pattern pattern=null;
	Event event;
	
	String CLASS_TAG = "XmlLoader";
	
	public XmlLoader(XmlResourceParser xrp){
		
		int patCnt=0;
		int evCnt=0;
		
		try {
			while(xrp.getEventType() != XmlResourceParser.END_DOCUMENT)
			{
				if (xrp.getEventType() == XmlResourceParser.START_TAG)
				{
					String s = xrp.getName();
					Log.e(CLASS_TAG, s);
					if(s.equals("Patterns")){
						patternCount = xrp.getAttributeIntValue(null,"Count",0);
						devID = xrp.getAttributeValue(null, "DEVICEID");
						accToken= xrp.getAttributeValue(null, "ACCESSTOKEN");
						MainActivity.patterns = new ArrayList<Pattern>(patternCount);
						
						pId = new int[patternCount];
						repeat = new int[patternCount];
						Log.d(CLASS_TAG,"Fertig mit patterns" + patternCount);
					}
					if(s.equals("Pattern")){
						if(pattern!=null) MainActivity.patterns.add(pattern);
						pId[patCnt] = xrp.getAttributeIntValue(null,"ID",0);
						repeat[patCnt] = xrp.getAttributeIntValue(null, "Repeat", 0);
						int anzEvent = xrp.getAttributeIntValue(null,  "Count", 0);
						eventCount = new int[anzEvent];
						Log.d(CLASS_TAG,"Fertig mit pattern" + patCnt);
						acId=new int[anzEvent][patternCount];
						intensity=new int[anzEvent][patternCount];
						targetIntensity=new int[anzEvent][patternCount];
						duration=new int[anzEvent][patternCount];
						pauseAfter=new int[anzEvent][patternCount];
						evCnt=0;
						patCnt++;
						
						int id = xrp.getAttributeIntValue(null,"ID",0);
						int rep = xrp.getAttributeIntValue(null,"Repeat",0);
						
						pattern = new Pattern(id,rep,false,anzEvent);
						
					}
					if(s.equals("Event")){
						acId[evCnt][patCnt-1] = xrp.getAttributeIntValue(null, "ActuatorID",0);
						intensity[evCnt][patCnt-1] = xrp.getAttributeIntValue(null, "Intensity", 0);
						targetIntensity[evCnt][patCnt-1] = xrp.getAttributeIntValue(null, "TargetIntensity", intensity[evCnt][patCnt-1]);
						duration[evCnt][patCnt-1] = xrp.getAttributeIntValue(null, "Duration", 100);
						pauseAfter[evCnt][patCnt-1]= xrp.getAttributeIntValue(null, "pauseAfter", 500);
						Log.d(CLASS_TAG, "Fertig mit event");
						evCnt++;
						
						int id = xrp.getAttributeIntValue(null, "ActuatorID",1);
						int inte = xrp.getAttributeIntValue(null, "Intensity", 0);
						int tinte = xrp.getAttributeIntValue(null, "TargetIntensity", inte);
						int dura = xrp.getAttributeIntValue(null, "Duration", 100);
						int pa = xrp.getAttributeIntValue(null, "pauseAfter", 500);
						
						event = new Event(id,inte, tinte,dura,pa);
						pattern.addEvent(event);						
					}
					
					
				}

				xrp.next();
			}
			MainActivity.patterns.add(pattern);
			xrp.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
