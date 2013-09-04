package at.ac.uibk.akari.puzzleSelector.controller;

import java.util.List;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.util.Log;
import at.ac.uibk.akari.controller.AbstractController;
import at.ac.uibk.akari.core.Puzzle;
import at.ac.uibk.akari.listener.MenuItemSeletedEvent;
import at.ac.uibk.akari.listener.MenuListener;
import at.ac.uibk.akari.puzzleSelector.listener.PuzzleSelectionEvent;
import at.ac.uibk.akari.puzzleSelector.listener.PuzzleSelectionListener;
import at.ac.uibk.akari.puzzleSelector.listener.ValueChangedEvent;
import at.ac.uibk.akari.puzzleSelector.listener.ValueChangedListener;
import at.ac.uibk.akari.puzzleSelector.view.LevelSelector;
import at.ac.uibk.akari.puzzleSelector.view.PuzzleSelectorHUD;
import at.ac.uibk.akari.utils.ListenerList;

public class PuzzleSelectionController extends AbstractController implements IOnSceneTouchListener, MenuListener, PuzzleSelectionListener, ValueChangedListener<Integer> {

	private Scene scene;
	private Camera camera;
	private VertexBufferObjectManager vertexBufferObjectManager;
	private LevelSelector levelSelector;
	private PuzzleSelectorHUD hud;

	protected ListenerList listeners;

	public PuzzleSelectionController(final Scene scene, final Camera camera, final VertexBufferObjectManager vertexBufferObjectManager) {
		this.listeners = new ListenerList();
		this.scene = scene;
		this.camera = camera;
		this.init();
	}

	private void init() {
		this.levelSelector = new LevelSelector(2, 2, this.camera, this.vertexBufferObjectManager);
		this.scene.attachChild(this.levelSelector);

		this.hud = new PuzzleSelectorHUD((int) this.camera.getWidth(), this.vertexBufferObjectManager);

	}

	public void setPuzzles(final List<Puzzle> puzzles) {
		this.levelSelector.setLevels(puzzles);
	}

	@Override
	public boolean start() {
		this.scene.setOnSceneTouchListener(this);
		this.levelSelector.addPuzzleSelectionListener(this);
		this.levelSelector.addValueChangedListener(this);
		this.hud.addPuzzleControlListener(this);
		this.camera.setHUD(this.hud);
		this.levelSelector.start();
		return true;
	}

	@Override
	public boolean stop() {
		this.scene.setOnSceneTouchListener(null);
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
		}
	}

}
