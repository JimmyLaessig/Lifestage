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

    public enum SelectMode
    {
        DEFAULT = 0, SELECT_CLOSEST = 1, SELECT_FAREST = 2 // TODO : Add other selection types
    };

    // The Selection Mode of the current scene. If set to Default a mode will be selected randomly
    public SelectMode SELECTMODE = SelectMode.DEFAULT;
    // Min number of Elements in the scene
    public int minElements = 5;
    // Number of elements in the scene. If set to 0 a random number will be generated within the min/max-Elements bounds
    public int numElements = 0;
    // Max number of elements in the scene
    public int maxElements = 20;

    // A Collection of Primitves to choose from for the scene
    public PrimitiveType[] primitiveTypes = { PrimitiveType.Cube, PrimitiveType.Sphere, PrimitiveType.Cylinder, PrimitiveType.Capsule };
    // A Collection of Materials to choose from for the primitives
    public Material[] materials = { };


    private bool isStarted = false;
    private bool isFinished = false;

    // The GameObject to select in the current scene
    private GameObject targetObj;
    // A List to store the generated GameObjects
    private List<GameObject> gameObjectList;

    private GameObject selectedObj;
    private Material selectedObjMaterial;

    public Material highlightingMaterial;


    /// <summary>
    /// Unity Callback
    /// Start is called once the script is initialized.
    /// </summary>
    void Start()
    {
        camera = GetComponentInChildren<Camera>();

        gameObjectList = new List<GameObject>();
    }

    /// <summary>
    /// Unity Callback
    /// OnGUI is called for rendering and handling GUI events.
    /// </summary>
    void OnGUI()
    {

        if (!IsRunning)
        {
            int relativeWidth = Screen.width / 5;

            int relativePosX = (Screen.width - relativeWidth) / 2;
            int relativePosY = (Screen.height) / 2;

            Rect window = new Rect(relativePosX, relativePosY, 0, 0);
            // Show GUI if scene is not yet started
            window = GUILayout.Window(0, window, OptionsWindow, "", GUILayout.Width(relativeWidth));
            GUI.skin.button.fontSize = 30;
        }

        int relativeInfoWidth = Screen.width / 4;

        Rect infoWindow = new Rect(0, 0, 0, 0);
        // Show GUI if scene is not yet started
        infoWindow = GUILayout.Window(1, infoWindow, MakeInfoWindow, "", GUILayout.Width(relativeInfoWidth));
        GUI.skin.label.fontSize = 15;

    }


    /// <summary>
    /// Creates the information overlay displaying the current SelectionMode and number of elements in the scene
    /// </summary>
    /// <param name="id"></param>
    void OptionsWindow(int id)
    {
        if (!isStarted)
        {
            if (GUILayout.Button("Start"))
            {
                ClearScene();
                GenerateScene();
                isStarted = true;
            }
        }
        // Show GUI if scene is finished
        if (isFinished)
        {
            if (GUILayout.Button("Restart"))
            {
                RestartScene();
                SelectObject(null);
            }
            GUILayout.Space(10);
            if (GUILayout.Button("Next Scene"))
            {
                ClearScene();
                GenerateScene();
                isStarted = true;
                isFinished = false;
            }
        }
    }


    /// <summary>
    /// Creates the main options window
    /// </summary>
    /// <param name="id"></param>
    void MakeInfoWindow(int id)
    {
        GUILayout.Label("<b>Number of Elements:</b>" + gameObjectList.Count.ToString());
        GUILayout.Label("<b>SelectionMode:</b>" + SELECTMODE.ToString());
        if (selectedObj)
            GUILayout.Label("Distance:" + (selectedObj.transform.position - camera.transform.position).magnitude);
        else
            GUILayout.Label("Distance: - ");

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
    /// This method is called when the user wants to finish the scene.
    /// It uses the previously selected gameObject to determine the interactions' success.
    /// Lastly it writes the result to the XML-File.
    /// </summary>
    /// <param name="time">The duration of the interaction</param>
    /// <param name="userID">The unique id of the user</param>
    /// <returns>True if all winning conditions are met (An Object was previously selected)</returns>
    public bool Finish(float time, string userID)
    {
        if (!IsRunning)
            return false;

        if (!selectedObj)
            return false;

        if (!targetObj)
        {
            Debug.Log("Target Object not set! This should not happen!");
            return false;
        }

        bool correct = false;

        if (selectedObj == targetObj)
            correct = true;

        Result r = new Result();

        r.userID = userID;
        r.correct = correct;
        r.time = time;
        r.numElements = gameObjectList.Count;
        //Result.WriteOutput(r);

        isFinished = true;

        return true;
    }


    /// <summary>
    /// Generates a scene containing previously defined number of objects.
    /// Note: Call ClearScene before generating a scene to avoid misbehaviour.
    /// </summary>
    public void GenerateScene()
    {
        int count = numElements;
        if (count <= 0)
        {
            count = Random.Range(minElements, maxElements);
        }

        Debug.Log("Creating " + count + " GameObejcts");

        // Spawn elements
        for (int i = 0; i < count; i++)
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
            GameObject obj = GameObject.CreatePrimitive(type);
            obj.transform.position = position;
            obj.transform.rotation = rotation;
            obj.GetComponent<Renderer>().material = material;

            gameObjectList.Add(obj);

            targetObj = obj;    // TODO: Change this: Set TargetObject to actual target specified by SelectMode
        }
    }

    /// <summary>
    /// Clear the current scene and destroys all elements within this scene.
    /// </summary>
    public void ClearScene()
    {
        SelectObject(null);

        // Remove all GameObjects from the scene to make room for the new scene
        for (int i = 0; i < gameObjectList.Count; i++)
        {
            GameObject obj = gameObjectList[i];
            gameObjectList.RemoveAt(i);
            Destroy(obj);
        }
    }


    /// <summary>
    /// Restarts the scene
    /// </summary>
    public void RestartScene()
    {
        isStarted = true;
        isFinished = false;
    }


    /// <summary>
    /// Returns true whether the scene is curently running (e.g. selection of an object is possible
    /// </summary>
    public bool IsRunning
    {
        get { return (isStarted && !isFinished); }
    }
}