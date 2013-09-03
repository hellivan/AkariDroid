package at.ac.uibk.akari.puzzleSelector.controller;

import java.util.ArrayList;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.graphics.PointF;
import at.ac.uibk.akari.controller.AbstractController;
import at.ac.uibk.akari.puzzleSelector.view.LevelSelector;
import at.ac.uibk.akari.utils.BackgroundLoader;
import at.ac.uibk.akari.utils.BackgroundLoader.BackgroundType;
import at.ac.uibk.akari.view.Cell;
import at.ac.uibk.akari.view.Cell.State;

public class PuzzleSelectionController extends AbstractController implements IOnSceneTouchListener {

	private Scene scene;
	private Camera camera;
	private VertexBufferObjectManager vertexBufferObjectManager;
	private LevelSelector levelSelector;

	public PuzzleSelectionController(final Scene scene, final Camera camera, final VertexBufferObjectManager vertexBufferObjectManager) {
		this.scene = scene;
		this.camera = camera;
	}

	private void initScene() {
		this.scene.setBackground(BackgroundLoader.getInstance().getBackground(BackgroundType.GAME_FIELD_BACKGROUND));
		this.scene.setBackgroundEnabled(true);

		ArrayList<Sprite> items = new ArrayList<Sprite>();
		for (State state : State.values()) {
			Cell lamp = new Cell(new PointF(0, 0), 100, 100, this.vertexBufferObjectManager);
			lamp.setCellState(state);
			items.add(lamp);
		}
		this.levelSelector = new LevelSelector(items, 2, 2, this.camera);
		this.scene.attachChild(this.levelSelector);
	}

	@Override
	public boolean start() {
		this.initScene();
		this.scene.setOnSceneTouchListener(this);
		return true;
	}

	@Override
	public boolean stop() {
		this.scene.detachChild(this.levelSelector);
		this.scene.setOnSceneTouchListener(null);
		return true;
	}

	@Override
	public boolean onSceneTouchEvent(final Scene scene, final TouchEvent pSceneTouchEvent) {
		this.levelSelector.onTouchEvent(pSceneTouchEvent);
		return false;
	}

}
