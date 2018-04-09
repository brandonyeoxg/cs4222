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
}