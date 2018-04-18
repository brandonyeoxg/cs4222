import java.util.ArrayList;

public class FloorDetector {
	private static final int MOTION_THRESH = 2;
	private float current_tracking_delta;
	private float curr_mean_bmp;
	private String state;
	private ArrayList<Float> sampleWindow;
	private static final int sampleWindowLen = 100;
	private static final int MIN_CLUSTER_DST = 26;
	private long curr_timestamp;
	private float prev_std_bmp;
	public FloorDetector() {
		state = ActivityState.NO_FLOOR_CHANGE;
		sampleWindow = new ArrayList<Float>(sampleWindowLen);
		current_tracking_delta = 0;
		curr_mean_bmp = 0;
		curr_timestamp = -1;
		prev_std_bmp = 0;
	}

	public OutputState compute(ArrayList<ActivityData> baroData) {
		for (ActivityData data : baroData) {
			float convertedToPa = data.data.get(0) * 100; 
			sampleWindow.add(convertedToPa);
			float mean_bmp = getMean(sampleWindow);
			float std_bmp = getStdDev(sampleWindow);

			//System.out.println("Relative std " + Math.abs(std_bmp - prev_std_bmp));
			if (state.equals(ActivityState.NO_FLOOR_CHANGE)) {
				curr_mean_bmp = mean_bmp;
				if (Math.abs(std_bmp - prev_std_bmp) > MOTION_THRESH) {
					state = ActivityState.FLOOR_CHANGE;
					prev_std_bmp = std_bmp;
				}
			} else {
				if (Math.abs(std_bmp - prev_std_bmp) < MOTION_THRESH) {
					state = ActivityState.NO_FLOOR_CHANGE;
					float prev_mean_bmp = curr_mean_bmp;
					curr_mean_bmp = mean_bmp;
					float journey_delta = curr_mean_bmp - prev_mean_bmp;
					float min_delta = getMinDelta(journey_delta);
					current_tracking_delta += journey_delta;
					prev_std_bmp = std_bmp;
				}
			}
			curr_timestamp = data.timestamp;
		}
		return new OutputState(curr_timestamp, state);
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