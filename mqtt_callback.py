import paho.mqtt.client as mqtt

def begin_mqtt_client(hostname, username, password, on_connect, on_message):
	client = mqtt.Client()
	client.username_pw_set(username, password)
	client.on_connect = on_connect
	client.on_message = on_message
	client.connect(hostname)
	client.loop_forever()