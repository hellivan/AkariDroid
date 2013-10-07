package at.ac.uibk.akari.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.content.res.AssetManager;
import android.util.Log;
import at.ac.uibk.akari.core.Puzzle;
import at.ac.uibk.akari.core.Puzzle.Difficulty;

public class PuzzleManager {

	private static Random rand = new Random();
	private static PuzzleManager manager;

	private String puzzleDirLocal;
	private AssetManager assetManager;
	private Map<Difficulty, List<Puzzle>> puzzlesList;

	public static PuzzleManager getInstance() {
		if (PuzzleManager.manager == null) {
			PuzzleManager.manager = new PuzzleManager();
		}
		return PuzzleManager.manager;
	}

	private PuzzleManager() {

	}

	public void init(final String puzzleDirLocal, final AssetManager assetManager) {
		this.puzzleDirLocal = puzzleDirLocal;
		this.assetManager = assetManager;
	}

	public List<Puzzle> getPuzzles() {
		return this.getPuzzles(Arrays.asList(Difficulty.values()));
	}

	public List<Puzzle> getPuzzles(final Difficulty difficulty) {
		List<Difficulty> difficulties = Arrays.asList(difficulty);
		return this.getPuzzles(difficulties);
	}

	public List<Puzzle> getPuzzles(final List<Difficulty> difficulties) {
		Log.d(this.getClass().getName(), "Getting levels of difficulties: " + difficulties);
		if (this.puzzlesList == null) {
			this.refreshPuzzlesList();
		}
		List<Puzzle> puzzles = new ArrayList<Puzzle>();
		for (Difficulty tmpDifficulty : difficulties) {
			puzzles.addAll(this.puzzlesList.get(tmpDifficulty));
		}
		return puzzles;
	}

	public void refreshPuzzlesList() {
		List<Puzzle> puzzles = new ArrayList<Puzzle>();
		try {
			puzzles.addAll(PuzzleLoader.loadPuzzles(this.assetManager, this.puzzleDirLocal));
		} catch (Exception e) {
			Log.e(this.getClass().getName(), "Error while loading levels-list: " + e.getMessage());
			e.printStackTrace();
		}
		this.puzzlesList = new HashMap<Puzzle.Difficulty, List<Puzzle>>();
		for (Puzzle puzzle : puzzles) {
			Difficulty difficulty = puzzle.getDifficulty();
			List<Puzzle> puzzlesForDifficulty = this.puzzlesList.get(difficulty);
			if (puzzlesForDifficulty == null) {
				puzzlesForDifficulty = new ArrayList<Puzzle>();
				this.puzzlesList.put(difficulty, puzzlesForDifficulty);
			}
			puzzlesForDifficulty.add(puzzle);
		}

		Log.d(this.getClass().getName(), "Loaded levels for difficulties:");
		for (Difficulty difficulty : Difficulty.values()) {
			List<Puzzle> groupedPuzzles = this.puzzlesList.get(difficulty);
			Log.d(this.getClass().getName(), difficulty + ": " + (groupedPuzzles == null ? 0 : groupedPuzzles.size()));
		}
	}

	public Puzzle getRandomPuzzle() {
		List<Puzzle> puzzles = this.getPuzzles();
		int puzzleIndex = PuzzleManager.rand.nextInt(puzzles.size());
		Log.d(this.getClass().getName(), "Returning random-level with index " + puzzleIndex);
		return puzzles.get(puzzleIndex);
	}

	public Puzzle getNextPuzzle(final Puzzle oldPuzzle) {
		List<Puzzle> puzzles = this.getPuzzles(oldPuzzle.getDifficulty());
		int oldIndex = puzzles.indexOf(oldPuzzle);
		if (oldIndex < 0) {
			Log.i(this.getClass().getName(), "Old level not found. Searching for random level.");
			return this.getRandomPuzzle();
		}
		int newIndex = (oldIndex + 1) % puzzles.size();
		Log.d(this.getClass().getName(), "Old level index was " + oldIndex + ", now returning level with index " + newIndex);
		return puzzles.get(newIndex);
	}

	public boolean isPuzzleResumable(final Puzzle puzzle) {
		GameFieldSaveState saveState = SaveGameManager.getInstance().loadGameFiledState(puzzle);
		if (saveState != null) {
			if ((saveState.getLamps().size() > 0) || (saveState.getMarks().size() > 0)) {
				return true;
			}
		}
		return false;
	}

}
