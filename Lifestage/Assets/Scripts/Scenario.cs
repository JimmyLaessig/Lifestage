using UnityEngine;
using System.Collections.Generic;
using System.Xml;
using System.Xml.Serialization;
using System.IO;
using System.Text;

public class Scenario
{
    private List<TestCase> openTestCases;
    private List<TestCase> allTestCases;
    private TestCase currentTestCase;
    private List<TestCase> solvedTestCases;

    private int numTestCases = 0;

    public Vector3 sceneScale;


    public int NumTestCases
    {
        get { return numTestCases; }
    }

    public int NumTestCasesLeft
    {
        get { return openTestCases.Count; }
    }


    /// <summary>
    /// Creates an empty scenario
    /// </summary>
    public Scenario()
    {
        allTestCases = new List<TestCase>();
        openTestCases = new List<TestCase>();
        solvedTestCases = new List<TestCase>();
    }


    /// <summary>
    /// Adds a new testcase to the scenario.
    /// </summary>
    /// <param name="t"></param>
    public void AddTestCase(TestCase t)
    {
        allTestCases.Add(t);
        openTestCases.Add(t);
    }


    /// <summary>
    /// Returns the next testcase. It chooses randomly a testcase from the remaining testcases. 
    /// If no testcase is available, it returns NULL. 
    /// Be sure to call SolveTestCase() before getting the next testcase in order to prevent any errors.
    /// </summary>
    /// <returns></returns>
    public TestCase GetNextTestCase()
    {
        if (openTestCases.Count <= 0)
        {
            Debug.Log("No Testcases available, returning NULL");
            currentTestCase = null;
        }
        else
		{
            Debug.Log("Returning new TestCase");
            int index = Random.Range(0, openTestCases.Count);
            currentTestCase = openTestCases[index];
            openTestCases.RemoveAt(index);
        }
        return currentTestCase;
    }


    /// <summary>
    /// Returns the current testcase of the scenario, or NULL if no testcase is available
    /// </summary>
    /// <returns></returns>
    public TestCase GetCurrentTestCase()
    {
        return currentTestCase;
    }


    /// <summary>
    /// Resets the scenario to the initial state. 
    /// Note: This call deletes all information about solved test cases
    /// </summary>
    public void Reset()
    {
        currentTestCase = null;
        openTestCases.Clear();
        solvedTestCases.Clear();
        openTestCases = new List<TestCase>(allTestCases);

        numTestCases = openTestCases.Count;
        Debug.Log("After Reset: NumTestCases: " + openTestCases.Count);
    }


    /// <summary>
    /// Solves the current TestCase. 
    /// Call this before getting the nextTestCase
    /// </summary>
    /// <param name="correct">Whether or not the testcase was solved correctly.</param>
    /// <param name="user">The ID of the user</param>
    /// <param name="attempts">The number of attempts</param>
    /// <param name="timeTotal">The duration of the testcase</param>
    /// <param name="interactionTime"> The duration of the last interaction</param>
    public void SolveCurrentTestCase(bool correct, string user, int attempts, float timeTotal, float interactionTime, int seedValue)
    {
		currentTestCase.userID = user;
		currentTestCase.attempts = attempts;
		currentTestCase.time = timeTotal;
        currentTestCase.interactionTime = interactionTime;
		currentTestCase.isCorrect=correct;
        currentTestCase.seedValue = seedValue;
		solvedTestCases.Add(currentTestCase);
		StorageManager.Instance.WriteTestCaseResult(currentTestCase);
		currentTestCase = null;
        Debug.Log("total time: " + timeTotal);
    }


    /// <summary>
    /// Returns a List of solved TestCases
    /// </summary>
    public List<TestCase> GetSolvedTestCases
    {
        get { return solvedTestCases; }
    }
}