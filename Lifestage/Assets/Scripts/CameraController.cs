using UnityEngine;
using System.Collections;
/// <summary>
/// This class handles the control of the camera whithin the scene as well as user input for the selection interaction.
/// </summary>
[RequireComponent(typeof(Camera))]
public class CameraController : MonoBehaviour
{

    public SceneController sceneController;

    private TrackCamera trackCamera;

    private VibrationProvider vibrationProvider;
    // The unique id of the user
    private string userID = "";

    // Offset to the ray position such that the ray does not go through the exact center of the camera
    public Vector3 rayOffset = new Vector3(0, -0.5f, 0);

    // The maximum distance of the Raycast
    public float rayMaxDistance = 100.0f;

    // The timeStamp of the start of a touch event
    private float startTime;
    // Determines whether a Raycast should be performed
    private bool doRaycast = false;


    /// <summary>
    /// Unity Callback
    /// Start is called once the script is initialized.
    /// </summary>
    void Start()
    {
        trackCamera = GetComponentInChildren<TrackCamera>();
        if (!trackCamera)
            Debug.Log("CameraController must have a child attached with a TrackCamera-Script!!");


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
        // Get the latest rotation of the camera Tracker
        if (trackCamera)
            this.transform.rotation = trackCamera.transform.rotation;

        // Process Touch Input
        GetInput();
    }


    /// <summary>
    /// This function processes the input for the interaction
    /// </summary>
    void GetInput()
    {
        // Only Process the Input while the scene is running.
        if (!sceneController.IsRunning)
            return; 

        // First Touch ( is the deepest).        
        if (Input.GetMouseButtonDown(0))
        {           
            startTime = Time.time;
            doRaycast = true;
        }

        // Perform a raycast if the finger is currently touching the screen 
        if (doRaycast)
        {            
            GameObject selectedObj = PerformRaycast();
            sceneController.SelectObject(selectedObj);
            if (selectedObj)
            {
                float distance = (this.transform.position - selectedObj.transform.position).magnitude;
                vibrationProvider.CalculateVibrationPattern(distance);
            }
        }

        // Touch event ended.
       
        if (Input.GetMouseButtonUp(0))
        {
            float timePassed = Time.time - startTime;
            doRaycast = false;
            sceneController.Finish(timePassed, userID);
        }
    }


    /// <summary>
    /// Performs a raycast from in the look direction from the current camera position with the previously defined offset
    /// </summary>
    /// <returns>Returns a GameObject if the ray hits it, otherwhise NULL.</returns>
    private GameObject PerformRaycast()
    {
        // TODO Draw Ray

        RaycastHit hit = new RaycastHit();
        Vector3 rayStart = this.gameObject.transform.position - rayOffset;
        Ray ray = new Ray(rayStart, this.gameObject.transform.forward);
        if (Physics.Raycast(ray, out hit, rayMaxDistance))
            return hit.collider.gameObject;
        return null;
    }
}