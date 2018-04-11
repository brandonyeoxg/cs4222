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
	}

	private int gMin, gMax, gOpt, gAbsMin, gAbsMax, gInterval;
	private String state = ActivityState.IDLE;
	private ArrayList<Vector> samples;

	public WalkDetector() {
		gMin = 120;
		gMax = 200;
		gOpt = 0;
		gAbsMin = 120;
		gAbsMax = 240;
		gInterval = 20;

		state = ActivityState.IDLE;

		samples = new ArrayList<Vector>();
	}

	public OutputState compute(ArrayList<ActivityData> accelData) {
		// Convert into vector
		for (int i = 0; i < accelData.size(); ++i) {
			Vector vec = new Vector(accelData.get(i));
			this.samples.add(vec);
		}

		return new OutputState(-1, ActivityState.IDLE);
	}

	//private Vector getMaxCorrelation(ArrayList<ActivityData> accelData);

	private float getStdDevOfAccelMag(int fromIdx, int windowSize) {
		ArrayList<Float> sampleMagnitudes = new ArrayList<Float>(windowSize);

		for (int k = 0; k < windowSize; ++k) {
			int targetIdx = fromIdx + k;
			sampleMagnitudes.add(this.samples.get(targetIdx).getMag());
		}
		return getStdDev(sampleMagnitudes);
	}

	private void handleGammaWindowShift() {
		this.gMin = this.gOpt - this.gInterval;
		if (this.gMin < this.gAbsMin) {
			this.gMin = this.gAbsMin;
			this.gMax = this.gMin + (this.gInterval * 2);
		} else {
			this.gMax = this.gOpt + this.gInterval;
			if (this.gMax > this.gAbsMax) {
				this.gMax = this.gAbsMax;
				this.gMin = this.gMax - (this.gInterval * 2);
			}
		}
	}

	private Vector getHighestCorrelation(Vector left, Vector right) {
		float leftSum = left.x + left.y + left.z;
		float rightSum = right.x + right.y + right.z;
		if (leftSum > rightSum) {
			return left;
		}
		return right;
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