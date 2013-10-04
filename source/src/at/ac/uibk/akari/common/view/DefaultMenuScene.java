package at.ac.uibk.akari.common.view;

import java.util.List;

import org.andengine.engine.camera.Camera;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import at.ac.uibk.akari.utils.BackgroundLoader;
import at.ac.uibk.akari.utils.BackgroundLoader.BackgroundType;
import at.ac.uibk.akari.utils.FontLoader.FontType;

public class DefaultMenuScene extends AbstractMenuScene {

	public DefaultMenuScene(final Camera camera, final VertexBufferObjectManager vertexBufferObjectManager, final List<MenuItem> itemTypes) {
		super(camera, vertexBufferObjectManager, itemTypes);
	}

	public DefaultMenuScene(final Camera camera, final VertexBufferObjectManager vertexBufferObjectManager) {
		super(camera, vertexBufferObjectManager);
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
