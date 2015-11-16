using UnityEngine;
using System.Collections.Generic;


/// <summary>
/// SceneController handles the applications major scene logic by providing generation functions
/// and winning conditions.
/// it handles UI Input and provides functionality for selecting an object, including highlighting
/// </summary>
public class SceneController : MonoBehaviour
{

    private new Camera camera;
    private CameraController cameraController;

    private Scenario scenario;
    private TestCase currentTestCase = null;

    private string userID = "-";

    // A Collection of Materials to choose from for the primitives
    public Material material;

    private bool inputEnabled = false;

    private bool performReset = false;

    private bool finished = true;
    private bool testcaseFinished = true;

    // The GameObject to select in the current scene
    private GameObject targetObject;
    // A List to store the generated GameObjects
    private List<GameObject> gameObjects;

    private GameObject selectedObj;
    private Material selectedObjMaterial;

    public Material highlightingMaterial;
    private GameObject boundingVolume;

    private int numRepetitions=-1;
	private int currentRepetition=-1;

    private float startTime;
    private bool isStarted = false;

    private int currentSeedValue;
    /// <summary>
    /// Returns true whether or not the scene currently is enabled for input
    /// </summary>
    public bool InputEnabled
    {
        get { return inputEnabled; }
    }


    /// <summary>
    /// Unity Callback
    /// Start is called once the script is initialized.
    /// </summary>
    void Start()
    {
        camera = GetComponentInChildren<Camera>();
        cameraController = GetComponentInChildren<CameraController>();
        gameObjects = new List<GameObject>();

		scenario = StorageManager.Instance.LoadScenario();       

        boundingVolume = GameObject.Find("BoundingVolume");
    }


    /// <summary>
    /// Unity Callback for Button click
    /// Starts a new circle of repetitions.
    /// </summary>
    public void StartButtonClicked()
    {
        userID = StorageManager.Instance.getLatestUserID() + "";
        numRepetitions = StorageManager.Instance.getNumberOfRepetitions();
        currentRepetition = StorageManager.Instance.getLatestRepetition();
        currentSeedValue = StorageManager.Instance.getSeedValue(currentRepetition);

        // Resetting Random Generator with new seed value
        Random.seed = currentSeedValue;
        Debug.Log("Starting new Repetition! Repetition " + (currentRepetition + 1) + " of " + numRepetitions +", new seed: " + currentSeedValue);
        
        isStarted = true;
        Reset();

        testcaseFinished = false;
        finished = false;
        ClearTestCase();
        StartNextTestCase();

        PluginManager.Instance.InitBaseRotation();
        PluginManager.Instance.setIntensities(currentTestCase.phoneIntensity, currentTestCase.vibroIntensity);
        PluginManager.Instance.SetMaxDistance = MaxDistance;
        PluginManager.Instance.SetMinDistance = MinDistance;

        inputEnabled = true;

        userID = StorageManager.Instance.getLatestUserID() + "";
    }


    /// <summary>
    /// Unity Callback for Button click
    /// Starts a new testcase.
    /// </summary>
    public void NextButtonClicked()
    {        
        currentRepetition = StorageManager.Instance.getLatestRepetition();

        testcaseFinished = false;

        ClearTestCase();
        StartNextTestCase();

        PluginManager.Instance.InitBaseRotation();
        PluginManager.Instance.setIntensities(currentTestCase.phoneIntensity, currentTestCase.vibroIntensity);
        PluginManager.Instance.SetMaxDistance = MaxDistance;
        PluginManager.Instance.SetMinDistance = MinDistance;
        inputEnabled = true;
    }


