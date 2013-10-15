package at.ac.uibk.akari.controller;

import java.util.ArrayList;
import java.util.List;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.entity.scene.Scene;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import android.util.Log;
import at.ac.uibk.akari.MainActivity;
import at.ac.uibk.akari.core.GameFieldModel;
import at.ac.uibk.akari.core.Puzzle;
import at.ac.uibk.akari.core.Puzzle.CellState;
import at.ac.uibk.akari.listener.GameListener;
import at.ac.uibk.akari.listener.MenuItemSeletedEvent;
import at.ac.uibk.akari.listener.MenuItemSeletedEvent.ItemType;
import at.ac.uibk.akari.listener.MenuListener;
<<<<<<< HEAD
import at.ac.uibk.akari.solver.AkariSolverFull;
import at.ac.uibk.akari.solver.AkariSolverFullClassicSAT;
import at.ac.uibk.akari.utils.TextureLoader;
import at.ac.uibk.akari.utils.TextureLoader.BackgroundType;
=======
import at.ac.uibk.akari.utils.BackgroundLoader;
import at.ac.uibk.akari.utils.BackgroundLoader.BackgroundType;
import at.ac.uibk.akari.view.menu.AbstractMenuScene;
import at.ac.uibk.akari.view.menu.MainMenuScene;
>>>>>>> branch 'master' of ssh://gitolite@server:22/AkariDroid.git
import at.ac.uibk.akari.view.menu.PopupMenuScene;

public class GameController extends AbstractController implements GameListener, MenuListener {

	private List<Puzzle> puzzles;
	private PuzzleController puzzleController;

	private int currentPuzzleIndex;

	private ZoomCamera gameCamera;
	private Scene gameScene;

	private PopupMenuScene winninMenuScene;
	private AbstractMenuScene mainMenuScene;

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

		// initialize main-menu-scene
		List<ItemType> mainMenuItems = new ArrayList<ItemType>();
		mainMenuItems.add(ItemType.START_PUZZLE);
		mainMenuItems.add(ItemType.QUIT);
		this.mainMenuScene = new MainMenuScene(this.gameCamera, this.vertexBufferObjectManager, mainMenuItems);

		// initialize game-scene
		this.gameScene.setBackground(BackgroundLoader.getInstance().getBackground(BackgroundType.GAME_FIELD_BACKGROUND));

		// initialize winning-menu-scene
		List<ItemType> winningMenuItems = new ArrayList<ItemType>();
		winningMenuItems.add(ItemType.NEXT);
		winningMenuItems.add(ItemType.REPLAY);
		winningMenuItems.add(ItemType.MAIN_MENU);
		this.winninMenuScene = new PopupMenuScene(this.gameCamera, this.vertexBufferObjectManager, winningMenuItems);

	}

	@Override
	public boolean start() {
		this.puzzleController.addGameListener(this);
		this.winninMenuScene.addMenuListener(this);
		
//		//--remove?
//		try {
//			this.puzzleController.setPuzzle(AkariSolverFullClassicSAT.generatePuzzle(10, 10));
//			this.puzzleController.start();
//		} catch (ContradictionException e) {
//			e.printStackTrace();
//		}
//		//remove
		
		this.mainMenuScene.addMenuListener(this);

		this.setCurrentGameScene(this.mainMenuScene);
		return true;
	}

	@Override
	public boolean stop() {
		this.puzzleController.removeGameListener(this);
		this.winninMenuScene.removeMenuListener(this);
		this.mainMenuScene.removeMenuListener(this);
		return true;
	}

	@Override
	public void puzzleSolved(final PuzzleController source, final long timeMs) {
		if (source.equals(this.puzzleController)) {
			this.puzzleController.stop();
			// display game-winning-menu
			this.gameScene.setChildScene(this.winninMenuScene, false, true, true);
		}
	}

	public void startLevel(final int index) {
		try {

			

			
			this.puzzleController.setPuzzle(AkariSolverFull.generatePuzzle(10, 10));
			this.puzzleController.start();
		} catch (ContradictionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void menuItemSelected(final MenuItemSeletedEvent event) {
		// source was the winning-menu-scene
		if (event.getSource() == this.winninMenuScene) {
			this.puzzleController.stop();
			this.winninMenuScene.back();

			switch (event.getItemType()) {
			case REPLAY:
				Log.i(this.getClass().getName(), "REPLAY-Game pressed");
				this.startLevel(this.currentPuzzleIndex);
				break;
			case NEXT:
				Log.i(this.getClass().getName(), "NEXT-Game pressed");
				this.resetGameCamera();
				this.startLevel(++this.currentPuzzleIndex);
				break;
			case MAIN_MENU:
				Log.i(this.getClass().getName(), "MAIN_MENU pressed");
				this.setCurrentGameScene(this.mainMenuScene);
				break;
			default:
				break;
			}
		}
		// source was the main-menu-scene
		else if (event.getSource() == this.mainMenuScene) {
			switch (event.getItemType()) {
			case START_PUZZLE:
				this.setCurrentGameScene(this.gameScene);
				this.currentPuzzleIndex = 0;
				this.startLevel(this.currentPuzzleIndex);
				break;
			case QUIT:
				this.puzzleController.stop();
				MainActivity.quit();
			default:
				break;
			}
		}
	}

	@Override
	public void puzzleStopped(final PuzzleController source) {
		if (source == this.puzzleController) {
			this.puzzleController.stop();
			this.winninMenuScene.back();
			this.setCurrentGameScene(this.mainMenuScene);
		}
	}

	private void resetGameCamera() {
		this.gameCamera.setZoomFactor(1);
		this.gameCamera.setCenter(this.gameCamera.getWidth() / 2, this.gameCamera.getHeight() / 2);
	}

	private void setCurrentGameScene(final Scene scene) {
		this.gameCamera.setHUD(null);
		this.resetGameCamera();
		MainActivity.setCurrentScene(scene);
	}
}
