using UnityEngine;
using System.Collections;

public class CameraController : MonoBehaviour
{

    public SceneController sceneController;

    // The unique id of the user
    private string userID = "";
    
    // Offset to the ray position such that the ray does not go through the exact center of the camera
    public Vector3 rayOffset = new Vector3(0, -0.5f, 0);
    public float rayMaxDistance = 100.0f;
    private Transform initTransform;

    public GameObject scene;

    private float startTime;
    bool doRaycast = false;
    // Use this for initialization
    void Start()
    {

        // TODO:
        // Initialize Service Receiver for camera orientation
        // Initialize Service Command for vibra feedback
        // Perform Initial Rotation of the scene so that the scene will face inti the -z direction of the camera       
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
        GetInput();
        
        
            this.transform.Rotate(Vector3.up, Input.GetAxis("Horizontal") * 90 * Time.deltaTime);
            this.transform.Rotate(Vector3.right, Input.GetAxis("Vertical") * 90 * Time.deltaTime);
        
        if (doRaycast)
        {
            // TODO: 
            // Draw Ray
        }
        }


    void GetInput()
    {

        if (!sceneController.IsStarted | sceneController.IsFinished)
        {
           // Debug.Log("no user input");
            return;
        }

       // if (Input.touches.Length <= 0)
       //     return;

        GameObject selectedObj = null;

        // Perform a raycast if the finger is currently touching the screen 
        if (doRaycast)
        {
            selectedObj = PerformRaycast();
           // Debug.Log("Raycast hit: " + selectedObj);
        }

        // First Touch ( is the deepest)
        if (/*Input.GetTouch(0).phase == TouchPhase.Began) ||*/ Input.GetMouseButtonDown(0))
        {
            startTime = Time.time;
            doRaycast = true;               
        }

        // Touch event ended
        if (/*Input.GetTouch(0).phase == TouchPhase.Ended || Input.GetTouch(0).phase == TouchPhase.Canceled || */ Input.GetMouseButtonUp(0))
        {
            float timePassed = Time.time - startTime;
            doRaycast = false;
          //  Debug.Log("Touch ended: " + selectedObj);
            if (selectedObj)
            {
                Debug.Log(selectedObj.name + " selected!");
                sceneController.FinishScene(selectedObj, timePassed, userID);
            }

        }
    }

    void DoVibration(GameObject focussedObject)
    {
        // TODO: Trigger Vibration
    }

    void DoLogic(GameObject selectedObject, float timePassed)
    {
        // TODO: Do Logic stuff here
    }


    private GameObject PerformRaycast()
    {
        RaycastHit hit = new RaycastHit();

        Vector3 rayStart = this.gameObject.transform.position - rayOffset;

        Ray ray = new Ray(rayStart, this.gameObject.transform.forward);
        if (Physics.Raycast(ray, out hit, rayMaxDistance))
            return hit.collider.gameObject;
        return null;
    }
}