    void Update()
    {
        if (testcaseFinished && finished)
        {
            UIController.Instance.HideAll();
            UIController.Instance.ShowStartButton(true);
            /*if (isStarted)
                UIController.Instance.ShowMessageText(true, "All Testcases finished! Starting a new Repetition.", Color.black);
            else
                UIController.Instance.ShowMessageText(true, "Welcome! Starting a new Repetition.", Color.black);*/

			switch(StorageManager.Instance.getCurrentScenarioNumber()) {
			case 0:
				UIController.Instance.ShowMessageText(true, "Starting Playground Scenario.", Color.black);
				break;
			case 1:
				UIController.Instance.ShowMessageText(true, "Starting Scenario Visual Only.", Color.black);
				break;
			case 2:
				UIController.Instance.ShowMessageText(true, "Starting Scenario Phone Only.", Color.black);
				break;
			case 3:
				UIController.Instance.ShowMessageText(true, "Starting Scenario Phone and Vibros.", Color.black);
				break;
			}

			if(currentRepetition>-1)
            	UIController.Instance.ShowInfoText(false, userID, scenario.NumTestCasesLeft, scenario.NumTestCases, currentRepetition, numRepetitions);
            
			if (currentRepetition == numRepetitions - 1)
            {				
				if(StorageManager.Instance.getCurrentScenarioNumber()<3) {
					scenario=StorageManager.Instance.NextScenario();
				} /*else {
					UIController.Instance.ShowMessageText(true, "All Testcases and Repetitions finished! Thank you for participating in LifeStage.", Color.black);
				}*/
            }

            inputEnabled = false;

        }
        else if (testcaseFinished && !finished)
        {
            UIController.Instance.HideAll();
            UIController.Instance.ShowNextButton(true);
            UIController.Instance.ShowInfoText(true, userID, scenario.NumTestCasesLeft, scenario.NumTestCases, currentRepetition, numRepetitions);
            inputEnabled = false;
        }
        else if (!testcaseFinished && !finished)
        {
            UIController.Instance.HideAll();
            UIController.Instance.ShowSelectText(true, currentTestCase.targetElement, currentTestCase.numElements);
            UIController.Instance.ShowInfoText(true, userID, scenario.NumTestCasesLeft, scenario.NumTestCases, currentRepetition, numRepetitions);
            UIController.Instance.ShowCancelButton(true);
            inputEnabled = true;
        }
    }


    /// <summary>
    /// Starts the next TestCase from the scenario.
    /// </summary>
    /// <returns>True if a next TestCase can be started. If no TestCase is available it returns false.</returns>
    private bool StartNextTestCase()
    {
        ClearTestCase();
        currentTestCase = scenario.GetNextTestCase();

        if (currentTestCase == null)
            return false;

        inputEnabled = true;
        GenerateGameObjects(currentTestCase);
        startTime = Time.time;
        return true;
    }


    /// <summary>
    /// Resets the scenario to its initial state.
    /// </summary>
    public void Reset()
    {
        StorageManager.Instance.ClearTestCaseProgress();
        //if(currentRepetition == numRepetitions -1)
        //	currentRepetition = StorageManager.Instance.getCurrentRepetition();

        ClearTestCase();
        scenario.Reset();
        currentTestCase = null;
        inputEnabled = false;
    }


    /// <summary>
    /// Clear the scene from the current TestCase and destroy all elements.
    /// </summary>
    public void ClearTestCase()
    {
        // Delect the currently selected object
        targetObject = null;
        startTime = 0;

        // Remove all GameObjects from the scene to make room for the new scene
        foreach (GameObject obj in gameObjects)
            Destroy(obj);

        gameObjects.Clear();
    }


    /// <summary>
    /// Cancels the current testcase
    /// </summary>
    /// <param name="userID">The ID of the user</param>
    /// <param name="attempts">Number of attempts</param>
    public void CancelTestCase(int attempts)
    {
        Debug.Log("Canceling TestCase after " + attempts + " attempts");
        scenario.SolveCurrentTestCase(false, userID, attempts, Time.time - startTime, 0, currentSeedValue);
        UIController.Instance.ShowCorrectMarker(false);
        testcaseFinished = true;

        if (scenario.NumTestCasesLeft == 0)
        {
            finished = true;
        }
        Debug.Log("Solving Testcase: Testcases left: + " + scenario.NumTestCasesLeft);
    }


    /// <summary>
    /// This method is called when the user wants to finish the test.
    /// It uses the previously selected gameObject to determine the interactions' success.
    /// </summary>
    /// <param name="interactionTime">The duration of the interaction</param>
    /// <param name="userID">The unique id of the user</param>
    /// <returns>True if all winning conditions are met (An Object was previously selected)</returns>
    public bool SolveTestCase(float interactionTime, int attempts)
    {
        bool isCorrect = false;

        if (selectedObj == targetObject)
        {
            isCorrect = true;
            scenario.SolveCurrentTestCase(true, userID, attempts, Time.time - startTime, interactionTime, currentSeedValue);
            testcaseFinished = true;

            if (scenario.NumTestCasesLeft == 0)
            {
                finished = true;
            }
            Debug.Log("Solving Testcase: Testcases left: + " + scenario.NumTestCasesLeft);
        }
        Debug.Log("Trying to Solve TestCase: isCorrect = " + isCorrect);
        UIController.Instance.ShowCorrectMarker(isCorrect);
        return isCorrect;
    }


