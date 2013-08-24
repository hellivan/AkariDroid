package at.ac.uibk.akari.puzzleSelector.view;

import java.util.ArrayList;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.graphics.PointF;
import at.ac.uibk.akari.puzzleSelector.controller.PuzzleSelectionController;
import at.ac.uibk.akari.view.Cell;
import at.ac.uibk.akari.view.Cell.State;

public class PuzzleSelectionScene extends Scene {

	private Camera camera;
	private LevelSelector levelSelector;

	public PuzzleSelectionScene(final Camera camera, final VertexBufferObjectManager vertexBufferObjectManager) {
		this.setBackground(new Background(100, 100, 100, 100));
		this.setBackgroundEnabled(true);
		this.camera = camera;
		ArrayList<Sprite> items = new ArrayList<Sprite>();
		for (State state : State.values()) {
			Cell lamp = new Cell(new PointF(0, 0), 100, 100, vertexBufferObjectManager);
			lamp.setCellState(state);
			items.add(lamp);
		}
		this.levelSelector = new LevelSelector(items, 2, 2, this.camera);

		this.setOnSceneTouchListener(new PuzzleSelectionController(this.levelSelector));
		this.attachChild(this.levelSelector);
	}
}
