package at.ac.uibk.akari.puzzleSelector.listener;

import java.util.EventListener;

public interface PuzzleSelectionListener extends EventListener {

	public void puzzleSelectionCanceled(final PuzzleSelectionEvent event);

	public void puzzleSelected(final PuzzleSelectionEvent event);

}
