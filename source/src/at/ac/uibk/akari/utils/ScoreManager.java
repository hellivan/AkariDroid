package at.ac.uibk.akari.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.util.Log;
import at.ac.uibk.akari.core.Puzzle;

public class ScoreManager {

	private SharedPreferences sharedPreferences;

	private static ScoreManager scoreManager;

	private static final long EMPTY_LONG = -1;
	private static final int EMPTY_INT = -1;
	private static final String EMPTY_STRING = "EMPTY";

	public static final int EMPTY_SCORE = ScoreManager.EMPTY_INT;

	private static final String SCORE_SUFFIX = "_SCORE";

	private static final String LAMPS_SUFFIX = "_LAMPS";
	private static final String TIME_SUFFIX = "_TIME";

	private static final String KEY_RESUME = "RESUME";

	public static ScoreManager getInstance() {
		if (ScoreManager.scoreManager == null) {
			ScoreManager.scoreManager = new ScoreManager();
		}
		return ScoreManager.scoreManager;
	}

	private ScoreManager() {

	}

	public void init(final Context context) {
		this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void saveScore(final Puzzle puzzle, final long score) {
		String puzzleID = ScoreManager.generatePuzzleID(puzzle);
		long oldScore = this.loadScore(puzzle);
		if ((oldScore == ScoreManager.EMPTY_SCORE) || (score < oldScore)) {
			Log.d(this.getClass().getName(), "Save score " + score + " for puzzle " + puzzleID);
			SharedPreferences.Editor editor = this.sharedPreferences.edit();
			editor.putLong(puzzleID + ScoreManager.SCORE_SUFFIX, score);
			editor.commit();
		} else {
			Log.d(this.getClass().getName(), "New score is worse than new score. Don't save new one");
		}
	}

	public long loadScore(final Puzzle puzzle) {
		String puzzleID = ScoreManager.generatePuzzleID(puzzle);
		long score = this.sharedPreferences.getLong(puzzleID + ScoreManager.SCORE_SUFFIX, ScoreManager.EMPTY_SCORE);
		Log.d(this.getClass().getName(), "Loaded score " + score + " for puzzle " + puzzleID);
		return score;
	}

	public void clearScore(final Puzzle puzzle) {
		String puzzleID = ScoreManager.generatePuzzleID(puzzle);
		Log.d(this.getClass().getName(), "Delete score for puzzle " + puzzleID);
		SharedPreferences.Editor editor = this.sharedPreferences.edit();
		editor.remove(puzzleID + ScoreManager.SCORE_SUFFIX);
		editor.commit();
	}

	public void clearAll() {
		SharedPreferences.Editor editor = this.sharedPreferences.edit();
		editor.clear();
		editor.commit();
	}

	public void saveGameFiledState(final GameFieldSaveState gameFieldSaveState) {
		String puzzleID = ScoreManager.generatePuzzleID(gameFieldSaveState.getPuzzle());
		Log.d(this.getClass().getName(), "Save game-field-state for puzzle " + puzzleID);
		SharedPreferences.Editor editor = this.sharedPreferences.edit();
		editor.putString(puzzleID + ScoreManager.LAMPS_SUFFIX, ScoreManager.convertPointsToString(gameFieldSaveState.getLamps()));
		editor.putLong(puzzleID + ScoreManager.TIME_SUFFIX, gameFieldSaveState.getSecondsElapsed());
		editor.commit();
	}

	public GameFieldSaveState loadGameFiledState(final Puzzle puzzle) {
		String puzzleID = ScoreManager.generatePuzzleID(puzzle);
		Log.d(this.getClass().getName(), "Loading game-field-state for puzzle " + puzzleID);
		String lampsLoaded = this.sharedPreferences.getString(puzzleID + ScoreManager.LAMPS_SUFFIX, ScoreManager.EMPTY_STRING);
		long timeLoaded = this.sharedPreferences.getLong(puzzleID + ScoreManager.TIME_SUFFIX, ScoreManager.EMPTY_LONG);
		if (lampsLoaded.equals(ScoreManager.EMPTY_STRING) || (timeLoaded == ScoreManager.EMPTY_LONG)) {
			return null;
		}
		return GameFieldSaveState.generate(puzzle, ScoreManager.convertStringToPoints(lampsLoaded), timeLoaded);
	}

	private static String generatePuzzleID(final Puzzle puzzle) {
		return Integer.toString(puzzle.hashCode());
	}

	private static String convertPointsToString(final List<Point> points) {
		StringBuffer result = new StringBuffer();
		for (Point point : points) {
			result.append("[" + point.x + "," + point.y + "]");
		}
		return result.toString();
	}

	private static List<Point> convertStringToPoints(final String points) {
		List<Point> result = new ArrayList<Point>();
		Pattern pattern = Pattern.compile("\\[(\\d*),(\\d*)\\]");
		Matcher matcher = pattern.matcher(points);
		while (matcher.find()) {
			result.add(new Point(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))));
		}
		return result;
	}

	public void savePuzzleToResume(final Puzzle puzzle) {
		String puzzleID = ScoreManager.generatePuzzleID(puzzle);
		Log.d(this.getClass().getName(), "Save puzzle " + puzzleID + " so that it can be resumed.");
		SharedPreferences.Editor editor = this.sharedPreferences.edit();
		editor.putString(ScoreManager.KEY_RESUME, puzzleID);
		editor.commit();
	}

	public Puzzle loadPuzzleToResume() {
		String puzzleIDToResume = this.sharedPreferences.getString(ScoreManager.KEY_RESUME, ScoreManager.EMPTY_STRING);
		if (!puzzleIDToResume.equals(ScoreManager.EMPTY_STRING)) {
			List<Puzzle> puzzles = PuzzleManager.getInstance().getPuzzles();
			for (Puzzle puzzle : puzzles) {
				if (ScoreManager.generatePuzzleID(puzzle).equals(puzzleIDToResume)) {
					return puzzle;
				}
			}
		}
		return null;
	}

	public void clearPuzzleToResume() {
		Log.d(this.getClass().getName(), "Delete entry for resuming puzzle");
		SharedPreferences.Editor editor = this.sharedPreferences.edit();
		editor.remove(ScoreManager.KEY_RESUME);
		editor.commit();
	}
}
