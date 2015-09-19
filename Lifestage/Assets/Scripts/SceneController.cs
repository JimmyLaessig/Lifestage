using UnityEngine;
using System.Collections;




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
    private Bounds bounds;

    private bool isStarted = false;
    private bool isFinished = false;

    // The GameObject to select in the current scene
    private GameObject targetObj;

    public PrimitiveType[] primitiveTypes = { PrimitiveType.Cube, PrimitiveType.Sphere, PrimitiveType.Cylinder, PrimitiveType.Capsule };
    public Material[] materials = { };


    void OnGUI()
    {
        // Show GUI if scene is not yet started
        if (!isStarted)
        {

        }
        // Show GUI if scene is finished
        if (isFinished)
        {

        }
    }

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
        // Initialize the bounding volume for the spawn area
        bounds = spawnArea.GetComponent<MeshRenderer>().bounds;
    }

    // Update is called once per frame
    void Update()
    {

    }

    //
    // This method is called when the scene is finished
    // 
    //
    public bool FinishScene(GameObject selectedObj, float time, string userID)
    {
        if (!targetObj)
        {
            Debug.Log("Target Object not set! This should not happen!");
            return false;
        }

        bool correct = true;

        if (!selectedObj)
            correct = false;

        if (selectedObj == targetObj)
            correct = true;

        Result r = new Result();
        
        r.userID = userID;
        r.correct = correct;       
        r.time = time;

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

        // Spawn elements
        for (int i = 0; i < count; i++)
        {

            Vector3 minWorld = bounds.min + spawnArea.transform.position;
            Vector3 maxWorld = bounds.max + spawnArea.transform.position;

            // Position
            Vector3 position;
            position.x = Random.Range(minWorld.x, maxWorld.x);
            position.y = Random.Range(minWorld.y, maxWorld.y);
            position.z = Random.Range(minWorld.z, maxWorld.z);

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

        }
    }

    public void reset()
    {
        isStarted = true;
        isFinished = false;
    }
}
