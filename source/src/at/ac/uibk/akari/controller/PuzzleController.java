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
import at.ac.uibk.akari.common.menu.DefaultMenuItem;
import at.ac.uibk.akari.common.menu.MenuItem;
import at.ac.uibk.akari.core.GameFieldModel;
import at.ac.uibk.akari.core.Puzzle;
import at.ac.uibk.akari.listener.GameFieldListener;
import at.ac.uibk.akari.listener.GameListener;
import at.ac.uibk.akari.listener.InputEvent;
import at.ac.uibk.akari.listener.MenuItemSeletedEvent;
import at.ac.uibk.akari.listener.MenuListener;
import at.ac.uibk.akari.solver.AkariSolverFull;
import at.ac.uibk.akari.utils.GameFieldSaveState;
import at.ac.uibk.akari.utils.ListenerList;
import at.ac.uibk.akari.utils.PuzzleManager;
import at.ac.uibk.akari.utils.SceneManager;
import at.ac.uibk.akari.utils.ScoreManager;
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
	private PopupMenuScene winninMenuScene;

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
		List<MenuItem> pauseMenuItems = new ArrayList<MenuItem>();
		pauseMenuItems.add(DefaultMenuItem.CONTINUE);
		pauseMenuItems.add(DefaultMenuItem.RESET);
		pauseMenuItems.add(DefaultMenuItem.MAIN_MENU);
		this.pauseScene = new PopupMenuScene(this.gameCamera, this.vertexBufferObjectManager, pauseMenuItems);

		// initialize winning-menu-scene
		List<MenuItem> winningMenuItems = new ArrayList<MenuItem>();
		winningMenuItems.add(DefaultMenuItem.NEXT);
		winningMenuItems.add(DefaultMenuItem.REPLAY);
		winningMenuItems.add(DefaultMenuItem.MAIN_MENU);
		this.winninMenuScene = new PopupMenuScene(this.gameCamera, this.vertexBufferObjectManager, winningMenuItems);

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
		GameFieldSaveState oldSaveState = ScoreManager.getInstance().loadGameFiledState(puzzle);
		if (oldSaveState != null) {
			MainActivity.showToast("Could be resumed at time " + oldSaveState.getSecondsElapsed(), Toast.LENGTH_LONG);
			this.puzzle.setLamps(oldSaveState.getLamps());
			this.stopClock.setSecondsElapsed(oldSaveState.getSecondsElapsed());
		} else {
			this.stopClock.reset();
		}
		this.solver = new AkariSolverFull(this.puzzle);
		this.gameField.setPuzzle(this.puzzle);
	}

	@Override
	public boolean start() {
		Log.d(this.getClass().getName(), "Start puzzle-controller");
		SceneManager.getInstance().setCurrentScene(this, this.gameScene, this.gameHUD);
		this.gameFieldController.addGameFieldListener(this);
		this.gameHUD.addPuzzleControlListener(this);
		this.gameScene.setOnSceneTouchListener(this);
		this.pauseScene.addMenuListener(this);
		this.winninMenuScene.addMenuListener(this);
		this.stopClock.start();
		return this.gameFieldController.start();

	}

	@Override
	public boolean stop() {
		Log.d(this.getClass().getName(), "Stop puzzle-controller");
		this.gameFieldController.removeGameFieldListener(this);
		this.gameHUD.removePuzzleControlListener(this);
		this.gameScene.setOnSceneTouchListener(null);
		this.pauseScene.removeMenuListener(this);
		this.winninMenuScene.removeMenuListener(this);
		this.stopClock.stop();
		return this.gameFieldController.stop();
	}

	private void onPuzzleSolved() {
		// saving score
		ScoreManager.getInstance().saveScore(this.getCurrentPuzzle(), this.stopClock.getSecondsElapsed());
		this.gameHUD.setEnabled(false);
		this.gameScene.setChildScene(this.winninMenuScene, false, true, true);
	}

	private void fireGameStopped() {
		InputEvent event = new InputEvent(this);
		for (GameListener listener : this.listenerList.getListeners(GameListener.class)) {
			listener.gameStopped(event);
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
				this.onPuzzleSolved();
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

			DefaultMenuItem selectedItem = (DefaultMenuItem) event.getMenuItem();
			switch (selectedItem) {
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

			DefaultMenuItem selectedItem = (DefaultMenuItem) event.getMenuItem();
			switch (selectedItem) {
			case CONTINUE:
				Log.i(this.getClass().getName(), "CONTINUE-Game pressed");
				this.stopClock.start();
				break;
			case MAIN_MENU:
				Log.i(this.getClass().getName(), "MAIN_MENU pressed");
				this.stop();
				ScoreManager.getInstance().saveGameFiledState(GameFieldSaveState.generate(this.puzzle, this.stopClock.getSecondsElapsed()));
				this.fireGameStopped();
				break;
			case RESET:
				Log.i(this.getClass().getName(), "RESET-Game pressed");
				this.stopClock.reset();
				this.gameFieldController.resetGameField();
				this.stopClock.start();
				break;
			default:
				break;
			}
		}
		// source was the winning-menu-scene
		else if (event.getSource() == this.winninMenuScene) {

			this.winninMenuScene.back();
			DefaultMenuItem selectedItem = (DefaultMenuItem) event.getMenuItem();
			switch (selectedItem) {
			case REPLAY:
				Log.i(this.getClass().getName(), "REPLAY-Game pressed");
				this.stopClock.reset();
				this.gameFieldController.resetGameField();
				this.stopClock.start();
				this.gameHUD.setEnabled(true);
				break;
			case NEXT:
				Log.i(this.getClass().getName(), "NEXT-Game pressed");
				try {
					this.setPuzzle(PuzzleManager.getInstance().getNextPuzzle(this.getCurrentPuzzle()));
					this.stopClock.reset();
					this.stopClock.start();
					this.gameHUD.setEnabled(true);
				} catch (ContradictionException e) {
					e.printStackTrace();
				}
				break;
			case MAIN_MENU:
				Log.i(this.getClass().getName(), "MAIN_MENU pressed");
				this.stop();
				this.fireGameStopped();
				break;
			default:
				break;
			}
		}
	}

	public Puzzle getCurrentPuzzle() {
		if (this.puzzle != null) {
			return this.puzzle.getPuzzle();
		}
		return null;
	}

	@Override
	public void onBackKeyPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGameStop() {
		ScoreManager.getInstance().saveGameFiledState(GameFieldSaveState.generate(this.puzzle, this.stopClock.getSecondsElapsed()));
		ScoreManager.getInstance().savePuzzleToResume(this.getCurrentPuzzle());
	}
}
