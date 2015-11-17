using UnityEngine;
using System;
using System.Collections;
using System.Collections.Generic;

/// <summary>
/// This script is used to track the cameras' orientation using the OrientationTracker service and provides an interface to the VibroModule
/// </summary>
public class PluginManager : MonoBehaviour
{
    private static PluginManager instance;
    public static PluginManager Instance
    {
        get { return instance; }
    }

    public static string MAIN_ACTIVITY_CLASS_NAME = "at.ac.tuwien.ims.lifestage.vibro.MainActivity";

#if !UNITY_EDITOR
	private static string SPARKAUTH_FILE_PATH = "/sdcard/spark_auth.txt";
#else
    private static string SPARKAUTH_FILE_PATH = "spark_auth.txt";
#endif

    private AndroidJavaObject pluginActivity;

    private Quaternion gyroRotation = Quaternion.identity;
    private float initRotationY;

    // Distance for Vibration Feedback
    private float distance = -1, maxDistance = 20, minDistance = 0;

    // Intervall in which the vibrations of a certain pattern are triggered in seconds
    private float vibrationIntervall = 0.201f, timeStamp = 0;
    private int duration = 150, pauseAfter = 40;
    private bool phoneVibration = false;

    private List<long[]> intensityPattern = new List<long[]>();
    private float intensityVibrateTimeLeft = 0.0f;

    private int[] phoneIntensity;
    private int[] vibroIntensity;

    #region [Unity Callback Methods]

    void Awake()
    {
        if (!PluginManager.instance)
            PluginManager.instance = this;
    }

    // Use this for initialization
    void Start()
    {
        Input.gyro.enabled = true;
        using (AndroidJavaClass jc = new AndroidJavaClass(MAIN_ACTIVITY_CLASS_NAME))
        {
            pluginActivity = jc.GetStatic<AndroidJavaObject>("context");
        }
        for (int i = 1; i <= 10; ++i)
        {
            intensityPattern.Add(new long[2] { 10 - i, i * 5 });
        }
        ConnectToSparkCore();
        phoneIntensity = new int[2];
        vibroIntensity = new int[2];
    }

    // Update is called once per frame
    void Update()
    {
#if !UNITY_EDITOR
		UpdateCameraRotation();
#endif
        UpdateVibration();
    }

    #endregion

    #region[Public Methods]

    /// <summary>
    /// Sets the distance that is used to calculate the vibration pattern.
    /// Setting its value to a negative value will disable the vibration
    /// </summary>
    public float SetDistance
    {
        set { distance = value; }
    }

    public float SetMaxDistance
    {
        set { maxDistance = value; }
    }

    public float SetMinDistance
    {
        set { minDistance = value; }
    }

    public void setIntensities(int[] intensity1, int[] intensity2)
    {
        phoneIntensity = intensity1;
        vibroIntensity = intensity2;
    }

    /// <summary>
    /// Returns the button state of the button attached tothe sparkcore
    /// </summary>
    /// <returns></returns>
    public bool GetButtonState()
    {
        if (pluginActivity != null)
            return pluginActivity.Call<bool>("getButtonState");
        return false;
    }

    /// <summary>
    /// Sets the current y rotation as base rotation
    /// This base rotation is then substracted from the current rotation such that the camera will look in the z direction.
    /// </summary>
    public void InitBaseRotation()
    {
        this.initRotationY = -gyroRotation.eulerAngles.y;
    }

    #endregion[Public Methods]

    #region[Private Methods]

