package at.ac.uibk.akari.utils;

import java.util.List;

import android.graphics.Point;
import at.ac.uibk.akari.core.JsonTools;
import at.ac.uibk.akari.core.Puzzle;

public class GameFieldSaveState {
	private Puzzle puzzle;
	private List<Point> lamps;
	private long timeElapsed;

	public GameFieldSaveState(final Puzzle puzzle, final List<Point> lamps, final long timeElapsed) {
		super();
		this.puzzle = puzzle;
		// todo: use clone to avoid changing list
		this.lamps = lamps;
		this.timeElapsed = timeElapsed;
	}

	public List<Point> getLamps() {
		return this.lamps;
	}

	public String getLampsAsString() {
		return JsonTools.getInstance().toJson(this.lamps, false);
	}

	public long getTimeElapsed() {
		return this.timeElapsed;
	}

	public Puzzle getPuzzle() {
		return this.puzzle;
	}

}