package at.ac.uibk.akari.controller;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.entity.scene.Scene;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import android.graphics.Point;
import android.util.Log;
import android.widget.Toast;
import at.ac.uibk.akari.MainActivity;
import at.ac.uibk.akari.core.GameFieldModel;
import at.ac.uibk.akari.core.Puzzle;
import at.ac.uibk.akari.listener.GameFieldListener;
import at.ac.uibk.akari.listener.GameListener;
import at.ac.uibk.akari.listener.InputEvent;
import at.ac.uibk.akari.listener.PuzzleControlListener;
import at.ac.uibk.akari.solver.AkariSolver;
import at.ac.uibk.akari.utils.ListenerList;
import at.ac.uibk.akari.view.GameField;
import at.ac.uibk.akari.view.menu.PuzzleHUD;

public class PuzzleController extends AbstractController implements GameFieldListener, PuzzleControlListener {

	private GameFieldController gameFieldController;
	private GameField gameField;
	private Scene gameScene;
	private ZoomCamera gameCamera;
	private PuzzleHUD gameHUD;

	private GameFieldModel puzzle;
	private VertexBufferObjectManager vertexBufferObjectManager;

	private AkariSolver solver;

	private ListenerList listenerList;

	public PuzzleController(final ZoomCamera gameCamera, final Scene gameScene, final VertexBufferObjectManager vertexBufferObjectManager) {
		this.listenerList = new ListenerList();
		this.gameCamera = gameCamera;
		this.gameScene = gameScene;
		this.vertexBufferObjectManager = vertexBufferObjectManager;
		this.init();
	}

	public void init() {
		this.gameField = new GameField(10, 10, this.vertexBufferObjectManager);
		this.gameFieldController = new GameFieldController(this.gameField);
		this.gameScene.attachChild(this.gameField);
		this.gameScene.registerTouchArea(this.gameField);
		this.gameHUD = new PuzzleHUD((int) this.gameCamera.getWidth(), this.vertexBufferObjectManager);
		this.gameCamera.setHUD(this.gameHUD);

	}

	public void setPuzzle(final Puzzle puzzle) throws ContradictionException {
		this.puzzle = new GameFieldModel(puzzle);
		this.solver = new AkariSolver(this.puzzle);
		this.gameField.setPuzzle(this.puzzle);
	}

	@Override
	public boolean start() {
		this.gameFieldController.addGameFieldListener(this);
		this.gameHUD.addPuzzleControlListener(this);
		return this.gameFieldController.start();

	}

	@Override
	public boolean stop() {
		this.gameFieldController.removeGameFieldListener(this);
		this.gameHUD.removePuzzleControlListener(this);
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

	@Override
	public void pausePuzzle(InputEvent event) {
		Log.i(this.getClass().getName(), "PAUSE-Game pressed");
		MainActivity.showToast("PAUSE", Toast.LENGTH_SHORT);
	}

	@Override
	public void helpPuzzle(InputEvent event) {
		Log.i(this.getClass().getName(), "HELP-Game pressed");
		MainActivity.showToast("HELP", Toast.LENGTH_SHORT);
		try {
			this.solver.setSolutionToModel();
			this.gameField.adaptFieldToModel();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
	}
}
