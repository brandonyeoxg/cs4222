import java.util.ArrayList;
import java.lang.Exception;

public class ActivityDetector {
	private static final int TIMESTAMP_FIELD = 0;
	private static final int SENSOR_TYPE_FIELD = 1;

	private FloorDetector fDetector;
	private IndoorDetector iDetector;
	private WalkDetector wDetector;

	private ArrayList<ActivityData> aList, bList, tList, lList, hList; 

	private String floorState = ActivityState.NO_FLOOR_CHANGE;
	private String indoorState = ActivityState.INDOOR;
	private String walkState = ActivityState.IDLE;

	public ActivityDetector() {
		System.out.println("Hello ActivityDetector here");
		fDetector = new FloorDetector();
		iDetector = new IndoorDetector();
		wDetector = new WalkDetector();

		aList = new ArrayList<ActivityData>(); // @ 20Hz
		bList = new ArrayList<ActivityData>(); // @ 2 Hz
		tList = new ArrayList<ActivityData>(); lList = new ArrayList<ActivityData>(); hList = new ArrayList<ActivityData>(); // @1 Hz
	}

	public void compute() {
		walkState = wDetector.compute(aList);
		floorState = fDetector.compute(bList);
		indoorState = iDetector.compute(tList, lList, hList);
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
		data.timestamp = tokens[TIMESTAMP_FIELD];
		data.data = payload;
		return data;
	}
}