package at.ac.uibk.akari.core;

import java.util.EventListener;

import at.ac.uibk.akari.core.Puzzle.CellState;

public interface ModelChangeListener extends EventListener {

	public void puzzleCellChanged(int x, int y, CellState state);

	public void lampRemoved(int x, int y);

	public void lampAdded(int x, int y);

}
