package at.ac.uibk.akari.controller;

import java.util.ArrayList;
import java.util.List;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.entity.scene.Scene;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import android.util.Log;
import android.widget.Toast;
import at.ac.uibk.akari.MainActivity;
import at.ac.uibk.akari.core.GameFieldModel;
import at.ac.uibk.akari.core.Puzzle;
import at.ac.uibk.akari.core.Puzzle.CellState;
import at.ac.uibk.akari.listener.GameListener;
import at.ac.uibk.akari.listener.MenuItemSeletedEvent;
import at.ac.uibk.akari.listener.MenuItemSeletedEvent.ItemType;
import at.ac.uibk.akari.listener.MenuListener;
import at.ac.uibk.akari.solver.AkariSolverFull;
import at.ac.uibk.akari.solver.AkariSolverFullClassicSAT;
import at.ac.uibk.akari.utils.TextureLoader;
import at.ac.uibk.akari.utils.TextureLoader.BackgroundType;
import at.ac.uibk.akari.view.menu.PopupMenuScene;

public class GameController extends AbstractController implements GameListener, MenuListener {

	private List<Puzzle> puzzles;
	private PuzzleController puzzleController;

	private int currentPuzzle;

	private ZoomCamera gameCamera;
	private Scene gameScene;

	private PopupMenuScene winninMenuScene;

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

		this.gameScene.setBackground(TextureLoader.getInstance().getBackground(BackgroundType.GAME_FIELD_BACKGROUND));

		List<ItemType> winningMenuItems = new ArrayList<ItemType>();
		winningMenuItems.add(ItemType.NEXT);
		winningMenuItems.add(ItemType.REPLAY);
		winningMenuItems.add(ItemType.MAIN_MENU);
		this.winninMenuScene = new PopupMenuScene(this.gameCamera, this.vertexBufferObjectManager, winningMenuItems);
	}

	@Override
	public boolean start() {
		this.currentPuzzle = 0;
		this.puzzleController.addGameListener(this);
		this.winninMenuScene.addMenuListener(this);
		try {
			this.puzzleController.setPuzzle(AkariSolverFullClassicSAT.generatePuzzle(10, 10));
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
	public void puzzleSolved(final PuzzleController source, final long timeMs) {
		if (source.equals(this.puzzleController)) {
			this.puzzleController.stop();
			MainActivity.showToast("Solved level", Toast.LENGTH_LONG);

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
		if (event.getSource() == this.winninMenuScene) {
			switch (event.getItemType()) {
			case REPLAY:
				Log.i(this.getClass().getName(), "REPLAY-Game pressed");
				this.winninMenuScene.back();
				this.startLevel(this.currentPuzzle);
				break;
			case NEXT:
				Log.i(this.getClass().getName(), "NEXT-Game pressed");
				this.winninMenuScene.back();
				this.startLevel(++this.currentPuzzle);
				break;
			case MAIN_MENU:
				Log.i(this.getClass().getName(), "STOP-Game pressed");
				MainActivity.showToast("STOP", Toast.LENGTH_SHORT);
				break;
			default:
				break;
			}
		}
	}
}
