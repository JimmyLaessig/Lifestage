using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using System;
using System.Xml;
using System.Xml.Serialization;
using System.IO;
using System.Text;

public class StorageManager : MonoBehaviour
{
    private static StorageManager instance;
#if !UNITY_EDITOR
    private static string SCENARIO_FILE_PATH = "/sdcard/lifestage_testcases.xml";
	private static string OUTPUT_FILE_PATH = "/sdcard/lifestage_output.xml";
#else
    private static string SCENARIO_FILE_PATH = "lifestage_testcases.xml";
	private static string OUTPUT_FILE_PATH = "lifestage_output.xml";
#endif
    private static string SOLVED_TESTCASES_KEY = "solvedCasesKey";

    /// <summary>
    /// Returns an instance of the StorageManager
    /// </summary>
    /// <returns></returns>
    public static StorageManager Instance
    {
        get { return StorageManager.instance; }
    }


    void Awake()
    {
        if (!StorageManager.instance)
            StorageManager.instance = this;
    }


    /// <summary>
    /// Loads a scenario from the given XML-File. 
    /// Be sure that the file exists, is well formed and is in to corresponding directory.
    /// </summary>
    /// <returns></returns>
    public Scenario LoadScenario()
    {
        Scenario scenario = new Scenario();
        try
        {
            XmlDocument doc = new XmlDocument();
            doc.Load(SCENARIO_FILE_PATH);

            XmlElement root = doc.DocumentElement;
            XmlNodeList list = root.GetElementsByTagName("TestCase");
            for (int i = 0; i < list.Count; i++)
            {
                TestCase t = new TestCase();
                t.numElements = Convert.ToInt32(list[i].Attributes["numElements"].Value);
				t.targetElementIndex = Convert.ToInt32(list[i].Attributes["targetElement"].Value) - 1;
				t.vibroMode = PluginManager.Instance.getEnum(list[i].Attributes["vibroMode"].Value);
                scenario.AddTestCase(t);
            }
            if (list.Count == 0)
            {
                UIController.Instance.ReportError(SCENARIO_FILE_PATH + ": No TestCases found!");
            }
        }
        catch (Exception ex)
		{
			Debug.Log("Xml Loading: " + ex.ToString());
            UIController.Instance.ReportError(SCENARIO_FILE_PATH + " not found or is invalid! Please make sure that the corresponding file exists and is well formed!");
        }

        return scenario;
    }


    /// <summary>
    /// Removes all stored information about previously solved TestCases
    /// </summary>
    public void ClearTestCaseProgress()
    {
        PlayerPrefs.DeleteKey(SOLVED_TESTCASES_KEY);
    }


    /// <summary>
    /// Saves the IDs of the TestCases to the internal storage. 
    /// This method overwrites previously stored information.
    /// </summary>
    /// <param name="testCases"></param>
    public void SaveTestCaseProgress(List<TestCase> testCases)
    {
        string solved = "";
        for (int i = 0; i < testCases.Count; i++)
            solved += testCases[i].id + " ";

        if (testCases.Count > 0)
        {
            PlayerPrefs.SetString(SOLVED_TESTCASES_KEY, solved);
            PlayerPrefs.Save();
        }
    }


    /// <summary>
    /// Retrieves a list of IDs of TestCases that where written to the internal storage.
    /// </summary>
    /// <returns></returns>
    public List<int> LoadSolvedTestCases()
    {
        List<int> solved = new List<int>();

        if (!PlayerPrefs.HasKey(SOLVED_TESTCASES_KEY))
            return solved;

        string tmp = PlayerPrefs.GetString(SOLVED_TESTCASES_KEY);
        string[] args = tmp.Split(' ');

        for (int i = 0; i < args.Length; i++)
        {
            solved.Add(Convert.ToInt32(args[i], 10));
        }
        return solved;
    }


	/// <summary>
	/// This method gets number of all available testcases.
	/// </summary>
	private int getNumberOfTestCases() {
		XmlDocument doc = new XmlDocument();
		doc.Load(SCENARIO_FILE_PATH);
		XmlElement root = doc.DocumentElement;
		XmlNodeList list = root.GetElementsByTagName("TestCase");
		return list.Count*getNumberOfRepetitions();
	}


	/// <summary>
	/// This method gets the number of repetitions.
	/// </summary>
	public int getNumberOfRepetitions() {
		XmlDocument doc = new XmlDocument();
		doc.Load(SCENARIO_FILE_PATH);
		XmlElement root = doc.DocumentElement;
		return Convert.ToInt32(root.GetAttribute("repetitions"));
	}

