using UnityEngine;
using System.Collections;
using UnityEngine.UI;

/// <summary>
/// This class functions as interface to the UI elements.
/// </summary>
public class UIController : MonoBehaviour
{

    private static string finishedMessage = "All tests finished! Thank you for participating in LifeStage!";

    private Button startButton;
    private Button nextButton;
    private Button cancelButton;

    private Text selectText;
    private Text messageText;
    private Text errorText;
    private GameObject messageObject;

    private Text userIDText;
    private Text testcaseText;
    private Text repetitionText;

    private Image correctImage;
    private Image incorrectImage;

    private static UIController instance;

    private bool inputEnabled;

    // Show marker for two seconds
    private float showMarkerDuration = 1.5f;
    private float startTime = 0;


    /// <summary>
    /// Returns an instance of the UIController
    /// </summary>
    public static UIController Instance
    {
        get { return instance; }
    }


    void Awake()
    {
        if (!instance)
            instance = this;

        inputEnabled = true;

        startButton = GameObject.Find("StartButton").GetComponent<Button>();
        nextButton = GameObject.Find("NextButton").GetComponent<Button>();
        cancelButton = GameObject.Find("CancelButton").GetComponent<Button>();

        messageObject = GameObject.Find("MessageField");
        messageText = GameObject.Find("MessageText").GetComponent<Text>();
        errorText = GameObject.Find("ErrorText").GetComponent<Text>();
        selectText = GameObject.Find("SelectText").GetComponent<Text>();

        userIDText = GameObject.Find("UserIDText").GetComponent<Text>();
        testcaseText = GameObject.Find("TestcaseText").GetComponent<Text>();
        repetitionText = GameObject.Find("RepetitionText").GetComponent<Text>();
        
        correctImage = GameObject.Find("CorrectMarker").GetComponent<Image>();
        incorrectImage = GameObject.Find("IncorrectMarker").GetComponent<Image>();

        correctImage.enabled = false;
        incorrectImage.enabled = false;

        HideAll();
    }


    /// <summary>
    /// Displays an error message and disables all other text elements
    /// </summary>
    /// <param name="msg">The error message.</param>
    public void ReportError(string msg)
    {
        ShowMessageText(false, "", Color.black);
        messageObject.SetActive(enabled);
        errorText.text = msg;
        errorText.color = Color.red;

        startButton.enabled = false;
        nextButton.enabled = false;
        cancelButton.enabled = false;
        inputEnabled = false;
    }


    /// <summary>
    /// Shows/hides the Next Button on/from the screen.
    /// </summary>
    public void ShowNextButton(bool enabled)
    {
        if (!inputEnabled)
            return;
        nextButton.gameObject.SetActive(enabled);
    }


    /// <summary>
    /// Shows/hides the Start Button on/from the screen.
    /// </summary>
    public void ShowStartButton(bool enabled)
    {
        if (!inputEnabled)
            return;

        startButton.gameObject.SetActive(enabled);
    }


    /// <summary>
    /// Shows/hides the Cancel Button on/from the screen.
    /// </summary>
    public void ShowCancelButton(bool enabled)
    {
        if (!inputEnabled)
            return;
        cancelButton.gameObject.SetActive(enabled);
    }


    /// <summary>
    /// Shows/hides the Info Text on/from the screen.
    /// </summary>
    /// <param name="enabled">shows/hides the text</param>
    /// <param name="userID">The users ID</param>
    /// <param name="numTestCasesLeft">the index of the current test case</param>
    /// <param name="numTestCases">The number of testcases</param>
    /// <param name="repetition">the current repetition</param>
    public void ShowInfoText(bool enabled, string userID, int numTestCasesLeft, int numTestCases, int repetition)
    {
        if (!inputEnabled)
            return;
        userIDText.text = "UserID: <b>" + userID + "</b>";
        testcaseText.text = (enabled) ? "Testcase: <b>" + (numTestCases - numTestCasesLeft) + " / " + numTestCases + "</b>" : "-";
        repetitionText.text = "Repetition: <b>" + (repetition) + "</b>" ;
    }


    /// <summary>
    /// Shows/hides the Message Text on/from the screen.
    /// </summary>
    /// <param name="enabled">shows/hides the field</param>
    /// <param name="txt">The text to be displayed</param>
    /// <param name="color">The colorof the text</param>
    public void ShowMessageText(bool enabled, string txt, Color color)
    {
       
        if (!inputEnabled)
            return;
        messageObject.SetActive(enabled);
        messageText.text = txt;
        messageText.color = color;
    }


    /// <summary>
    /// Shows/hides the finished message.
    /// </summary>
    /// <param name="enabled"></param>
    public void ShowFinishedMessage(bool enabled)
    {
        if (!inputEnabled)
            return;
        ShowMessageText(enabled, finishedMessage, Color.black);
    }


    /// <summary>
    /// Shows/hides the Selet Text on/from the screen.
    /// </summary>
    /// <param name="enabled">Shows/hides the text</param>
    /// <param name="selectIndex">The index of the element to select</param>
    /// <param name="numElements">The number of elements</param>
    public void ShowSelectText(bool enabled, int selectIndex, int numElements)
    {
        string txt = "";
        Debug.Log("Showing Message Text: selectIndex: " + selectIndex);
        if (selectIndex == 1)
            txt = "Select the closest Element";
        else if (selectIndex == 2)
            txt = "Select the second closest Element";
        else if (selectIndex == 3)
            txt = "Select the third closest Element";
        else if (selectIndex == numElements)
            txt = "Select the furthermost Element";
        else if (selectIndex == (int)(numElements / 2))
            txt = "Select the Element in the Middle";
        else if (selectIndex > 0)
            txt = "Select " + selectIndex + "th Element";
        Debug.Log("SelectText should be: " + txt);
        selectText.enabled = enabled;
        selectText.text = txt;
    }


    /// <summary>
    /// Shows a marker on the screen.
    /// </summary>
    /// <param name="correct">Shows a green check on if true, otherwhise a red x</param>
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


    /// <summary>
    /// Update is called once per frame.
    /// </summary>
    void Update()
    {

        // Hide Marker once time is up or the user touches the screen
        if (Time.time - startTime >= showMarkerDuration || Input.touchCount > 0)
        {
            correctImage.enabled = false;
            incorrectImage.enabled = false;
            startTime = 0;
        }
    }

    /// <summary>
    /// Hides all UI elements
    /// </summary>
    public void HideAll()
    { 
        startButton.gameObject.SetActive(false);
        nextButton.gameObject.SetActive(false);
        cancelButton.gameObject.SetActive(false);

            ShowMessageText(false, "", Color.black);

        ShowSelectText(false, 0, 0);
        ShowInfoText(false, "-", 0, 0, 0);    
    }
}
