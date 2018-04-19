import java.util.ArrayList;
import java.lang.Exception;

public class ActivityDetector {
	private static final int TIMESTAMP_FIELD = 0;
	private static final int SENSOR_TYPE_FIELD = 1;

	private FloorDetector fDetector;
	private IndoorDetector iDetector;
	private WalkDetector wDetector;
    
    private static boolean changedState = false;

	private ArrayList<ActivityData> aList, bList, tList, lList, hList; 

	private OutputState floorState;
	private OutputState indoorState;
	private OutputState walkState;

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
	}

	public boolean compute() {
		OutputState curFloor, curIndoor, curWalk;

		curFloor = fDetector.compute(bList);
		curIndoor = iDetector.compute(tList, lList, hList);
		curWalk = wDetector.compute(aList);

		changedState = printIfChangeInActivityState(curFloor, curIndoor, curWalk);
		// Clear our lists
		flushLists();
        return changedState;
	}

	public void computeWalk() {
		wDetector.compute(aList);
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

	private String sanitisePayload(String payload) {
		// Sample payload is
		// {"value": "unicast message received from 82.2,518914,b,1006.56,,", "nodeid": "27648", "time": "2018-04-10T11:42:56.268125Z"}
		int initialPos = payload.indexOf(',');
		int lastPos = payload.indexOf('"', initialPos);
		String sanitisedString = payload.substring(initialPos + 1, lastPos);
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

	private boolean printIfChangeInActivityState(OutputState floor, OutputState indoor, OutputState walk) {
		if (floorState.equals(floor) == false) {
			printActivityState(floor);
			floorState = floor;
            return true;
		}
		if (indoorState.equals(indoor) == false) {
			if (indoor.timestamp != -1) { 
				printActivityState(indoor);
				indoorState = indoor;
                return true;
            }
		}
		if (walkState.equals(walk) == false) {
			printActivityState(walk);
			walkState = walk;
            return true;
		}
        return false;
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
