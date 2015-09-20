using UnityEngine;
using System.Collections.Generic;




public class SceneController : MonoBehaviour
{

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

    void Start()
    {     
        gameObjectList = new List<GameObject>();
    }


    void OnGUI()
    {
        // Show GUI if scene is not yet started
        if (!isStarted)
        {

            if (GUI.Button(new Rect(new Vector2(300, 300), new Vector2(100, 20)), "Start"))
            {
                ClearScene();
                GenerateScene();
                isStarted = true;
            }
        }
        // Show GUI if scene is finished
        if (isFinished)
        {
            if (GUI.Button(new Rect(new Vector2(300, 330), new Vector2(100, 20)), "Restart"))
            {
                RestartScene();
            }
            //if (GUI.Button(new Rect(new Vector2(300, 360), new Vector2(100, 20)), "Next Scene"))
            //{
            //    ClearScene();
            //    GenerateScene();
            //    isStarted = false;
            //    isFinished = false;
            //}
        }
    }


    // Call this funtion when an object was selected. 
    // Sets the GameObject to a selected state, highlighting it in the scene.
    // if the given GameObject is NULL the function reset the previous selected GameObject to its default state
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


    // This method is called when the user wants to finish the scene.
    // It uses the previously selected gameObject to determine the interactions' success.
    // Lastly it writes the result to the XML-File.
    public bool Finish( float time, string userID)
    {
        Debug.Log("Finish Called!");
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
        Result.WriteOutput(r);


        isFinished = true;

        return true;
    }


    // Generates a scene containing previously defined number of objects
    // Note: Call ClearScene before generating a scene to avoid misbehaviour
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


    public void ClearScene()
    {
        // Remove all GameObjects from the scene to make room for the new scene
        for (int i = 0; i < gameObjectList.Count; i++)
        {
            GameObject obj = gameObjectList[i];
            gameObjectList.RemoveAt(i);
            Destroy(obj);
        }
        isStarted = false;
        isFinished = false;
    }


    // Resets the scene to the generated state
    public void RestartScene()
    {
        isStarted = true;
        isFinished = false;
    }


    // Returns true whether the scene is curently running
    public bool IsRunning
    {
        get { return (isStarted && !isFinished); }
    }
}