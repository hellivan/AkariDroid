package at.ac.uibk.akari.utils;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.scene.Scene;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import at.ac.uibk.akari.MainActivity;
import at.ac.uibk.akari.common.controller.AbstractController;

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
		this.setCurrentScene(controller, scene, null, true);
	}

	public void setCurrentScene(final AbstractController controller, final Scene scene, final boolean resetCamera) {
		this.setCurrentScene(controller, scene, null, resetCamera);
	}

	public void setCurrentScene(final AbstractController controller, final Scene scene, final HUD hud) {
		this.setCurrentScene(controller, scene, hud, true);
	}

	public void setCurrentScene(final AbstractController controller, final Scene scene, final HUD hud, final boolean resetCamera) {
		this.currentController = controller;
		this.currentScene = scene;
		this.baseGameActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (SceneManager.this.baseGameActivity.getEngine() == null) {
					MainActivity.restartGame();
				} else {
					SceneManager.this.baseGameActivity.getEngine().setScene(scene);
					SceneManager.this.camera.setHUD(hud);
					if (resetCamera) {
						SceneManager.this.camera.setZoomFactor(1);
						SceneManager.this.camera.setCenter(SceneManager.this.camera.getWidth() / 2, SceneManager.this.camera.getHeight() / 2);
					}
				}
			}
		});
	}

	public ZoomCamera getCurrentCamera() {
		return this.camera;
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

	public void onBackPressed() {
		if (this.currentController != null) {
			this.currentController.onBackKeyPressed();
		}
	}

	public void refreshCurrenScene() {
		this.setCurrentScene(this.currentController, this.currentScene, this.currentHUD);
	}
}
