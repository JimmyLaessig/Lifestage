using UnityEngine;
using System.Collections;

public class VibrationProvider : MonoBehaviour
{
    private AndroidJavaClass javaClass;
    private AndroidJavaObject activity;

    private int buttonState;

    // Use this for initialization
    void Start()
    {
        using (javaClass = new AndroidJavaClass("at.ac.tuwien.ims.lifestage.vibro.MainActivity"))
        {
            activity = javaClass.GetStatic<AndroidJavaObject>("context");
        }
        buttonState = -1;
    }

    // Update is called once per frame
    void Update()
    {

                buttonState = activity.Call<int>("getStatus");
            
        
    }



    public void OnGUI()
    {
        GUI.Label(new Rect(200, 200, 200, 200), "ButtonState:" + buttonState);
    }


    public void CalculateVibrationPattern(float distance)
    {
        // TODO Calculate Vibro Pattern and send to Service
    }
}