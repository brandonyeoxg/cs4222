import java.util.ArrayList;
import java.lang.Math;

public class WalkDetector {
	private int gMin, gMax, gOpt, gAbsMin, gAbsMax;
	private String state = ActivityState.IDLE;

	public WalkDetector() {
		gMin = 120;
		gMax = 200;
		gOpt = 0;
		gAbsMin = 120;
		gAbsMax = 240;

		state = ActivityState.IDLE;
	}

	public OutputState compute(ArrayList<ActivityData> accelData) {
		for (int i = 0; i < accelData.size(); ++i) {

		}

		return new OutputState(-1, ActivityState.IDLE);
	}

	private class Vector {
		public float x, y, z;
		public long timestamp;

		public Vector(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public Vector(ArrayList<Float> data, long timestamp) {
			this.timestamp = timestamp;

			x = data.get(0);
			y = data.get(1);
			z = data.get(2);
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

	public Vector getHighestCorrelation(Vector left, Vector right) {
		float leftSum = left.x + left.y + left.z;
		float rightSum = right.x + right.y + right.z;
		if (leftSum > rightSum) {
			return left;
		}
		return right;
	}	
}