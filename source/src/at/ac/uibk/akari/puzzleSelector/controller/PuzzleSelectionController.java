package at.ac.uibk.akari.puzzleSelector.controller;

import java.util.ArrayList;
import java.util.List;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.graphics.PointF;
import android.util.Log;
import at.ac.uibk.akari.controller.AbstractController;
import at.ac.uibk.akari.core.Puzzle;
import at.ac.uibk.akari.listener.InputEvent;
import at.ac.uibk.akari.listener.MenuItemSeletedEvent;
import at.ac.uibk.akari.listener.MenuListener;
import at.ac.uibk.akari.listener.TouchListener;
import at.ac.uibk.akari.puzzleSelector.listener.PuzzleSelectionEvent;
import at.ac.uibk.akari.puzzleSelector.listener.PuzzleSelectionListener;
import at.ac.uibk.akari.puzzleSelector.view.LevelItem;
import at.ac.uibk.akari.puzzleSelector.view.LevelSelector;
import at.ac.uibk.akari.utils.BackgroundLoader;
import at.ac.uibk.akari.utils.BackgroundLoader.BackgroundType;
import at.ac.uibk.akari.utils.ListenerList;
import at.ac.uibk.akari.view.Cell.State;
import at.ac.uibk.akari.view.menu.hud.PuzzleSelectorHUD;

public class PuzzleSelectionController extends AbstractController implements IOnSceneTouchListener, MenuListener, TouchListener {

	private Scene scene;
	private Camera camera;
	private VertexBufferObjectManager vertexBufferObjectManager;
	private LevelSelector levelSelector;
	private PuzzleSelectorHUD hud;

	protected ListenerList listeners;

	private List<LevelItem> items;

	private List<Puzzle> puzzles;

	public PuzzleSelectionController(final Scene scene, final Camera camera, final VertexBufferObjectManager vertexBufferObjectManager) {
		this.listeners = new ListenerList();
		this.scene = scene;
		this.camera = camera;
	}

	private void initScene() {
		this.scene.setBackground(BackgroundLoader.getInstance().getBackground(BackgroundType.GAME_FIELD_BACKGROUND));
		this.scene.setBackgroundEnabled(true);

		this.items = new ArrayList<LevelItem>();
		for (int levelIndex = 0; levelIndex < this.puzzles.size(); levelIndex++) {
			LevelItem item = new LevelItem(new PointF(0, 0), 100, 100, this.vertexBufferObjectManager, levelIndex);
			item.setCellState(State.LAMP);
			this.items.add(item);
		}
		this.levelSelector = new LevelSelector(this.items, 2, 2, this.camera);
		this.scene.attachChild(this.levelSelector);

		this.hud = new PuzzleSelectorHUD((int) this.camera.getWidth(), this.vertexBufferObjectManager);
		this.hud.addPuzzleControlListener(this);
		this.camera.setHUD(this.hud);

	}

	public void setPuzzles(final List<Puzzle> puzzles) {
		this.puzzles = puzzles;
	}

	@Override
	public boolean start() {
		this.initScene();
		this.scene.setOnSceneTouchListener(this);
		for (LevelItem levelItem : this.items) {
			this.scene.registerTouchArea(levelItem);
			levelItem.addTouchListener(this);

		}
		return true;
	}

	@Override
	public boolean stop() {
		this.scene.detachChild(this.levelSelector);
		this.scene.setOnSceneTouchListener(null);
		for (LevelItem levelItem : this.items) {
			this.scene.unregisterTouchArea(levelItem);
			levelItem.removeTouchListener(this);
		}
		this.camera.setHUD(null);
		this.camera.setChaseEntity(null);
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
			switch (event.getItemType()) {
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
	public void touchPerformed(final InputEvent event) {
		if (event.getSource() instanceof LevelItem) {
			LevelItem selectedItem = (LevelItem) event.getSource();
			Log.d(this.getClass().getName(), "Level selected " + selectedItem.getItemIndex());
			this.stop();
			this.firePuzzleSelected(this.puzzles.get(selectedItem.getItemIndex()));
		}

	}

}
