package at.ac.uibk.akari.puzzleSelector.view;

import java.util.ArrayList;
import java.util.List;

import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.HorizontalAlign;

import android.graphics.PointF;
import at.ac.uibk.akari.core.Puzzle;
import at.ac.uibk.akari.listener.TouchListener;
import at.ac.uibk.akari.utils.FontLoader;
import at.ac.uibk.akari.utils.FontLoader.FontType;
import at.ac.uibk.akari.utils.ListenerList;
import at.ac.uibk.akari.utils.ScoreManager;
import at.ac.uibk.akari.utils.StringUtils;
import at.ac.uibk.akari.utils.TextureLoader;
import at.ac.uibk.akari.utils.TextureLoader.TextureType;

public class LevelItem extends Sprite {

	protected ListenerList listeners;
	private boolean enabled;

	private int deltaX = 7;
	private int deltaY = 7;
	private PointF lastDownPoint;
	private Puzzle puzzle;

	private VertexBufferObjectManager vertexBufferObjectManager;

	public LevelItem(final VertexBufferObjectManager vertexBufferObjectManager, final Puzzle puzzle) {
		super(0, 0, 200, 150, TextureLoader.getInstance().getTextureRegion(TextureType.LEVEL_ITEM_BACKGROUND), vertexBufferObjectManager);
		this.vertexBufferObjectManager = vertexBufferObjectManager;
		this.listeners = new ListenerList();
		this.setEnabled(true);
		this.puzzle = puzzle;
		this.initItem();
	}

	private void initItem() {
		int startY = 22;
		int hGap = 2;

		List<Text> texts = new ArrayList<Text>();

		texts.add(new Text(0, 0, FontLoader.getInstance().getFont(FontType.DROID_30_WHITE), this.puzzle.getWidth() + "/" + this.puzzle.getHeight(), 10, new TextOptions(HorizontalAlign.CENTER), this.vertexBufferObjectManager));
		texts.add(new Text(0, 0, FontLoader.getInstance().getFont(FontType.DROID_30_WHITE), this.puzzle.getDifficulty().getDescription(), this.vertexBufferObjectManager));
		long puzzleScore = ScoreManager.getInstance().loadScore(this.puzzle);
		if (puzzleScore == ScoreManager.EMPTY_SCORE) {
			puzzleScore = 0;
		}
		texts.add(new Text(0, 0, FontLoader.getInstance().getFont(FontType.DROID_30_WHITE), StringUtils.convertSecondsToTimeString(puzzleScore), 10, new TextOptions(HorizontalAlign.CENTER), this.vertexBufferObjectManager));

		for (Text text : texts) {
			text.setX((this.getWidth() / 2) - (text.getWidth() / 2));
			text.setY(startY);
			this.attachChild(text);

			startY += text.getHeight();
			startY += hGap;

		}
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
					return false;
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

	public Puzzle getPuzzle() {
		return this.puzzle;
	}

}
