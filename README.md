# Project
## Setting Up
### MQTT on python
1) sudo apt-get install python-pkg-resources=3.3-1ubuntu1 <br>
2) sudo apt-get install python-setuptools <br>
3) sudo apt-get install python-pip <br>
4) sudo pip install paho-mqtt <br>

### Main files to look at
`main.py` is the entry point of the entire program. <br>
`activity_detector.py` governs the activity detection. <br>
`walk_detector.py` detects if the person is walking or not. <br>
`indoor_detector.py` detects if the person is indoor or not. <br>
`floor_detector.py` detects if the person has changed floor or not. <br>
`mqtt_callback.py` handles the use of callback mqtt. <br>