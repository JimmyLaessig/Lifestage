using UnityEngine;
using System.Collections;
/// <summary>
/// This class handles the control of the camera whithin the scene as well as user input for the selection interaction.
/// </summary>
[RequireComponent(typeof(Camera))]
public class CameraController : MonoBehaviour
{

    public SceneController sceneController;

    private TrackMobileGyroscope gyroTracker;

    private LineRenderer rayRenderer;

    private VibrationProvider vibrationProvider;

    // The unique id of the user
    private string userID = "";
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


    public bool useKeyBoard = false;



    /// <summary>
    /// Unity Callback
    /// Start is called once the script is initialized.
    /// </summary>
    void Start()
    {
        if (!useKeyBoard)
        {
            this.transform.Translate(offsetToScene);
            gyroTracker = GetComponentInChildren<TrackMobileGyroscope>();
            if (!gyroTracker)
                Debug.Log("CameraController must have a child attached with a TrackCamera-Script!!");
        }
        rayRenderer = GetComponent<LineRenderer>();
        rayRenderer.enabled = false;

        vibrationProvider = GetComponentInChildren<VibrationProvider>();
        if (!vibrationProvider)
            Debug.Log("CameraController must have a child attached with a VibrationProvider!!");
    }


    /// <summary>
    /// Unity Callback
    /// Update is called once per frame
    /// </summary>
    void Update()
    {
        if (useKeyBoard)
        {
            this.transform.Rotate(Vector3.up, Input.GetAxis("Horizontal") * 90 * Time.deltaTime);
            this.transform.Rotate(Vector3.right, Input.GetAxis("Vertical") * 90 * Time.deltaTime);
        }
        else
        {
            // Get the latest rotation of the camera Tracker
            if (gyroTracker)
                this.transform.rotation = gyroTracker.transform.rotation;
        }

        // Only Process the Input while the scene is running.
        if (!sceneController.IsRunning)
            return;

        // Process Touch Input
        GetInput();

        // Perform a raycast if the finger is currently touching the screen 
        selectedObj = null;
        if (doRaycast)
            selectedObj = PerformRaycast();

        sceneController.SelectObject(selectedObj);

        // Perform Vibration Feedback based on the distance to the object
        if (selectedObj)
        {
            float distance = (this.transform.position - selectedObj.transform.position).magnitude;
            vibrationProvider.CalculateVibrationPattern(distance);
        }
    }


    /// <summary>
    /// This function processes the input for the interaction
    /// </summary>
    private void GetInput()
    {
        if (useKeyBoard)
        {

            // First Touch ( is the deepest).        
            //if (Input.touchCount > 0 && !doRaycast)
            if (Input.GetKey(KeyCode.Space) && !doRaycast)
            {
                Debug.Log("Start selection");
                startTime = Time.time;
                doRaycast = true;
                rayRenderer.enabled = true;
            }

            // Touch event ended.
            // if (Input.touchCount <= 0 && doRaycast)
            if (!Input.GetKey(KeyCode.Space) && doRaycast)
            {
                Debug.Log("End selection");
                float timePassed = Time.time - startTime;
                doRaycast = false;
                rayRenderer.enabled = false;
                sceneController.Finish(timePassed, userID);
            }
        }
        else
        {

            // First Touch ( is the deepest).        
            if (Input.touchCount > 0 && !doRaycast)
            {
                Debug.Log("Start selection");
                startTime = Time.time;
                doRaycast = true;
                rayRenderer.enabled = true;
            }

            // Touch event ended.
            if (Input.touchCount <= 0 && doRaycast)
            {
                Debug.Log("End selection");
                float timePassed = Time.time - startTime;
                doRaycast = false;
                rayRenderer.enabled = false;
                sceneController.Finish(timePassed, userID);
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
}