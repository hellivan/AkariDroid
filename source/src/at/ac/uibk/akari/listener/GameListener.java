package at.ac.uibk.akari.listener;

import java.util.EventListener;

public interface GameListener extends EventListener {

	public void gameStopped(final InputEvent event);

}
