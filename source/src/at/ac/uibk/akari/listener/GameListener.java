package at.ac.uibk.akari.listener;

import java.util.EventListener;

import at.ac.uibk.akari.controller.PuzzleController;

public interface GameListener extends EventListener {

	public void puzzleSolved(PuzzleController source, long timeMs);
	
	public void puzzleStopped(PuzzleController source);

}
