using UnityEngine;
using System.Collections.Generic;
using System;
using System.Xml;
using System.Xml.Serialization;
using System.IO;
using System.Text;

public class Scenario
{

    private List<TestCase> openTestCases;
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
        openTestCases = new List<TestCase>();
        solvedTestCases = new List<TestCase>();
    }


    /// <summary>
    /// Adds a new testcase to the scenario.
    /// </summary>
    /// <param name="t"></param>
    public void AddTestCase(TestCase t)
    {
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
            int index = UnityEngine.Random.Range(0, openTestCases.Count);
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
        if (currentTestCase != null)
            openTestCases.Add(currentTestCase);
        currentTestCase = null;

        foreach (TestCase t in solvedTestCases)
        {
            openTestCases.Add(t);            
        }
        solvedTestCases.Clear();
        numTestCases = openTestCases.Count;
        Debug.Log("After Reset: NumTestCases: " + openTestCases.Count);
    }


    /// <summary>
    /// Solves the current TestCase. 
    /// Call this before getting the nextTestCase
    /// </summary>
    /// <param name="correct">Whether or not the testcase was solved correctly.</param>
    /// <param name="user">The ID of the user</param>
    /// <param name="tries">The number of tries</param>
    public void SolveCurrentTestCase(bool correct, string user, int attempts, float time)
    {
		currentTestCase.userID = user;
		currentTestCase.attempts = attempts;
		currentTestCase.time = time;
		currentTestCase.isCorrect=correct;
		solvedTestCases.Add(currentTestCase);
		StorageManager.Instance.WriteTestCaseResult(currentTestCase);
		currentTestCase = null;
    }


    /// <summary>
    /// Returns a List of solved TestCases
    /// </summary>
    public List<TestCase> GetSolvedTestCases
    {
        get { return solvedTestCases; }
    }
}