    /// <summary>
    /// Connects the phone to the SparkCore
    /// </summary>
    private void ConnectToSparkCore()
    {
        try
        {
            String text = System.IO.File.ReadAllText(SPARKAUTH_FILE_PATH);
            if (text != null && text != string.Empty)
            {
                string[] lines = text.Split(new string[] { Environment.NewLine }, StringSplitOptions.None);
                if (lines[0].Contains("spark_auth"))
                {
                    string[] line = lines[1].Split(' ');
                    if (line.Length == 2)
                    {
                        pluginActivity.Call("connect", line);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            Debug.Log("Something went wrong with the spark authentication file... falling back to static id and token.");
            string[] param = new string[2];
            param[0] = pluginActivity.GetStatic<string>("id");
            param[1] = pluginActivity.GetStatic<string>("token");
            pluginActivity.Call("connect", param);
        }
    }

    /// <summary>
    /// Disconnects the phone from the SparkCore.
    /// </summary>
    private void DisconnectFromSparkCore()
    {
        if (pluginActivity != null)
            pluginActivity.Call("disconnect");
    }

    /// <summary>
    /// Returns the status between the device and the SparkCore.
    /// CONNECTED = 1;
    /// CONNECTING = 2;
    /// NOT_CONNECTED = 3;
    /// </summary>
    private int GetConnectionStateFromCore()
    {
        if (pluginActivity == null)
            return 3;
        return pluginActivity.Call<int>("getConnectionState");
    }

    /// <summary>
    /// Updates this transforms rotation to the data received from the gyroscope
    /// </summary>
    private void UpdateCameraRotation()
    {
        // Get the latest rotation matrix from the Receiver
        // float[] rotation = pluginActivity.Call<float[]>("getOrientationMatrix");
        // if (rotation == null)
        //    return;

        // Transform the rotation matrix to a quaternion
        // Vector3 euler = GetRotationFromMatrixArray(rotation).eulerAngles;
        // gyroRotation.eulerAngles = new Vector3(euler.y, -euler.x, euler.z); 

        gyroRotation = Quaternion.Slerp(gyroRotation,
           ConvertRotation(Quaternion.Inverse(Quaternion.Euler(90, 0, 0)) * Input.gyro.attitude), 0.8f);

        transform.rotation = gyroRotation;

        transform.Rotate(Vector3.up, initRotationY, Space.World);
    }

    /// <summary>
    /// Updates the vibration module
    /// </summary>
    private void UpdateVibration()
    {
        //intensity vibration on phone
        if (phoneVibration && intensityVibrateTimeLeft < 0.0f)
        {
            Debug.Log("Cancel vibration");
            CancelVibrationOnPhone();
            phoneVibration = false;
        }
        if (intensityVibrateTimeLeft > 0.0f)
        {
            intensityVibrateTimeLeft -= Time.deltaTime;
        }

        timeStamp += Time.deltaTime;
        if (distance >= 0 && timeStamp >= vibrationIntervall)
        {
            timeStamp = 0;
            if (phoneIntensity[1] == 0 && vibroIntensity[1] == 0)
            { //NOTHING
                //do nohing
            }
            else if (phoneIntensity[1] > 0 && vibroIntensity[1] > 0)
            { //VIBRO AND PHONE
                float halfdist = minDistance + ((maxDistance - minDistance) / 2f);
                if (distance < halfdist)
                {
                    VibratePhone(
                        linearInterpolationPhone(phoneIntensity[0], phoneIntensity[1], minDistance, halfdist, distance),
                        duration);
                }
                else
                {
                    SendVibrationToCore(0,
                                        linearInterpolationVibro(vibroIntensity[0], vibroIntensity[1], halfdist, maxDistance, distance),
                                        linearInterpolationVibro(vibroIntensity[0], vibroIntensity[1], halfdist, maxDistance, distance),
                                        duration, pauseAfter);
                }
            }
            else if (phoneIntensity[1] > 0 && vibroIntensity[1] == 0)
            { //ONLY PHONE
                VibratePhone(
                    linearInterpolationPhone(phoneIntensity[0], phoneIntensity[1], minDistance, maxDistance, distance),
                    duration);
            }
            else if (phoneIntensity[1] == 0 && vibroIntensity[1] > 0)
            { //ONLY VIBRO
                SendVibrationToCore(0,
                                    linearInterpolationVibro(vibroIntensity[0], vibroIntensity[1], minDistance, maxDistance, distance),
                                    linearInterpolationVibro(vibroIntensity[0], vibroIntensity[1], minDistance, maxDistance, distance),
                                    duration, pauseAfter);
            }
        }
    }

    /// <summary>
    /// Computes the linear interpolation of the given parameters for phone vibration.
    /// </summary>
    private int linearInterpolationPhone(int intensityMin, int intensityMax, float distanceMin, float distanceMax, float currentDist)
    {
        float result = (((float)intensityMin + (((float)intensityMax - (float)intensityMin) / (distanceMax - distanceMin)) * (currentDist - distanceMin)) - 10f) / 10f;
        //Debug.Log("phone: distmin: " + distanceMin +", distmax: " + distanceMax +", currentdist: "+ currentDist +", result: " + (10-(int)Math.Round(result, 0)));
        int intres = (int)Math.Round(result, 0);
        if (result < 0 || (10 - intres < 0))
            return 0;
        return 10 - intres;
    }

    /// <summary>
    /// Computes the linear interpolation of the given parameters for vibro external vibration.
    /// </summary>
    private int linearInterpolationVibro(int intensityMin, int intensityMax, float distanceMin, float distanceMax, float currentDist)
    {
        float result = (float)intensityMax - (((float)intensityMax - (float)intensityMin) / (distanceMax - distanceMin)) * (currentDist - distanceMin);
        Debug.Log("vibro: distmin: " + distanceMin + ", distmax: " + distanceMax + ", currentdist: " + currentDist + " intensityMin: " + intensityMin + " intensityMax: " + intensityMax + " result new: " + ((int)Math.Round(result, 0)));
        int intres = (int)Math.Round(result, 0);
        if (intres < 0)
            return 0;
        else if (intres > 100)
            return 100;
        return intres;
    }

    /// <summary>
    /// Sends a vibrationPattern to the SparkCore
    /// <param name="actuatorId">id of the actuator to use (if vibros shows north, 0 is left, 1 is right)</param>
    /// <param name="intensity">intesity of vibration</param>
    /// <param name="endingIntensity">ending intesity (interpolaton), set 0 for normal vibration</param>
    /// <param name="duration">duration of vibration</param>
    /// <param name="pauseAfter">pause after vibbration</param>
    /// </summary>
    private void SendVibrationToCore(int actuatorId, int intensity, int endingIntensity, int duration, int pauseAfter)
    {
        if (GetConnectionStateFromCore() == 1)
        {
            bool a = pluginActivity.Call<bool>("sendEvent", actuatorId, intensity, endingIntensity, duration, pauseAfter);
            if (!a)
            {
                Debug.Log("Error while vibrating actuators.");
            }
        }
    }

    /// <summary>
    /// Check whether or not the phone has a VibrationModule attached.
    /// </summary>
    /// <returns></returns>
    private bool HasVibrator()
    {
        if (pluginActivity == null)
            return false;
        return pluginActivity.Call<bool>("hasVibrator");
    }


    /// <summary>
    /// Vibrates device for given amount of ms.
    /// </summary>
    /// <param name="milliseconds">time in ms to vibrate</param>
    private void VibratePhone(long milliseconds)
    {
        if (HasVibrator())
        {
            CancelVibrationOnPhone();
            pluginActivity.Call("vibrate", milliseconds);
        }
    }


    /// <summary>
    /// Vibrates the device with a given pattern for a give amount of times.
    /// </summary>
    /// <param name="pattern">pattern that used for vibration</param>
    /// <param name="repeat">times that the pattern will be repeated (0=repeat indefinitely, -1=no repeat)</param>
    private void VibratePhone(long[] pattern, int repeat)
    {
        if (HasVibrator())
        {
            CancelVibrationOnPhone();
            pluginActivity.Call("vibrate", pattern, repeat);
        }
    }


    /// <summary>
    /// Vibrate at the specified intensity.
    /// </summary>
    /// <param name="milliseconds">time in ms to vibrate</param>
    /// <param name="intensity">intensity of the vibration (0..9)</param>
    private void VibratePhone(int intensity, long milliseconds)
    {
        if (HasVibrator())
        {
            if (intensity >= 0 && intensity < 10)
            {
                phoneVibration = true;
                VibratePhone(intensityPattern[intensity], 0);
                intensityVibrateTimeLeft = milliseconds / 1000.0f;
                return;
            }
        }
    }

    /// <summary>
    /// Cancels the current vibration
    /// </summary>
    private void CancelVibrationOnPhone()
    {
        pluginActivity.Call("cancelVibration");
    }

    #endregion

    #region[Static Helper Functions]


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

    /// <summary>
    /// Converts the Rotation from left handed to right handed.
    /// </summary>
    private static Quaternion ConvertRotation(Quaternion q)
    {
        return new Quaternion(q.x, q.y, -q.z, -q.w);
    }

    #endregion
}