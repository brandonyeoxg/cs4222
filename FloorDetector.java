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
	private ArrayList<Long> timestamps;
	private static final int sampleWindowLen = 4;
	private static final float MIN_CLUSTER_DST = 26.0f;
	private long curr_timestamp;
	private float prev_std_bmp;
	private float curr_level;
	public FloorDetector() {
		state = floorState.NOT_MOVING;
		sampleWindow = new ArrayList<Float>(sampleWindowLen);
		timestamps = new ArrayList<Long>(sampleWindowLen);
		curr_tracking_delta = 0;
		curr_mean_bmp = 0;
		curr_timestamp = -1;
		prev_std_bmp = 0;
		outputState = ActivityState.NO_FLOOR_CHANGE;
		curr_level = 0;
	}

	public OutputState compute(ArrayList<ActivityData> baroData) {
		for (ActivityData data : baroData) {
			if (sampleWindow.size() >= sampleWindowLen) {
				sampleWindow.remove(0);
				timestamps.remove(0);
			}
			float convertedToPa = data.data.get(0) * 100;
			sampleWindow.add(convertedToPa);
			timestamps.add(data.timestamp);
			float mean_bmp = getMean(sampleWindow);
			float std_bmp = getStdDev(sampleWindow);
			if (state == floorState.NOT_MOVING) {
				curr_mean_bmp = mean_bmp;
				if (std_bmp >= MOTION_THRESH) {
					state = floorState.MOVING;
				} else {
					outputState = ActivityState.NO_FLOOR_CHANGE;
					curr_timestamp = getTimestampMean(this.timestamps);
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
					if (Math.abs(curr_level) > 0) {
						outputState = ActivityState.FLOOR_CHANGE;
						curr_timestamp = getTimestampMean(this.timestamps);
						break;
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
		float output = 0;
		for (int i = 0 ; i < samples.size(); ++i) {
			output += samples.get(i);
		}
		return output / samples.size();
	}

	private long getTimestampMean(ArrayList<Long> samples) {
		long output = 0;
		for (int i = 0 ; i < samples.size(); ++i) {
			output += samples.get(i);
		}
		return output / samples.size();
	}
}