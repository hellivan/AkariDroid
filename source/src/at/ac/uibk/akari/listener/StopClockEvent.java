package at.ac.uibk.akari.listener;

public class StopClockEvent {

	private Object source;
	private long currentClockSeconds;

	public StopClockEvent(final Object source, final long currentClockSeconds) {
		this.source = source;
		this.currentClockSeconds = currentClockSeconds;
	}

	public Object getSource() {
		return this.source;
	}

	public long getCurrentClockSeconds() {
		return this.currentClockSeconds;
	}

}
