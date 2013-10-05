package at.ac.uibk.akari.mainMenu.controller;

import java.util.ArrayList;
import java.util.List;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.entity.scene.Scene;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.sat4j.specs.ContradictionException;

import android.util.Log;
import at.ac.uibk.akari.MainActivity;
import at.ac.uibk.akari.common.controller.AbstractController;
import at.ac.uibk.akari.common.listener.InputEvent;
import at.ac.uibk.akari.common.listener.MenuItemSeletedEvent;
import at.ac.uibk.akari.common.listener.MenuListener;
import at.ac.uibk.akari.common.view.AbstractMenuScene;
import at.ac.uibk.akari.common.view.DefaultMenuItem;
import at.ac.uibk.akari.common.view.DefaultMenuScene;
import at.ac.uibk.akari.common.view.MenuItem;
import at.ac.uibk.akari.core.Puzzle;
import at.ac.uibk.akari.gamePlay.controller.PuzzleController;
import at.ac.uibk.akari.gamePlay.controller.PuzzleController.ResumeBehaveior;
import at.ac.uibk.akari.gamePlay.listener.GameListener;
import at.ac.uibk.akari.puzzleSelector.controller.PuzzleSelectionController;
import at.ac.uibk.akari.puzzleSelector.listener.PuzzleSelectionEvent;
import at.ac.uibk.akari.puzzleSelector.listener.PuzzleSelectionListener;
import at.ac.uibk.akari.utils.BackgroundLoader;
import at.ac.uibk.akari.utils.BackgroundLoader.BackgroundType;
import at.ac.uibk.akari.utils.PuzzleManager;
import at.ac.uibk.akari.utils.SaveGameManager;
import at.ac.uibk.akari.utils.SceneManager;

public class MainMenuController extends AbstractController implements GameListener, MenuListener, PuzzleSelectionListener {

	private PuzzleController puzzleController;
	private PuzzleSelectionController puzzleSelectionController;

	private ZoomCamera gameCamera;

	private Scene gameScene;
	private Scene puzzleSelectionScene;
	private AbstractMenuScene mainMenuScene;

	private VertexBufferObjectManager vertexBufferObjectManager;

	public MainMenuController(final ZoomCamera gameCamera, final Scene gameScene, final VertexBufferObjectManager vertexBufferObjectManager) {
		this.gameCamera = gameCamera;
		this.gameScene = gameScene;
		this.vertexBufferObjectManager = vertexBufferObjectManager;
		this.init();
	}

	private void init() {
		// initialize puzzle controller
		this.puzzleController = new PuzzleController(this.gameCamera, this.gameScene, this.vertexBufferObjectManager);

		// initialize main-menu-scene
		this.mainMenuScene = new DefaultMenuScene(this.gameCamera, this.vertexBufferObjectManager);
		this.initMainMenuItems(true);

		// initialize game-scene
		this.gameScene.setBackgroundEnabled(true);
		this.gameScene.setBackground(BackgroundLoader.getInstance().getBackground(BackgroundType.GAME_FIELD_BACKGROUND));

		// initialize puzzle-selection-scene and controller
		this.puzzleSelectionScene = new Scene();
		this.puzzleSelectionScene.setBackgroundEnabled(true);
		this.puzzleSelectionScene.setBackground(BackgroundLoader.getInstance().getBackground(BackgroundType.GAME_FIELD_BACKGROUND));
		this.puzzleSelectionController = new PuzzleSelectionController(this.puzzleSelectionScene, this.gameCamera, this.vertexBufferObjectManager);
	}

	private void initMainMenuItems(final boolean hasResumeItem) {
		List<MenuItem> mainMenuItems = new ArrayList<MenuItem>();
		if (hasResumeItem && (SaveGameManager.getInstance().loadPuzzleToResume() != null)) {
			mainMenuItems.add(DefaultMenuItem.RESUME_PUZZLE);
		}
		mainMenuItems.add(DefaultMenuItem.RANDOM_PUZZLE);
		mainMenuItems.add(DefaultMenuItem.SELECT_PUZZLE);
		mainMenuItems.add(DefaultMenuItem.QUIT);
		this.mainMenuScene.setItemTypes(mainMenuItems);
	}

	@Override
	public boolean start() {
		this.puzzleController.addGameListener(this);
		this.mainMenuScene.addMenuListener(this);
		this.puzzleSelectionController.addPuzzleSelectionListener(this);
		SceneManager.getInstance().setCurrentScene(this, this.mainMenuScene);
		return true;
	}

	@Override
	public boolean stop() {
		this.puzzleController.removeGameListener(this);
		this.mainMenuScene.removeMenuListener(this);
		this.puzzleSelectionController.removePuzzleSelectionListener(this);

		return true;
	}

	public void startLevel(final Puzzle puzzle, final ResumeBehaveior resumeBehaveior) {
		try {
			this.puzzleController.setPuzzle(puzzle, resumeBehaveior);
			this.puzzleController.start();
			this.initMainMenuItems(false);
		} catch (ContradictionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void menuItemSelected(final MenuItemSeletedEvent event) {
		if (event.getSource() == this.mainMenuScene) {
			DefaultMenuItem selectedItem = (DefaultMenuItem) event.getMenuItem();
			switch (selectedItem) {
			case RANDOM_PUZZLE:
				this.startLevel(PuzzleManager.getInstance().getRandomPuzzle(), ResumeBehaveior.ASK_FOR_RESUME);
				break;
			case SELECT_PUZZLE:
				this.puzzleSelectionController.start();
				break;
			case RESUME_PUZZLE:
				Puzzle puzzle = SaveGameManager.getInstance().loadPuzzleToResume();
				if (puzzle != null) {
					this.startLevel(puzzle, ResumeBehaveior.AUTO_RESUME);
				}
				break;
			case QUIT:
				SaveGameManager.getInstance().clearPuzzleToResume();
				MainActivity.quit();
			default:
				break;
			}
		}
	}

	@Override
	public void gameStopped(final InputEvent event) {
		if (event.getSource() == this.puzzleController) {
			SceneManager.getInstance().setCurrentScene(this, this.mainMenuScene);
		}
	}

	@Override
	public void puzzleSelectionCanceled(final PuzzleSelectionEvent event) {
		if (event.getSource().equals(this.puzzleSelectionController)) {
			Log.i(this.getClass().getName(), "Puzzle-selection cancelled");
			SceneManager.getInstance().setCurrentScene(this, this.mainMenuScene);
		}
	}

	@Override
	public void puzzleSelected(final PuzzleSelectionEvent event) {
		if (event.getSource().equals(this.puzzleSelectionController)) {
			this.startLevel(event.getPuzzle(), ResumeBehaveior.ASK_FOR_RESUME);
		}
	}

	@Override
	public void onBackKeyPressed() {
		MainActivity.quit();
	}

	@Override
	public void onGameStop() {
		// TODO Auto-generated method stub
	}
}
