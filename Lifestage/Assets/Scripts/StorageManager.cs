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
	private static string OUTPUT_FILE_PATH = "/sdcard/lifestege_output.xml";
#else
    private static string SCENARIO_FILE_PATH = "lifestage_testcases.xml";
	private static string OUTPUT_FILE_PATH = "lifestage_output.xml";
#endif
    private static string SOLVED_TESTCASES_KEY = "solvedCasesKey";

    private Exception ex = null;

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


    void OnGUI()
    {
        if (ex != null)
        {
            GUI.contentColor = Color.red;
            Rect box = new Rect(200, 200, 200, 200);
            GUI.Label(box, SCENARIO_FILE_PATH + " not found or is invalid! Please make sure that the corresponding file exists and is well formed!");
        }
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
				// TODO: Read all necessary stuff from the .xml file
                t.numElements = Convert.ToInt32(list[i].Attributes["numElements"].Value);
                t.targetElementIndex = Convert.ToInt32(list[i].Attributes["targetElement"].Value) - 1;
                scenario.AddTestCase(t);
            }
        }
        catch (Exception ex)
        {
            UIController.Instance.SetMessageField(SCENARIO_FILE_PATH + " not found or is invalid! Please make sure that the corresponding file exists and is well formed!", Color.red);
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
    /// This method writes the results of a TestCase to the corresponding XML-File.
    /// </summary>
	public void WriteTestCaseResult(Scenario scenario) {
		if (scenario.GetSolvedTestCases.Count == 0)
			return;

		XmlDocument xmlDoc = new XmlDocument();
        if (File.Exists (OUTPUT_FILE_PATH)) {            
			xmlDoc.Load (OUTPUT_FILE_PATH);
			XmlElement elmRoot = xmlDoc.DocumentElement;
			
			int user_id = 0;
			if(elmRoot.LastChild != null) {
				int id = Int32.Parse(elmRoot.LastChild.Attributes["userID"].Value);
				user_id=id+1;
			}
			XmlElement elm = xmlDoc.CreateElement("Result");

			XmlAttribute userId = xmlDoc.CreateAttribute("userID"); 
			userId.Value = user_id+"";
			elm.Attributes.Append(userId);

			foreach(TestCase testcase in scenario.GetSolvedTestCases) {
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

				elm.AppendChild(elmNew);
			}

			elmRoot.AppendChild(elm);
            xmlDoc.Save(OUTPUT_FILE_PATH);
        }
    }

	public void ResetTestCaseResult() {
		XmlDocument xmlDoc = new XmlDocument();
		if (File.Exists (OUTPUT_FILE_PATH)) {   
			xmlDoc.Load(OUTPUT_FILE_PATH);
			XmlElement elmRoot = xmlDoc.DocumentElement;
			elmRoot.RemoveAll();
			xmlDoc.Save(OUTPUT_FILE_PATH);
		}
	}
}