package at.ac.uibk.akari.controller;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
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
import at.ac.uibk.akari.solver.AkariSolverFull;
import at.ac.uibk.akari.utils.ListenerList;
import at.ac.uibk.akari.view.GameField;
import at.ac.uibk.akari.view.menu.PuzzleHUD;

public class PuzzleController extends AbstractController implements GameFieldListener, PuzzleControlListener, IOnSceneTouchListener, IScrollDetectorListener, IPinchZoomDetectorListener {

	private GameFieldController gameFieldController;
	private GameField gameField;
	private Scene gameScene;
	private ZoomCamera gameCamera;
	private PuzzleHUD gameHUD;

	private GameFieldModel puzzle;
	private VertexBufferObjectManager vertexBufferObjectManager;

	private AkariSolverFull solver;

	private ListenerList listenerList;

	private float mPinchZoomStartedCameraZoomFactor;

	private PinchZoomDetector mPinchZoomDetector;
	private SurfaceScrollDetector mScrollDetector;

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

		this.mScrollDetector = new SurfaceScrollDetector(this);
		this.mPinchZoomDetector = new PinchZoomDetector(this);

		this.gameScene.setOnSceneTouchListener(this);
	}

	public void setPuzzle(final Puzzle puzzle) throws ContradictionException {
		this.puzzle = new GameFieldModel(puzzle);
		this.solver = new AkariSolverFull(this.puzzle);
		this.gameField.setPuzzle(this.puzzle);
	}

	@Override
	public boolean start() {
		this.gameFieldController.addGameFieldListener(this);
		this.gameHUD.addPuzzleControlListener(this);
		this.gameScene.setOnSceneTouchListener(this);
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

	private void onGameFieldChanged() {
		try {
			if (this.solver.isSolved()) {
				this.firePuzzleSolved();
			}
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void lampPlaced(final GameFieldController source, final Point position) {
		if (source.equals(this.gameFieldController)) {
			this.onGameFieldChanged();
		}
	}

	@Override
	public void lampRemoved(final GameFieldController source, final Point position) {
		if (source.equals(this.gameFieldController)) {
			this.onGameFieldChanged();
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
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		this.gameField.adaptFieldToModel();
		this.onGameFieldChanged();
	}

	@Override
	public boolean onSceneTouchEvent(final Scene scene, final TouchEvent pSceneTouchEvent) {

		if (pSceneTouchEvent.getMotionEvent().getPointerCount() == 2) {
			this.mScrollDetector.setEnabled(false);
			this.mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);
			return true;

		}

		if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
			this.mScrollDetector.setEnabled(true);
		}

		this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
		return true;
	}

	@Override
	public void onPinchZoom(final PinchZoomDetector arg0, final TouchEvent arg1, final float pZoomFactor) {
		Log.d(this.getClass().getName(), "MainActivity.onPinchZoom()");
		this.gameCamera.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);

	}

	@Override
	public void onPinchZoomFinished(final PinchZoomDetector arg0, final TouchEvent arg1, final float pZoomFactor) {
		Log.d(this.getClass().getName(), "MainActivity.onPinchZoomFinished()");
		this.gameCamera.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);

	}

	@Override
	public void onPinchZoomStarted(final PinchZoomDetector arg0, final TouchEvent arg1) {
		Log.d(this.getClass().getName(), "MainActivity.onPinchZoomStarted()");
		this.mPinchZoomStartedCameraZoomFactor = this.gameCamera.getZoomFactor();

	}

	@Override
	public void onScroll(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
		Log.d(this.getClass().getName(), "MainActivity.onScroll()");
		final float zoomFactor = this.gameCamera.getZoomFactor();
		Log.d(this.getClass().getName(), "ZoomFactor=" + zoomFactor + ", PointerID=" + pPointerID + ", pDistanceX=" + pDistanceX + ", pDistanceY=" + pDistanceY);
		this.gameCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
	}

	@Override
	public void onScrollFinished(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
		Log.d(this.getClass().getName(), "MainActivity.onScrollFinished()");
		final float zoomFactor = this.gameCamera.getZoomFactor();
		this.gameCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
	}

	@Override
	public void onScrollStarted(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
		Log.d(this.getClass().getName(), "MainActivity.onScrollStarted()");
		final float zoomFactor = this.gameCamera.getZoomFactor();
		this.gameCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
	}
}
