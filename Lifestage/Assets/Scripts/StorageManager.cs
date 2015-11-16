using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using System;
using System.Xml;
using System.Xml.Serialization;
using System.IO;
using System.Text;
using System.Linq;

public class StorageManager : MonoBehaviour
{
    private static StorageManager instance;
#if !UNITY_EDITOR
	private static string SCENARIO_FILE_PATH0= "/sdcard/lifestage_testcases0.xml";
	private static string SCENARIO_FILE_PATH1 = "/sdcard/lifestage_testcases1.xml";
	private static string SCENARIO_FILE_PATH2 = "/sdcard/lifestage_testcases2.xml";
	private static string SCENARIO_FILE_PATH3 = "/sdcard/lifestage_testcases3.xml";
	private static string OUTPUT_FILE_PATH = "/sdcard/lifestage_output.xml";
#else
	private static string SCENARIO_FILE_PATH1 = "lifestage_testcases0.xml";
	private static string SCENARIO_FILE_PATH0 = "lifestage_testcases1.xml";
	private static string SCENARIO_FILE_PATH2 = "lifestage_testcases2.xml";
	private static string SCENARIO_FILE_PATH3 = "lifestage_testcases3.xml";
	private static string OUTPUT_FILE_PATH = "lifestage_output.xml";
#endif
    private static string SOLVED_TESTCASES_KEY = "solvedCasesKey";

