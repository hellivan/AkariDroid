package at.ac.uibk.akari.common.view;

import org.andengine.entity.sprite.TiledSprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.util.Log;
import at.ac.uibk.akari.common.listener.TouchListener;
import at.ac.uibk.akari.utils.ListenerList;

public class HUDToggleButton extends TiledSprite implements IHUDButton {

	private int tileIndexPressed;
	private int tileIndexReleased;

	private boolean pressed;

	protected ListenerList listeners;
	private boolean enabled;
	private DefaultMenuItem itemType;

	public HUDToggleButton(final float posX, final float posY, final int width, final int height, final VertexBufferObjectManager vertexBufferObjectManager, final ITiledTextureRegion texture, final int tileIndexPressed, final int tileIndexReleased, final DefaultMenuItem itemType) {
		super(posX, posY, width, height, texture, vertexBufferObjectManager);
		this.listeners = new ListenerList();
		this.setEnabled(true);
		this.itemType = itemType;

		this.tileIndexPressed = tileIndexPressed;
		this.tileIndexReleased = tileIndexReleased;
		this.pressed = false;
		this.setCurrentTileIndex(this.tileIndexReleased);
	}

	public void onButtonPressed() {
		this.pressed = !this.pressed;
		Log.d(this.getClass().getName(), "Setting tile-index: " + (this.pressed ? this.tileIndexPressed : this.tileIndexReleased));
		this.setCurrentTileIndex(this.pressed ? this.tileIndexPressed : this.tileIndexReleased);
	}

	@Override
	public DefaultMenuItem getItemType() {
		return this.itemType;
	}

	@Override
	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		if (!this.isEnabled()) {
			return false;
		}
		if (pSceneTouchEvent.isActionUp()) {
			// this.setScale(1f);
			this.onButtonPressed();
			this.fireTouched();
			return true;
		} else if (pSceneTouchEvent.isActionDown()) {
			// this.setScale(1.2f);
			return true;
		} else if (pSceneTouchEvent.isActionOutside()) {
			// this.setScale(1f);
			return true;
		} else if (pSceneTouchEvent.isActionMove()) {
			return true;
		}
		return true;
	}

	@Override
	public void addTouchListener(final TouchListener listener) {
		this.listeners.addListener(TouchListener.class, listener);
	}

	@Override
	public void removeTouchListener(final TouchListener listener) {
		this.listeners.removeListener(TouchListener.class, listener);
	}

	protected void fireTouched() {
		at.ac.uibk.akari.common.listener.InputEvent event = new at.ac.uibk.akari.common.listener.InputEvent(this);
		for (TouchListener listener : this.listeners.getListeners(TouchListener.class)) {
			listener.touchPerformed(event);
		}
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	@Override
	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isPressed() {
		return this.pressed;
	}
}
