import java.util.ArrayList;

public class IndoorDetector {
	public IndoorDetector() {
		System.out.println("Hello IndoorDetector here");
	}
	
	public String compute(ArrayList<ActivityData> tempData, ArrayList<ActivityData> lightData, ArrayList<ActivityData> humidData) {
		return ActivityState.INDOOR;
	}
}