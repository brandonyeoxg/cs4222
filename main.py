import mqtt_callback as mqtt
from activity_detection import ActivityDetection

activity = ActivityDetection()

def on_connect(client, userdata, flags, rc):
	print('Connected with result code ' + str(rc))
	client.subscribe('#')

def on_message(client, userdata, msg):
	print('[Topic %s] has payload: %s' % (msg.topic, str(msg.payload)))
	activity.insertNewData(msg.payload)
	print('Current Data Count: %d' % (len(activity.getData())))

if __name__ == '__main__':
	hostname = 'ocean.comp.nus.edu.sg'
	username = 'cs4222.team13@gmail.com'
	password = 'cs4222team13'
	filename = 'walk_1_prepro/data_collect_2018_03_20_13_52_12.csv'
	#mqtt.begin_mqtt_client(hostname, username, password, on_connect, on_message)
	mqtt.begin_mqtt_client_driver(filename, on_message)

	print('Data len of the data: %d' % (len(activity.getData())))
	for dataline in activity.getData():
		print (dataline)