using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Xml;

namespace XMLShuffler
{
    public partial class Form1 : Form
    {
        private string XMLFile = "testcases_ordered.xml";
        private string XMLFileOut = "testcases_shuffled.xml";
        private string calibrationXMLFile = "calibrationPatterns.xml";
        private Random randomNumberGen;
        public Form1()
        {
            randomNumberGen = new Random();
            InitializeComponent();
        }

        private void button1_Click(object sender, EventArgs e)
        {
            shuffle();
        }

        private void shuffle()
        {
            XmlDocument doc = new XmlDocument();
            doc.Load(XMLFile);
            XmlDocument outputdoc = new XmlDocument();
            //<Testcases screenWidth="63" screenHeight="114" minObjectSize="10" maxObjectSize="25">
            outputdoc.LoadXml("<Testcases></Testcases>");
            outputdoc.DocumentElement.SetAttribute("screenWidth", doc.DocumentElement.GetAttribute("screenWidth"));
            outputdoc.DocumentElement.SetAttribute("screenHeight", doc.DocumentElement.GetAttribute("screenHeight"));
            outputdoc.DocumentElement.SetAttribute("minObjectSize", doc.DocumentElement.GetAttribute("minObjectSize"));
            outputdoc.DocumentElement.SetAttribute("maxObjectSize", doc.DocumentElement.GetAttribute("maxObjectSize"));

            Random rnd = new Random(1);
            outputdoc.ImportNode(doc.DocumentElement, false);

            XmlNode rootNode = doc.DocumentElement;
            int patternID = 1;
            int numChildrenOverall=rootNode.ChildNodes.Count;
            while (rootNode.HasChildNodes)
            {
                int numChildren = rootNode.ChildNodes.Count;
                int index = rnd.Next(0, numChildren);
                XmlNode childNodeRemove = rootNode.ChildNodes[index];
                if ((int.Parse((childNodeRemove as XmlElement).GetAttribute("maxIntensity")) > 0) || (numChildren <= (numChildrenOverall / 2)))
                {
                    //(childNodeRemove as XmlElement).SetAttribute("id", patternID++.ToString());
                    outputdoc.DocumentElement.AppendChild(outputdoc.ImportNode(childNodeRemove, true));
                    rootNode.RemoveChild(childNodeRemove);
                }
            }

            outputdoc.Save(XMLFileOut);

            foreach (XmlNode node in doc.DocumentElement.ChildNodes)
            {
                string text = node.Name; //or loop through its children as well
                OutputConsole.Text = OutputConsole.Text + text;
            }
        }

        private void openFileDialog1_FileOk(object sender, CancelEventArgs e)
        {


        }

