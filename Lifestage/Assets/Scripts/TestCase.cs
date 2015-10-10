using UnityEngine;
using System.Collections;
using System.Xml;
using System.Xml.Serialization;
using System.IO;
using System.Text;


/// <summary>
/// This class encapsulates a TestCase for an interaction scene. 
/// It contains information on the duration of the interaction, the setting of the scene, the success of the interaction, as well as information on the users id.
/// </summary>
[XmlRoot("Result")]
public class TestCase
{
    private static int ID_COUNTER = 0;
    // The name of the output file
    private static string LIVESTAGE_OUTPUT_PATH = "LiveStage_Output.xml";
    [XmlAttribute("id")]
    public int id;

    [XmlAttribute("userID")]
    public string userID;
    // Number of Elements in the scene
    [XmlAttribute("numElements")]
    public int numElements;

    // Time needed to select the Object
    [XmlAttribute("time")]
    public float time;

    // True if the correct GameObject was selected
    [XmlAttribute("correct")]
    public bool correct;

    [XmlAttribute("targetElementIndex")]
    public int targetElementIndex;


    public TestCase()
    {
        id = ID_COUNTER;
        ID_COUNTER++;
    }


    /// <summary>
    /// Writes the state of the given TestCase to the XML File.
    /// </summary>
    /// <param name="r">The Result, must not be NULL</param>
    public static void WriteOutput(TestCase r)
    {
        
        //Debug.Log("Writing Result: " + "userID:" + r.userID + ", correct: " + r.correct + ", time: " + r.time + ", numElements: " + r.numElements);
        //XmlSerializerNamespaces names = new XmlSerializerNamespaces();
        //names.Add("", "");

        //XmlWriterSettings settings = new XmlWriterSettings();
        //settings.Indent = true;
        //settings.OmitXmlDeclaration = true;

        //XmlSerializer serializer = new XmlSerializer(typeof(TestCase));
        //using (var stream = new FileStream(path, FileMode.Append))
        //using (var writer = XmlWriter.Create(stream, settings))
        //{
        //    serializer.Serialize(writer, r, names);
        //    byte[] newline = Encoding.ASCII.GetBytes("\n");
        //    stream.Write(newline, 0, newline.Length);
        //    stream.Flush();
        //}
    }
}
