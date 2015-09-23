using UnityEngine;
using System.Collections;

/// <summary>
/// This script is used to track the cameras' orientation using the OrientationTracker service.
/// </summary>
public class TrackCamera : MonoBehaviour
{

    private AndroidJavaClass orientationTracker;
    private bool initRotation = true;

    private Vector3 initRotationEuler;


    // Use this for initialization
    void Start()
    {
        orientationTracker = new AndroidJavaClass("at.ac.tuwien.ims.lifestage.androidtounityinterface.AndroidToUnityReceiver");
        orientationTracker.CallStatic("createInstance");
    }

    void OnGUI()
    {


        bool bla = true;
        if (bla = orientationTracker.GetStatic<bool>("buttonPressed"));

        GUI.Label(new Rect(200, 200, 200, 200), "ButtonPressed: " + bla);
    }


    // Update is called once per frame
    void Update()
    {
        // Get the latest rotation matrix from the Receiver
        float[] rotation = orientationTracker.GetStatic<float[]>("orientation");

        Debug.Log("numrotation elements: " + rotation.Length);
        if (rotation.Length <= 0)
            return;
        Matrix4x4 rotationMatrix = Matrix4x4.identity;
        rotationMatrix.SetRow(0, new Vector4(rotation[0], rotation[1], rotation[2], rotation[3]));
        rotationMatrix.SetRow(1, new Vector4(rotation[4], rotation[5], rotation[6], rotation[7]));
        rotationMatrix.SetRow(2, new Vector4(rotation[8], rotation[9], rotation[10], rotation[11]));
        rotationMatrix.SetRow(3, new Vector4(rotation[12], rotation[13], rotation[14], rotation[15]));
        // Transform the rotation matrix to a quaternion
        transform.rotation = ExtractRotationFromMatrix(ref rotationMatrix);

        // The cameras rotation origin is measured based on the orientation in PortraitMode. Since we use LandscapeMode we have to rotate to 
        transform.Rotate(transform.forward, -90);

        // Save the initial orientation for compensation 
        if (initRotation)
        {
            initRotation = false;
            initRotationEuler = transform.rotation.eulerAngles;
        }

        // We want the camera at the start to be looking in the z-direction of the scene. 
        // To achieve this we have to compensate with a rotation against the initial rotation.
        transform.Rotate(-initRotationEuler);
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
        Vector3 forward;
        forward.x = matrix.m02;
        forward.y = matrix.m12;
        forward.z = matrix.m22;

        Vector3 upwards;
        upwards.x = matrix.m01;
        upwards.y = matrix.m11;
        upwards.z = matrix.m21;

        return Quaternion.LookRotation(forward, upwards);
    }
}