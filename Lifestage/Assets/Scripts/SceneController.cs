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

    private Scenario scenario;
    private TestCase currentTestCase = null;

    // A Collection of Primitves to choose from for the scene
    public PrimitiveType[] primitiveTypes = { PrimitiveType.Cube, PrimitiveType.Sphere, PrimitiveType.Cylinder, PrimitiveType.Capsule };
    // A Collection of Materials to choose from for the primitives
    public Material[] materials = { };

    private bool inputEnabled = false;
    private bool loadNextTestCase = false;
    private bool performReset = false;


    // The GameObject to select in the current scene
    private GameObject targetObject;
    // A List to store the generated GameObjects
    private List<GameObject> gameObjects;

    private GameObject selectedObj;
    private Material selectedObjMaterial;

    public Material highlightingMaterial;
    private GameObject boundingVolume;



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

        gameObjects = new List<GameObject>();

        scenario = StorageManager.Instance.LoadScenario();

        boundingVolume = GameObject.Find("BoundingVolume");

        PluginManager.Instance.SetMaxDistance = MaxDistance;
    }

    /// <summary>
    /// Unity Callback for Button click
    /// </summary>
    public void StartButtonClicked()
    {
        loadNextTestCase = true;
        performReset = true;
        PluginManager.Instance.InitBaseRotation();
    }


    void SetMessageText()
    {
        // if (!inputEnabled)
        //     return;

        string txt = "";
        if (currentTestCase != null)
        {
            txt = "Select ";
            if (currentTestCase.targetElementIndex == 0)
                txt += "closest Element";
            else if (currentTestCase.targetElementIndex == 1)
                txt += "second closest Element";
            else if (currentTestCase.targetElementIndex == 2)
                txt += "third closest Element";
            else if (currentTestCase.targetElementIndex == currentTestCase.numElements - 1)
                txt += " farthermost Element";
            else
                txt += (currentTestCase.targetElementIndex + 1) + "th closest Element";
        }
        else
        {
            txt = "Press Start to load TestCases!";
        }

        UIController.Instance.SetMessageField(txt, Color.black);
    }


    void Update()
    {       
        if (performReset)
        {
            Debug.Log("Performing Reset");
            Reset();
            performReset = false;
        }
        if (loadNextTestCase)
        {
            loadNextTestCase = false;
            if (!StartNextTestCase())
            {
                Debug.Log("Cannot start new TestCase! Need to Reset it");
                performReset = true;
                inputEnabled = false;
            }
        }
        SetMessageText();
        UIController.Instance.ShowStartButton(!inputEnabled);
        UIController.Instance.ShowCancelButton(inputEnabled);
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
		PluginManager.Instance.SetVibroMode(currentTestCase.vibroMode);
        return true;
    }


    /// <summary>
    /// Resets the scene to its initial state.
    /// </summary>
    public void Reset()
    {
        StorageManager.Instance.ClearTestCaseProgress();

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

        // Remove all GameObjects from the scene to make room for the new scene
        foreach (GameObject obj in gameObjects)
        {
            Destroy(obj);
        }
        gameObjects.Clear();
    }


    public void CancelTestCase(string userID, int attempts)
	{
        Debug.Log("Canceling TestCase after " + attempts + " attempts");
		scenario.SolveCurrentTestCase(false, userID, attempts, 0);
        UIController.Instance.ShowCorrectMarker(false);
        loadNextTestCase = true;
    }

    /// <summary>
    /// This method is called when the user wants to finish the scene.
    /// It uses the previously selected gameObject to determine the interactions' success.
    /// Lastly it writes the result to the XML-File.
    /// </summary>
    /// <param name="time">The duration of the interaction</param>
    /// <param name="userID">The unique id of the user</param>
    /// <returns>True if all winning conditions are met (An Object was previously selected)</returns>
    public bool SolveTestCase(float time, string userID, int attempts)
    {
        bool isCorrect = false;

        if (selectedObj == targetObject)
        {
            isCorrect = true;
            scenario.SolveCurrentTestCase(true, userID, attempts, time);
            loadNextTestCase = true;

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

        Quaternion rotation = Random.rotation;

        Bounds bounds = boundingVolume.GetComponent<MeshFilter>().mesh.bounds;
        // Calculate the maximum distance
        float maxZ = MaxDistance;
        // calculate the absolute max Height
        float maxY = bounds.max.y * this.transform.localScale.y;
        float maxX = bounds.max.x * this.transform.localScale.x;

        // Spawn elements
        for (int i = 0; i < testCase.numElements; i++)
        {
            // Calculate distance of object
            float currentDistance = maxZ * (i + 1)  / testCase.numElements;

            // Calculate maximum height based on current distance
            float currentMaxHeight = Mathf.Min(currentDistance, maxY);

            // Calculate random height of the object
            float y = Random.Range(-currentMaxHeight, currentMaxHeight);

            // Calculate random width of the object

            // Calculate current maxWidth based on the radius of a circle
            float currentMaxWidth = Mathf.Min(Mathf.Sqrt(currentDistance * currentDistance - y * y), maxX);

            float x = Random.Range(-currentMaxWidth, currentMaxWidth);

            float z = Mathf.Sqrt(currentDistance * currentDistance - y * y - x * x);
            // float x = Mathf.Sqrt()
            // Calculate random 


            // Calculate Random coordinateson 
            //  Vector2 xz = Random.insideUnitCircle.normalized * radius;

            Vector3 position = new Vector3(x, y, Mathf.Abs(z));
            GameObject obj = GameObject.CreatePrimitive(PrimitiveType.Cube);
            obj.transform.rotation = rotation;
            obj.transform.position = position;
            gameObjects.Insert(i, obj);
        }
        targetObject = gameObjects[testCase.targetElementIndex];
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
    /// returns the maximum distance of an object, relatively to this GameObject position
    /// </summary>
    public float MaxDistance
    {
        get
        {
            Bounds bounds = boundingVolume.GetComponent<MeshFilter>().mesh.bounds;
            float distance = bounds.max.z * this.transform.localScale.z + boundingVolume.transform.position.z - this.transform.position.z;

            Debug.Log("Distance: " + distance);
            Debug.Log("Scene Area: " + this.transform.position);
            Debug.Log("Bounding Volume: " + boundingVolume.transform.position);
            Debug.Log("Bounds.size: " + bounds);

            return distance;
        }
    }
}