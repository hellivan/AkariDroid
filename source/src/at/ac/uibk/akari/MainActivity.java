package at.ac.uibk.akari;

import java.util.List;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.entity.util.FPSLogger;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.font.IFont;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.HorizontalAlign;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import android.graphics.Typeface;
import android.util.Log;
import android.view.Display;
import at.ac.uibk.akari.controller.GameFieldController;
import at.ac.uibk.akari.core.GameFieldModel;
import at.ac.uibk.akari.core.GameFieldModel.CellState;
import at.ac.uibk.akari.solver.AkariSolver;
import at.ac.uibk.akari.testsolver.Akari;
import at.ac.uibk.akari.utils.LevelLoader;
import at.ac.uibk.akari.utils.TextureLoader;
import at.ac.uibk.akari.view.GameField;
import at.ac.uibk.akari.view.Lamp;
import browser.Browser;

public class MainActivity extends SimpleBaseGameActivity {

	private static int SCREEN_WIDTH = 800;
	private static int SCREEN_HEIGHT = 480;

	private Camera gameCamera;
	private Scene gameScene;

	private List<GameFieldModel> levels;
	private AkariSolver solver;

	@Override
	public EngineOptions onCreateEngineOptions() {
		Log.d(this.getClass().toString(), "Called create engine-options");

		Display display = this.getWindowManager().getDefaultDisplay();

		Log.i(this.getClass().toString(), "Got display with resolution " + display.getWidth() + "x" + display.getHeight());

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
		Log.i(this.getClass().toString(), "Got screen aspect of " + screenAspect);

		this.gameCamera = new Camera(0, 0, MainActivity.SCREEN_WIDTH, MainActivity.SCREEN_HEIGHT);
		Log.i(this.getClass().toString(), "Got camera resolution " + MainActivity.SCREEN_WIDTH + "x" + MainActivity.SCREEN_HEIGHT);

		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, new RatioResolutionPolicy(realScreenWidth, realScreenHeight), this.gameCamera);
		engineOptions.getTouchOptions().setNeedsMultiTouch(true);

		return engineOptions;
	}

	@Override
	protected void onCreateResources() {
		Log.d(this.getClass().toString(), "Called create resources");
		TextureLoader.getInstance().init(this.getTextureManager(), this);

		try {
			Browser borwser = new Browser(10000, 10000);

			this.levels = LevelLoader.readLevelsFromFile(this.getFilesDir().getAbsolutePath());
			if (this.levels.size() < 31) {
				this.levels = LevelLoader.fetchLevels(borwser);
				LevelLoader.writeToFiles(this.levels, this.getFilesDir().getAbsolutePath());
			}

			Log.i(this.getClass().toString(), "Loaded " + this.levels.size() + " levels...");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected Scene onCreateScene() {
		Log.d(this.getClass().toString(), "Called create scene");
		this.mEngine.registerUpdateHandler(new FPSLogger());
		this.gameScene = new Scene();
		this.gameScene.setOnAreaTouchTraversalFrontToBack();
		this.gameScene.setTouchAreaBindingOnActionDownEnabled(true);
		this.gameScene.setBackground(new Background(0.2f, 0.6f, 0.8f, 0.1f));

		{
			IFont pFont = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 64);
			pFont.load();
			Text helloWorld = new Text(0, 0, pFont, "Hello Akari!", new TextOptions(HorizontalAlign.CENTER), this.getVertexBufferObjectManager());
			helloWorld.setPosition((this.gameCamera.getWidth() - helloWorld.getLineWidthMaximum()) / 2, (this.gameCamera.getHeight() - pFont.getLineHeight()) / 2);
			helloWorld.setRotation(45);

			this.gameScene.attachChild(helloWorld);
		}
		{

			GameFieldModel gameFieldModel = this.levels.get(5);
			try {
				System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
				solver = new AkariSolver(gameFieldModel, 10000);
				System.out.println(""+solver.isSatisfiableWithCurrentLamps());
				solver.setSolutionToModel();
				System.out.println(""+solver.isSatisfiableWithCurrentLamps());
				

			} catch (ContradictionException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
			}

			GameField gameField = new GameField(150, 20, gameFieldModel, this.getVertexBufferObjectManager());

			GameFieldController controller = new GameFieldController(gameField,solver);
			controller.start();

			this.gameScene.attachChild(gameField);
			this.gameScene.registerTouchArea(gameField);
		}
		{
			Lamp lamp = new Lamp(50, 50, 50, 50, this.getVertexBufferObjectManager());
			// lamp.animate(120);
			this.gameScene.attachChild(lamp);
			this.gameScene.registerTouchArea(lamp);
		}

		return this.gameScene;

	}

	public GameFieldModel generateLevel() {
		GameFieldModel model = new GameFieldModel(10, 10);
		model.setCellState(1, 1, CellState.BLOCK4);
		model.setCellState(4, 1, CellState.BARRIER);
		model.setCellState(8, 1, CellState.BLOCK2);
		model.setCellState(2, 2, CellState.BARRIER);
		model.setCellState(8, 2, CellState.BARRIER);
		model.setCellState(4, 3, CellState.BARRIER);
		model.setCellState(5, 3, CellState.BLOCK0);
		model.setCellState(4, 6, CellState.BLOCK1);
		model.setCellState(5, 6, CellState.BARRIER);
		model.setCellState(1, 7, CellState.BLOCK1);
		model.setCellState(7, 7, CellState.BLOCK0);
		model.setCellState(1, 8, CellState.BLOCK1);
		model.setCellState(5, 8, CellState.BLOCK1);
		model.setCellState(8, 8, CellState.BARRIER);
		return model;
	}

	public void testSolver() {
		try {
			Log.d(this.getClass().toString(), "Starting  solving");
			long timeStart = System.currentTimeMillis();

			new Akari();
			long timeStop = System.currentTimeMillis();
			float secondsNeeded = ((float) (timeStop - timeStart)) / 1000;
			Log.d(this.getClass().toString(), "Finished solving puzzle  in " + secondsNeeded + " seconds ...");
		} catch (ContradictionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}

	}

}
