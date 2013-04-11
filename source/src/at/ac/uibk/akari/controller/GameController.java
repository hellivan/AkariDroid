package at.ac.uibk.akari.controller;

import org.andengine.entity.scene.Scene;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import android.graphics.Point;
import at.ac.uibk.akari.core.GameFieldModel;
import at.ac.uibk.akari.core.Puzzle;
import at.ac.uibk.akari.listener.GameFieldListener;
import at.ac.uibk.akari.listener.GameListener;
import at.ac.uibk.akari.solver.AkariSolver;
import at.ac.uibk.akari.utils.ListenerList;
import at.ac.uibk.akari.view.GameField;

public class GameController extends AbstractController implements GameFieldListener {

	private GameFieldController gameFieldController;
	private GameField gameField;
	private Scene gameScene;

	private GameFieldModel puzzle;
	private VertexBufferObjectManager vertexBufferObjectManager;

	private AkariSolver solver;

	private ListenerList listenerList;

	public GameController(final Scene gameScene, final VertexBufferObjectManager vertexBufferObjectManager) {
		this.listenerList = new ListenerList();
		this.gameScene = gameScene;
		this.vertexBufferObjectManager = vertexBufferObjectManager;
		this.init();
	}

	public void init() {
		this.gameField = new GameField(10, 10, this.vertexBufferObjectManager);
		this.gameFieldController = new GameFieldController(this.gameField);
		this.gameScene.attachChild(this.gameField);
		this.gameScene.registerTouchArea(this.gameField);

	}

	public void setPuzzle(final Puzzle puzzle) throws ContradictionException {
		this.puzzle = new GameFieldModel(puzzle);
		this.solver = new AkariSolver(this.puzzle);
		this.gameField.setPuzzle(this.puzzle);
	}

	@Override
	public boolean start() {
		this.gameFieldController.addGameFieldListener(this);
		return this.gameFieldController.start();

	}

	@Override
	public boolean stop() {
		this.gameFieldController.removeGameFieldListener(this);

		return this.gameFieldController.stop();
	}

	private void firePuzzleSolved() {
		for (GameListener listener : this.listenerList.getListeners(GameListener.class)) {
			listener.puzzleSolved(this, 0);
		}
	}

	public void addGameListener(final GameListener listener) {
		this.listenerList.addListener(GameListener.class, listener);
	}

	public void removeGameListener(final GameListener listener) {
		this.listenerList.removeListener(GameListener.class, listener);
	}

	@Override
	public void lampPlaced(final GameFieldController source, final Point position) {
		if (source.equals(this.gameFieldController)) {
			try {
				if (this.solver.isSolved()) {
					this.firePuzzleSolved();
				}
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void lampRemoved(final GameFieldController source, final Point position) {
		if (source.equals(this.gameFieldController)) {
			try {
				if (this.solver.isSolved()) {
					this.firePuzzleSolved();
				}
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
		}
	}
}
