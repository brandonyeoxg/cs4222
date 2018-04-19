# Project
## Setting Up
### Installing Java 8
1) sudo apt-get install python-software-properties
2) sudo add-apt-repository ppa:webupd8team/java
3) sudo apt-get update
4) sudo apt-get install oracle-java8-installer
5) sudo gedit /etc/environment
	* Add `JAVA_HOME="/usr/lib/jvm/java-8-oracle"` into the file 
6) source /etc/environment
	* Verify with `echo $JAVA_HOME` and `java -version`

### Main files to look at
`MainApp.java` is the entry point of the entire program. <br>
`ActivityDetector.java` governs the activity detection. <br>
`WalkDetector.java` detects if the person is walking or not. <br>
`IndoorDetector.java` detects if the person is indoor or not. <br>
`FloorDetector.java` detects if the person has changed floor or not. <br>

### Building the project
To build the project we just need to run `./make_java` <br>

### Running the project
To run the project we just need to run `./run_java`. <br>

### Running the experiment
1) Go to `ocean.comp.nus.edu.sg` to schedule **project-receiver-27648** for about 15mins~
2) On a new terminal `./run_java` to run our `MainApp`
3) Flash the `unicast_send_from_usb.bin` into sensortag
4) On a fresh terminal run `./emulator_script`
	- you can change the data set to run inside `./emulator_script`
5) Press the reset button (one of the side buttons on sensor tag) to start
	- If it jams, just reflash `unicast_send_from_usb.bin` into sensortag

**If all else fails.... read the project pdf again!**