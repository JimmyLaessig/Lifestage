using UnityEngine;
using System.Collections;
/// <summary>
/// This class handles the control of the camera whithin the scene as well as user input for the selection interaction.
/// </summary>
[RequireComponent(typeof(Camera))]
public class CameraController : MonoBehaviour
{

    public SceneController sceneController;

    private PluginManager pluginManager;

    private LineRenderer rayRenderer;


    // The unique id of the user
    private string userID = "todo";
    public Vector3 offsetToScene = new Vector3(0, 0, -1);
    // Offset to the ray position such that the ray does not go through the exact center of the camera
    public float rayOffset = -0.5f;

    // The maximum distance of the Raycast
    public float rayMaxDistance = 50.0f;

    // The timeStamp of the start of a touch event
    private float startTime;
    // Determines whether a Raycast should be performed
    private bool doRaycast = false;


    private GameObject selectedObj;

    private int attempts = 0;

    /// <summary>
    /// Unity Callback
    /// Start is called once the script is initialized.
    /// </summary>
    void Start()
    {
#if !UNITY_EDITOR
        
            this.transform.Translate(offsetToScene);
            pluginManager = GetComponent<PluginManager>();
            if (!pluginManager)
                Debug.Log("CameraController must have a child attached with a PluginManager-Script!!");
        
#endif
        rayRenderer = GetComponent<LineRenderer>();
        rayRenderer.enabled = false;
    }


    /// <summary>
    /// Unity Callback
    /// Update is called once per frame
    /// </summary>
    void Update()
    {
#if UNITY_EDITOR
        this.transform.Rotate(Vector3.up, Input.GetAxis("Horizontal") * 90 * Time.deltaTime);
        this.transform.Rotate(Vector3.right, Input.GetAxis("Vertical") * 90 * Time.deltaTime);
#endif


        // Only Process the Input while the scene is running.
        if (!sceneController.InputEnabled)
            return;

        // Process Touch Input
        GetInput();

        // Perform a raycast if the finger is currently touching the screen 
        selectedObj = null;
        if (doRaycast)
            selectedObj = PerformRaycast();

        sceneController.SelectObject(selectedObj);

        // Perform Vibration Feedback based on the distance to the object
        float distance = -1;
        if (selectedObj)
            distance = (this.transform.position - selectedObj.transform.position).magnitude;
#if !UNITY_EDITOR
        pluginManager.SetDistance = distance;
#endif
    }


    /// <summary>
    /// This function processes the input for the interaction
    /// </summary>
    private void GetInput()
    {
        // First Touch ( is the deepest).     
#if UNITY_EDITOR
        if (Input.GetKey(KeyCode.Space) && !doRaycast)
#else
        if (Input.touchCount > 0 && !doRaycast)
#endif
        {
            Debug.Log("Start selection");
            startTime = Time.time;
            doRaycast = true;
            rayRenderer.enabled = true;
        }

        // Touch event ended.
#if UNITY_EDITOR
        if (!Input.GetKey(KeyCode.Space) && doRaycast)
#else
        if (Input.touchCount <= 0 && doRaycast)
#endif
        {
            Debug.Log("End selection");
            attempts++;
            float timePassed = Time.time - startTime;
            doRaycast = false;
            rayRenderer.enabled = false;
            // Solve the TestCase if an object was selected
            if (selectedObj)
            {
                if(sceneController.SolveTestCase(timePassed, userID, attempts))
                    attempts = 0;
            }
        }
    }


    /// <summary>
    /// Performs a raycast from in the look direction from the current camera position with the previously defined offset
    /// </summary>
    /// <returns>Returns a GameObject if the ray hits it, otherwhise NULL.</returns>
    private GameObject PerformRaycast()
    {
        Vector3 rayStart = this.gameObject.transform.position + this.gameObject.transform.up * rayOffset;

        rayRenderer.SetPosition(0, rayStart);
        rayRenderer.SetPosition(1, rayStart + this.gameObject.transform.forward * rayMaxDistance);

        RaycastHit hit = new RaycastHit();
        Ray ray = new Ray(rayStart, this.gameObject.transform.forward);
        if (Physics.Raycast(ray, out hit, rayMaxDistance))
        {
            rayRenderer.SetPosition(1, hit.point);
            return hit.collider.gameObject;
        }
        return null;
    }

    void OnGUI()
    {
        // Cancel Button pressed
        if (!sceneController.InputEnabled)
            return;
        if (GUI.Button(new Rect(Screen.width - 40, 0, 40, 40), "x"))
        {
            sceneController.CancelTestCase("",attempts);   // TODO Set real values           
        }
    }
}