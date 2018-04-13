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
			// if (x <= num) {
			// 	return false;
			// }
			// if (y <= num) {
			// 	return false;
			// }
			// if (z <= num) {
			// 	return false;
			// }
			// return true;
		}

		public float getMag() {
			return (float)Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));
		}

		@Override
		public String toString() {
			return new String("X: " + this.x + " Y: " + this.y + " Z: " + this.z);
		}
	}

	private int gMin, gMax, gOpt, gAbsMin, gAbsMax, gInterval;
	private int lastKnownSample;
	private long lastKnownTimeStamp;
	private String state, lastKnownState;
	private ArrayList<Vector> samples;

	public WalkDetector() {
		gMin = 40;
		gMax = 100;
		gOpt = 0;
		gAbsMin = 40;
		gAbsMax = 100;
		gInterval = 10;

		state = ActivityState.IDLE;

		samples = new ArrayList<Vector>();
		
		lastKnownSample = 0;
		lastKnownTimeStamp = 0;
		lastKnownState = state;
	}

	public OutputState compute(ArrayList<ActivityData> accelData) {
		// Convert into vector
		for (int i = 0; i < accelData.size(); ++i) {
			Vector vec = new Vector(accelData.get(i));
			this.samples.add(vec);
		}
		state = ActivityState.IDLE;
		int numStepsCtr = 0;
		int stepCount = 0;
		for (int i = lastKnownSample; i < this.samples.size(); ++i) {
			if (i + this.gMax + this.gMax > this.samples.size()) {
				return new OutputState(lastKnownTimeStamp, lastKnownState);
			}
			Vector highestCorrelation = getMaxCorrelation(i);
			if (getStdDevOfAccelMag(i, this.gOpt) < 0.01f) {			
				state = ActivityState.IDLE;
				numStepsCtr = 0;
				lastKnownSample = i + this.gOpt;
				lastKnownTimeStamp = this.samples.get(i).timestamp;
				lastKnownState = ActivityState.IDLE;
				return new OutputState(lastKnownTimeStamp, lastKnownState);
			} else if (highestCorrelation.moreThan(0.7f)) {					
				state = ActivityState.WALK;
				this.lastKnownSample = i + this.gOpt;
				this.lastKnownState = ActivityState.WALK;
				this.lastKnownTimeStamp = this.samples.get(i).timestamp;
				return new OutputState(lastKnownTimeStamp, lastKnownState);
			}
			if (state == ActivityState.IDLE) {
				continue;
			}
			if (numStepsCtr > this.gOpt / 2) {						
				numStepsCtr = 0;
				stepCount += 1;
			}
			numStepsCtr += 1;
			lastKnownSample = i;
		}
		// System.out.println("Computing IDLE");
		return new OutputState(lastKnownTimeStamp, lastKnownState);
	}

	private Vector getMaxCorrelation(int fromIdx) {
		Vector highestCorrelation = new Vector(0,0,0);
		int highestGamma = this.gMin;
		for (int gamma = this.gMin; gamma < this.gMax; ++gamma) {
			if (fromIdx + gamma + gamma >= this.samples.size()) {
				break;
			}
			Vector correlation = getAutoCorrelation(this.samples, fromIdx, gamma);
			highestCorrelation = getHighestCorrelation(highestCorrelation, correlation);
			if (highestCorrelation.equals(correlation)) {
				highestGamma = gamma;
			}
		}
		this.gOpt = highestGamma;
		return highestCorrelation;
	}

	private Vector getAutoCorrelation(ArrayList<Vector> accelData, int m, int gamma) {
		Vector outputCorrelation = new Vector(0, 0, 0);
		for (int k = 0; k < gamma; ++k) {
			if (m + k + gamma + gamma >= accelData.size()) {
				break;
			}

			Vector meanVec = getAccelMeanFromTill(m, gamma);
			Vector left = this.samples.get(m + k).minus(meanVec);

			meanVec = getAccelMeanFromTill(m + gamma, gamma);
			Vector right = this.samples.get(m + k + gamma).minus(meanVec);

			Vector result = left.mult(right);
			outputCorrelation = outputCorrelation.add(result);
		}

		Vector stdDevLeftVec = getAccelStdDevFromTill(m, gamma);
		Vector stdDevRightVec = getAccelStdDevFromTill(m + gamma, gamma);

		Vector denominatorVec = stdDevLeftVec.mult(stdDevRightVec);
		denominatorVec = denominatorVec.multConst(gamma);

		outputCorrelation = outputCorrelation.div(denominatorVec);
		return outputCorrelation;
	}

	private Vector getAccelMean(ArrayList<Vector> dataSets, int gamma) {
		float x = 0.0f, y = 0.0f, z = 0.0f;
		for (int i = 0; i < gamma; ++i) {
			x += dataSets.get(i).x;
			y += dataSets.get(i).y;
			z += dataSets.get(i).z;
		}
		Vector outputVec = new Vector(x / gamma, y / gamma, z / gamma);
		return outputVec;
	}

	private Vector getAccelStdDev(ArrayList<Vector> dataSets, int gamma) {
		Vector meanVec = getAccelMean(dataSets, gamma);
		Vector stdDevVec = new Vector(0.0f, 0.0f, 0.0f);
		for(int i = 0; i < gamma; ++i) {
			stdDevVec.x += Math.pow(dataSets.get(i).x - meanVec.x, 2);
			stdDevVec.y += Math.pow(dataSets.get(i).y - meanVec.y, 2);
			stdDevVec.z += Math.pow(dataSets.get(i).z - meanVec.z, 2);
		}

		stdDevVec.x = (float) Math.sqrt(stdDevVec.x / gamma);
		stdDevVec.y = (float) Math.sqrt(stdDevVec.y / gamma);
		stdDevVec.z = (float) Math.sqrt(stdDevVec.z / gamma);
		
		return stdDevVec;
	}

	private Vector getAccelMeanFromTill(int m, int gamma) {
		ArrayList<Vector> newSample = new ArrayList<Vector>(gamma);
		for (int i = 0; i < gamma; ++i) {
			int targetIdx = m + i;
			newSample.add(this.samples.get(targetIdx));
		}
		Vector outputMeanVec = getAccelMean(newSample, gamma);
		return outputMeanVec;
	}

	private Vector getAccelStdDevFromTill(int m, int gamma) {
		ArrayList<Vector> newSample = new ArrayList<Vector>(gamma);
		for(int i = 0; i < gamma; ++i) {
			int targetIdx = m + i;
			newSample.add(this.samples.get(targetIdx));
		}

		Vector outputStdDevVec = getAccelStdDev(newSample, gamma);
		return outputStdDevVec;
	}

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