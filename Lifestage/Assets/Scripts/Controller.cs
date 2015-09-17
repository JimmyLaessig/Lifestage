using UnityEngine;
using System.Collections;

public class Controller : MonoBehaviour
{
    // Offset to the ray position such that the ray does not go through the exact center of the camera
    public Vector3 rayOffset = new Vector3(0, -0.5f, 0);
    public float rayMaxDistance = 100.0f;
    private Transform initTransform;

    public GameObject scene;

    private float startTime;
    bool touchStarted = false;
    // Use this for initialization
    void Start()
    {
        
        // TODO:
        // Initialize Service Receiver for camera orientation
        // Initialize Service Command for vibra feedback
        // Perform Initial Rotation of the scene so that the scene will face inti the -z direction of the camera       

    }

    // Update is called once per frame
    void Update()
    {
        // TODO: Get current camera orientation from Service Interface
        GetInput();
        if (touchStarted)
        {
            // TODO: 
            // Draw Ray
        }
    }


    void GetInput()
    {

        GameObject selectedObj = null;
        // Perform a raycast if the finger is currently touching the screen 
        if (touchStarted)
        {
            selectedObj = PerformRaycast();
        }

        // First Touch ( is the deepest)
        if (Input.GetTouch(0).phase == TouchPhase.Began)
        {
            startTime = Time.time;
            touchStarted = true;
        }

        // Touch event ended
        else if (Input.GetTouch(0).phase == TouchPhase.Ended || Input.GetTouch(0).phase == TouchPhase.Canceled)
        {
            float timePassed = Time.time - startTime;
            touchStarted = false;

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