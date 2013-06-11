package at.ac.uibk.akari.controller;

import java.util.ArrayList;
import java.util.List;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
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
import at.ac.uibk.akari.listener.MenuItemSeletedEvent;
import at.ac.uibk.akari.listener.MenuItemSeletedEvent.ItemType;
import at.ac.uibk.akari.listener.MenuListener;
import at.ac.uibk.akari.solver.AkariSolverFull;
import at.ac.uibk.akari.utils.ListenerList;
import at.ac.uibk.akari.utils.StopClockModel;
import at.ac.uibk.akari.view.GameField;
import at.ac.uibk.akari.view.menu.PopupMenuScene;
import at.ac.uibk.akari.view.menu.hud.PuzzleHUD;

public class PuzzleController extends AbstractController implements GameFieldListener, MenuListener, IOnSceneTouchListener, IScrollDetectorListener, IPinchZoomDetectorListener {

	private GameFieldController gameFieldController;
	private GameField gameField;
	private Scene gameScene;
	private ZoomCamera gameCamera;
	private PuzzleHUD gameHUD;
	private PopupMenuScene pauseScene;

	private GameFieldModel puzzle;
	private VertexBufferObjectManager vertexBufferObjectManager;

	private AkariSolverFull solver;

	private ListenerList listenerList;

	private float mPinchZoomStartedCameraZoomFactor;

	private PinchZoomDetector mPinchZoomDetector;
	private SurfaceScrollDetector mScrollDetector;

	private StopClockModel stopClock;

	public PuzzleController(final ZoomCamera gameCamera, final Scene gameScene, final VertexBufferObjectManager vertexBufferObjectManager) {
		this.listenerList = new ListenerList();
		this.gameCamera = gameCamera;
		this.gameScene = gameScene;
		this.vertexBufferObjectManager = vertexBufferObjectManager;
		this.init();
	}

	public void init() {
		this.gameField = new GameField(10, 80, this.vertexBufferObjectManager);
		this.gameFieldController = new GameFieldController(this.gameField);
		this.gameScene.attachChild(this.gameField);
		this.gameScene.registerTouchArea(this.gameField);

		// initializing pause-menu
		List<ItemType> pauseMenuItems = new ArrayList<ItemType>();
		pauseMenuItems.add(ItemType.CONTINUE);
		pauseMenuItems.add(ItemType.RESET);
		pauseMenuItems.add(ItemType.MAIN_MENU);
		this.pauseScene = new PopupMenuScene(this.gameCamera, this.vertexBufferObjectManager, pauseMenuItems);

		// initializing game-field-touch-control
		this.mScrollDetector = new SurfaceScrollDetector(this);
		this.mPinchZoomDetector = new PinchZoomDetector(this);
		this.gameScene.setOnSceneTouchListener(this);

		// initializing stop-clock
		this.stopClock = new StopClockModel();
		MainActivity.registerUpdateHandler(this.stopClock.createNewTimerHandler());

		// initializing game-HUD
		this.gameHUD = new PuzzleHUD((int) this.gameCamera.getWidth(), this.vertexBufferObjectManager);
		this.gameHUD.setStopClockModel(this.stopClock);
	}

	public void setPuzzle(final Puzzle puzzle) throws ContradictionException {
		this.puzzle = new GameFieldModel(puzzle);
		this.solver = new AkariSolverFull(this.puzzle);
		this.gameField.setPuzzle(this.puzzle);
	}

	@Override
	public boolean start() {
		// setting the game-HUD if it was not already set
		if (this.gameCamera.getHUD() != this.gameHUD) {
			this.gameCamera.setHUD(this.gameHUD);
		}
		this.gameFieldController.addGameFieldListener(this);
		this.gameHUD.addPuzzleControlListener(this);
		this.gameScene.setOnSceneTouchListener(this);
		this.pauseScene.addMenuListener(this);
		this.stopClock.reset();
		this.stopClock.start();
		return this.gameFieldController.start();

	}

	@Override
	public boolean stop() {
		this.gameFieldController.removeGameFieldListener(this);
		this.gameHUD.removePuzzleControlListener(this);
		this.gameScene.setOnSceneTouchListener(null);
		this.pauseScene.addMenuListener(this);
		this.stopClock.stop();
		return this.gameFieldController.stop();
	}

	private void firePuzzleSolved() {
		for (GameListener listener : this.listenerList.getListeners(GameListener.class)) {
			listener.puzzleSolved(this, this.stopClock.getSecondsElapsed());
		}
	}

	private void firePuzzleStopped() {
		for (GameListener listener : this.listenerList.getListeners(GameListener.class)) {
			listener.puzzleStopped(this);
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
				this.stopClock.stop();
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

	@Override
	public void menuItemSelected(final MenuItemSeletedEvent event) {
		if (event.getSource() == this.gameHUD) {

			this.stopClock.stop();

			switch (event.getItemType()) {
			case PAUSE:
				Log.i(this.getClass().getName(), "PAUSE-Game pressed");
				this.gameScene.setChildScene(this.pauseScene, false, true, true);
				this.gameHUD.setEnabled(false);
				break;
			case HELP:
				Log.i(this.getClass().getName(), "HELP-Game pressed");
				MainActivity.showToast("HELP", Toast.LENGTH_SHORT);
				try {
					this.solver.setSolutionToModel();
				} catch (TimeoutException e) {
					e.printStackTrace();
				}
				this.gameField.adaptFieldToModel();
				this.onGameFieldChanged();
				break;
			default:
				break;
			}
		} else if (event.getSource() == this.pauseScene) {

			this.pauseScene.back();
			this.gameHUD.setEnabled(true);

			switch (event.getItemType()) {
			case CONTINUE:
				Log.i(this.getClass().getName(), "CONTINUE-Game pressed");
				this.stopClock.start();
				break;
			case MAIN_MENU:
				Log.i(this.getClass().getName(), "MAIN_MENU pressed");
				this.stopClock.stop();
				this.firePuzzleStopped();
				break;
			case RESET:
				Log.i(this.getClass().getName(), "RESET-Game pressed");
				this.stopClock.reset();
				this.gameFieldController.resetGameField();
				break;
			default:
				break;
			}
		}
	}
}
