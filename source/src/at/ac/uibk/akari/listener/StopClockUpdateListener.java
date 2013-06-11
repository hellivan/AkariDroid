package at.ac.uibk.akari.listener;

import java.util.EventListener;

public interface StopClockUpdateListener extends EventListener {

	public void stopClockUpdated(final StopClockEvent event);

}
