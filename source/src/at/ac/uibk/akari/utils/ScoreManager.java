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

	public static final int EMPTY_SCORE = -1;

	private static final long EMPTY_TIME = -1;
	private static final String EMPTY_LAMPS = "EMPTY";

	private static final String LAMPS_SUFFIX = "_LAMPS";
	private static final String TIME_SUFFIX = "_TIME";

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
			editor.putLong(puzzleID, score);
			editor.commit();
		} else {
			Log.d(this.getClass().getName(), "New score is worse than new score. Don't save new one");
		}
	}

	public long loadScore(final Puzzle puzzle) {
		String puzzleID = ScoreManager.generatePuzzleID(puzzle);
		long score = this.sharedPreferences.getLong(puzzleID, ScoreManager.EMPTY_SCORE);
		Log.d(this.getClass().getName(), "Loaded score " + score + " for puzzle " + puzzleID);
		return score;
	}

	public void clearScore(final Puzzle puzzle) {
		String puzzleID = ScoreManager.generatePuzzleID(puzzle);
		Log.d(this.getClass().getName(), "Delete score for puzzle " + puzzleID);
		SharedPreferences.Editor editor = this.sharedPreferences.edit();
		editor.remove(puzzleID);
		editor.commit();
	}

	public void clearScore() {
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
		String lampsLoaded = this.sharedPreferences.getString(puzzleID + ScoreManager.LAMPS_SUFFIX, ScoreManager.EMPTY_LAMPS);
		long timeLoaded = this.sharedPreferences.getLong(puzzleID + ScoreManager.TIME_SUFFIX, ScoreManager.EMPTY_TIME);
		if (lampsLoaded.equals(ScoreManager.EMPTY_LAMPS) || (timeLoaded == ScoreManager.EMPTY_TIME)) {
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
}
