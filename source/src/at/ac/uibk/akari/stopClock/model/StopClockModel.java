package at.ac.uibk.akari.stopClock.model;

import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;

import at.ac.uibk.akari.stopClock.listener.StopClockEvent;
import at.ac.uibk.akari.stopClock.listener.StopClockUpdateListener;
import at.ac.uibk.akari.utils.ListenerList;

public class StopClockModel implements ITimerCallback {

	private boolean started;
	private long secondsElapsed;
	private TimerHandler currentTimerHandler;
	protected ListenerList listeners;

	public StopClockModel() {
		this.listeners = new ListenerList();
		this.started = false;
		this.secondsElapsed = 0;
	}

	public synchronized void start() {
		this.started = true;
	}

	public synchronized void stop() {
		this.started = false;
	}

	public synchronized void reset() {
		this.secondsElapsed = 0;
		this.fireClockUpdated();
	}

	public long getSecondsElapsed() {
		return this.secondsElapsed;
	}

	public void setSecondsElapsed(final long secondsElapsed) {
		this.secondsElapsed = secondsElapsed;
	}

	@Override
	public synchronized void onTimePassed(final TimerHandler handler) {
		if (this.currentTimerHandler != handler) {
			throw new RuntimeException("Invalid timer-handler for stop-clock. Use createNewTimerHandler() to create a new one.");
		}
		if (this.started) {
			this.secondsElapsed++;
			this.fireClockUpdated();
		}
	}

	public TimerHandler createNewTimerHandler() {
		this.currentTimerHandler = new TimerHandler(1, true, this);
		return this.currentTimerHandler;
	}

	public void addStopClockUpdateListener(final StopClockUpdateListener listener) {
		this.listeners.addListener(StopClockUpdateListener.class, listener);
	}

	public void removeStopClockUpdateListener(final StopClockUpdateListener listener) {
		this.listeners.removeListener(StopClockUpdateListener.class, listener);
	}

	protected void fireClockUpdated() {
		StopClockEvent event = new StopClockEvent(this, this.secondsElapsed);
		for (StopClockUpdateListener listener : this.listeners.getListeners(StopClockUpdateListener.class)) {
			listener.stopClockUpdated(event);
		}
	}
}
