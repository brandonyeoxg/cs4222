import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

public class MainApp {
	private static final String password = "fUDqUGVy0XAjftpC";
	private static final String userName = "cs4222.team13@gmail.com";
	private static final String brokerUrl = "ocean.comp.nus.edu.sg";
	private static final String clientId = "";
	private static final boolean cleanSession = true;
	private static final boolean quietMode = true;

	public static void main(String args[]) {
		System.out.println("Hello");
		ActivityDetector detector = new ActivityDetector();

		try {
			MqttAsyncCallback mqtt = new MqttAsyncCallback(brokerUrl, clientId, cleanSession, quietMode, userName, password);	
		} catch (MqttException me) {
			System.out.println("Mqtt init problem!");
			System.exit(-1);
		}
		

		// MQTT code
	}

	public static void setupMqtt() {
	}
}