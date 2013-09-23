package at.ac.uibk.akari.utils;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.scene.Scene;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import at.ac.uibk.akari.controller.AbstractController;

public class SceneManager {

	private static SceneManager sceneManager;

	private HUD currentHUD;
	private Scene currentScene;
	private AbstractController currentController;

	private SimpleBaseGameActivity baseGameActivity;

	private ZoomCamera camera;

	public static SceneManager getInstance() {
		if (SceneManager.sceneManager == null) {
			SceneManager.sceneManager = new SceneManager();
		}
		return SceneManager.sceneManager;
	}

	private SceneManager() {

	}

	public void init(final SimpleBaseGameActivity baseGameActivity, final ZoomCamera camera) {
		this.baseGameActivity = baseGameActivity;
		this.camera = camera;
	}

	public void setCurrentScene(final AbstractController controller, final Scene scene) {
		this.setCurrentScene(controller, scene, null);
	}

	public void setCurrentScene(final AbstractController controller, final Scene scene, final HUD hud) {
		this.currentController = controller;
		this.currentScene = scene;
		this.baseGameActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				SceneManager.this.baseGameActivity.getEngine().setScene(scene);
				SceneManager.this.camera.setHUD(hud);
			}
		});
	}

	public AbstractController getCurrentController() {
		return this.currentController;
	}

	public Scene getCurrentScene() {
		return this.currentScene;
	}

	public HUD getCurrentHUD() {
		return this.currentHUD;
	}
}