	private List<int> scenarioQueue;
	private int scenario = 0;

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
        if (!StorageManager.instance) {
			StorageManager.instance = this;

			scenarioQueue = Enumerable.Range(1, 3).ToList();
			scenarioQueue=Shuffle(scenarioQueue);
			scenarioQueue.Insert(0, 0);
		}
    }

	private List<int> Shuffle(List<int> list)  
	{  
		List<int> ret = list;
		System.Random rng = new System.Random();
		int n = ret.Count;  
		while (n > 1) {  
			n--;  
			int k = rng.Next(n + 1);  
			int value = ret[k];  
			ret[k] = ret[n];  
			ret[n] = value;  
		}  
		return ret;
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
			doc.Load(getCurrentFileName());
            XmlElement root = doc.DocumentElement;
            XmlNodeList list = root.GetElementsByTagName("TestCase");
            for (int i = 0; i < list.Count; i++)
            {
                TestCase t = new TestCase();
                t.numElements = Convert.ToInt32(list[i].Attributes["numElements"].Value);
				t.targetElement = Convert.ToInt32(list[i].Attributes["targetElement"].Value);
				t.phoneIntensity[0]=Convert.ToInt32(list[i].Attributes["phoneIntensityMin"].Value);
				t.phoneIntensity[1]=Convert.ToInt32(list[i].Attributes["phoneIntensityMax"].Value);
				t.vibroIntensity[0]=Convert.ToInt32(list[i].Attributes["vibroIntensityMin"].Value);
				t.vibroIntensity[1]=Convert.ToInt32(list[i].Attributes["vibroIntensityMax"].Value);

                Vector3 scale = new Vector3();
                scale.x = (list[i].Attributes["scaleX"] != null) ? (float)Convert.ToDouble(list[i].Attributes["scaleX"].Value) : 40.0f;
                scale.y = (list[i].Attributes["scaleY"] != null) ? (float)Convert.ToDouble(list[i].Attributes["scaleY"].Value) : 10.0f;
                scale.z = (list[i].Attributes["scaleZ"] != null) ? (float)Convert.ToDouble(list[i].Attributes["scaleZ"].Value) : 20.0f;
                t.sceneScale = scale;
                scenario.AddTestCase(t);
            }
            if (list.Count == 0)
            {
				UIController.Instance.ReportError(getCurrentFileName() + ": No TestCases found!");
            }
        }
        catch (Exception ex)
		{
			Debug.Log("Xml Loading: " + ex.ToString());
			UIController.Instance.ReportError(getCurrentFileName() + " not found or is invalid! Please make sure that the corresponding file exists and is well formed!");
        }

        return scenario;
    }

	public Scenario NextScenario()
	{
		scenario++;
		return LoadScenario();
	}

	private string getCurrentFileName() {
		switch(getCurrentScenarioNumber()) {
		case 1:
			return SCENARIO_FILE_PATH1;
			break;
		case 2:
			return SCENARIO_FILE_PATH2;
			break;
		case 3:
			return SCENARIO_FILE_PATH3;
			break;
		default:
			return SCENARIO_FILE_PATH0;
			break;
		}
	}

	public int getCurrentScenarioNumber(){
		return scenarioQueue.ElementAt(scenario);
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
	/// This method gets number of all available testcases or -1 if xml file does not exist.
	/// </summary>
	private int getNumberOfTestCases() {
		XmlDocument doc = new XmlDocument();
		if (File.Exists(getCurrentFileName())) {  
			doc.Load(getCurrentFileName());
			XmlElement root = doc.DocumentElement;
			XmlNodeList list = root.GetElementsByTagName("TestCase");
			return list.Count;
		}
		Debug.Log ("Output XML File does not exist, please make sure it does.");
		return -1;
	}


	/// <summary>
	/// This method gets the number of repetitions or -1 if xml file does not exist.
	/// </summary>
	public int getNumberOfRepetitions() {
		XmlDocument doc = new XmlDocument();
		if (File.Exists(getCurrentFileName())) {   
			doc.Load(getCurrentFileName());
			XmlElement root = doc.DocumentElement;
			return root.GetElementsByTagName("Repetition").Count;
		}
		Debug.Log ("Output XML File does not exist, please make sure it does.");
		return -1;
	}

    /// <summary>
    /// Returns the seed value for random generation for the given repetition
    /// </summary>
    public int getSeedValue(int repetition)
    {
        XmlDocument doc = new XmlDocument();
		if (File.Exists(getCurrentFileName()))
        {
			doc.Load(getCurrentFileName());
            XmlElement root = doc.DocumentElement;
            return Convert.ToInt32(root.GetElementsByTagName("Repetition").Item(repetition).Attributes["seed"].Value);
        }
        Debug.Log("Output XML File does not exist, please make sure it does.");
        return 0;
    }

	/// <summary>
	/// This method gets the latest repetition number from the xml file or 0 if xml file does not exist.
	/// </summary>
	public int getLatestRepetition() {
		XmlDocument doc = new XmlDocument();
		if (File.Exists(OUTPUT_FILE_PATH)) {   
			doc.Load(OUTPUT_FILE_PATH);
			XmlElement root = doc.DocumentElement;
			int rep = 0;
			if (root.LastChild != null) {
				if(root.LastChild.ChildNodes.Count<getNumberOfTestCases()) {
					rep = Int32.Parse(root.LastChild.Attributes["repetition"].Value);
				} else {
					rep = Int32.Parse(root.LastChild.Attributes["repetition"].Value)+1;
				}
				if (rep>=getNumberOfRepetitions()) {
					rep=0;
				}
			}
			return rep;
		}
		Debug.Log ("Output XML File does not exist, please make sure it does.");
		return 0;
	}

	/// <summary>
	/// This method gets the latest userID from the xml file or -1 if xml file does not exist.
	/// </summary>
	public int getLatestUserID() {
		XmlDocument doc = new XmlDocument();
		if (File.Exists(OUTPUT_FILE_PATH)) {            
			doc.Load(OUTPUT_FILE_PATH);
			XmlElement root = doc.DocumentElement;
			
			int user_id = 0;
			if (root.LastChild != null) {
				if(root.ChildNodes.Count % 5 == 0) {
					user_id = Int32.Parse(root.LastChild.Attributes["userID"].Value)+1;
				} else {
					user_id = Int32.Parse(root.LastChild.Attributes["userID"].Value);
				}
			}
			return user_id;
		}
		Debug.Log ("Output XML File does not exist, please make sure it does.");
		return -1;
	}


    /// <summary>
    /// This method writes the results of a TestCase to the corresponding XML-File.
    /// </summary>
	public void WriteTestCaseResult(TestCase testcase) {
		if (testcase == null)
			return;

		XmlDocument xmlDoc = new XmlDocument();
		if (File.Exists (OUTPUT_FILE_PATH)) {
			bool newResult = true;
			xmlDoc.Load (OUTPUT_FILE_PATH);

			if (xmlDoc.DocumentElement.LastChild != null)
				if (xmlDoc.DocumentElement.LastChild.ChildNodes.Count < getNumberOfTestCases())
					newResult = false;

			if (newResult) {
				XmlElement elm = xmlDoc.CreateElement ("Result");
				XmlAttribute userId = xmlDoc.CreateAttribute ("userID");
				userId.Value = getLatestUserID() + "";
				elm.Attributes.Append(userId);
				XmlAttribute repetition = xmlDoc.CreateAttribute ("repetition");
				repetition.Value = "" + getLatestRepetition();
				elm.Attributes.Append(repetition);

				XmlElement elmNew = xmlDoc.CreateElement ("TestCase");
				XmlAttribute testcaseID = xmlDoc.CreateAttribute ("testcaseID");
				testcaseID.Value = testcase.id + "";
				elmNew.Attributes.Append (testcaseID);
				XmlAttribute noElem = xmlDoc.CreateAttribute ("numElements");
				noElem.Value = testcase.numElements + "";
				elmNew.Attributes.Append (noElem);
				XmlAttribute rightObject = xmlDoc.CreateAttribute ("targetElementIndex");
				rightObject.Value = testcase.targetElement + "";
				elmNew.Attributes.Append (rightObject);
				XmlAttribute pickSuccessful = xmlDoc.CreateAttribute ("correct");
				pickSuccessful.Value = testcase.isCorrect + "";
				elmNew.Attributes.Append (pickSuccessful);
				XmlAttribute timePassed = xmlDoc.CreateAttribute ("time");
				timePassed.Value = testcase.time + "";
                elmNew.Attributes.Append(timePassed);
                XmlAttribute interactionTime = xmlDoc.CreateAttribute("interactionTime");
                interactionTime.Value = testcase.interactionTime + "";
                elmNew.Attributes.Append(interactionTime);
				XmlAttribute attemptS = xmlDoc.CreateAttribute ("attempts");
				attemptS.Value = testcase.attempts + "";
				elmNew.Attributes.Append (attemptS);
                XmlAttribute seedValue = xmlDoc.CreateAttribute("seed");
                seedValue.Value = testcase.seedValue + "";
                elmNew.Attributes.Append(seedValue);

				elm.AppendChild (elmNew);
				xmlDoc.DocumentElement.AppendChild (elm);
			} else {			
				XmlNode elm = xmlDoc.DocumentElement.LastChild;	

				XmlNodeList list = elm.ChildNodes;
				for (int i = 0; i < list.Count; i++) {
					if(Convert.ToInt32(list[i].Attributes["testcaseID"].Value)==testcase.id) {
						xmlDoc.DocumentElement.LastChild.RemoveChild(list[i]);
						break;
					}
				}
					
				XmlElement elmNew = xmlDoc.CreateElement ("TestCase");
				XmlAttribute testcaseID = xmlDoc.CreateAttribute ("testcaseID");
				testcaseID.Value = testcase.id + "";
				elmNew.Attributes.Append (testcaseID);
				XmlAttribute noElem = xmlDoc.CreateAttribute ("numElements");
				noElem.Value = testcase.numElements + "";
				elmNew.Attributes.Append (noElem);
				XmlAttribute rightObject = xmlDoc.CreateAttribute ("targetElementIndex");
				rightObject.Value = testcase.targetElement + "";
				elmNew.Attributes.Append (rightObject);
				XmlAttribute pickSuccessful = xmlDoc.CreateAttribute ("correct");
				pickSuccessful.Value = testcase.isCorrect + "";
				elmNew.Attributes.Append (pickSuccessful);
				XmlAttribute timePassed = xmlDoc.CreateAttribute ("time");
				timePassed.Value = testcase.time + "";
				elmNew.Attributes.Append (timePassed);
                XmlAttribute interactionTime = xmlDoc.CreateAttribute("interactionTime");
                interactionTime.Value = testcase.interactionTime + "";
                elmNew.Attributes.Append(interactionTime);
				XmlAttribute attemptS = xmlDoc.CreateAttribute ("attempts");
				attemptS.Value = testcase.attempts + "";
				elmNew.Attributes.Append (attemptS);
                XmlAttribute seedValue = xmlDoc.CreateAttribute("seed");
                seedValue.Value = testcase.seedValue + "";
                elmNew.Attributes.Append(seedValue);
					
				elm.AppendChild (elmNew);
			}
			xmlDoc.Save (OUTPUT_FILE_PATH);
			Debug.Log ("Writing in output XML file.");
		} else {
			Debug.Log ("Output XML File does not exist, please make sure it does.");
		}
    }
}