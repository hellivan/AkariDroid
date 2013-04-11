package at.ac.uibk.akari.listener;

import java.util.EventListener;

import at.ac.uibk.akari.controller.GameController;

public interface GameListener extends EventListener {

	public void puzzleSolved(GameController source, long timeMs);

}
