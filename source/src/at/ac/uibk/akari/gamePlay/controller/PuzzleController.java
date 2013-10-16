package at.ac.uibk.akari.gamePlay.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
import at.ac.uibk.akari.common.controller.AbstractController;
import at.ac.uibk.akari.common.listener.InputEvent;
import at.ac.uibk.akari.common.listener.MenuItemSeletedEvent;
import at.ac.uibk.akari.common.listener.MenuListener;
import at.ac.uibk.akari.common.view.DefaultMenuItem;
import at.ac.uibk.akari.common.view.DefaultMenuScene;
import at.ac.uibk.akari.common.view.MenuItem;
import at.ac.uibk.akari.common.view.PopupMenuScene;
import at.ac.uibk.akari.core.GameFieldModel;
import at.ac.uibk.akari.core.Puzzle;
import at.ac.uibk.akari.gameField.listener.GameFieldDragEvent;
import at.ac.uibk.akari.gameField.listener.GameFieldInputListener;
import at.ac.uibk.akari.gameField.listener.GameFieldTouchEvent;
import at.ac.uibk.akari.gameField.view.GameField;
import at.ac.uibk.akari.gamePlay.listener.GameListener;
import at.ac.uibk.akari.gamePlay.view.PuzzleHUD;
import at.ac.uibk.akari.solver.AkariSolver;
import at.ac.uibk.akari.stopClock.model.StopClockModel;
import at.ac.uibk.akari.utils.GameFieldSaveState;
import at.ac.uibk.akari.utils.ListenerList;
import at.ac.uibk.akari.utils.PuzzleManager;
import at.ac.uibk.akari.utils.SaveGameManager;
import at.ac.uibk.akari.utils.SceneManager;

public class PuzzleController extends AbstractController implements MenuListener, IOnSceneTouchListener, IScrollDetectorListener, IPinchZoomDetectorListener, GameFieldInputListener {

	public enum ResumeBehaveior {
		AUTO_RESUME,

		DO_NOT_RESUME,

		ASK_FOR_RESUME,

		UNDEFINED;

	}

	private static Random rand = new Random();

	private static final int SECONDS_AMOUNT_ON_HINT = 300;

	private GameField gameField;
	private Scene gameScene;
	private ZoomCamera gameCamera;
	private PuzzleHUD gameHUD;
	private PopupMenuScene pauseScene;
	private PopupMenuScene winninMenuScene;
	private DefaultMenuScene resumeMenuScene;

	private GameFieldModel puzzle;
	private VertexBufferObjectManager vertexBufferObjectManager;

	private AkariSolver solver;

	private ListenerList listenerList;

	private float mPinchZoomStartedCameraZoomFactor;

	private PinchZoomDetector mPinchZoomDetector;
	private SurfaceScrollDetector mScrollDetector;

	private StopClockModel stopClock;

	private ResumeBehaveior resumeBehaveior;

	public PuzzleController(final ZoomCamera gameCamera, final Scene gameScene, final VertexBufferObjectManager vertexBufferObjectManager) {
		this.listenerList = new ListenerList();
		this.gameCamera = gameCamera;
		this.gameScene = gameScene;
		this.vertexBufferObjectManager = vertexBufferObjectManager;
		this.init();
	}

