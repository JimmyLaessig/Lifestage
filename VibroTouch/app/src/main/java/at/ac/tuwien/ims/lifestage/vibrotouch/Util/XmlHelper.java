package at.ac.tuwien.ims.lifestage.vibrotouch.Util;

import android.content.res.XmlResourceParser;
import android.graphics.Color;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.*;
import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Object;

/**
 * XmlHelper for testcase input and output.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class XmlHelper {
    public final static String inputXMLPath="vibrotouch_testcases.xml";
    public final static String outputtXMLPath="vibrotouch_output.xml";

    private static XmlPullParser createParser(String filename) throws Exception {
        File file = new File (filename);
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        FileInputStream fis = new FileInputStream(file);
        xpp.setInput(new InputStreamReader(fis));
        return xpp;
    }

    public static String[] getIDandToken(String filename) {
        String[] result=new String[2];
        result[0] = "";
        result[1]= "";
        try {
            XmlPullParser xpp=createParser(filename);
            int eventType = xpp.getEventType();
            while(eventType != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG) {
                    String s = xpp.getName();
                    if(s.equals("SparkCore")) {
                        result[0] = xpp.getAttributeValue(null, "id");
                        result[1]= xpp.getAttributeValue(null, "token");
                        return result;
                    }
                }
                eventType=xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static ArrayList<Testcase> getTestcases(String filename) {
        ArrayList<Testcase> testcases=null;
        Testcase current=null;
        try {
            XmlPullParser xpp=createParser(filename);
            int eventType = xpp.getEventType();
            while(eventType != XmlResourceParser.END_DOCUMENT) {
                String name = null;
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        testcases = new ArrayList<>();
                        break;
                    case XmlPullParser.START_TAG:
                        name = xpp.getName();
                        if (name.equals("Testcase")) {
                            current=new Testcase();
                            current.setId(Integer.parseInt(xpp.getAttributeValue(null, "id")));
                            current.setScenario(Integer.parseInt(xpp.getAttributeValue(null, "scenario")));
                            current.setButtonOn(xpp.getAttributeValue(null, "button").equals("on"));
                        } else if (current != null && name.equals("Object")) {
                            int width=Integer.parseInt(xpp.getAttributeValue(null, "width"));
                            int height=Integer.parseInt(xpp.getAttributeValue(null, "height"));
                            current.addObject(new Object(0, 0, width, height, Color.RED, 0,0,0,0));//TODO
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        name = xpp.getName();
                        if (name.equals("Testcase") && testcases!=null && current!=null){
                            testcases.add(current);
                        }
                }
                eventType=xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return testcases;
    }

    public static void writeTestCase1Result(int id, int scenario, long time, int attempts, int screenPlacements, float accuracyDeviation) {
        try {
                /*String format = TODO
                        "<?xml version='1.0' encoding='UTF-8'?>" +
                                "<record>" +
                                "   <study id='%d'>" +
                                "       <topic>%s</topic>" +
                                "       <content>%s</content>" +
                                "       <author>%s</author>" +
                                "       <date>%s</date>" +
                                "   </study>" +
                                "</record>";
                XmlSerializer xmlSerializer = Xml.newSerializer();
                StringWriter writer = new StringWriter();

                xmlSerializer.setOutput(writer);
                xmlSerializer.startDocument("UTF-8", true);
                // open tag: <record>
                xmlSerializer.startTag("", Study.RECORD);
                // open tag: <study>
                xmlSerializer.startTag("", Study.STUDY);
                xmlSerializer.attribute("", Study.ID, String.valueOf(study.mId));

                // open tag: <topic>
                xmlSerializer.startTag("", Study.TOPIC);
                xmlSerializer.text(study.mTopic);
                // close tag: </topic>
                xmlSerializer.endTag("", Study.TOPIC);

                // open tag: <content>
                xmlSerializer.startTag("", Study.CONTENT);
                xmlSerializer.text(study.mContent);
                // close tag: </content>
                xmlSerializer.endTag("", Study.CONTENT);

                // open tag: <author>
                xmlSerializer.startTag("", Study.AUTHOR);
                xmlSerializer.text(study.mAuthor);
                // close tag: </author>
                xmlSerializer.endTag("", Study.AUTHOR);

                // open tag: <date>
                xmlSerializer.startTag("", Study.DATE);
                xmlSerializer.text(study.mDate);
                // close tag: </date>
                xmlSerializer.endTag("", Study.DATE);

                // close tag: </study>
                xmlSerializer.endTag("", Study.STUDY);
                // close tag: </record>
                xmlSerializer.endTag("", Study.RECORD);
                xmlSerializer.endDocument();

                String result=writer.toString();*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeTestCase2Result(int id, int scenario, long time, float deviation) {

    }
}
