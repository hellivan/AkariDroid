package at.ac.uibk.akari.utils;

import java.util.Set;

import at.ac.uibk.akari.core.GameFieldModel;
import at.ac.uibk.akari.core.GameFieldPoint;
import at.ac.uibk.akari.core.Puzzle;

public class GameFieldSaveState {
	private Puzzle puzzle;
	private Set<GameFieldPoint> specialPoints;
	private long secondsElapsed;

	private GameFieldSaveState() {
	}

	public long getSecondsElapsed() {
		return this.secondsElapsed;
	}

	public Puzzle getPuzzle() {
		return this.puzzle;
	}

	public static GameFieldSaveState generate(final GameFieldModel gameFieldModel, final long secondsElapsed) {
		return GameFieldSaveState.generate(gameFieldModel.getPuzzle(), gameFieldModel.getGameFieldPoints(), secondsElapsed);
	}

	public Set<GameFieldPoint> getSpecialPoints() {
		return this.specialPoints;
	}

	public static GameFieldSaveState generate(final Puzzle puzzle, final Set<GameFieldPoint> specialPoints, final long secondsElapsed) {
		GameFieldSaveState saveState = new GameFieldSaveState();
		saveState.secondsElapsed = secondsElapsed;
		saveState.puzzle = puzzle;
		saveState.specialPoints = specialPoints;
		return saveState;
	}

}