package at.ac.uibk.akari;

import java.io.File;
import java.util.List;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.level.LevelLoader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import android.util.Log;
import android.view.Display;
import android.widget.Toast;
import at.ac.uibk.akari.controller.GameController;
import at.ac.uibk.akari.core.GameFieldModel;
import at.ac.uibk.akari.listener.GameListener;
import at.ac.uibk.akari.testsolver.Akari;
import at.ac.uibk.akari.utils.PuzzleLoader;
import at.ac.uibk.akari.utils.TextureLoader;

public class MainActivity extends SimpleBaseGameActivity implements GameListener, IOnSceneTouchListener, IScrollDetectorListener, IPinchZoomDetectorListener {

	private static int SCREEN_WIDTH = 800;
	private static int SCREEN_HEIGHT = 480;

	private static String puzzlesDir = "puzzles";

	private ZoomCamera gameCamera;
	private Scene gameScene;

	private List<GameFieldModel> puzzles;

	private float mPinchZoomStartedCameraZoomFactor;

	private PinchZoomDetector mPinchZoomDetector;
	private SurfaceScrollDetector mScrollDetector;

	private GameController gameController;

	private int currentPuzzle;

	@Override
	public EngineOptions onCreateEngineOptions() {
		Log.d(this.getClass().getName(), "Called create engine-options");

		Display display = this.getWindowManager().getDefaultDisplay();

		Log.i(this.getClass().getName(), "Got display with resolution " + display.getWidth() + "x" + display.getHeight());

		// determine the right landscape resolution
		int realScreenWidth = 0;
		int realScreenHeight = 0;

		if (display.getWidth() > display.getHeight()) {
			realScreenWidth = display.getWidth();
			realScreenHeight = display.getHeight();
		} else {
			realScreenWidth = display.getHeight();
			realScreenHeight = display.getWidth();
		}

		float screenAspect = (float) realScreenWidth / (float) realScreenHeight;
		MainActivity.SCREEN_WIDTH = (int) (MainActivity.SCREEN_HEIGHT * screenAspect);
		Log.i(this.getClass().getName(), "Got screen aspect of " + screenAspect);

		// this.gameCamera = new Camera(0, 0, MainActivity.SCREEN_WIDTH,
		// MainActivity.SCREEN_HEIGHT);
		this.gameCamera = new ZoomCamera(0, 0, MainActivity.SCREEN_WIDTH, MainActivity.SCREEN_HEIGHT);

		Log.i(this.getClass().getName(), "Got camera resolution " + MainActivity.SCREEN_WIDTH + "x" + MainActivity.SCREEN_HEIGHT);

		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, new RatioResolutionPolicy(realScreenWidth, realScreenHeight), this.gameCamera);
		engineOptions.getTouchOptions().setNeedsMultiTouch(true);

		return engineOptions;
	}

	@Override
	protected void onCreateResources() {
		Log.d(this.getClass().getName(), "Called create resources");
		TextureLoader.getInstance().init(this.getTextureManager(), this);

		try {
			int syncedPuzzles = PuzzleLoader.synchronizePuzzleList("http://helama.us.to/akari/", this.getFilesDir().getAbsolutePath() + File.separator + MainActivity.puzzlesDir);
			Log.i(this.getClass().getName(), "Synchronized " + syncedPuzzles + " puzzles");

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

		this.mScrollDetector = new SurfaceScrollDetector(this);
		this.mPinchZoomDetector = new PinchZoomDetector(this);

		this.gameScene.setOnSceneTouchListener(this);


		this.currentPuzzle = 0;
		this.gameController = new GameController(this.gameScene, this.getVertexBufferObjectManager());
		this.gameController.addGameListener(this);
		try {
			this.gameController.setPuzzle(this.puzzles.get(this.currentPuzzle++));
			this.gameController.start();
		} catch (ContradictionException e) {
			e.printStackTrace();
		}

		return this.gameScene;

	}

	public void testSolver() {
		try {
			Log.d(this.getClass().getName(), "Starting  solving");
			long timeStart = System.currentTimeMillis();

			new Akari();
			long timeStop = System.currentTimeMillis();
			float secondsNeeded = ((float) (timeStop - timeStart)) / 1000;
			Log.d(this.getClass().getName(), "Finished solving puzzle  in " + secondsNeeded + " seconds ...");
		} catch (ContradictionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean onSceneTouchEvent(final Scene scene, final TouchEvent pSceneTouchEvent) {
		Log.d(this.getClass().getName(), "MainActivity.onSceneTouchEvent()");
		if (pSceneTouchEvent.getMotionEvent().getPointerCount() == 2) {
			this.mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);
			this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
			return true;
		}
		return false;
	}

	@Override
	public void onPinchZoom(final PinchZoomDetector arg0, final TouchEvent arg1, final float pZoomFactor) {
		Log.d(this.getClass().getName(), "MainActivity.onPinchZoom()");
		this.gameCamera.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);

	}

	@Override
	public void onPinchZoomFinished(final PinchZoomDetector arg0, final TouchEvent arg1, final float pZoomFactor) {
		Log.d(this.getClass().getName(), "MainActivity.onPinchZoomFinished()");
		this.gameCamera.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);

	}

	@Override
	public void onPinchZoomStarted(final PinchZoomDetector arg0, final TouchEvent arg1) {
		Log.d(this.getClass().getName(), "MainActivity.onPinchZoomStarted()");
		this.mPinchZoomStartedCameraZoomFactor = this.gameCamera.getZoomFactor();

	}

	@Override
	public void onScroll(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
		Log.d(this.getClass().getName(), "MainActivity.onScroll()");
		final float zoomFactor = this.gameCamera.getZoomFactor();
		this.gameCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
	}

	@Override
	public void onScrollFinished(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
		Log.d(this.getClass().getName(), "MainActivity.onScrollFinished()");
		final float zoomFactor = this.gameCamera.getZoomFactor();
		this.gameCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
	}

	@Override
	public void onScrollStarted(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
		Log.d(this.getClass().getName(), "MainActivity.onScrollStarted()");
		final float zoomFactor = this.gameCamera.getZoomFactor();
		this.gameCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
	}

	@Override
	public void puzzleSolved(final GameController source, final long timeMs) {
		if (source.equals(this.gameController)) {
			this.gameController.stop();
			try {
				this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(MainActivity.this, "Solved level", Toast.LENGTH_LONG).show();
					}
				});
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				this.gameController.setPuzzle(this.puzzles.get(this.currentPuzzle++));
				this.gameController.start();
			} catch (ContradictionException e) {
				e.printStackTrace();
			}
		}
	}
}
