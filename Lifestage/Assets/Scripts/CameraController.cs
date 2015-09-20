using UnityEngine;
using System.Collections;

public class CameraController : MonoBehaviour
{

    public SceneController sceneController;

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


    // Use this for initialization
    void Start()
    {

        // TODO:
        // Initialize Service Receiver for camera orientation
        // Initialize Service Command for vibra feedback
        // Perform Initial Rotation of the camera so that the scene will face in the z direction of the camera       
        Result r = new Result();
        r.correct = true;
        r.userID = "1";
        r.numElements = 10;
        r.time = Time.time;

    }

    // Update is called once per frame
    void Update()
    {
        // TODO: Get current camera orientation from Service Interface
        this.transform.Rotate(Vector3.up, Input.GetAxis("Horizontal") * 90 * Time.deltaTime);
        this.transform.Rotate(Vector3.right, Input.GetAxis("Vertical") * 90 * Time.deltaTime);

        // Process Touch Input
        GetInput();
    }

    // This function processes the input for the interaction
    void GetInput()
    {

        // Only Process the Input while the scene is running.
        if (!sceneController.IsRunning)
            return;

        // No touches mean no Input. 
        // if (Input.touches.Length <= 0)
        //     return;      

        // First Touch ( is the deepest).
        //  if (Input.GetTouch(0).phase == TouchPhase.Began)
        if (Input.GetMouseButtonDown(0))
        {
            //Debug.Log("Mouse down");
            startTime = Time.time;
            doRaycast = true;
        }

        // Perform a raycast if the finger is currently touching the screen 
        if (doRaycast)
        {
            //Debug.Log("Performing Raycast");
            GameObject selectedObj = PerformRaycast();
            sceneController.SelectObject(selectedObj);
            if (selectedObj)
            {
                float distance = (this.transform.position - selectedObj.transform.position).magnitude;
                DoVibration(distance);
            }
        }

        // Touch event ended.
        // if(Input.GetTouch(0).phase == TouchPhase.Ended || Input.GetTouch(0).phase == TouchPhase.Canceled)
        if (Input.GetMouseButtonUp(0))
        {
            //Debug.Log("Mouse up");
            float timePassed = Time.time - startTime;
            doRaycast = false;
            sceneController.Finish(timePassed, userID);
        }
    }


    void DoVibration(float distance)
    {
        // TODO: Trigger vibration depending on the distance and a Vibration Pattern     
    }


    // Performs a raycast from in the look direction from the current camera position with the previously defined offset
    // Returns a GameObject if the ray hits it, otherwhise NULL.
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