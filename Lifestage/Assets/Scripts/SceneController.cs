using UnityEngine;
using System.Collections.Generic;


/// <summary>
/// SceneController handles the applications major scene logic by providing generation functions
/// and winning conditions.
/// it handles UI Input and provides functionality for selecting an object, including highlighting
/// </summary>

[RequireComponent(typeof(MeshFilter))]
public class SceneController : MonoBehaviour
{

    private Camera camera;

    private Scenario scenario;
    private TestCase currentTestCase = null;
    private int attempts = 0;


    // A Collection of Primitves to choose from for the scene
    public PrimitiveType[] primitiveTypes = { PrimitiveType.Cube, PrimitiveType.Sphere, PrimitiveType.Cylinder, PrimitiveType.Capsule };
    // A Collection of Materials to choose from for the primitives
    public Material[] materials = { };

    private bool inputEnabled = false;
    private bool loadNextTestCase = false;
    private bool performReset = false;

    private bool allTestCasesFinished = false;

    // The GameObject to select in the current scene
    private GameObject targetObject;
    // A List to store the generated GameObjects
    private List<GameObject> gameObjects;

    private GameObject selectedObj;
    private Material selectedObjMaterial;

    public Material highlightingMaterial;



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
        scenario = StorageManager.Instance().LoadScenario();
    }


    /// <summary>
    /// Unity Callback
    /// OnGUI is called for rendering and handling GUI events.
    /// </summary>
    void OnGUI()
    {
        if (!inputEnabled)
        {
            int buttonWidth = 400;
            int buttonHeight = 100;

            // Start Button pressed. Start new Scenario
            if (GUI.Button(new Rect((Screen.width - buttonWidth) / 2, (Screen.height - buttonHeight) / 2, buttonWidth, buttonHeight), "Start"))
            {
                loadNextTestCase = true;
                performReset = true;
            }
        }
        else
        {


            string txt = "";
            if (currentTestCase != null)
            {
                txt = "Select ";
                if (currentTestCase.targetElementIndex == 0)
                    txt += "closest Element";
                else if (currentTestCase.targetElementIndex == 1)
                    txt += "second  closest Element";
                else if (currentTestCase.targetElementIndex == 2)
                    txt += "third closest Element";
                else
                    txt += (currentTestCase.targetElementIndex + 1) + "th closest Element";                             
            }
            else
            {
                txt = "No TestCase available!";
            }
            int infoWidth = 400;
            int infoHeight = 100;
            GUI.Box(new Rect(0, 0, infoWidth, infoHeight), txt);
        }
        //if (loadNextTestCase)
        //{
        //    int infoWidth = 400;
        //    int infoHeight = 100;
        //    // TODO CHANGE FONT FOR MESSAGE BOX
        //    GUI.Box(new Rect((Screen.width - infoWidth) / 2, infoHeight, infoWidth, infoHeight), "CORRECT");
        //}
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
        return true;
    }


    /// <summary>
    /// Resets the scene to its initial state.
    /// </summary>
    public void Reset()
    {
        StorageManager.Instance().ClearTestCaseProgress();
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

        if (selectedObj && selectedObj == targetObject)
        {
            isCorrect = true;           
            scenario.SolveCurrentTestCase(true, userID, attempts, time);
            loadNextTestCase = true;
        }

        Debug.Log("Trying to Solve TestCase: isCorrect = " + isCorrect);

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
        // Spawn elements
        for (int i = 0; i < testCase.numElements; i++)
        {

            Bounds bounds = GetComponent<MeshFilter>().mesh.bounds;
            // Position
            Vector3 position;
            position.x = Random.Range(bounds.min.x, bounds.max.x);
            position.y = Random.Range(bounds.min.x, bounds.max.y);
            position.z = Random.Range(bounds.min.z, bounds.max.z);

            position.Scale(transform.localScale);
            position += transform.position;

            // Rotation
            Quaternion rotation = Random.rotation;

            // Primitve Type
            PrimitiveType type = primitiveTypes[Random.Range(0, primitiveTypes.Length - 1)];

            // Material
            Material material = materials[Random.Range(0, materials.Length - 1)];

            // Create GameObject
            GameObject newObject = GameObject.CreatePrimitive(type);
            newObject.transform.position = position;
            newObject.transform.rotation = rotation;
            newObject.GetComponent<Renderer>().material = material;


            float distance = (newObject.transform.position - camera.transform.position).magnitude;
            int index = 0;

            // Add the new GameObject to the list sorted by distance ascending
            while (index < gameObjects.Count)
            {
                GameObject obj = gameObjects[index];
                float objDistance = (obj.transform.position - camera.transform.position).magnitude;
                if (distance < objDistance)
                    break;

                index++;
            }

            gameObjects.Insert(index, newObject);
        }

        // Set the targetObject to the index provided by the TestCase in the list sorted by distance
        targetObject = gameObjects[testCase.targetElementIndex];

        // Debug Ausgabe
        //Debug.Log("Creating TestCase ID " + testCase.id + ": NumElements = " + testCase.numElements + ", targetElementIndex =" + testCase.targetElementIndex);
        //Debug.Log("GameObjects sorted by distance: ");
        //for (int i = 0; i < gameObjectList.Count; i++)
        //{
        //    Debug.Log("Index = " + i + ": Distance = " + (gameObjectList[i].transform.position - camera.transform.position).magnitude);
        //}
        //float d = (targetObject.transform.position - camera.transform.position).magnitude;
        //Debug.Log("TargetGameObject: Index = " + testCase.targetElementIndex + ": Distance = " + d);
    }
}