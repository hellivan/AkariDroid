package at.ac.uibk.akari.utils;

import org.andengine.entity.scene.Scene;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import at.ac.uibk.akari.controller.AbstractController;

public class SceneManager {

	private static SceneManager sceneManager;

	private Scene currentScene;
	private AbstractController currentController;

	private SimpleBaseGameActivity baseGameActivity;

	public static SceneManager getInstance() {
		if (SceneManager.sceneManager == null) {
			SceneManager.sceneManager = new SceneManager();
		}
		return SceneManager.sceneManager;
	}

	public void init(final SimpleBaseGameActivity baseGameActivity) {
		this.baseGameActivity = baseGameActivity;
	}

	public void setCurrentScene(final AbstractController controller, final Scene scene) {
		this.currentController = controller;
		this.currentScene = scene;
		this.baseGameActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				baseGameActivity.getEngine().setScene(scene);
			}
		});
	}

	public AbstractController getCurrentController() {
		return currentController;
	}

	public Scene getCurrentScene() {
		return currentScene;
	}

}
