package at.ac.uibk.akari.view.menu;

import java.util.List;

import org.andengine.engine.camera.Camera;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import at.ac.uibk.akari.common.menu.ItemType;
import at.ac.uibk.akari.utils.FontLoader.FontType;

public class PopupMenuScene extends AbstractMenuScene {

	public PopupMenuScene(final Camera camera, final VertexBufferObjectManager vertexBufferObjectManager, final List<ItemType> itemTypes) {
		super(camera, vertexBufferObjectManager, itemTypes);
	}

	@Override
	protected void setSceneOptions() {
		int bgWidth = 500;
		int bgHeight = 300;
		PopupBackground backGround = new PopupBackground(this.getCamera().getCenterX() - bgWidth / 2, this.getCamera().getCenterY() - bgHeight / 2, bgWidth, bgHeight, this.getVertexBufferObjectManager());
		this.attachChild(backGround);
		this.setBackgroundEnabled(false);
	}

	@Override
	protected FontType getItemsFontType() {
		return FontType.DROID_48_BLACK;
	}

}
