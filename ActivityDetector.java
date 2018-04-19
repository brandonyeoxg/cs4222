import java.util.ArrayList;
import java.lang.Exception;

public class ActivityDetector {
	private static final int TIMESTAMP_FIELD = 0;
	private static final int SENSOR_TYPE_FIELD = 1;

	private FloorDetector fDetector;
	private IndoorDetector iDetector;
	private WalkDetector wDetector;

	private ArrayList<ActivityData> aList, bList, tList, lList, hList; 

	private OutputState floorState;
	private OutputState indoorState;
	private OutputState walkState;

	private long prevFloorChange, prevIndoorChange, prevWalkChange;

	public ActivityDetector() {
		fDetector = new FloorDetector();
		iDetector = new IndoorDetector();
		wDetector = new WalkDetector();

		aList = new ArrayList<ActivityData>(); // @ 20Hz
		bList = new ArrayList<ActivityData>(); // @ 2 Hz
		tList = new ArrayList<ActivityData>(); lList = new ArrayList<ActivityData>(); hList = new ArrayList<ActivityData>(); // @1 Hz

		floorState = new OutputState(ActivityState.NO_FLOOR_CHANGE);
		indoorState = new OutputState(ActivityState.INDOOR);
		walkState = new OutputState(ActivityState.IDLE);

		prevFloorChange = prevIndoorChange = prevWalkChange = 0;
	}

	public void compute() {
		OutputState curFloor, curIndoor, curWalk;

		curFloor = fDetector.compute(bList);
		curIndoor = iDetector.compute(tList, lList, hList);
		curWalk = wDetector.compute(aList);

		printIfChangeInActivityState(curFloor, curIndoor, curWalk);
		// // Clear our lists
		flushLists();
	}
	
	public void printLists() {
		System.out.println("Printing out light data: ");
		for (ActivityData d : lList) {
			System.out.println("Timestamp: " + d.timestamp + " Data: " + d.data.get(0));
		}
		
		System.out.println("Printing out humid data: ");
		for (ActivityData d : hList) {
			System.out.println("Timestamp: " + d.timestamp + " Data: " + d.data.get(0));
		}

		System.out.println("Printing out temp data: ");
		for (ActivityData d : tList) {
			System.out.println("Timestamp: " + d.timestamp + " Data: " + d.data.get(0));
		}

		System.out.println("Printing out baro data: ");
		for (ActivityData d : bList) {
			System.out.println("Timestamp: " + d.timestamp + " Data: " + d.data.get(0));
		}				
	}

	public void consumeData(String mqttPayload) {
		String sanitisedPayload = sanitisePayload(mqttPayload);
		String[] tokens = sanitisedPayload.split(",");
		ActivityData activityData = sanitiseTokens(tokens);		
		switch(tokens[SENSOR_TYPE_FIELD]) {
			case "a":
				aList.add(activityData);
				break;
			case "b":
				bList.add(activityData);
				break;
			case "t":
				tList.add(activityData);
				break;
			case "l":
				lList.add(activityData);
				break;
			case "h":
				hList.add(activityData);
				break;
			default:
				System.out.println("There is no proper sensor type!");
		}
	}

	public void consumeTestData(String mqttPayload) {
		int initialPos = mqttPayload.indexOf(',');
		int lastPos = mqttPayload.indexOf('"', initialPos);
		String sanitisedString = mqttPayload.substring(initialPos + 1, lastPos);
		String[] tokens = sanitisedString.split(",");
		ActivityData activityData = sanitiseTokens(tokens);		
		switch(tokens[SENSOR_TYPE_FIELD]) {
			case "a":
				aList.add(activityData);
				break;
			case "b":
				bList.add(activityData);
				break;
			case "t":
				tList.add(activityData);
				break;
			case "l":
				lList.add(activityData);
				break;
			case "h":
				hList.add(activityData);
				break;
			default:
				System.out.println("There is no proper sensor type!");
		}		
	}

	private String sanitisePayload(String payload) {
		// Sample payload is
		// Message:	{"nodeid": "27648", "value": "unicast message received from 179.130,386750,b,1006.71,,", "time": "2018-04-19T04:19:36.476723Z"}
		String initialSanitiseData = payload.split("value\":")[1];
		int initialPos = initialSanitiseData.indexOf(',');
		int lastPos = initialSanitiseData.indexOf('"', initialPos);
		String sanitisedString = initialSanitiseData.substring(initialPos + 1, lastPos);
		return sanitisedString;
	}

	private ActivityData sanitiseTokens(String[] tokens) {
		int sizeOfData = tokens.length - 2;
		ActivityData data = new ActivityData();
		ArrayList<Float> payload = new ArrayList<Float>(sizeOfData);
		for (int i = 0; i < sizeOfData; ++i) {
			payload.add(Float.parseFloat(tokens[i + SENSOR_TYPE_FIELD + 1]));
		}
		data.timestamp = Integer.parseInt(tokens[TIMESTAMP_FIELD]);
		data.data = payload;
		return data;
	}

	private void printIfChangeInActivityState(OutputState floor, OutputState indoor, OutputState walk) {
		long currentTime = System.currentTimeMillis();
		long elapsedIndoor = currentTime - prevIndoorChange;
		long elapsedFloor = currentTime - prevFloorChange;
		long elapsedWalk = currentTime - prevWalkChange;

		if (floorState.equals(floor) == false && elapsedFloor/1000 > 10) {
			System.out.println("Floor state timer expired");
			printActivityState(floor);
			floorState = floor;
			prevFloorChange = currentTime;
		}
		if (indoorState.equals(indoor) == false && elapsedIndoor/1000 > 10) {
			if (indoor.timestamp != -1) { 
				System.out.println("Indoor state timer expired");
				printActivityState(indoor);
				indoorState = indoor;
				prevIndoorChange = currentTime;
            }
		}
		if (walkState.equals(walk) == false && elapsedWalk/1000 > 10) {
			System.out.println("Indoor state timer expired");
			printActivityState(walk);
			walkState = walk;
			prevWalkChange = currentTime;
			System.out.println("Walk state timer expired");
		}
	}

	private void printActivityState(OutputState toBeOutput) {
		System.out.printf("%d,%s\n", toBeOutput.timestamp, toBeOutput.activityState);
	}

	private void flushLists() {
		aList.clear();
		bList.clear();
		tList.clear();
		lList.clear();
		hList.clear();
	}
}