        //Generate
        private void button2_Click(object sender, EventArgs e)
        {
            //Generate Xml-File
            //XmlDocument doc = new XmlDocument();
            //doc.Load(XMLFile);

           // OutputConsole.Text = OutputConsole.Text + "Saving file" + DateTime.Now.ToFileTime().ToString() + XMLFile + "/n";
           // doc.Save(DateTime.Now.ToFileTime().ToString() + XMLFile);
            //doc.
            //DateTime.Now.

            XmlDocument outputdoc = new XmlDocument();
            //<SparkCore id="48ff71065067555011472387" token="20e1ee31e3f0b0ecace2820c73ab71f5966acbf6"/>
      /*      XmlElement sparkEl = outputdoc.CreateElement("SparkCore");
            //childEl.CreateAttrip
            sparkEl.SetAttribute("id", "48ff71065067555011472387");
            sparkEl.SetAttribute("token", "20e1ee31e3f0b0ecace2820c73ab71f5966acbf6");
            outputdoc.AppendChild(sparkEl);
    */
            //outputdoc.LoadXml("<Patterns></Patterns>");


            //XmlNode outrootNode = outputdoc.DocumentElement;

            //int totalRepetition = 10;//Number of times we repeat all condition combination
            int totalRepetition = int.Parse(Calibration_numSteps.Text);
            int screenWidth=63;
            int screenHeight=114;
            int minObjectSize=10;
            int maxObjectSize=25;
            //int IDCounter = 1;
            int minIntensity = 30;//int.Parse(Obj_min.Text);//50;
           // OutputConsole.Text = OutputConsole.Text + "Obj min" + Obj_min.Text;
            int maxIntensity = 85;// int.Parse(Obj_max.Text);//50;
            //OutputConsole.Text = OutputConsole.Text + "Obj max" + Obj_max.Text;


            //<Testcases screenWidth="63" screenHeight="114" minObjectSize="10" maxObjectSize="25">
            XmlElement testCasesEl = outputdoc.CreateElement("Testcases");
            //childEl.CreateAttrip
            testCasesEl.SetAttribute("screenWidth", screenWidth.ToString());
            testCasesEl.SetAttribute("screenHeight", screenHeight.ToString());
            testCasesEl.SetAttribute("minObjectSize", minObjectSize.ToString());
            testCasesEl.SetAttribute("maxObjectSize", maxObjectSize.ToString());

            outputdoc.AppendChild(testCasesEl);
            int testCaseIDCounter=0;
            //insert Playground testcase
            testCaseIDCounter++;
            for (int feedbackCondition = 0; feedbackCondition <= 1; feedbackCondition++)
            {
                for (int inumObj = 2; inumObj <=4; inumObj++)
                {
                    for (int iRep = 1; iRep <= totalRepetition; iRep++)
                    {
                        //private void CreateAddTestcase(ref XmlDocument doc, ref XmlElement parentEl, int scenarioNr, int testcaseID, int numObjects, int minIntensity, int maxIntensity, int minObjectSize, int maxObjectSize, int maxXPosition, int maxYPosition)
                        if (feedbackCondition == 0)
                        {
                            CreateAddTestcase(ref outputdoc, ref testCasesEl, 1, testCaseIDCounter, inumObj, 0, 0, minObjectSize, maxObjectSize, screenWidth, screenHeight / 2);
                        }
                        else
                        {
                            CreateAddTestcase(ref outputdoc, ref testCasesEl, 1, testCaseIDCounter, inumObj, minIntensity, maxIntensity, minObjectSize, maxObjectSize, screenWidth, screenHeight / 2);
                        }

                        testCaseIDCounter++;
                    }
                }
            }

            outputdoc.Save(XMLFile);
        }

        private XmlElement CreatePattern(ref XmlDocument doc, int id, int count,int handState,int ftype)
        {
            XmlElement childEl = doc.CreateElement("Pattern");
            //childEl.CreateAttrip
            childEl.SetAttribute("ID", "", (id).ToString());
            childEl.SetAttribute("Count", "", (count).ToString());
            childEl.SetAttribute("HandState", "", (handState).ToString());
            childEl.SetAttribute("Type", "", (ftype).ToString());
            doc.DocumentElement.AppendChild(childEl);
            return childEl;
        }


        /*private void CreateAddEventAllIntensities(ref XmlDocument doc, ref XmlElement parentEl, int actuatorID, int duration, int pauseAfter, int minIntensity,int avgIntensity,int maxIntensity,int targetIntensity = -1)
        {
            CreateAddEvent(ref doc, ref parentEl, actuatorID, minIntensity, duration, pauseAfter);
            CreateAddEvent(ref doc, ref parentEl, actuatorID, avgIntensity, duration, pauseAfter);
            CreateAddEvent(ref doc, ref parentEl, actuatorID, maxIntensity, duration, pauseAfter);
        }*/


