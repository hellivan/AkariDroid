package at.ac.uibk.akari.puzzleSelector.listener;

import at.ac.uibk.akari.core.Puzzle;

public class PuzzleSelectionEvent {

	private Object source;
	private Puzzle puzzle;

	public PuzzleSelectionEvent(final Object source, final Puzzle puzzle) {
		this.source = source;
		this.puzzle = puzzle;
	}

	public Object getSource() {
		return this.source;
	}

	public Puzzle getPuzzle() {
		return this.puzzle;
	}

}
