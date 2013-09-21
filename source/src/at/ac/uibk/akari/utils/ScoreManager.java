package at.ac.uibk.akari.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import at.ac.uibk.akari.core.Puzzle;

public class ScoreManager {

	private SharedPreferences sharedPreferences;

	private static ScoreManager scoreManager;

	public static final int EMPTY_SCORE = -1;

	public static ScoreManager getInstance() {
		if (ScoreManager.scoreManager == null) {
			ScoreManager.scoreManager = new ScoreManager();
		}
		return ScoreManager.scoreManager;
	}

	public void init(final Context context) {
		this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void saveScore(final Puzzle puzzle, final long score) {
		String puzzleID = ScoreManager.generatePuzzleID(puzzle);
		Log.d(this.getClass().getName(), "Save score " + score + " for puzzle " + puzzleID);
		SharedPreferences.Editor editor = this.sharedPreferences.edit();
		editor.putLong(puzzleID, score);
		editor.commit();
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

	private static String generatePuzzleID(final Puzzle puzzle) {
		return Integer.toString(puzzle.hashCode());
	}
}
