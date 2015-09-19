package at.ac.tuwien.ims.lifestage.vibro.Util;

import android.content.res.XmlResourceParser;
import android.util.Log;

import java.util.ArrayList;

import at.ac.tuwien.ims.lifestage.vibro.Entity.Event;
import at.ac.tuwien.ims.lifestage.vibro.Entity.Pattern;

/**
 * Xml Reader for patterns and eventually more.
 * <p/>
 * Application: Vibro
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class XmlReader {
    private final static String TAG = "XmlReader";

    private String devID, accToken;
    private ArrayList<Pattern> patterns;

    private int patternCount=0;
    private int[] pId;
    private int[][] acId;
    private int[][] intensity;
    private int[][] targetIntensity;
    private int[][] duration;
    private int[][] pauseAfter;
    private int[] repeat;

    public XmlReader() {
        devID="";
        accToken="";
        patterns = new ArrayList<>();
    }

    public void loadPatterns(XmlResourceParser xrp) {
        Pattern pattern = null;
        Event event;
        int patCnt = 0;
        int evCnt = 0;

        try {
            while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlResourceParser.START_TAG) {
                    String s = xrp.getName();
                    Log.e(TAG, s);
                    if (s.equals("Patterns")) {
                        patternCount = xrp.getAttributeIntValue(null, "Count", 0);
                        devID = xrp.getAttributeValue(null, "DEVICEID");
                        accToken = xrp.getAttributeValue(null, "ACCESSTOKEN");
                        patterns = new ArrayList<>(patternCount);

                        pId = new int[patternCount];
                        repeat = new int[patternCount];
                        Log.d(TAG, "Fertig mit patterns" + patternCount);
                    }
                    if (s.equals("Pattern")) {
                        if (pattern != null) patterns.add(pattern);

                        pId[patCnt] = xrp.getAttributeIntValue(null, "ID", 0);
                        repeat[patCnt] = xrp.getAttributeIntValue(null, "Repeat", 0);
                        int anzEvent = xrp.getAttributeIntValue(null, "Count", 0);
                        Log.d(TAG, "Fertig mit pattern" + patCnt);
                        acId = new int[anzEvent][patternCount];
                        intensity = new int[anzEvent][patternCount];
                        targetIntensity = new int[anzEvent][patternCount];
                        duration = new int[anzEvent][patternCount];
                        pauseAfter = new int[anzEvent][patternCount];
                        evCnt = 0;
                        patCnt++;

                        int id = xrp.getAttributeIntValue(null, "ID", 0);
                        int rep = xrp.getAttributeIntValue(null, "Repeat", 0);

                        pattern = new Pattern(id, rep, false, anzEvent);

                    }
                    if (s.equals("Event")) {
                        acId[evCnt][patCnt - 1] = xrp.getAttributeIntValue(null, "ActuatorID", 0);
                        intensity[evCnt][patCnt - 1] = xrp.getAttributeIntValue(null, "Intensity", 0);
                        targetIntensity[evCnt][patCnt - 1] = xrp.getAttributeIntValue(null, "TargetIntensity", intensity[evCnt][patCnt - 1]);
                        duration[evCnt][patCnt - 1] = xrp.getAttributeIntValue(null, "Duration", 100);
                        pauseAfter[evCnt][patCnt - 1] = xrp.getAttributeIntValue(null, "pauseAfter", 500);
                        Log.d(TAG, "Fertig mit event");
                        evCnt++;

                        int id = xrp.getAttributeIntValue(null, "ActuatorID", 1);
                        int inte = xrp.getAttributeIntValue(null, "Intensity", 0);
                        int tinte = xrp.getAttributeIntValue(null, "TargetIntensity", inte);
                        int dura = xrp.getAttributeIntValue(null, "Duration", 100);
                        int pa = xrp.getAttributeIntValue(null, "pauseAfter", 500);

                        event = new Event(id, inte, tinte, dura, pa);
                        if (pattern != null)
                            pattern.addEvent(event);
                    }
                }
                xrp.next();
            }
            patterns.add(pattern);
            xrp.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Pattern> getPatterns() {
        return patterns;
    }

    public String getDevID() {
        return devID;
    }

    public String getAccessToken() {
        return accToken;
    }
}
