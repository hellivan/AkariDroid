package at.ac.uibk.akari.common.view;

import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.graphics.PointF;
import at.ac.uibk.akari.common.listener.TouchListener;
import at.ac.uibk.akari.utils.ListenerList;

public class HUDButton extends Sprite {

	protected ListenerList listeners;
	private boolean enabled;
	private DefaultMenuItem itemType;

	public HUDButton(final PointF location, final int width, final int height, final VertexBufferObjectManager vertexBufferObjectManager, final ITextureRegion texture, final DefaultMenuItem itemType) {
		this(location.x, location.y, width, height, vertexBufferObjectManager, texture, itemType);
	}

	public HUDButton(final float posX, final float posY, final int width, final int height, final VertexBufferObjectManager vertexBufferObjectManager, final ITextureRegion texture, final DefaultMenuItem itemType) {
		super(posX, posY, width, height, texture, vertexBufferObjectManager);
		this.listeners = new ListenerList();
		this.setEnabled(true);
		this.itemType = itemType;
	}

	public DefaultMenuItem getItemType() {
		return this.itemType;
	}

	@Override
	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		if (!this.isEnabled()) {
			return false;
		}
		if (pSceneTouchEvent.isActionUp()) {
			this.setScale(1f);
			this.fireTouched();
			return true;
		} else if (pSceneTouchEvent.isActionDown()) {
			this.setScale(1.2f);
			return true;
		} else if (pSceneTouchEvent.isActionOutside()) {
			this.setScale(1f);
			return true;
		} else if (pSceneTouchEvent.isActionMove()) {
			return true;
		}
		return true;
	}

	public void addTouchListener(final TouchListener listener) {
		this.listeners.addListener(TouchListener.class, listener);
	}

	public void removeTouchListener(final TouchListener listener) {
		this.listeners.removeListener(TouchListener.class, listener);
	}

	protected void fireTouched() {
		at.ac.uibk.akari.common.listener.InputEvent event = new at.ac.uibk.akari.common.listener.InputEvent(this);
		for (TouchListener listener : this.listeners.getListeners(TouchListener.class)) {
			listener.touchPerformed(event);
		}
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}
}
