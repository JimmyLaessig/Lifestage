using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class UIController : MonoBehaviour {


    private Canvas canvas;
    private Button startButton;
    private Button cancelButton;
    private Text messageField;
    private Image correctImage;
    private Image incorrectImage;
    private static UIController instance;

    // Show marker for two seconds
    private float showMarkerDuration = 2.0f;
    private float startTime = 0;
    public static UIController Instance
    {
        get { return instance; }
    }


    void Awake()
    {
        if(!instance)
         instance = this;
    }

	// Use this for initialization
	void Start () {
        canvas = GetComponent<Canvas>();
        
        startButton = GameObject.Find("StartButton").GetComponent<Button>();
        cancelButton = GameObject.Find("CancelButton").GetComponent<Button>();
        messageField = GameObject.Find("MessageText").GetComponent<Text>();
       
        correctImage = GameObject.Find("CorrectMarker").GetComponent<Image>();
       
        incorrectImage = GameObject.Find("IncorrectMarker").GetComponent<Image>();

        correctImage.enabled = false;
        incorrectImage.enabled = false;
	}

    public void ShowCancelButton(bool enabled)
    {
        cancelButton.gameObject.SetActive(enabled);
    }
    public void ShowStartButton(bool enabled)
    {       
        startButton.gameObject.SetActive(enabled);
    }


    public void SetMessageField(string text, Color color)
    {
        messageField.text = text;
        messageField.color = color;
    }

    public void ShowCorrectMarker(bool correct)
    {
        startTime = Time.time;
        if (correct)
        {
            correctImage.enabled = true;
            incorrectImage.enabled = false;
        }
        else 
        {
            correctImage.enabled = false;
            incorrectImage.enabled = true;
        }
    }


	// Update is called once per frame
	void Update () 
    {
        // Hide Marker once time is up or the user touches the screen
        
        if (Time.time - startTime >= showMarkerDuration || Input.touchCount > 0)
        {
            correctImage.enabled = false;
            incorrectImage.enabled = false;
            startTime = 0;
        }
	}
}
