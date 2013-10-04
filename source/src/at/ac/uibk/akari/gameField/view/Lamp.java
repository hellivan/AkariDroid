package at.ac.uibk.akari.gameField.view;

import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.graphics.PointF;
import at.ac.uibk.akari.utils.TextureLoader;
import at.ac.uibk.akari.utils.TextureLoader.TextureType;

public class Lamp extends AnimatedSprite {

	private boolean grabbed;
	private static TextureType textureType = TextureType.LAMP;

	public Lamp(final PointF location, final int width, final int height, final VertexBufferObjectManager vertexBufferObjectManager) {
		this(location.x, location.y, width, height, vertexBufferObjectManager);
	}

	public Lamp(final float posX, final float posY, final int width, final int height, final VertexBufferObjectManager vertexBufferObjectManager) {
		super(posX, posY, width, height, TextureLoader.getInstance().getTextureRegion(Lamp.textureType), vertexBufferObjectManager);
		this.grabbed = false;
		this.setCurrentTileIndex(Lamp.textureType.getTileNumber(2, 0));
	}

	@Override
	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		switch (pSceneTouchEvent.getAction()) {
		case TouchEvent.ACTION_DOWN:
			this.setScale(1.25f);
			this.grabbed = true;
			break;
		case TouchEvent.ACTION_MOVE:
			if (this.grabbed) {
				this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
			}
			break;
		case TouchEvent.ACTION_UP:
			if (this.grabbed) {
				this.grabbed = false;
				this.setScale(1.0f);
			}
			break;
		}
		return true;
	}
}
