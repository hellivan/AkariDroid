package at.ac.uibk.akari.utils;

import java.util.List;

import android.graphics.Point;
import at.ac.uibk.akari.core.GameFieldModel;
import at.ac.uibk.akari.core.Puzzle;

public class GameFieldSaveState {
	private Puzzle puzzle;
	private List<Point> lamps;
	private long secondsElapsed;

	private GameFieldSaveState() {
	}

	public List<Point> getLamps() {
		return this.lamps;
	}

	public long getSecondsElapsed() {
		return this.secondsElapsed;
	}

	public Puzzle getPuzzle() {
		return this.puzzle;
	}

	public static GameFieldSaveState generate(final GameFieldModel gameFieldModel, final long secondsElapsed) {
		return GameFieldSaveState.generate(gameFieldModel.getPuzzle(), gameFieldModel.getLamps(), secondsElapsed);
	}

	public static GameFieldSaveState generate(final Puzzle puzzle, final List<Point> lamps, final long secondsElapsed) {
		GameFieldSaveState saveState = new GameFieldSaveState();
		saveState.secondsElapsed = secondsElapsed;
		saveState.puzzle = puzzle;
		saveState.lamps = lamps;
		return saveState;
	}

}