using UnityEngine;
using System.Collections;
using System.Xml;
using System.Xml.Serialization;
using System.IO;
using System.Text;

[XmlRoot("Result")]
public class Result
{

        public static string path = "LiveStage_Output.xml";

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
    

    // Writes the state of the given Result to the XML File
    public static void WriteOutput(Result r)
    {
        XmlSerializerNamespaces names = new XmlSerializerNamespaces();
        names.Add("", "");

        XmlWriterSettings settings = new XmlWriterSettings();
        settings.Indent = true;
        settings.OmitXmlDeclaration = true;

        XmlSerializer serializer = new XmlSerializer(typeof(Result));
        using (var stream = new FileStream(path, FileMode.Append))
        using (var writer = XmlWriter.Create(stream, settings))
        {                    
            serializer.Serialize(writer, r, names);
            byte[] newline = Encoding.ASCII.GetBytes("\n");
            stream.Write(newline, 0, newline.Length);
            stream.Flush();
        }
    }
}
