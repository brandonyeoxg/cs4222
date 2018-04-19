import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.io.File;
import java.util.Scanner;

public class MainApp {
	private static final String password = "fUDqUGVy0XAjftpC";
	private static final String userName = "cs4222.team13@gmail.com";
	private static final String brokerUrl = "tcp://ocean.comp.nus.edu.sg:1883";
	private static final String clientId = "";
	private static final String topic = "#";
	private static final int qos = 2;
	private static final boolean cleanSession = true;
	private static final boolean quietMode = false;
	private static final long timeInterval = 1000;
    private static long lastChangedTime = 0;
    private static boolean changedState = false;
    private static boolean ableToChange = false;

	public static void main(String args[]) {
		// realExecution();
		testExecution();
	}

	private static void realExecution() {
		final ActivityDetector detector = new ActivityDetector();
		try {
			MqttAsyncCallback mqtt = new MqttAsyncCallback(brokerUrl, clientId, cleanSession, quietMode, userName, password, detector);
			// Compute after every x time
			Runnable runnable = new Runnable() {
				public void run() {
					while(true) {
                        if ((System.currentTimeMillis() - lastChangedTime) > (10 * Math.pow(10,3))) {
                            ableToChange = true;
                        }
                        if (ableToChange) {
                            changedState = detector.compute();
//                            System.out.println(System.currentTimeMillis());
                            if (changedState) {
                                lastChangedTime = System.currentTimeMillis();
                                ableToChange = false;
                            }
                        }
						try {
							Thread.sleep(timeInterval);
						} catch(InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			};

			Thread thread = new Thread(runnable);
			thread.start();			
			mqtt.subscribe(topic, qos);
		} catch (MqttException me) {
			System.out.println("Mqtt init problem!");
			System.exit(-1);
		} catch(Throwable t) {
			System.out.println("Throwable caught "+ t);
			t.printStackTrace();			
		}		
	}

	private static void testExecution() {
		final ActivityDetector detector = new ActivityDetector();
		final String testfile = "walk_1_Addition.csv";
        int counter = 0;
		try {
			File file = new File(testfile);
			Scanner sc = new Scanner(file);

			while (sc.hasNextLine()) {
				for (int i = 0; i < 25; ++i) { // Read 25 lines since 1 second we will have 25 data points
					String line = sc.nextLine();

					detector.consumeData(line);
					if (sc.hasNextLine() == false) {
						break;
					}
				}
//                System.out.println(counter);
                if (counter == 0) {
                    ableToChange = true;
                }
                if (ableToChange) {
                    changedState = detector.compute();
                    if (changedState) {
                        counter = 10;
                        ableToChange = false;
                    }
                } else {
                    counter -= 1;
                }
			}			
		} catch(Exception e) {
			e.printStackTrace();
		}

		// detector.computeWalk();
	}
}
