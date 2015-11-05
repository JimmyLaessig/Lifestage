using UnityEngine;
using System.Collections;

/// <summary>
/// This class encapsulates a TestCase for an interaction scene. 
/// It contains information on the duration of the interaction, the setting of the scene, the success of the interaction, as well as information on the users id.
/// </summary>
public class TestCase
{
	private static int ID_COUNTER = 0;

    public int id;
	public int numElements;
	public int targetElement;
	public bool isCorrect;
	public float time;
    public float interactionTime;
	public int attempts;
	public string userID;
    public int seed;
    public Vector3 sceneScale;
	public int[] phoneIntensity;
	public int[] vibroIntensity;

	public TestCase()
	{
		phoneIntensity=new int[2];
		vibroIntensity=new int[2];
		id = ID_COUNTER;
		ID_COUNTER++;
	}
}
