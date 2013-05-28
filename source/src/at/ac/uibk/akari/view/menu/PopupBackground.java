package at.ac.uibk.akari.view.menu;

import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.graphics.PointF;
import at.ac.uibk.akari.utils.TextureLoader;
import at.ac.uibk.akari.utils.TextureLoader.TextureType;

public class PopupBackground extends Sprite {

	public PopupBackground(final PointF location, final int width, final int height, final VertexBufferObjectManager vertexBufferObjectManager) {
		this(location.x, location.y, width, height, vertexBufferObjectManager);
	}

	public PopupBackground(final float posX, final float posY, final int width, final int height, final VertexBufferObjectManager vertexBufferObjectManager) {
		super(posX, posY, width, height, TextureLoader.getInstance().getTexture(TextureType.POPUP_BACKGROUND, 0, 0), vertexBufferObjectManager);
	}

}
