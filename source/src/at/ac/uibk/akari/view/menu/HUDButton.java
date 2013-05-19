package at.ac.uibk.akari.view.menu;

import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.graphics.PointF;
import at.ac.uibk.akari.listener.TouchListener;
import at.ac.uibk.akari.utils.ListenerList;
import at.ac.uibk.akari.utils.TextureLoader;
import at.ac.uibk.akari.utils.TextureLoader.TextureType;

public class HUDButton extends Sprite {

	protected ListenerList listeners;

	public HUDButton(final PointF location, final int width, final int height, final VertexBufferObjectManager vertexBufferObjectManager) {
		this(location.x, location.y, width, height, vertexBufferObjectManager);
	}

	public HUDButton(final float posX, final float posY, final int width, final int height, final VertexBufferObjectManager vertexBufferObjectManager) {
		super(posX, posY, width, height, TextureLoader.getInstance().getTexture(TextureType.LAMP, 1, 0), vertexBufferObjectManager);
		this.listeners = new ListenerList();
	}

	@Override
	public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
		if (pSceneTouchEvent.isActionUp()) {
			this.fireTouched();
		}
		return false;
	}

	public void addTouchListener(TouchListener listener) {
		this.listeners.addListener(TouchListener.class, listener);
	}

	public void removeTouchListener(TouchListener listener) {
		this.listeners.removeListener(TouchListener.class, listener);
	}

	protected void fireTouched() {
		at.ac.uibk.akari.listener.InputEvent event = new at.ac.uibk.akari.listener.InputEvent(this);
		for (TouchListener listener : this.listeners.getListeners(TouchListener.class)) {
			listener.touchPerformed(event);
		}
	}
}
