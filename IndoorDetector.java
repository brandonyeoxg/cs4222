import java.util.ArrayList;

public class IndoorDetector {
	public IndoorDetector() {
		System.out.println("Hello IndoorDetector here");
	}
	
	public OutputState compute(ArrayList<ActivityData> tempData, ArrayList<ActivityData> lightData, ArrayList<ActivityData> humidData) {
		return new OutputState(-1, ActivityState.INDOOR);
	}
}