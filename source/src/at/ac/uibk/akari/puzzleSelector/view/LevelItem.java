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
import at.ac.uibk.akari.utils.SaveGameManager;
import at.ac.uibk.akari.utils.StringUtils;
import at.ac.uibk.akari.utils.TextureLoader;
import at.ac.uibk.akari.utils.TextureLoader.TextureType;
import at.ac.uibk.akari.view.Insets;

public class LevelItem extends Sprite {

	protected ListenerList listeners;
	private boolean enabled;

	private int deltaX = 7;
	private int deltaY = 7;
	private PointF lastDownPoint;
	private Puzzle puzzle;

	private VertexBufferObjectManager vertexBufferObjectManager;
	private Insets insets;

	public LevelItem(final VertexBufferObjectManager vertexBufferObjectManager, final Puzzle puzzle) {
		super(0, 0, 200, 150, TextureLoader.getInstance().getTextureRegion(TextureType.LEVEL_ITEM_BACKGROUND), vertexBufferObjectManager);
		this.vertexBufferObjectManager = vertexBufferObjectManager;
		this.listeners = new ListenerList();
		this.setEnabled(true);
		this.puzzle = puzzle;
		this.initItem();
	}

	public Insets getInsets() {
		return this.insets;
	}

	private void initItem() {
		this.insets = new Insets(22, 0, 22, 0);

		long puzzleScore = SaveGameManager.getInstance().loadScore(this.puzzle);

		List<Text> texts = new ArrayList<Text>();
		texts.add(new Text(0, 0, FontLoader.getInstance().getFont(FontType.DROID_30_WHITE), this.puzzle.getWidth() + "x" + this.puzzle.getHeight(), 10, new TextOptions(HorizontalAlign.CENTER), this.vertexBufferObjectManager));
		if (puzzleScore != SaveGameManager.EMPTY_SCORE) {
			texts.add(new Text(0, 0, FontLoader.getInstance().getFont(FontType.DROID_30_WHITE), StringUtils.convertSecondsToTimeString(puzzleScore), 10, new TextOptions(HorizontalAlign.CENTER), this.vertexBufferObjectManager));
		}

		int row = 0;

		for (Text text : texts) {

			float drawablePosY = this.getInsets().getNorth();
			float drawablePosX = this.getInsets().getWest();

			float drawableHeight = this.getHeight() - (this.getInsets().getNorth() + this.getInsets().getSouth());
			float drawableWidth = this.getWidth() - (this.getInsets().getWest() + this.getInsets().getEast());

			float textPosX = drawablePosX;
			textPosX += (drawableWidth / 2);
			textPosX -= (text.getWidth() / 2);

			float textPosY = drawablePosY;
			textPosY += (row * (drawableHeight / texts.size()));
			textPosY += (drawableHeight / (texts.size() * 2));
			textPosY -= text.getHeight() / 2;

			text.setPosition(textPosX, textPosY);

			this.attachChild(text);
			row++;
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
