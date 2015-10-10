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
    private static string OUTPUT_FILE_PATH = "lifestege_output.xml";
#endif
    private static string SOLVED_TESTCASES_KEY = "solvedCasesKey";

    private Exception ex = null;

    /// <summary>
    /// Returns an instance of the StorageManager
    /// </summary>
    /// <returns></returns>
    public static StorageManager Instance()
    {
        return StorageManager.instance;
    }



    void Awake()
    {
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
            this.ex = ex;
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
    public void WriteTestCaseResult()
    {

    }
}