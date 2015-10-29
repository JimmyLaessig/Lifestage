#VIBRO
###Lifestage
Das Unity Projekt.  

###Vibro
Beinhaltet den Service für die Verbindung zwischen Android und dem SparkCore sowie den OrientationService.  

####Verwendung
1. Das Projekt am Smartphone installieren.
2. Das Smartphone mit Wlan verbinden
3. Sparkcore mit Vibros bereitstellen und mit dem Wlan verbinden.  
4. lifestage_testcases.xml muss auf den Massenspeicher des Handys kopiert werden oder in den Projekt-Folder des Untiy Projektmodes für den Playmode.  
5. lifestage_output.xml file muss auf den Massenspeicher des Handys kopiert werden oder in den Projekt-Folder des Untiy Projektmodes für den Playmode.  
6. spark_auth.txt muss auf den Massenspeicher des Handys kopiert werden oder in den Projekt-Folder des Untiy Projektmodes für den Playmode.  

#### Aufbau des lifestage_testcases.xml
Das Root-Element ist TestCaseCollection mit dem Attribut repetitions, dass die Anzahl der Wiederholungen der gesamten Testreihe beschreibt
TestCase-Element besteht aus:
numElements: die Anzahl der Elemente
targetElement: der Index des auszuwählenden Elements
		NOTE: Der ABGERUNDETE Index numElements/2 wird textuell als mittleres Element beschrieben
IntensityMin/IntensityMax:
	phoneIntensityMin: Minimalste Intensität der Phone Vibration.
	phoneIntensityMax: Maximalste Intensität der Phone Vibration.
	vibroIntensityMin: Minimalste Intensität der Externen Vibration.
	vibroIntensityMax: Maximalste Intensität der Externen Vibration.
	Wenn man nur Externe oder nur Phone Vibration benützen will, dann stellt man den jeweils anderen Maximalwert auf 0 (z.B. Nur Phone Vibration zwischen 30% und 70%:  phoneIntensityMin="30" phoneIntensityMax="70" vibroIntensityMin="0" vibroIntensityMax="0"). Werte nur zwischen 0 und 100.
Skalierung: 
	scaleX: Beschreibt die Skalierung der Szene in x-Richtung
	scaleY: Beschreibt die Skalierung der Szene in y-Richtung
	scaleZ: Beschreibt die Skalierung der Szene in z-Richtung: Auf diesem Wert beruht die Berechnung der Maximal-Distanz der Objekte
	Sind dieser Werte nicht gesetzt wird die Default-Skalierung (40, 10, 20) verwendet
	Die Skalierung beeinflusst nicht die Größe der Elemente
