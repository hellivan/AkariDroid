package at.ac.uibk.akari.stopClock.listener;

import java.util.EventListener;

public interface StopClockUpdateListener extends EventListener {

	public void stopClockUpdated(final StopClockEvent event);

}
