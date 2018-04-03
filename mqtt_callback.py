import paho.mqtt.client as mqtt

class DummyMsg:
	def __init__(self, topic, payload):
		self.topic = topic
		self.payload = payload

def begin_mqtt_client(hostname, username, password, on_connect, on_message):
	client = mqtt.Client()
	client.username_pw_set(username, password)
	client.on_connect = on_connect
	client.on_message = on_message
	client.connect(hostname)
	client.loop_forever()

def begin_mqtt_client_driver(filename, on_message):
	f = open(filename, 'r')
	for line in f:
		msg = DummyMsg('dummyClient', line)
		on_message('dummyClient', 'dummyUserData', msg)
	f.close()