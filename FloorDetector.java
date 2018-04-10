import java.util.ArrayList;

public class FloorDetector {
	public FloorDetector() {
		System.out.println("Hello FloorDetector here");
	}

	public OutputState compute(ArrayList<ActivityData> baroData) {
		return new OutputState(-1,ActivityState.NO_FLOOR_CHANGE);
	}
}