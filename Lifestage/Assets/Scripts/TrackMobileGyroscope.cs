using UnityEngine;
using System.Collections;

/// <summary>
/// This script is used to track the cameras' orientation using the OrientationTracker service.
/// </summary>
public class TrackMobileGyroscope : MonoBehaviour
{

    public static string ANDROID_GYROSCOPE_ACTIVITY = "at.ac.tuwien.ims.lifestage.vibro.MainActivity";
    public GameObject parent;

    private AndroidJavaObject pluginActivity;

    private bool initRotation = true;

    private Vector3 initRotationEuler;


    // Use this for initialization
    void Start()
    {
        using (AndroidJavaClass jc = new AndroidJavaClass(ANDROID_GYROSCOPE_ACTIVITY))
        {
            pluginActivity = jc.GetStatic<AndroidJavaObject>("context");
        }
    }


    // Update is called once per frame
    void Update()
    {


        // Get the latest rotation matrix from the Receiver
        float[] rotation = pluginActivity.Call<float[]>("getOrientationMatrix");
        // num = rotation.Length;
        // num = activity.Call<int>("getConnectionState");
        // Debug.Log("numrotation elements: " + rotation.Length);
        if (rotation.Length <= 0)
            return;
        Matrix4x4 rotationMatrix = Matrix4x4.identity;
  
        rotationMatrix.SetRow(0, new Vector4(rotation[0], rotation[1], rotation[2], rotation[3]));
        rotationMatrix.SetRow(1, new Vector4(rotation[4], rotation[5], rotation[6], rotation[7]));
        rotationMatrix.SetRow(2, new Vector4(rotation[8], rotation[9], rotation[10], rotation[11]));
        rotationMatrix.SetRow(3, new Vector4(rotation[12], rotation[13], rotation[14], rotation[15]));
       
        // Transform the rotation matrix to a quaternion
       Vector3 euler = ExtractRotationFromMatrix(ref rotationMatrix).eulerAngles;
       this.transform.eulerAngles = new Vector3(euler.y, -euler.x, euler.z);
    }


    /// <summary>
    /// Extract rotation quaternion from transform matrix.
    /// Referenced on http://forum.unity3d.com/threads/how-to-assign-matrix4x4-to-transform.121966/
    /// Found on https://github.com/lordofduct/spacepuppy-unity-framework/blob/master/SpacepuppyBase/Utils/TransformUtil.cs
    /// </summary>
    /// <param name="matrix">Transform matrix. This parameter is passed by reference
    /// to improve performance; no changes will be made to it.</param>
    /// <returns>
    /// Quaternion representation of rotation transform.
    /// </returns>
    public static Quaternion ExtractRotationFromMatrix(ref Matrix4x4 matrix)
    {
       return Quaternion.LookRotation(matrix.GetColumn(2), matrix.GetColumn(1));
    }
}