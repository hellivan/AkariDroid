package at.ac.uibk.akari.core;

import at.ac.uibk.akari.core.Puzzle.CellState;

public interface ModelChangeListener {

	public void puzzleCellChanged(int x, int y, CellState state);
	public void lampRemoved(int x,int y);
	public void lampAdded(int x,int y);
	
}
