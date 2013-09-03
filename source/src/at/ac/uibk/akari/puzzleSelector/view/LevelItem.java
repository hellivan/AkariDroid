package at.ac.uibk.akari.puzzleSelector.view;

import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.graphics.PointF;
import at.ac.uibk.akari.listener.TouchListener;
import at.ac.uibk.akari.utils.ListenerList;
import at.ac.uibk.akari.view.Cell;

public class LevelItem extends Cell {

	protected ListenerList listeners;
	private boolean enabled;

	private int deltaX = 7;
	private int deltaY = 7;
	private PointF lastDownPoint;
	private int itemIndex;

	public LevelItem(final PointF location, final int width, final int height, final VertexBufferObjectManager vertexBufferObjectManager, final int itemIndex) {
		this(location.x, location.y, width, height, vertexBufferObjectManager, itemIndex);
	}

	public LevelItem(final float posX, final float posY, final int width, final int height, final VertexBufferObjectManager vertexBufferObjectManager, final int itemIndex) {
		super(posX, posY, width, height, vertexBufferObjectManager);
		this.listeners = new ListenerList();
		this.setEnabled(true);
		this.itemIndex = itemIndex;
	}

	private boolean wasMoved(final PointF newPoint) {
		if (this.lastDownPoint == null) {
			return true;
		}
		if (((this.lastDownPoint.x + this.deltaX) < newPoint.x) || ((this.lastDownPoint.x - this.deltaX) > newPoint.x)) {
			return true;
		}
		if (((this.lastDownPoint.y + this.deltaY) < newPoint.y) || ((this.lastDownPoint.y - this.deltaY) > newPoint.y)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		if (this.isEnabled()) {
			if (pSceneTouchEvent.isActionUp()) {
				this.setScale(1f);
				if (!this.wasMoved(new PointF(pSceneTouchEvent.getX(), pSceneTouchEvent.getY()))) {
					this.fireTouched();
					// return true because event was handeled (avoid iterating
					// over other touchares that are possibly removed after this
					// levelSelection)
					return true;
				}
			} else if (pSceneTouchEvent.isActionDown()) {
				this.lastDownPoint = new PointF(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
				this.setScale(1.1f);
			} else if (pSceneTouchEvent.isActionOutside()) {
				this.lastDownPoint = null;
				this.setScale(1f);
			} else if (pSceneTouchEvent.isActionMove()) {
				if (this.wasMoved(new PointF(pSceneTouchEvent.getX(), pSceneTouchEvent.getY()))) {
					this.lastDownPoint = null;
					this.setScale(1f);
				}
			}
		}
		return false;
	}

	public void addTouchListener(final TouchListener listener) {
		this.listeners.addListener(TouchListener.class, listener);
	}

	public void removeTouchListener(final TouchListener listener) {
		this.listeners.removeListener(TouchListener.class, listener);
	}

	protected void fireTouched() {
		at.ac.uibk.akari.listener.InputEvent event = new at.ac.uibk.akari.listener.InputEvent(this);
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

	public int getItemIndex() {
		return this.itemIndex;
	}

}