        //<Testcase id="0" scenario="0" button="on" minIntensity="50" maxIntensity="100">
		//<Object size="10" posX="35" posY="25"/>
		//<Object size="20" posX="15" posY="25"/>
		//<Object size="25" posX="35" posY="55"/>
	    //</Testcase>
        private void CreateAddTestcase(ref XmlDocument doc, ref XmlElement parentEl, int scenarioNr, int testcaseID, int numObjects, int minIntensity, int maxIntensity, int minObjectSize, int maxObjectSize, int maxXPosition, int maxYPosition)
        {
            XmlElement eventEl = doc.CreateElement("Testcase");
            eventEl.SetAttribute("id", testcaseID.ToString());
            eventEl.SetAttribute("scenario", scenarioNr.ToString());
            eventEl.SetAttribute("button", "on");
            eventEl.SetAttribute("minIntensity", minIntensity.ToString());
            eventEl.SetAttribute("maxIntensity", maxIntensity.ToString());
            parentEl.AppendChild(eventEl);
            for (int i = 0; i < numObjects; i++)
            {
                XmlElement objectEl = doc.CreateElement("Object");
                objectEl.SetAttribute("size", RandomRange(minObjectSize,maxObjectSize).ToString());
                objectEl.SetAttribute("posX", RandomRange(minObjectSize / 2, maxXPosition - minObjectSize / 2).ToString());
                objectEl.SetAttribute("posY", RandomRange(minObjectSize / 2, maxYPosition - minObjectSize / 2).ToString());
                eventEl.AppendChild(objectEl);
            }          
        }
        
        private int RandomRange(int startValue, int endValue)
        {
            return (startValue+randomNumberGen.Next(endValue-startValue + 1));
        }

        /*
        private void CreateAddEvent(ref XmlDocument doc, ref XmlElement parentEl, int actuatorID, int intensity, int duration, int pauseAfter, int targetIntensity = -1)
        {
            XmlElement eventEl = doc.CreateElement("Event");
            eventEl.SetAttribute("ActuatorID", actuatorID.ToString());
            eventEl.SetAttribute("Intensity", intensity.ToString());
            if (targetIntensity >= 0)
            {
                eventEl.SetAttribute("TargetIntensity", targetIntensity.ToString());
            }
            eventEl.SetAttribute("Duration", duration.ToString());
            eventEl.SetAttribute("PauseAfter", pauseAfter.ToString());


            parentEl.AppendChild(eventEl);
        }*/

        private void label1_Click(object sender, EventArgs e)
        {

        }

        private void button3_Click(object sender, EventArgs e)
        {
            int numberOfSteps = int.Parse(Calibration_numSteps.Text);//50;
            OutputConsole.Text = OutputConsole.Text + "Number of Calibratio steps" + Calibration_numSteps.Text;
            XmlDocument doc = new XmlDocument();
            doc.Load(XMLFile);
            XmlDocument outputdoc = new XmlDocument();
            outputdoc.LoadXml("<Patterns></Patterns>");
            outputdoc.DocumentElement.SetAttribute("Count", numberOfSteps.ToString());
            outputdoc.DocumentElement.SetAttribute("DEVICEID", doc.DocumentElement.GetAttribute("DEVICEID"));
            outputdoc.DocumentElement.SetAttribute("ACCESSTOKEN", doc.DocumentElement.GetAttribute("ACCESSTOKEN"));

            
            int startIntensity = 30;
            int maxIntensity = 100;
            int IDCounter = 1;
            for (int i = 0; i < numberOfSteps; i++)
            {
                XmlElement patternEl0a = CreatePattern(ref outputdoc, IDCounter++, 2,1,2);
               // CreateAddEvent(ref outputdoc, ref patternEl0a, 0, startIntensity + (maxIntensity*i - startIntensity*i) / (numberOfSteps-1) , 1000, 250);
                //CreateAddEvent(ref outputdoc, ref patternEl0a, 1, startIntensity + (maxIntensity*i - startIntensity*i) /  (numberOfSteps-1) , 1000, 250);
            }
            outputdoc.Save(calibrationXMLFile);
        }
    }
}
