package at.ac.uibk.akari.view.menu;

import java.util.List;

import org.andengine.engine.camera.Camera;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import at.ac.uibk.akari.common.menu.MenuItem;
import at.ac.uibk.akari.utils.BackgroundLoader;
import at.ac.uibk.akari.utils.BackgroundLoader.BackgroundType;
import at.ac.uibk.akari.utils.FontLoader.FontType;

public class MainMenuScene extends AbstractMenuScene {

	public MainMenuScene(final Camera camera, final VertexBufferObjectManager vertexBufferObjectManager, List<MenuItem> menuItems) {
		super(camera, vertexBufferObjectManager, menuItems);
	}

	@Override
	protected FontType getItemsFontType() {
		return FontType.DROID_48_BLACK;
	}
	
	@Override
	protected void setSceneOptions() {
		this.setBackground(BackgroundLoader.getInstance().getBackground(BackgroundType.GAME_FIELD_BACKGROUND));
		this.setBackgroundEnabled(true);
	}
}
