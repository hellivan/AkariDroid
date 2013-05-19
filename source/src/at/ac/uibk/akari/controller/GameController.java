package at.ac.uibk.akari.controller;

import java.util.List;

import org.sat4j.specs.ContradictionException;

import android.content.Context;
import android.widget.Toast;
import at.ac.uibk.akari.MainActivity;
import at.ac.uibk.akari.core.Puzzle;
import at.ac.uibk.akari.listener.GameListener;

public class GameController extends AbstractController implements GameListener {

	private List<Puzzle> puzzles;
	private PuzzleController puzzleController;

	private int currentPuzzle;

	public GameController(final List<Puzzle> puzzles) {
		this.puzzles = puzzles;
		this.currentPuzzle = 0;
	}

	@Override
	public boolean start() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean stop() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void puzzleSolved(PuzzleController source, long timeMs) {
		if (source.equals(this.puzzleController)) {
			this.puzzleController.stop();
			try {
				MainActivity.showToast("Solved level", Toast.LENGTH_LONG);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				this.puzzleController.setPuzzle(this.puzzles.get(this.currentPuzzle++));
				this.puzzleController.start();
			} catch (ContradictionException e) {
				e.printStackTrace();
			}
		}

	}

}
