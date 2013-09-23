package at.ac.uibk.akari;

import java.io.File;
import java.util.List;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.util.FPSLogger;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import android.util.Log;
import android.view.Display;
import android.widget.Toast;
import at.ac.uibk.akari.controller.GameController;
import at.ac.uibk.akari.core.Puzzle;
import at.ac.uibk.akari.utils.BackgroundLoader;
import at.ac.uibk.akari.utils.FontLoader;
import at.ac.uibk.akari.utils.PuzzleLoader;
import at.ac.uibk.akari.utils.PuzzleManager;
import at.ac.uibk.akari.utils.SceneManager;
import at.ac.uibk.akari.utils.ScoreManager;
import at.ac.uibk.akari.utils.TextureLoader;

public class MainActivity extends SimpleBaseGameActivity {

	private static String PUZZLE_SYNC_URL = "http://helli.ath.cx/akari/";

	private static boolean PUZZLE_SYNC = true;
	private static boolean PUZZLE_EXTERNAL = false;

	private static final String PUZZLES_DIR_EXTERNAL = "puzzles";
	private static final String PUZZLES_DIR_LOCAL = "puzzles";

	private static SimpleBaseGameActivity staticActivity;

	private static int CAMERA_WIDTH = 800;
	private static int CAMERA_HEIGHT = 480;

	private static int SCREEN_WIDTH;
	private static int SCREEN_HEIGHT;

	private ZoomCamera gameCamera;
	private Scene gameScene;

	private List<Puzzle> puzzles;

	// @Override
	// public Engine onCreateEngine(final EngineOptions pEngineOptions) {
	// return new LimitedFPSEngine(pEngineOptions, 60);
	// }

	@Override
	public EngineOptions onCreateEngineOptions() {
		MainActivity.staticActivity = this;
		Log.d(this.getClass().getName(), "Called create engine-options");

		Display display = this.getWindowManager().getDefaultDisplay();

		Log.i(this.getClass().getName(), "Got display with resolution " + display.getWidth() + "x" + display.getHeight());

		// determine the right landscape resolution

		if (display.getWidth() > display.getHeight()) {
			MainActivity.SCREEN_WIDTH = display.getWidth();
			MainActivity.SCREEN_HEIGHT = display.getHeight();
		} else {
			MainActivity.SCREEN_WIDTH = display.getHeight();
			MainActivity.SCREEN_HEIGHT = display.getWidth();
		}

		float screenAspect = (float) MainActivity.SCREEN_WIDTH / (float) MainActivity.SCREEN_HEIGHT;
		MainActivity.CAMERA_WIDTH = (int) (MainActivity.CAMERA_HEIGHT * screenAspect);
		Log.i(this.getClass().getName(), "Got screen aspect of " + screenAspect);

		// this.gameCamera = new Camera(0, 0, MainActivity.SCREEN_WIDTH,
		// MainActivity.SCREEN_HEIGHT);
		this.gameCamera = new ZoomCamera(0, 0, MainActivity.CAMERA_WIDTH, MainActivity.CAMERA_HEIGHT);

		Log.i(this.getClass().getName(), "Got screen resolution " + MainActivity.SCREEN_WIDTH + "x" + MainActivity.SCREEN_HEIGHT);
		Log.i(this.getClass().getName(), "Got camera resolution " + MainActivity.CAMERA_WIDTH + "x" + MainActivity.CAMERA_HEIGHT);

		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, new RatioResolutionPolicy(MainActivity.SCREEN_WIDTH, MainActivity.SCREEN_HEIGHT), this.gameCamera);
		engineOptions.getTouchOptions().setNeedsMultiTouch(true);

