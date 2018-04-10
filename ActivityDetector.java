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

	public ActivityDetector() {
		System.out.println("Hello ActivityDetector here");
		fDetector = new FloorDetector();
		iDetector = new IndoorDetector();
		wDetector = new WalkDetector();

		aList = new ArrayList<ActivityData>(); // @ 20Hz
		bList = new ArrayList<ActivityData>(); // @ 2 Hz
		tList = new ArrayList<ActivityData>(); lList = new ArrayList<ActivityData>(); hList = new ArrayList<ActivityData>(); // @1 Hz

		floorState = new OutputState(ActivityState.NOF_FLOOR_CHANGE);
		indoorState = new OutputState(ActivityState.INDOOR);
		walkState = new OutputState(ActivityState.IDLE);
	}

	public void compute() {
		OutputState curFloor, curIndoor, curWalk;

		curFloor = fDetector.compute(bList);
		curIndoor = iDetector.compute(tList, lList, hList);
		curWalk = wDetector.compute(aList);

		printIfChangeInActivityState(curFloor, curIndoor, curWalk);
	}

	public void consumeData(String mqttPayload) {
		String[] tokens = mqttPayload.split(",");
		ActivityData activityData = sanitisePayload(tokens);
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

	private ActivityData sanitisePayload(String[] tokens) {
		int sizeOfData = tokens.length - 2;
		ActivityData data = new ActivityData();
		ArrayList<Float> payload = new ArrayList<Float>(sizeOfData);
		data.timestamp = Integer.parseInt(tokens[TIMESTAMP_FIELD]);
		data.data = payload;
		return data;
	}

	private void printIfChangeInActivityState(OutputState floor, OutputState indoor, OutputState walk) {
		if (floorState.equals(floor) == false) {
			printActivityState(floor);
			floorState = floor;
		}
		if (indoorState.equals(indoor) == false) {
			printActivityState(indoor);
			indoorState = indoor;
		}
		if (walkState.equals(walkState) == false) {
			printActivityState(walk);
			walkState = walk;
		}
	}

	private void printActivityState(OutputState toBeOutput) {
		System.out.printf("%d,%s\n", toBeOutput.timestamp, toBeOutput.activityState);
	}
}