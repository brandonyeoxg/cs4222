import java.util.ArrayList;

public class FloorDetector {
	private static final int MOTION_THRESH = 2;
	private float curr_tracking_delta;
	private float curr_mean_bmp;
	private enum floorState {
		NOT_MOVING,
		MOVING
	}
	private floorState state;
	private String outputState;
	private ArrayList<Float> sampleWindow;
	private static final int sampleWindowLen = 2;
	private static final float MIN_CLUSTER_DST = 26.0f;
	private long curr_timestamp;
	private float prev_std_bmp;
	private float curr_level;
	public FloorDetector() {
		state = floorState.NOT_MOVING;
		sampleWindow = new ArrayList<Float>(sampleWindowLen);
		curr_tracking_delta = 0;
		curr_mean_bmp = 0;
		curr_timestamp = -1;
		prev_std_bmp = 0;
		outputState = ActivityState.NO_FLOOR_CHANGE;
		curr_level = 0;
	}

	public OutputState compute(ArrayList<ActivityData> baroData) {
		for (ActivityData data : baroData) {
			float convertedToPa = data.data.get(0) * 100;
			sampleWindow.add(convertedToPa);
			if (sampleWindow.size() >= sampleWindowLen) {
				float mean_bmp = getMean(sampleWindow);
				float std_bmp = getStdDev(sampleWindow);
				float relative_bmp = std_bmp;
				if (state == floorState.NOT_MOVING) {
					curr_mean_bmp = mean_bmp;
					outputState = ActivityState.NO_FLOOR_CHANGE;
					curr_timestamp = data.timestamp;
					if (std_bmp >= MOTION_THRESH) {
						state = floorState.MOVING;
					}
				} else {
					if (std_bmp < MOTION_THRESH) {
						state = floorState.NOT_MOVING;
						float prev_mean_bmp = curr_mean_bmp;
						curr_mean_bmp = mean_bmp;
						float journey_delta = curr_mean_bmp - prev_mean_bmp;
						float min_delta = getMinDelta(journey_delta);
						curr_tracking_delta += journey_delta;
						curr_level = Math.round(curr_tracking_delta / min_delta);
						System.out.println("curr_tracking_delta: " + curr_tracking_delta + " Journey Delta: " + journey_delta + " Mean: " + mean_bmp);
						System.out.println("Curr level: " + curr_level);						
						curr_timestamp = data.timestamp;
						if (Math.abs(curr_level) > 0) {
							outputState = ActivityState.FLOOR_CHANGE;
							curr_timestamp = data.timestamp;
							break;
						} else {
							outputState = ActivityState.NO_FLOOR_CHANGE;
						}
					}
				}
			}
		}
		return new OutputState(curr_timestamp, outputState);
	}

	private float getMinDelta(float journey_delta) {
		return MIN_CLUSTER_DST;
	}

	private float getStdDev(ArrayList<Float> samples) {
		float mean = getMean(samples);
		float stdDev = 0;
		for (int i = 0; i < samples.size(); ++i) {
			stdDev += Math.pow(samples.get(i) - mean, 2);
		}
		stdDev = (float)Math.sqrt(stdDev / samples.size());
		return stdDev;
	}

	private float getMean(ArrayList<Float> samples) {
		if (samples.size() < 1) {
			System.exit(-1);
		}
		float output = 0;
		for (int i = 0 ; i < samples.size(); ++i) {
			output += samples.get(i);
		}
		return output / samples.size();
	}	
}