package at.ac.uibk.akari.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import at.ac.uibk.akari.core.GameFieldPoint;
import at.ac.uibk.akari.core.JsonTools;
import at.ac.uibk.akari.core.Puzzle;

public class SaveGameManager {

	private SharedPreferences sharedPreferences;

	private static SaveGameManager scoreManager;

	private static final long EMPTY_LONG = -1;
	private static final int EMPTY_INT = -1;
	private static final String EMPTY_STRING = "EMPTY";

	public static final int EMPTY_SCORE = SaveGameManager.EMPTY_INT;

	private static final String SCORE_SUFFIX = "_SCORE";
	private static final String TIME_SUFFIX = "_TIME";
	private static final String SPECIAL_POINTS_SUFFIX = "_SPECIAL_POINTS";

	private static final String KEY_RESUME = "RESUME";

	public static SaveGameManager getInstance() {
		if (SaveGameManager.scoreManager == null) {
			SaveGameManager.scoreManager = new SaveGameManager();
		}
		return SaveGameManager.scoreManager;
	}

	private SaveGameManager() {

	}

	public void init(final Context context) {
		this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void saveScore(final Puzzle puzzle, final long score) {
		String puzzleID = SaveGameManager.generatePuzzleID(puzzle);
		long oldScore = this.loadScore(puzzle);
		if ((oldScore == SaveGameManager.EMPTY_SCORE) || (score < oldScore)) {
			Log.d(this.getClass().getName(), "Save score " + score + " for puzzle " + puzzleID);
			SharedPreferences.Editor editor = this.sharedPreferences.edit();
			editor.putLong(puzzleID + SaveGameManager.SCORE_SUFFIX, score);
			editor.commit();
		} else {
			Log.d(this.getClass().getName(), "New score is worse than new score. Don't save new one");
		}
	}

	public long loadScore(final Puzzle puzzle) {
		String puzzleID = SaveGameManager.generatePuzzleID(puzzle);
		long score = this.sharedPreferences.getLong(puzzleID + SaveGameManager.SCORE_SUFFIX, SaveGameManager.EMPTY_SCORE);
		Log.d(this.getClass().getName(), "Loaded score " + score + " for puzzle " + puzzleID);
		return score;
	}

	public void clearScore(final Puzzle puzzle) {
		String puzzleID = SaveGameManager.generatePuzzleID(puzzle);
		Log.d(this.getClass().getName(), "Delete score for puzzle " + puzzleID);
		SharedPreferences.Editor editor = this.sharedPreferences.edit();
		editor.remove(puzzleID + SaveGameManager.SCORE_SUFFIX);
		editor.commit();
	}

	public void clearAll() {
		SharedPreferences.Editor editor = this.sharedPreferences.edit();
		editor.clear();
		editor.commit();
	}

	public void saveGameFiledState(final GameFieldSaveState gameFieldSaveState) {
		String puzzleID = SaveGameManager.generatePuzzleID(gameFieldSaveState.getPuzzle());
		Log.d(this.getClass().getName(), "Save game-field-state for puzzle " + puzzleID);
		SharedPreferences.Editor editor = this.sharedPreferences.edit();

		GameFieldPoint[] specialPoints = gameFieldSaveState.getSpecialPoints().toArray(new GameFieldPoint[gameFieldSaveState.getSpecialPoints().size()]);
		editor.putString(puzzleID + SaveGameManager.SPECIAL_POINTS_SUFFIX, JsonTools.getInstance().toJson(specialPoints, true));

		editor.putLong(puzzleID + SaveGameManager.TIME_SUFFIX, gameFieldSaveState.getSecondsElapsed());

		editor.commit();
	}

	public GameFieldSaveState loadGameFiledState(final Puzzle puzzle) {
		String puzzleID = SaveGameManager.generatePuzzleID(puzzle);
		Log.d(this.getClass().getName(), "Loading game-field-state for puzzle " + puzzleID);

		String specialPointsLoaded = this.sharedPreferences.getString(puzzleID + SaveGameManager.SPECIAL_POINTS_SUFFIX, SaveGameManager.EMPTY_STRING);
		long timeLoaded = this.sharedPreferences.getLong(puzzleID + SaveGameManager.TIME_SUFFIX, SaveGameManager.EMPTY_LONG);

		if ((timeLoaded == SaveGameManager.EMPTY_LONG) || specialPointsLoaded.equals(SaveGameManager.EMPTY_STRING)) {
			return null;
		}

		return GameFieldSaveState.generate(puzzle, new HashSet<GameFieldPoint>(Arrays.asList(JsonTools.getInstance().fromJson(GameFieldPoint[].class, specialPointsLoaded))), timeLoaded);
	}

	public void clearGameFiledState(final Puzzle puzzle) {
		String puzzleID = SaveGameManager.generatePuzzleID(puzzle);
		Log.d(this.getClass().getName(), "Delete game-field-state for puzzle " + puzzleID);
		SharedPreferences.Editor editor = this.sharedPreferences.edit();
		editor.remove(puzzleID + SaveGameManager.SPECIAL_POINTS_SUFFIX);
		editor.remove(puzzleID + SaveGameManager.TIME_SUFFIX);
		editor.commit();
	}

	public void savePuzzleToResume(final Puzzle puzzle) {
		String puzzleID = SaveGameManager.generatePuzzleID(puzzle);
		Log.d(this.getClass().getName(), "Save puzzle " + puzzleID + " so that it can be resumed.");
		SharedPreferences.Editor editor = this.sharedPreferences.edit();
		editor.putString(SaveGameManager.KEY_RESUME, puzzleID);
		editor.commit();
	}

	public Puzzle loadPuzzleToResume() {
		String puzzleIDToResume = this.sharedPreferences.getString(SaveGameManager.KEY_RESUME, SaveGameManager.EMPTY_STRING);
		if (!puzzleIDToResume.equals(SaveGameManager.EMPTY_STRING)) {
			List<Puzzle> puzzles = PuzzleManager.getInstance().getPuzzles();
			for (Puzzle puzzle : puzzles) {
				if (SaveGameManager.generatePuzzleID(puzzle).equals(puzzleIDToResume)) {
					return puzzle;
				}
			}
		}
		return null;
	}

	public void clearPuzzleToResume() {
		Log.d(this.getClass().getName(), "Delete entry for resuming puzzle");
		SharedPreferences.Editor editor = this.sharedPreferences.edit();
		editor.remove(SaveGameManager.KEY_RESUME);
		editor.commit();
	}

	private static String generatePuzzleID(final Puzzle puzzle) {
		return Integer.toString(puzzle.hashCode());
	}

}
