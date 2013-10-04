package at.ac.uibk.akari.gamePlay.listener;

import java.util.EventListener;

import at.ac.uibk.akari.common.listener.InputEvent;

public interface GameListener extends EventListener {

	public void gameStopped(final InputEvent event);

}
