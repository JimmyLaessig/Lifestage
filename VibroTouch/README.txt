#vibrotouch_testcases.xml:
sizes/x/y/deviation are all in millimeters
! vibrotouch_output.xml and vibrotouch_testcases.xml have to be on the devices internal storage !

##testcase 1:
posX, posY is the center of the object

output:
	-time: measured in seconds after popup is closed
	-errors: how often a user has tried to put an object on a wrong target space
	-screenPlacements: how often objects were put on white screen again
	-deviation: deviation of placed object center and target space center in mm


##testcase 2:
size: mm
output:
	-time: measured in seconds after popup is closed
	-deviation: deviation of original and new object area size


#IMPORTANT!!!! FILL IN screenWidth AND screenHeight IN MM FOR CORRECT MEASUREMENT

____________________________________________
INPUT EXAMPLE:

<SparkCore id="48ff71065067555011472387" token="20e1ee31e3f0b0ecace2820c73ab71f5966acbf6"/>
<Testcases screenWidth="65" screenHeight="115" minObjectSize="10" maxObjectSize="25">
	<Testcase id="0" scenario="0" button="on" minIntensity="50" maxIntensity="100">
		<Object size="10" posX="35" posY="25"/>
		<Object size="20" posX="15" posY="25"/>
		<Object size="25" posX="35" posY="55"/>
	</Testcase>
	<!-- ^ Playground Testcase -->
	<!-- v Other Testcases -->
	<Testcase id="1" scenario="1" button="on" minIntensity="50" maxIntensity="100">
		<Object size="20" posX="25" posY="25"/>
		<Object size="10" posX="45" posY="25"/>
	</Testcase>
	<Testcase id="6" scenario="2" button="on" minIntensity="10" maxIntensity="100">
		<Object size="30"/>
	</Testcase>
</Testcases>

____________________________________________
OUTPUT EXAMPLE:

<Output>
	<Textcase userId="1" id="6" scenario="1" time="6.125" errors="2" screenPlacements="0" deviationObject0="3.2165" deviationObject1="2.654"/>
	<Textcase userId="2" id="22" scenario="2" time="2.302" devation="4.201"/>
</Output>