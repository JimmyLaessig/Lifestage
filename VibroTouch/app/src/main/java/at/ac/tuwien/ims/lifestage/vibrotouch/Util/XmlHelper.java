package at.ac.tuwien.ims.lifestage.vibrotouch.Util;

import android.content.res.XmlResourceParser;
import android.graphics.Color;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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

    private static XmlPullParser createParser(String filename) throws FileNotFoundException, XmlPullParserException {
        File file = new File (filename);
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        FileInputStream fis = new FileInputStream(file);
        xpp.setInput(new InputStreamReader(fis));
        return xpp;
    }

    public static String[] getIDandToken(String filename) throws XmlPullParserException, IOException {
        String[] result=new String[2];
        result[0] = "";
        result[1]= "";
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
        return result;
    }

    public static float[] getScreenWidthAndHeight(String filename) throws XmlPullParserException, IOException {
        float[] result=new float[2];
        result[0]=(-1f);
        result[1]=(-1f);
        XmlPullParser xpp=createParser(filename);
        int eventType = xpp.getEventType();
        while(eventType != XmlResourceParser.END_DOCUMENT) {
            if (eventType == XmlResourceParser.START_TAG) {
                String s = xpp.getName();
                if(s.equals("Testcases")) {
                    result[0] = Float.parseFloat(xpp.getAttributeValue(null, "screenWidth"));
                    result[1]= Float.parseFloat(xpp.getAttributeValue(null, "screenHeight"));
                    return result;
                }
            }
            eventType=xpp.next();
        }
        return result;
    }

    public static ArrayList<Testcase> getTestcases(String filename) throws XmlPullParserException, IOException {
        ArrayList<Testcase> testcases=null;
        Testcase current=null;
        XmlPullParser xpp=createParser(filename);
        float minSize=0;
        float maxSize=0;
        int eventType = xpp.getEventType();
        while(eventType != XmlResourceParser.END_DOCUMENT) {
            String name = null;
            switch (eventType){
                case XmlPullParser.START_DOCUMENT:
                    testcases = new ArrayList<>();
                    testcases.add(new Testcase(0, 0, true, 10, 100));
                    break;
                case XmlPullParser.START_TAG:
                    name = xpp.getName();
                    if (name.equals("Testcases")) {
                        minSize=Integer.parseInt(xpp.getAttributeValue(null, "minObjectSize"));
                        maxSize=Integer.parseInt(xpp.getAttributeValue(null, "maxObjectSize"));
                    } else if (name.equals("Testcase")) {
                        current=new Testcase();
                        current.setId(Integer.parseInt(xpp.getAttributeValue(null, "id")));
                        current.setScenario(Integer.parseInt(xpp.getAttributeValue(null, "scenario")));
                        current.setButtonOn(xpp.getAttributeValue(null, "button").equals("on"));
                        current.setMinIntensity(Integer.parseInt(xpp.getAttributeValue(null, "minIntensity")));
                        current.setMaxIntensity(Integer.parseInt(xpp.getAttributeValue(null, "maxIntensity")));
                    } else if (current != null && name.equals("Object")) {
                        int size=Integer.parseInt(xpp.getAttributeValue(null, "size"));
                        if (current.getScenario()==1) {
                            float x=Float.parseFloat(xpp.getAttributeValue(null, "posX"));
                            float y=Float.parseFloat(xpp.getAttributeValue(null, "posY"));
                            current.addObject(new Object(size, x, y, minSize, maxSize, Color.RED));
                        } else if (current.getScenario()==2)
                            current.addObject(new Object(size, 0, 0, minSize, maxSize, Color.RED));
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
        return testcases;
    }

    public static void writeTestCase1Result(String filename, int userId, int id, int scenario, long time, int attempts, int screenPlacements, float accuracyDeviation) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(new File(filename));
        Element rootElement = doc.getDocumentElement();


        Element testcase = doc.createElement("Textcase");
        rootElement.appendChild(testcase);

        Attr attr_id = doc.createAttribute("id");
        attr_id.setValue(id+"");
        testcase.setAttributeNode(attr_id);

        Attr attr_userId = doc.createAttribute("userId");
        attr_userId.setValue(userId+"");
        testcase.setAttributeNode(attr_userId);

        Attr attr_scenario = doc.createAttribute("scenario");
        attr_scenario.setValue(scenario+"");
        testcase.setAttributeNode(attr_scenario);

        Attr attr_time = doc.createAttribute("time");
        attr_time.setValue(time+"");
        testcase.setAttributeNode(attr_time);

        Attr attr_attempts = doc.createAttribute("attempts");
        attr_attempts.setValue(attempts+"");
        testcase.setAttributeNode(attr_attempts);

        Attr attr_screenPlacements = doc.createAttribute("screenPlacements");
        attr_screenPlacements.setValue(screenPlacements+"");
        testcase.setAttributeNode(attr_screenPlacements);

        Attr attr_accuracyDeviation = doc.createAttribute("accuracyDeviation");
        attr_accuracyDeviation.setValue(accuracyDeviation+"");
        testcase.setAttributeNode(attr_accuracyDeviation);


        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(new DOMSource(doc), new StreamResult(new File(filename)));
    }

    public static void writeTestCase2Result(String filename, int userId, int id, int scenario, long time, float deviation) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(new File(filename));
        Element rootElement = doc.getDocumentElement();


        Element testcase = doc.createElement("Textcase");
        rootElement.appendChild(testcase);

        Attr attr_id = doc.createAttribute("id");
        attr_id.setValue(id+"");
        testcase.setAttributeNode(attr_id);

        Attr attr_userId = doc.createAttribute("userId");
        attr_userId.setValue(userId+"");
        testcase.setAttributeNode(attr_userId);

        Attr attr_scenario = doc.createAttribute("scenario");
        attr_scenario.setValue(scenario+"");
        testcase.setAttributeNode(attr_scenario);

        Attr attr_time = doc.createAttribute("time");
        attr_time.setValue(time+"");
        testcase.setAttributeNode(attr_time);

        Attr attr_deviation = doc.createAttribute("deviation");
        attr_deviation.setValue(deviation+"");
        testcase.setAttributeNode(attr_deviation);


        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(new DOMSource(doc), new StreamResult(new File(filename)));
    }
}