	public void init() {
		this.gameField = new GameField(10, 80, this.vertexBufferObjectManager);
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

		// initialize resume-menu-scene
		List<MenuItem> resumeMenuItems = new ArrayList<MenuItem>();
		resumeMenuItems.add(DefaultMenuItem.RESUME_PUZZLE);
		resumeMenuItems.add(DefaultMenuItem.RESTART_PUZZLE);
		this.resumeMenuScene = new DefaultMenuScene(this.gameCamera, this.vertexBufferObjectManager, resumeMenuItems);

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

	public void setPuzzle(final Puzzle puzzle, final ResumeBehaveior resumeBehaveior) throws ContradictionException {
		this.puzzle = new GameFieldModel(puzzle);
		this.gameField.setPuzzle(this.puzzle);
		this.solver = new AkariSolver(this.puzzle);
		this.stopClock.reset();
		this.resumeBehaveior = resumeBehaveior;
	}

	private void resumePuzzle() {
		GameFieldSaveState oldSaveState = SaveGameManager.getInstance().loadGameFiledState(this.getCurrentPuzzle());
		this.puzzle.setGameFieldPoints(oldSaveState.getSpecialPoints());

		this.gameField.adaptFieldToModel();
		SceneManager.getInstance().setCurrentScene(this, this.gameScene, this.gameHUD);
		this.stopClock.setSecondsElapsed(oldSaveState.getSecondsElapsed());
		this.stopClock.start();
	}

	private void notResumePuzzle() {
		SceneManager.getInstance().setCurrentScene(this, this.gameScene, this.gameHUD);
		this.stopClock.reset();
		this.stopClock.start();
	}

	@Override
	public boolean start() {
		Log.d(this.getClass().getName(), "Start puzzle-controller");
		this.gameField.addGameFieldInputListener(this);
		this.gameHUD.addPuzzleControlListener(this);
		this.gameScene.setOnSceneTouchListener(this);
		this.pauseScene.addMenuListener(this);
		this.winninMenuScene.addMenuListener(this);
		this.resumeMenuScene.addMenuListener(this);

		this.gameHUD.setEnabled(true);
		switch (this.resumeBehaveior) {
		case ASK_FOR_RESUME:
			if (PuzzleManager.getInstance().isPuzzleResumable(this.getCurrentPuzzle())) {
				SceneManager.getInstance().setCurrentScene(this, this.resumeMenuScene);
			} else {
				this.notResumePuzzle();
			}
			break;

		case AUTO_RESUME:
			if (PuzzleManager.getInstance().isPuzzleResumable(this.getCurrentPuzzle())) {
				this.resumePuzzle();
			} else {
				this.notResumePuzzle();
			}
			break;

		case DO_NOT_RESUME:
			this.notResumePuzzle();
			break;

		case UNDEFINED:
			throw new RuntimeException("Resume-behaveior not set before starting the puzzle");
		default:
			throw new RuntimeException("Undefined resume-behaveior " + this.resumeBehaveior);
		}

		this.resumeBehaveior = ResumeBehaveior.UNDEFINED;
		return true;

	}

	@Override
	public boolean stop() {
		Log.d(this.getClass().getName(), "Stop puzzle-controller");
		this.gameField.removeGameFieldInputListener(this);
		this.gameHUD.removePuzzleControlListener(this);
		this.gameScene.setOnSceneTouchListener(null);
		this.pauseScene.removeMenuListener(this);
		this.winninMenuScene.removeMenuListener(this);
		this.resumeMenuScene.removeMenuListener(this);
		this.stopClock.stop();
		return true;
	}

	private void onPuzzleSolved() {
		// saving score
		SaveGameManager.getInstance().saveScore(this.getCurrentPuzzle(), this.stopClock.getSecondsElapsed());
		// clear game-fieldstate and game-to-resume, so that the current puzzle
		// will not be provided to resume
		SaveGameManager.getInstance().clearGameFiledState(this.getCurrentPuzzle());
		SaveGameManager.getInstance().clearPuzzleToResume();

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
		this.zoomCamera(pZoomFactor);
	}

	@Override
	public void onPinchZoomFinished(final PinchZoomDetector arg0, final TouchEvent arg1, final float pZoomFactor) {
		this.zoomCamera(pZoomFactor);
	}

	private void zoomCamera(final float pZoomFactor) {
		float zoomInsetsY = 20;
		float zoomInsetsX = 20;

		float newZoomFactor = this.mPinchZoomStartedCameraZoomFactor * pZoomFactor;

		float visibleHeight = this.gameCamera.getHeightRaw();
		visibleHeight -= this.gameHUD.getDesiredHUDHeight();
		visibleHeight -= zoomInsetsY;
		float visibleHeightScaled = visibleHeight / newZoomFactor;

		float visibleWidth = this.gameCamera.getWidthRaw();
		visibleWidth -= zoomInsetsX;
		float visibleWidthScaled = visibleWidth / newZoomFactor;

		// zooming out
		if (newZoomFactor < this.mPinchZoomStartedCameraZoomFactor) {
			// only if not zoomed out to far
			if ((this.gameField.getWidth() < visibleWidthScaled) && (this.gameField.getHeight() < visibleHeightScaled)) {
				float zoomFactorFitWidth = visibleWidth / this.gameField.getWidth();
				float zoomFactorFitHeight = visibleHeight / this.gameField.getHeight();
				newZoomFactor = Math.min(zoomFactorFitWidth, zoomFactorFitHeight);
			}

		}
		// zooming in
		else {
			// at least 5 cells are visible
			float tmpHeight = GameField.CELL_HEIGHT * 5;
			float tmpWeight = GameField.CELL_WIDTH * 5;
			// only if not zoomed in to far
			if ((tmpWeight > visibleWidthScaled) || (tmpHeight > visibleHeightScaled)) {
				float zoomFactorFitWidth = visibleWidth / tmpWeight;
				float zoomFactorFitHeight = visibleHeight / tmpHeight;
				newZoomFactor = Math.min(zoomFactorFitWidth, zoomFactorFitHeight);
			}
		}

		Log.d(this.getClass().getName(), "Finished zooming and set zoom-factor to " + newZoomFactor);
		Log.d(this.getClass().getName(), "Camera-size: " + this.gameCamera.getWidth() + "/" + this.gameCamera.getHeight());
		Log.d(this.getClass().getName(), "Gamefield-size: " + this.gameField.getWidth() + "/" + this.gameField.getHeight());
		this.gameCamera.setZoomFactor(newZoomFactor);

	}

	@Override
	public void onPinchZoomStarted(final PinchZoomDetector arg0, final TouchEvent arg1) {
		this.mPinchZoomStartedCameraZoomFactor = this.gameCamera.getZoomFactor();
		Log.d(this.getClass().getName(), "Started zooming with current zoom-factor " + this.mPinchZoomStartedCameraZoomFactor);
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

			DefaultMenuItem selectedItem = (DefaultMenuItem) event.getMenuItem();
			switch (selectedItem) {
			case PAUSE:
				Log.i(this.getClass().getName(), "PAUSE-Game pressed");
				this.stopClock.stop();
				this.openPauseMenu();
				break;
			case HELP:
				Log.i(this.getClass().getName(), "HELP-Game pressed");
				try {
					if (this.solver.hasWrongPlacedLamps()) {
						// remove an arbitrary wrong placed lamp
						List<Point> wrongLamps = this.solver.getWrongPlacedLamp();
						Log.i(this.getClass().getName(), "Got " + wrongLamps.size() + " remove-hints");
						Point toRemove = wrongLamps.get(PuzzleController.rand.nextInt(wrongLamps.size()));
						Log.i(this.getClass().getName(), "Got hint to remove lamp at " + toRemove);
						this.gameField.removeLampAt(toRemove);
						this.stopClock.increaseSecondsElapsed(PuzzleController.SECONDS_AMOUNT_ON_HINT);

					} else {
						// place an arbitrary lamp that leads to solve the
						// puzzle
						List<Point> missingLamps = this.solver.getHints();
						if ((missingLamps == null) || (missingLamps.size() < 1)) {
							MainActivity.showToast("No hint available", Toast.LENGTH_SHORT);
						} else {
							Log.i(this.getClass().getName(), "Got " + missingLamps.size() + " add-hints");
							Point toAdd = missingLamps.get(PuzzleController.rand.nextInt(missingLamps.size()));
							Log.i(this.getClass().getName(), "Got hint to place lamp at " + toAdd);
							this.gameField.setLampAt(toAdd);
							this.stopClock.increaseSecondsElapsed(PuzzleController.SECONDS_AMOUNT_ON_HINT);
						}
					}

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

			this.closePauseMenu();

			DefaultMenuItem selectedItem = (DefaultMenuItem) event.getMenuItem();
			switch (selectedItem) {
			case CONTINUE:
				Log.i(this.getClass().getName(), "CONTINUE-Game pressed");
				this.stopClock.start();
				break;
			case MAIN_MENU:
				Log.i(this.getClass().getName(), "MAIN_MENU pressed");
				this.stop();
				this.saveCurrentGameState();
				this.fireGameStopped();
				break;
			case RESET:
				Log.i(this.getClass().getName(), "RESET-Game pressed");
				this.stopClock.reset();
				this.gameField.clearField();
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
				this.gameField.clearField();
				this.stopClock.start();
				this.gameHUD.setEnabled(true);
				break;
			case NEXT:
				Log.i(this.getClass().getName(), "NEXT-Game pressed");
				try {
					this.stop();
					this.setPuzzle(PuzzleManager.getInstance().getNextPuzzle(this.getCurrentPuzzle()), ResumeBehaveior.ASK_FOR_RESUME);
					this.start();
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

		// source was the resume-menu-scene
		else if (event.getSource() == this.resumeMenuScene) {
			DefaultMenuItem selectedItem = (DefaultMenuItem) event.getMenuItem();
			switch (selectedItem) {
			case RESUME_PUZZLE:
				this.resumePuzzle();
				break;

			case RESTART_PUZZLE:
				this.notResumePuzzle();
				break;

			default:
				break;
			}
		}
	}

	private void openPauseMenu() {
		this.gameScene.setChildScene(this.pauseScene, false, true, true);
		this.gameHUD.setEnabled(false);
	}

	private void closePauseMenu() {
		this.pauseScene.back();
		this.gameHUD.setEnabled(true);
	}

	private Puzzle getCurrentPuzzle() {
		if (this.puzzle != null) {
			return this.puzzle.getPuzzle();
		}
		return null;
	}

	@Override
	public void onBackKeyPressed() {
		Scene currentCildScene = this.gameScene.getChildScene();
		if (currentCildScene == null) {
			this.stopClock.stop();
			this.openPauseMenu();
		} else {
			if (currentCildScene.equals(this.pauseScene)) {
				this.stopClock.start();
				this.closePauseMenu();
			}
		}

	}

	@Override
	public void onGameStop() {
		this.saveCurrentPuzzleToResume();

	}

	private boolean saveCurrentGameState() {
		try {
			if (!this.solver.isSolved()) {
				SaveGameManager.getInstance().saveGameFiledState(GameFieldSaveState.generate(this.puzzle, this.stopClock.getSecondsElapsed()));
				return true;
			}
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean saveCurrentPuzzleToResume() {
		if (this.saveCurrentGameState()) {
			SaveGameManager.getInstance().savePuzzleToResume(this.getCurrentPuzzle());
			return true;
		}
		return false;
	}

	@Override
	public void gameFieldTouched(final GameFieldTouchEvent event) {
		if (event.getSource().equals(this.gameField)) {
			Point cellPosition = event.getTouchCell();

			Log.d(this.getClass().getName(), "GameField touched at point " + cellPosition.toString());

			if (this.gameField.isMarkAt(cellPosition) || this.gameField.isLampAt(cellPosition)) {
				if (this.gameField.clearCellAt(cellPosition)) {
					Log.d(this.getClass().getName(), "Cleared cell at " + cellPosition.toString());
					this.onGameFieldChanged();
				}
			}

			else {
				if (this.gameHUD.isLampPlaceingSelected()) {
					if (this.gameField.setMarkAt(cellPosition)) {
						Log.d(this.getClass().getName(), "Placed mark at " + cellPosition.toString());
						this.onGameFieldChanged();
					}
				} else {
					if (this.gameField.setLampAt(cellPosition)) {
						Log.d(this.getClass().getName(), "Placed lamp at " + cellPosition.toString());
						this.onGameFieldChanged();
					}
				}
			}
		}
	}

	@Override
	public void gameFieldDragged(final GameFieldDragEvent event) {
		if (event.getSource().equals(this.gameField)) {
			Point lastCell = event.getLastCell();
			Point currentCell = event.getCurrentCell();
			Log.d(this.getClass().toString(), "GameField dragged from  " + lastCell.toString() + " to " + currentCell.toString());
		}
	}

}
