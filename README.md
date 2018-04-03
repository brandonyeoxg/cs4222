# Project
## Setting Up
### MQTT on python
1) sudo apt-get install python-pkg-resources=3.3-1ubuntu1 <br>
2) sudo apt-get install python-setuptools <br>
3) sudo apt-get install python-pip <br>
4) sudo pip install paho-mqtt <br>

### Main Files to look at
`main.py` is the entry point of the entire program. <br>
`activity_detection.py` is the python file that governs the activity detection.<br>
`mqtt_callback.py` is the python file that handles the use of callback mqtt.<br>