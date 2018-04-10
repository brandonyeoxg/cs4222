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