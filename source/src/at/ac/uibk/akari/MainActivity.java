package at.ac.uibk.akari;

import java.io.File;
import java.util.List;

import org.andengine.engine.Engine;
import org.andengine.engine.LimitedFPSEngine;
import org.andengine.engine.camera.ZoomCamera;
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
import at.ac.uibk.akari.utils.TextureLoader;

public class MainActivity extends SimpleBaseGameActivity {

	private static final String PUZZLE_SYNC_URL = "http://helama.us.to/akari/";

	private static SimpleBaseGameActivity staticActivity;

	private static int CAMERA_WIDTH = 800;
	private static int CAMERA_HEIGHT = 480;

	private static int SCREEN_WIDTH;
	private static int SCREEN_HEIGHT;

	private static String puzzlesDir = "puzzles";

	private ZoomCamera gameCamera;
	private Scene gameScene;

	private List<Puzzle> puzzles;

	@Override
	public Engine onCreateEngine(final EngineOptions pEngineOptions) {
		return new LimitedFPSEngine(pEngineOptions, 30);
	}

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

		// load textures
		Log.d(this.getClass().getName(), "Loading textures");
		TextureLoader.getInstance().init(this.getTextureManager(), this);

		// load backgrounds
		Log.d(this.getClass().getName(), "Loading background-textures");
		BackgroundLoader.getInstance().init(this.getTextureManager(), this.getAssets(), this.getVertexBufferObjectManager(), this.gameCamera.getWidth(), this.gameCamera.getHeight());

		// load fonts
		Log.d(this.getClass().getName(), "Loading fonts");
		FontLoader.getInstance().init(this.getTextureManager(), this.getFontManager(), this.getAssets());

		// synchronize levels
		Log.d(this.getClass().getName(), "Synchronizing levels using url '" + MainActivity.PUZZLE_SYNC_URL + "'");
		try {
			int syncedPuzzles = PuzzleLoader.synchronizePuzzleList("http://helama.us.to/akari/", this.getFilesDir().getAbsolutePath() + File.separator + MainActivity.puzzlesDir);
			Log.i(this.getClass().getName(), "Synchronized " + syncedPuzzles + " puzzles");
			MainActivity.showToast("Synchronized " + syncedPuzzles + " puzzles", Toast.LENGTH_SHORT);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// load local levels
		Log.d(this.getClass().getName(), "Loading levels from memory");
		try {
			this.puzzles = PuzzleLoader.loadPuzzles(this.getFilesDir().getAbsolutePath() + File.separator + MainActivity.puzzlesDir);
			Log.i(this.getClass().getName(), "Loaded " + this.puzzles.size() + " levels...");

		} catch (Exception e) {
			e.printStackTrace();
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

		GameController gameController = new GameController(this.gameCamera, this.gameScene, this.getVertexBufferObjectManager(), this.puzzles);
		gameController.start();
		return this.gameScene;

	}

	public static void showToast(final String text, final int length) {
		MainActivity.staticActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(MainActivity.staticActivity, text, length).show();
			}
		});
	}

	public static void setCurrentScene(final Scene scene) {
		MainActivity.staticActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				MainActivity.staticActivity.getEngine().setScene(scene);
			}
		});
	}

	public static void quit() {
		Log.d(MainActivity.class.getName(), "Quitting activity");
		MainActivity.staticActivity.finish();
	}

}
