package at.ac.uibk.akari.listener;

import java.util.EventListener;

public interface PuzzleControlListener extends EventListener{
	
	public void pausePuzzle(final InputEvent event);
	
	public void helpPuzzle(final InputEvent event);

}