	/// <summary> TODO
	/// This method gets the last userID from the xml file or -1 if xml file does not exist.
	/// </summary>
	public int getLastIDfromXML() {
		XmlDocument xmlDoc = new XmlDocument();
		if (File.Exists(OUTPUT_FILE_PATH)) {            
			xmlDoc.Load(OUTPUT_FILE_PATH);
			XmlElement elmRoot = xmlDoc.DocumentElement;
			
			int user_id = 0;
			if (elmRoot.LastChild != null) {
				int id = Int32.Parse(elmRoot.LastChild.Attributes["userID"].Value);
				user_id = id;
			}
			return user_id;
		}
		return -1;
	}


    /// <summary>
    /// This method writes the results of a TestCase to the corresponding XML-File.
    /// </summary>
	public void WriteTestCaseResult(TestCase testcase) {
		if (testcase == null)
			return;

		XmlDocument xmlDoc = new XmlDocument();
		if (File.Exists(OUTPUT_FILE_PATH)) {
			bool newResult = true;
			xmlDoc.Load(OUTPUT_FILE_PATH);

			if(xmlDoc.DocumentElement.LastChild!=null)
				if(xmlDoc.DocumentElement.LastChild.ChildNodes.Count<getNumberOfTestCases())
					newResult=false;

			if(newResult) {
				XmlElement elm = xmlDoc.CreateElement("Result");
				int user_id = getLastIDfromXML()+1;
				XmlAttribute userId = xmlDoc.CreateAttribute("userID"); 
				userId.Value = user_id+"";
				elm.Attributes.Append(userId);

				XmlElement elmNew = xmlDoc.CreateElement("TestCase");
				XmlAttribute testcaseID = xmlDoc.CreateAttribute("testcaseID");
				testcaseID.Value = testcase.id + "";
				elmNew.Attributes.Append(testcaseID);
				XmlAttribute noElem = xmlDoc.CreateAttribute("numElements");
				noElem.Value = testcase.numElements + "";
				elmNew.Attributes.Append(noElem);
				XmlAttribute rightObject = xmlDoc.CreateAttribute("targetElementIndex");
				rightObject.Value = testcase.targetElementIndex + "";
				elmNew.Attributes.Append(rightObject);
				XmlAttribute pickSuccessful = xmlDoc.CreateAttribute("correct");
				pickSuccessful.Value = testcase.isCorrect + "";
				elmNew.Attributes.Append(pickSuccessful);
				XmlAttribute timePassed = xmlDoc.CreateAttribute("time");
				timePassed.Value = testcase.time + "";
				elmNew.Attributes.Append(timePassed);
				XmlAttribute attemptS = xmlDoc.CreateAttribute("attempts");
				attemptS.Value = testcase.attempts + "";
				elmNew.Attributes.Append(attemptS);
				XmlAttribute repetitions = xmlDoc.CreateAttribute("repetition");
				repetitions.Value = ""+1;
				elmNew.Attributes.Append(repetitions);

				elm.AppendChild(elmNew);
				xmlDoc.DocumentElement.AppendChild(elm);
			} else {
				XmlNodeList list = xmlDoc.DocumentElement.LastChild.ChildNodes;
				int count=0;
				for (int i = 0; i < list.Count; i++) {
					if(Convert.ToInt32(list[i].Attributes["testcaseID"].Value)==testcase.id) {
						count++;
					}
				}
				if(count<testcase.repetitions) {				
					XmlNode elm = xmlDoc.DocumentElement.LastChild;
					
					XmlElement elmNew = xmlDoc.CreateElement("TestCase");
					XmlAttribute testcaseID = xmlDoc.CreateAttribute("testcaseID");
					testcaseID.Value = testcase.id + "";
					elmNew.Attributes.Append(testcaseID);
					XmlAttribute noElem = xmlDoc.CreateAttribute("numElements");
					noElem.Value = testcase.numElements + "";
					elmNew.Attributes.Append(noElem);
					XmlAttribute rightObject = xmlDoc.CreateAttribute("targetElementIndex");
					rightObject.Value = testcase.targetElementIndex + "";
					elmNew.Attributes.Append(rightObject);
					XmlAttribute pickSuccessful = xmlDoc.CreateAttribute("correct");
					pickSuccessful.Value = testcase.isCorrect + "";
					elmNew.Attributes.Append(pickSuccessful);
					XmlAttribute timePassed = xmlDoc.CreateAttribute("time");
					timePassed.Value = testcase.time + "";
					elmNew.Attributes.Append(timePassed);
					XmlAttribute attemptS = xmlDoc.CreateAttribute("attempts");
					attemptS.Value = testcase.attempts + "";
					elmNew.Attributes.Append(attemptS);
					XmlAttribute repetitions = xmlDoc.CreateAttribute("repetition");
					count++;
					repetitions.Value = count+"";
					elmNew.Attributes.Append(repetitions);
					
					elm.AppendChild(elmNew);
				}
			}
			xmlDoc.Save(OUTPUT_FILE_PATH);
			Debug.Log("Writing in output XML file.");
        }
    }
}