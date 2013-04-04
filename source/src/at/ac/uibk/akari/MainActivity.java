package at.ac.uibk.akari;

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

import android.graphics.Point;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Display;

public class MainActivity extends SimpleBaseGameActivity {

	private Camera gameCamera;
	private Scene gameScene;
	private static final String LOG_TAG = "hela";

	@Override
	public EngineOptions onCreateEngineOptions() {
		Log.i(MainActivity.LOG_TAG, "Called create engine-options");

		Display display = getWindowManager().getDefaultDisplay();
		Point screenSize = new Point();
		display.getSize(screenSize);

		this.gameCamera = new Camera(0, 0, screenSize.x, screenSize.y);
		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, new RatioResolutionPolicy(screenSize.x, screenSize.y), this.gameCamera);

	}

	@Override
	protected void onCreateResources() {
		Log.i(MainActivity.LOG_TAG, "Called create resources");

	}

	@Override
	protected Scene onCreateScene() {
		Log.i(MainActivity.LOG_TAG, "Called create scene");
		this.mEngine.registerUpdateHandler(new FPSLogger());
		this.gameScene = new Scene();

		IFont pFont = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 64);
		pFont.load();

		Text helloWorld = new Text(0, 0, pFont, "Hello Akari!", new TextOptions(HorizontalAlign.CENTER), this.getVertexBufferObjectManager());
		helloWorld.setPosition((this.gameCamera.getWidth() - helloWorld.getLineWidthMaximum()) / 2, (this.gameCamera.getHeight() - pFont.getLineHeight()) / 2);
		helloWorld.setRotation(45);

		this.gameScene.attachChild(helloWorld);

		this.gameScene.setBackground(new Background(0.2f, 0.6f, 0.8f, 0.1f));
		return gameScene;
	}

}
