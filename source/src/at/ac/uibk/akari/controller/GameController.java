package at.ac.uibk.akari.controller;

import java.util.List;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.sat4j.specs.ContradictionException;

import android.widget.Toast;
import at.ac.uibk.akari.MainActivity;
import at.ac.uibk.akari.core.Puzzle;
import at.ac.uibk.akari.listener.GameListener;

public class GameController extends AbstractController implements GameListener {

	private List<Puzzle> puzzles;
	private PuzzleController puzzleController;

	private int currentPuzzle;

	private ZoomCamera gameCamera;
	private Scene gameScene;

	private Scene winningScene;

	private int counter;

	private VertexBufferObjectManager vertexBufferObjectManager;

	public GameController(final ZoomCamera gameCamera, final Scene gameScene, final VertexBufferObjectManager vertexBufferObjectManager, final List<Puzzle> puzzles) {
		this.puzzles = puzzles;
		this.gameCamera = gameCamera;
		this.gameScene = gameScene;
		this.vertexBufferObjectManager = vertexBufferObjectManager;
		this.init();
	}

	private void init() {
		this.puzzleController = new PuzzleController(this.gameCamera, this.gameScene, this.vertexBufferObjectManager);
		this.winningScene = new Scene();
		this.winningScene.setBackgroundEnabled(false);

		this.winningScene.setOnSceneTouchListener(new IOnSceneTouchListener() {

			@Override
			public boolean onSceneTouchEvent(Scene arg0, TouchEvent arg1) {
				if (arg0 == winningScene&& counter>2) {
					startNextLevel();
				}
				counter++;
				return false;
			}
		});

	}

	@Override
	public boolean start() {
		this.currentPuzzle = 0;
		this.puzzleController.addGameListener(this);
		try {
			this.puzzleController.setPuzzle(this.puzzles.get(this.currentPuzzle++));
			this.puzzleController.start();
		} catch (ContradictionException e) {
			e.printStackTrace();
		}

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
			MainActivity.showToast("Solved level", Toast.LENGTH_LONG);

			counter=0;
			this.gameScene.setChildScene(this.winningScene);
		}
	}

	public void startNextLevel() {
		try {
			this.gameScene.clearChildScene();
			this.puzzleController.setPuzzle(this.puzzles.get(this.currentPuzzle++));
			this.puzzleController.start();
		} catch (ContradictionException e) {
			e.printStackTrace();
		}
	}

}