		return engineOptions;
	}

	@Override
	protected void onCreateResources() {
		Log.d(this.getClass().getName(), "Called create resources");

		Log.d(this.getClass().getName(), "Initializing puzzle-manager");
		PuzzleManager.getInstance().init(MainActivity.PUZZLES_DIR_LOCAL, this.getAssets());

		Log.d(this.getClass().getName(), "Initializing scene-manager");
		SceneManager.getInstance().init(this, this.gameCamera);

		// initialize score-manager
		Log.d(this.getClass().getName(), "Initializing score-manager");
		ScoreManager.getInstance().init(this);

		// load textures
		Log.d(this.getClass().getName(), "Loading textures");
		TextureLoader.getInstance().init(this.getTextureManager(), this);

		// load backgrounds
		Log.d(this.getClass().getName(), "Loading background-textures");
		BackgroundLoader.getInstance().init(this.getTextureManager(), this.getAssets(), this.getVertexBufferObjectManager(), this.gameCamera.getWidth(), this.gameCamera.getHeight());

		// load fonts
		Log.d(this.getClass().getName(), "Loading fonts");
		FontLoader.getInstance().init(this.getTextureManager(), this.getFontManager(), this.getAssets());

		// load levels from assets
		Log.d(this.getClass().getName(), "Loading asset levels");
		try {
			this.puzzles = PuzzleLoader.loadPuzzles(this.getAssets(), MainActivity.PUZZLES_DIR_LOCAL);
			Log.i(this.getClass().getName(), "Loaded " + this.puzzles.size() + " levels...");

		} catch (Exception e) {
			e.printStackTrace();
		}

		// synchronize levels from external source if enabled
		if (MainActivity.PUZZLE_EXTERNAL && MainActivity.PUZZLE_SYNC) {
			Log.d(this.getClass().getName(), "Synchronizing levels using url '" + MainActivity.PUZZLE_SYNC_URL + "'");
			try {
				int syncedPuzzles = PuzzleLoader.synchronizePuzzleList(MainActivity.PUZZLE_SYNC_URL, this.getFilesDir().getAbsolutePath() + File.separator + MainActivity.PUZZLES_DIR_EXTERNAL);
				Log.i(this.getClass().getName(), "Synchronized " + syncedPuzzles + " puzzles");
				MainActivity.showToast("Synchronized " + syncedPuzzles + " puzzles", Toast.LENGTH_SHORT);

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Log.d(this.getClass().getName(), "Synchronizing levels is disabled");
		}

		// load local levels from external source if enabled
		if (MainActivity.PUZZLE_EXTERNAL) {
			Log.d(this.getClass().getName(), "Loading levels from memory");
			try {
				this.puzzles = PuzzleLoader.loadPuzzles(this.getFilesDir().getAbsolutePath() + File.separator + MainActivity.PUZZLES_DIR_EXTERNAL);
				Log.i(this.getClass().getName(), "Loaded " + this.puzzles.size() + " levels...");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	protected Scene onCreateScene() {
		Log.d(this.getClass().getName(), "Called create scene");
		this.mEngine.registerUpdateHandler(new FPSLogger());
		this.gameScene = new Scene();
		this.gameScene.setOnAreaTouchTraversalFrontToBack();
		this.gameScene.setTouchAreaBindingOnActionDownEnabled(true);
		this.gameScene.setBackground(new Background(0.2f, 0.6f, 0.8f, 0.1f));

		GameController gameController = new GameController(this.gameCamera, this.gameScene, this.getVertexBufferObjectManager());
		gameController.start();
		return this.gameScene;

	}

	@Override
	public Engine getEngine() {
		return this.mEngine;
	}

	@Override
	public void onBackPressed() {
		SceneManager.getInstance().onBackPressed();
	}

	public static void showToast(final String text, final int length) {
		MainActivity.staticActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(MainActivity.staticActivity, text, length).show();
			}
		});
	}

	public static void quit() {
		Log.d(MainActivity.class.getName(), "Quitting activity");
		MainActivity.staticActivity.finish();
	}

	public static void registerUpdateHandler(final IUpdateHandler updateHandler) {
		MainActivity.staticActivity.getEngine().registerUpdateHandler(updateHandler);
	}

	public static void unregisterUpdateHandler(final IUpdateHandler updateHandler) {
		MainActivity.staticActivity.getEngine().unregisterUpdateHandler(updateHandler);
	}
}
