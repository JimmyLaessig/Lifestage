#VIBRO
###Lifestage
Das Unity Projekt.  

###Vibro
Beinhaltet den Service für die Verbindung zwischen Android und dem SparkCore sowie den OrientationService.  

####Verwendung
1. Das Projekt am Smartphone installieren.
2. Das Smartphone mit Wlan verbinden
3. Sparkcore mit Vibros bereitstellen und mit dem Wlan verbinden.  
4a. lifestage_testcases.xml muss auf den Massenspeicher des Handys kopiert werden oder in den Projekt-Folder des Untiy Projektmodes für den Playmode.  
4b. VibroModes für lifestage_testcases.xml: Both, SmartphoneOnly, VibroOnly, None.  
5. lifestage_output.xml file muss auf den Massenspeicher des Handys kopiert werden oder in den Projekt-Folder des Untiy Projektmodes für den Playmode.  
6. spark_auth.txt muss auf den Massenspeicher des Handys kopiert werden oder in den Projekt-Folder des Untiy Projektmodes für den Playmode.  

#### Aufbau des lifestage_testcases.xml
Das Root-Element ist TestCaseCollection mit dem Attribut repetitions, dass die Anzahl der Wiederholungen der gesamten Testreihe beschreibt
TestCase-Element besteht aus:
numElements: die Anzahl der Elemente
targetElement: der Index des auszuwählenden Elements
		NOTE: Der ABGERUNDETE Index numElements/2 wird textuell als mittleres Element beschrieben
vibroMode: Both, SmartphoneOnly, VibroOnly, None
Skalierung: 
	scaleX: Beschreibt die Skalierung der Szene in x-Richtung
	scaleY: Beschreibt die Skalierung der Szene in y-Richtung
	scaleZ: Beschreibt die Skalierung der Szene in z-Richtung: Auf diesem Wert beruht die Berechnung der Maximal-Distanz der Objekte
	Sind dieser Werte nicht gesetzt wird die Default-Skalierung (40, 10, 20) verwendet
	Die Skalierung beeinflusst nicht die Größe der Elemente
