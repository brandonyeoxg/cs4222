import java.util.ArrayList;
import java.lang.Math;

public class WalkDetector {
private class Vector {
		public float x, y, z;
		public long timestamp;

		public Vector(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;

			this.timestamp = 0;
		}

		public Vector(ActivityData ad) {
			this.timestamp = ad.timestamp;

			this.x = ad.data.get(0);
			this.y = ad.data.get(1);
			this.z = ad.data.get(2);
		}

		public Vector add(Vector vec) {
			float outX, outY, outZ;
			outX = this.x + vec.x;
			outY = this.y + vec.y;
			outZ = this.z + vec.z;

			return new Vector(outX, outY, outZ);
		}

		public Vector minus(Vector vec) {
			float outX, outY, outZ;
			outX = this.x - vec.x;
			outY = this.y - vec.y;
			outZ = this.z - vec.z;

			return new Vector(outX, outY, outZ);
		}

		public Vector mult(Vector vec) {
			float outX, outY, outZ;
			outX = this.x * vec.x;
			outY = this.y * vec.y;
			outZ = this.z * vec.z;

			return new Vector(outX, outY, outZ);
		}
		
		public Vector multConst(float num) {
			float outX, outY, outZ;
			outX = this.x * num;
			outY = this.y * num;
			outZ = this.z * num;

			return new Vector(outX, outY, outZ);
		}
		
		public Vector div(Vector vec) {
			float outX, outY, outZ;
			outX = this.x / vec.x;
			outY = this.y / vec.y;
			outZ = this.z / vec.z;

			return new Vector(outX, outY, outZ);
		}

		public boolean equals(Vector vec) {
			if (x != vec.x) {
				return false;
			}
			if (y != vec.y) {
				return false;
			}
			if (z != vec.z) {
				return false;
			}
			return true;
		}
	
		public boolean moreThan(Float num) {
			if (x > num) {
				return true;
			}
			if (y > num) {
				return true;
			}
			if (z > num) {
				return true;
			}
			return false;
		}

		public float getMag() {
			return (float)Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));
		}

		@Override
		public String toString() {
			return new String("X: " + this.x + " Y: " + this.y + " Z: " + this.z);
		}
	}

	private String state;
	private ArrayList<Float> samples;
	private ArrayList<Long> timestamps;
	private final int winSize = 20;
	private final float c = 1.0f/(2 * winSize + 1);
	private int sampleIndex = 0;
	private final float T1_THRESH = 2.0f;
	private final float T2_THRESH = 1.0f;
	private float moving_t1_thresh = T1_THRESH;
	private float moving_t2_thresh = T2_THRESH;
	private float b1, b2;
	private float prevB1;
	private final float MAX_ERROR_ALLOWED = 0.4f;
	private enum phase {
		STANCE,
		SWING
	}

	private phase curPhase;
	private long curTimeStamp;
	private boolean waitNextWindow;
	public WalkDetector() {
		state = ActivityState.IDLE;
		samples = new ArrayList<Float>();
		timestamps = new ArrayList<Long>();
		b1 = b2 = 0;
		curPhase = phase.STANCE;
		curTimeStamp = 0;
		waitNextWindow = false;
	}

	public OutputState compute(ArrayList<ActivityData> accelData) {
		waitNextWindow = false;
		for (ActivityData data : accelData) {
			Vector vec = new Vector(data).multConst(9.80665f);
			samples.add(vec.getMag());
			timestamps.add(data.timestamp);
			for (; sampleIndex < samples.size(); ++sampleIndex) {
				if (0 > sampleIndex - winSize) {
					continue;
				}
				if (sampleIndex + winSize >= samples.size()) {
					break;
				}
				float variance = getLocalVariance(sampleIndex);
				float stdDev = (float)Math.sqrt(variance);
				if (curPhase == phase.STANCE) {
					if (Math.abs(stdDev - moving_t1_thresh) <= MAX_ERROR_ALLOWED ) {
						moving_t1_thresh = 0.2f * moving_t1_thresh + stdDev * 0.8f;
						if (Math.abs(T1_THRESH - moving_t1_thresh) < 0.8f) {
							moving_t1_thresh = T1_THRESH * 0.7f + moving_t1_thresh * 0.3f;
						}
						System.out.println("Moving T1 thresh: " + moving_t1_thresh);
						b1 = moving_t1_thresh;
						curPhase = phase.SWING;
						state = ActivityState.WALK;
						curTimeStamp = getMeanTimestamp(sampleIndex);
					} else {
						b1 = 0;
					}
				}
				if (curPhase == phase.SWING) {
					if (stdDev < moving_t2_thresh) {
						b2 = moving_t2_thresh;
					}
					float forwardStd = lookForwardStd(sampleIndex);
					if (prevB1 < b1 && Math.abs(forwardStd - moving_t2_thresh) <= MAX_ERROR_ALLOWED) {
						moving_t2_thresh = 0.2f * moving_t2_thresh + forwardStd * 0.8f;
						if (Math.abs(moving_t2_thresh - T2_THRESH) < 0.8f) {
							moving_t2_thresh = T2_THRESH * 0.7f + moving_t2_thresh * 0.3f;
						}
						System.out.println("Moving T2 thresh: " + moving_t2_thresh);
						curPhase = phase.STANCE;
						state = ActivityState.IDLE;
						curTimeStamp = getMeanTimestamp(sampleIndex);
					}
				}
				if (waitNextWindow == true) {
					break;
				}
			}
		}
		return new OutputState(curTimeStamp , state);
	}

	private float getLocalVariance(int idx) {
		float variance = 0.0f;
		float mean = getLocalMean(idx);
		for (int i = idx - winSize; i < idx + winSize; ++i) {
			variance += Math.pow(this.samples.get(i) - mean, 2);
		}
		return variance * this.c;
	}

	private float getLocalMean(int idx) {
		float sum = 0.0f;
		for(int i = idx - winSize; i < idx + winSize; ++i) {
			sum += this.samples.get(i);
		}
		return sum * this.c;
	}

	private float lookForwardStd(int idx) {
		float maxStd = 0.0f; 
		for (int i = idx; i < idx + winSize; ++i) {
			if (i + winSize >= this.samples.size()) {
				waitNextWindow = true;
				sampleIndex = i - winSize;
				break;
			}
			float std = getLocalVariance(i);
			if (maxStd < std) {
				maxStd = std;
			}
		}
		return maxStd;
	}

	private long getMeanTimestamp(int idx) {
		long timestamp = 0;
		for (int i = idx - winSize; i < idx + winSize; ++i) {
			timestamp += timestamps.get(i);
		}
		return timestamp / (winSize * 2);
	}
}