    /// <summary>
    /// Call this funtion when an object was selected. 
    /// Sets the GameObject to a selected state, highlighting it in the scene.
    /// If the given GameObject is NULL the function reset the previous selected GameObject to its default state.
    /// </summary>
    /// <param name="obj">The GameObject to be selected</param>
    public void SelectObject(GameObject obj)
    {
        // Restore the previous GameObject.
        if (selectedObj)
            selectedObj.GetComponent<Renderer>().material = selectedObjMaterial;

        selectedObj = obj;

        // Set the obj as selected object and save its default material
        if (obj)
        {
            selectedObjMaterial = selectedObj.GetComponent<Renderer>().material;
            selectedObj.GetComponent<Renderer>().material = highlightingMaterial;
        }
    }


    /// <summary>
    /// Generates a scene containing previously defined number of objects.
    /// Note: Call ClearScene before generating a scene to avoid misbehaviour.
    /// </summary>
    public void GenerateGameObjects(TestCase testCase)
    {       
        // Set the scale to the scale of the TestCase
        this.transform.localScale = testCase.sceneScale;

        // Calculate the Rotation of all elements
        Quaternion rotation = Random.rotation;

        // Calculate the max bounds of the spawning area
        Bounds bounds = boundingVolume.GetComponent<MeshFilter>().mesh.bounds;
        float maxZ = bounds.max.z * this.transform.localScale.z + boundingVolume.transform.position.z - this.transform.position.z;
        float maxY = bounds.max.y * this.transform.localScale.y;
        float maxX = bounds.max.x * this.transform.localScale.x;

        // Spawn elements
        for (int i = 0; i < testCase.numElements; i++)
        {
            // Create new GameObject

            GameObject obj = GameObject.CreatePrimitive(PrimitiveType.Cube);
            obj.GetComponent<Renderer>().material = material;
            obj.transform.rotation = rotation;

            bool fitted = false;

            // Calculate a position till it fits
            while (!fitted)
            {
                // Calculate distance of object
                float currentZ = maxZ * (i + 1) / testCase.numElements;

                // Calculate maximum height based on current distance
                float currentMaxY = Mathf.Min(currentZ, maxY);

                // Calculate random y of the object position
                float y = Random.Range(-currentMaxY, currentMaxY);

                // Calculate random x of the object position

                // Calculate current maxWidth based on the radius of a circle
                float currentMaxX = Mathf.Min(Mathf.Sqrt(currentZ * currentZ - y * y), maxX);
                float x = Random.Range(-currentMaxX, currentMaxX);
                float z = Mathf.Sqrt(currentZ * currentZ - y * y - x * x);


                Vector3 position = new Vector3(x, y, Mathf.Abs(z));
                obj.transform.position = position;

                // Perform Raycast to see of the object is occluded by another object
                if (obj == PerformRaycast(camera.transform.position, position))
                {
                    fitted = true;
                }
            }
            gameObjects.Insert(i, obj);
        }
        targetObject = gameObjects[testCase.targetElement - 1];

    }


    /// <summary>
    /// Returns the maximum width of the bounding volume
    /// </summary>
    public float MaxWidth
    {
        get
        {
            Bounds bounds = boundingVolume.GetComponent<MeshFilter>().mesh.bounds;
            float width = (bounds.max.x - bounds.min.y) * this.transform.localScale.x;
            return width;
        }
    }

    /// <summary>
    /// returns the maximum distance of an object, relatively to the camera.
    /// </summary>
    public float MaxDistance
    {
        get
        {
            return (gameObjects[gameObjects.Count - 1].transform.position - camera.transform.position).magnitude;
        }
    }

    /// <summary>
    /// Returns the minimum distance of an object, relatively to the camera.
    /// </summary>
    public float MinDistance
    {
        get
        {
            return (gameObjects[0].transform.position - camera.transform.position +  camera.transform.position).magnitude;
        }
    }


    /// <summary>
    /// Performs a raycast from start to target
    /// </summary>
    /// <param name="start">The start point of the ray</param>
    /// <param name="target">The target point of the ray</param>
    /// <returns>Returns the GameObject that was hit, or null if no hit occured</returns>
    private GameObject PerformRaycast(Vector3 start, Vector3 target)
    {
        RaycastHit hit = new RaycastHit();
        Ray ray = new Ray(start, target - start);
        if (Physics.Raycast(ray, out hit, Mathf.Infinity))
        {
            return hit.collider.gameObject;
        }
        return null;
    }
}