import java.util.ArrayList;

public class WalkDetector {
	public WalkDetector() {
		System.out.println("Hello WalkDetector here");
	}

	public OutputState compute(ArrayList<ActivityData> accelData) {
		return new OutputState(-1, ActivityState.IDLE);
	}
}