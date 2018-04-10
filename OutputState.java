public class OutputState {
	public int timestamp;
	public String activityState; 

	public OutputState(String activityState) {
		timestamp = -1;
		this.activityState = activityState;
	}

	public OutputState(int timestamp, String activityState) {
		this.timestamp = timestamp;
		this.activityState = activityState;
	}

	public boolean equals(OutputState state) {
		return this.activityState.equals(state.activityState);
	}
}