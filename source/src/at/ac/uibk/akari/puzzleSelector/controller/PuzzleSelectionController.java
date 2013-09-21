package at.ac.uibk.akari.puzzleSelector.controller;

import java.util.ArrayList;
import java.util.List;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.util.Log;
import at.ac.uibk.akari.common.menu.ItemType;
import at.ac.uibk.akari.common.menu.MenuItem;
import at.ac.uibk.akari.controller.AbstractController;
import at.ac.uibk.akari.core.Puzzle;
import at.ac.uibk.akari.listener.MenuItemSeletedEvent;
import at.ac.uibk.akari.listener.MenuListener;
import at.ac.uibk.akari.puzzleSelector.listener.PuzzleSelectionEvent;
import at.ac.uibk.akari.puzzleSelector.listener.PuzzleSelectionListener;
import at.ac.uibk.akari.puzzleSelector.listener.ValueChangedEvent;
import at.ac.uibk.akari.puzzleSelector.listener.ValueChangedListener;
import at.ac.uibk.akari.puzzleSelector.view.DifficultyMenuScene;
import at.ac.uibk.akari.puzzleSelector.view.LevelSelector;
import at.ac.uibk.akari.puzzleSelector.view.PuzzleSelectorHUD;
import at.ac.uibk.akari.utils.ListenerList;
import at.ac.uibk.akari.utils.SceneManager;
import at.ac.uibk.akari.view.Insets;

public class PuzzleSelectionController extends AbstractController implements IOnSceneTouchListener, MenuListener, PuzzleSelectionListener, ValueChangedListener<Integer> {

	private Scene selectorScene;
	private DifficultyMenuScene difficultyScene;
	private Camera camera;
	private VertexBufferObjectManager vertexBufferObjectManager;
	private LevelSelector levelSelector;
	private PuzzleSelectorHUD hud;

	protected ListenerList listeners;

	public PuzzleSelectionController(final Scene scene, final Camera camera, final VertexBufferObjectManager vertexBufferObjectManager) {
		this.listeners = new ListenerList();
		this.selectorScene = scene;
		this.camera = camera;
		this.init();
	}

	private void init() {

		List<MenuItem> itemTypes = new ArrayList<MenuItem>();
		itemTypes.add(Puzzle.Difficulty.EASY);
		itemTypes.add(Puzzle.Difficulty.MEDIUM);
		itemTypes.add(Puzzle.Difficulty.HARD);
		this.difficultyScene = new DifficultyMenuScene(this.camera, this.vertexBufferObjectManager, itemTypes);

		this.hud = new PuzzleSelectorHUD((int) this.camera.getWidth(), this.vertexBufferObjectManager);

		this.levelSelector = new LevelSelector(3, 2, new Insets(this.hud.getDesiredHUDHeight(), 40, 0, 40), this.camera, this.vertexBufferObjectManager);

		this.selectorScene.attachChild(this.levelSelector);
	}

	public void setPuzzles(final List<Puzzle> puzzles) {
		this.levelSelector.setLevels(puzzles);
	}

	@Override
	public boolean start() {
		this.selectorScene.setOnSceneTouchListener(this);
		this.levelSelector.addPuzzleSelectionListener(this);
		this.levelSelector.addValueChangedListener(this);
		this.hud.addPuzzleControlListener(this);
		this.camera.setHUD(this.hud);
		this.levelSelector.start();
		this.hud.setIndicatorIndex(this.levelSelector.getCurrentPageIndex(), this.levelSelector.getPagesCount());
		SceneManager.getInstance().setCurrentScene(this, this.selectorScene);
		return true;
	}

	@Override
	public boolean stop() {
		this.selectorScene.setOnSceneTouchListener(null);
		this.levelSelector.removePuzzleSelectionListener(this);
		this.levelSelector.removeValueChangedListener(this);
		this.hud.removePuzzleControlListener(this);
		this.camera.setHUD(null);
		this.levelSelector.stop();
		return true;
	}

	@Override
	public boolean onSceneTouchEvent(final Scene scene, final TouchEvent pSceneTouchEvent) {
		this.levelSelector.onTouchEvent(pSceneTouchEvent);
		return false;
	}

	@Override
	public void menuItemSelected(final MenuItemSeletedEvent event) {
		if (event.getSource().equals(this.hud)) {
			ItemType selectedItem = (ItemType) event.getMenuItem();
			switch (selectedItem) {
			case BACK:
				this.stop();
				this.firePuzzleSelectionCanceled();
				break;
			default:
				break;
			}

		}
	}

	public void addPuzzleSelectionListener(final PuzzleSelectionListener listener) {
		this.listeners.addListener(PuzzleSelectionListener.class, listener);
	}

	public void removePuzzleSelectionListener(final PuzzleSelectionListener listener) {
		this.listeners.removeListener(PuzzleSelectionListener.class, listener);
	}

	protected void firePuzzleSelected(final Puzzle puzzle) {
		PuzzleSelectionEvent event = new PuzzleSelectionEvent(this, puzzle);
		for (PuzzleSelectionListener listener : this.listeners.getListeners(PuzzleSelectionListener.class)) {
			listener.puzzleSelected(event);
		}
	}

	protected void firePuzzleSelectionCanceled() {
		PuzzleSelectionEvent event = new PuzzleSelectionEvent(this, null);
		for (PuzzleSelectionListener listener : this.listeners.getListeners(PuzzleSelectionListener.class)) {
			listener.puzzleSelectionCanceled(event);
		}
	}

	@Override
	public void puzzleSelectionCanceled(final PuzzleSelectionEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void puzzleSelected(final PuzzleSelectionEvent event) {
		if (event.getSource().equals(this.levelSelector)) {
			this.stop();
			this.firePuzzleSelected(event.getPuzzle());
		}
	}

	@Override
	public void valueChanged(final ValueChangedEvent<Integer> event) {
		if (event.getSource().equals(this.levelSelector)) {
			Log.d(this.getClass().getName(), "Changed page from " + event.getOldValue() + " to " + event.getNewValue());
			this.hud.setIndicatorIndex(event.getNewValue(), this.levelSelector.getPagesCount());
		}
	}

}
