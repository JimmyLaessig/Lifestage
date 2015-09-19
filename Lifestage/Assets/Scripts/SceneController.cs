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

    // The GameObejct that describes the spawn area
    public GameObject spawnArea;

    private bool isStarted = false;
    private bool isFinished = false;

    // The GameObject to select in the current scene
    private GameObject targetObj;

    // A List to store all gameObjects in the scene
    private List<GameObject> gameObjectList;

    public PrimitiveType[] primitiveTypes = { PrimitiveType.Cube, PrimitiveType.Sphere, PrimitiveType.Cylinder, PrimitiveType.Capsule };
    public Material[] materials = { };




    // Use this for initialization
    void Start()
    {
        if (!spawnArea)
        {
            Debug.Log("Scene must have a defined spawn area");
            return;
        }
        if (!spawnArea.GetComponent<MeshRenderer>())
        {
            Debug.Log("Spawn area must have a defined geometry mesh");
            return;
        }

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
            if (GUI.Button(new Rect(new Vector2(300, 300), new Vector2(100, 20)), "Reset"))
            {
                ResetScene();
            }
            if (GUI.Button(new Rect(new Vector2(300, 500), new Vector2(100, 20)), "Next Scene"))
            {
                ClearScene();
                GenerateScene();
                isStarted = true;
            }
        }
    }


    // Update is called once per frame
    void Update()
    {

    }

    //
    // This method is called when the scene is finished
    // 
    public bool FinishScene(GameObject selectedObj, float time, string userID)
    {
        if (!targetObj)
        {
            Debug.Log("Target Object not set! This should not happen!");
            return false;
        }

        bool correct = false;

        if (!selectedObj)
            correct = false;

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

            Bounds bounds = spawnArea.GetComponent<MeshFilter>().mesh.bounds;
            // Position
            Vector3 position;
            position.x = Random.Range(bounds.min.x, bounds.max.x);
            position.y = Random.Range(bounds.min.x, bounds.max.y);
            position.z = Random.Range(bounds.min.z, bounds.max.z);

            position.Scale(spawnArea.transform.localScale);
            position += spawnArea.transform.position;

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
            targetObj = obj;
        }
    }


    public void ClearScene()
    {
        // Remove all gameObjects from the scene to make room for the new scene
        for (int i = 0; i < gameObjectList.Count; i++)
        {
            GameObject obj = gameObjectList[i];
            gameObjectList.RemoveAt(i);
            Destroy(obj);
        }

        isStarted = false;
        isFinished = false;
    }


    // Resets the scene to the last state
    public void ResetScene()
    {
        isStarted = true;
        isFinished = false;
    }


    public bool IsStarted
    {
        get { return isStarted; }
    }


    public bool IsFinished
    {
        get { return isFinished; }
    }
}
