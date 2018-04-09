public class ActivityDetector {
	FloorDetector fDetector;
	IndoorDetector iDetector;
	WalkDetector wDetector;

	public ActivityDetector() {
		System.out.println("Hello ActivityDetector here");
		fDetector = new FloorDetector();
		iDetector = new IndoorDetector();
		wDetector = new WalkDetector();
	}

	public void compute() {
		// This is for debug purposes now
		System.out.println(fDetector.compute());
		System.out.println(iDetector.compute());
		System.out.println(wDetector.compute());
	}
}