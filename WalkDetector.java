import java.util.ArrayList;

public class WalkDetector {
	public WalkDetector() {
		System.out.println("Hello WalkDetector here");
	}

	public String compute(ArrayList<ActivityData> accelData) {
		return ActivityState.IDLE;
	}
}