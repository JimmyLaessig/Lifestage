using UnityEngine;
using System.Collections;

/// <summary>
/// This script is used to track the cameras' orientation using the OrientationTracker service.
/// </summary>
public class PluginManager : MonoBehaviour
{

    public static string MAIN_ACTIVITY_CLASS_NAME = "at.ac.tuwien.ims.lifestage.vibro.MainActivity";

    private AndroidJavaObject pluginActivity;

    private bool initRotation = true;

    private Vector3 initRotationEuler;

    private float distance = -1;

    // Intervall in which the vibrations of a certain pattern are triggered in seconds
    public float vibrationIntervall = 0.5f;
    private float timeStamp = 0;

    // Use this for initialization
    void Start()
    {
        using (AndroidJavaClass jc = new AndroidJavaClass(MAIN_ACTIVITY_CLASS_NAME))
        {
            pluginActivity = jc.GetStatic<AndroidJavaObject>("context");

            //ConnectToSparkCore();
        }
    }


    void ConnectToSparkCore()
    {
        string[] param = new string[2];
        param[0] = pluginActivity.GetStatic<string>("id");
        param[1] = pluginActivity.GetStatic<string>("token");
        pluginActivity.Call("connect", param);
    }


    void DisconnectFromSparkCore()
    {
        pluginActivity.Call("disconnect");
    }


    // Update is called once per frame
    void Update()
    {
        UpdateCameraOrientation();
        // UpdateVibration();
    }


    private void UpdateVibration()
    {

        timeStamp += Time.deltaTime;
        // Reset the timeStamp to not trigger the event
        if (distance < 0)
            timeStamp = 0;

        if (timeStamp >= vibrationIntervall)
        {
            timeStamp -= vibrationIntervall;
            pluginActivity.Call("sendTestPattern");
            // TODO: Either calculate Pattern on Unity Site ( very extensive) or create Method in Vibro.jar that calculates Pattern based on distance and VibrationIntervall            
        }
    }


    private void UpdateCameraOrientation()
    {
        // Get the latest rotation matrix from the Receiver
        float[] rotation = pluginActivity.Call<float[]>("getOrientationMatrix");
        if (rotation == null)
            return;

        // Transform the rotation matrix to a quaternion
        Vector3 euler = GetRotationFromMatrixArray(rotation).eulerAngles;
        this.transform.eulerAngles = new Vector3(euler.y, -euler.x, euler.z);
    }


    /// <summary>
    /// Sets the distance that is used to calculate the vibration pattern.
    /// Setting its value to a negative value will disable the vibration
    /// </summary>
    public float SetDistance
    {
        set { distance = value; }
    }

    /// <summary>
    /// Returns the button state of the button attached tothe sparkcore
    /// </summary>
    /// <returns></returns>
    public bool GetButtonState()
    {
        return pluginActivity.Call<bool>("getButtonState");
    }


    /// <summary>
    /// Extract rotation quaternion from transform matrix.
    /// </summary>
    /// <param name="matrix">Transform matrix.</param>
    /// <returns>
    /// Quaternion representation of rotation transform.
    /// </returns>
    private static Quaternion GetRotationFromMatrixArray(float[] rotation)
    {
        Matrix4x4 rotationMatrix = Matrix4x4.identity;
        rotationMatrix.SetRow(0, new Vector4(rotation[0], rotation[1], rotation[2], rotation[3]));
        rotationMatrix.SetRow(1, new Vector4(rotation[4], rotation[5], rotation[6], rotation[7]));
        rotationMatrix.SetRow(2, new Vector4(rotation[8], rotation[9], rotation[10], rotation[11]));
        rotationMatrix.SetRow(3, new Vector4(rotation[12], rotation[13], rotation[14], rotation[15]));

        return Quaternion.LookRotation(rotationMatrix.GetColumn(2), rotationMatrix.GetColumn(1));
    }
}