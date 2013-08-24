package at.ac.uibk.akari.puzzleSelector.controller;

import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.input.touch.TouchEvent;

import at.ac.uibk.akari.controller.AbstractController;
import at.ac.uibk.akari.puzzleSelector.view.LevelSelector;

public class PuzzleSelectionController extends AbstractController implements IOnSceneTouchListener {

	private LevelSelector levelSelector;

	public PuzzleSelectionController(final LevelSelector levelSelector) {
		this.levelSelector = levelSelector;
	}

	@Override
	public boolean start() {
		return true;
	}

	@Override
	public boolean stop() {
		return true;
	}

	@Override
	public boolean onSceneTouchEvent(final Scene arg0, final TouchEvent pSceneTouchEvent) {
		this.levelSelector.onTouchEvent(pSceneTouchEvent);
		return false;
	}

}
