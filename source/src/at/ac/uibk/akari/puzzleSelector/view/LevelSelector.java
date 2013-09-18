package at.ac.uibk.akari.puzzleSelector.view;

import java.util.ArrayList;
import java.util.List;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.Entity;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.Constants;
import org.andengine.util.modifier.ease.EaseQuadOut;
import org.andengine.util.modifier.ease.IEaseFunction;

import android.graphics.PointF;
import android.util.Log;
import android.view.VelocityTracker;
import at.ac.uibk.akari.core.Puzzle;
import at.ac.uibk.akari.listener.InputEvent;
import at.ac.uibk.akari.listener.TouchListener;
import at.ac.uibk.akari.puzzleSelector.listener.PuzzleSelectionEvent;
import at.ac.uibk.akari.puzzleSelector.listener.PuzzleSelectionListener;
import at.ac.uibk.akari.puzzleSelector.listener.ValueChangedEvent;
import at.ac.uibk.akari.puzzleSelector.listener.ValueChangedListener;
import at.ac.uibk.akari.utils.ListenerList;
import at.ac.uibk.akari.view.Insets;

public class LevelSelector extends Entity implements IScrollDetectorListener, TouchListener {

	private static int THRESHOLD_FLING_VELOCITY = 500;

	private List<LevelItem> levelItems;

	private int columnsOnPages;
	private int rowsOnPages;

	private int currentPageIndex;

	private Camera gameCamera;

	private SurfaceScrollDetector mScrollDetector;
	private VelocityTracker mVelocityTracker;

	private Entity easeEntity;
	private IEaseFunction easeFunction;
	private float easeMultiplicator;

	private VertexBufferObjectManager vertexBufferObjectManager;

	protected ListenerList listeners;

	private Insets insets;

	public int getColumnsOnPages() {
		return this.columnsOnPages;
	}

	public int getRowsOnPages() {
		return this.rowsOnPages;
	}

	public int getPagesCount() {
		float maxItemsPerPage = this.getColumnsOnPages() * this.getRowsOnPages();
		return (int) Math.ceil(this.levelItems.size() / maxItemsPerPage);
	}

	public LevelSelector(final int cols, final int rows, final Camera camera, final VertexBufferObjectManager vertexBufferObjectManager) {
		this(cols, rows, new Insets(0, 0, 0, 0), camera, vertexBufferObjectManager);

	}

	public LevelSelector(final int cols, final int rows, final Insets insets, final Camera camera, final VertexBufferObjectManager vertexBufferObjectManager) {
		if ((cols == 0) || (rows == 0)) {
			throw new IllegalArgumentException();
		}
		this.insets = insets;
		this.listeners = new ListenerList();
		this.columnsOnPages = cols;
		this.rowsOnPages = rows;
		this.gameCamera = camera;
		this.levelItems = new ArrayList<LevelItem>();
		this.vertexBufferObjectManager = vertexBufferObjectManager;
		this.mScrollDetector = new SurfaceScrollDetector(this);

		this.easeFunction = EaseQuadOut.getInstance();
		this.easeMultiplicator = 2;
		this.easeEntity = new Entity();
		this.attachChild(this.easeEntity);
	}

	public void start() {
		this.currentPageIndex = 0; // start at the zeroth page
		this.moveCameraToPage(this.getCurrentPageIndex());
		this.gameCamera.setChaseEntity(this.easeEntity);
	}

	public void stop() {
		this.gameCamera.setChaseEntity(null);
	}

	public Insets getInsets() {
		return this.insets;
	}

	private void initLevelSelector() {

		int column = 0;
		int row = 0;
		int pageIndex = 0;

		for (Sprite item : this.levelItems) {

			float selectorPosX = this.getX() + this.getInsets().getWest();
			float selectorPosY = this.getY() + this.getInsets().getNorth();

			float cameraWidth = this.gameCamera.getWidth();
			float cameraHeight = this.gameCamera.getHeight();

			float selectorWidth = cameraWidth - (this.getInsets().getWest() + this.getInsets().getEast());
			float selectorHeight = cameraHeight - (this.getInsets().getNorth() + this.getInsets().getSouth());

			float itemPosX = selectorPosX;
			itemPosX += (column) * (selectorWidth / (this.getColumnsOnPages()));
			itemPosX += (selectorWidth / (this.getColumnsOnPages() * 2));
			itemPosX -= item.getWidth() / 2;
			itemPosX += pageIndex * cameraWidth;

			float itemPosY = selectorPosY;
			itemPosY += (row * (selectorHeight / this.getRowsOnPages()));
			itemPosY += (selectorHeight / (this.getRowsOnPages() * 2));
			itemPosY -= item.getHeight() / 2;

			item.setPosition(itemPosX, itemPosY);
			this.attachChild(item);

			column++;
			if (column >= this.columnsOnPages) {
				column = 0;
				row++;
				if (row >= this.rowsOnPages) {
					pageIndex++;
					column = 0;
					row = 0;
				}
			}
		}
	}

	public void onTouchEvent(final TouchEvent pSceneTouchEvent) {
		if (pSceneTouchEvent.isActionUp() || pSceneTouchEvent.isActionOutside() || pSceneTouchEvent.isActionCancel()) {
			this.mVelocityTracker.addMovement(pSceneTouchEvent.getMotionEvent());
			this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
			if ((this.mVelocityTracker != null) && (pSceneTouchEvent.getMotionEvent().getPointerCount() == 1)) {
				Log.d(this.getClass().getName(), "Recycle velocity tracker");
				this.mVelocityTracker.recycle();
				this.mVelocityTracker = null;
			}
		} else {
			if (this.mVelocityTracker == null) {
				Log.d(this.getClass().getName(), "Obtain velocity tracker");
				this.mVelocityTracker = VelocityTracker.obtain();
			}
			this.mVelocityTracker.addMovement(pSceneTouchEvent.getMotionEvent());
			this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
		}

		// foreward touch-ecent to level-items
		for (LevelItem item : this.levelItems) {
			if (item.contains(pSceneTouchEvent.getX(), pSceneTouchEvent.getY())) {

				final float[] touchAreaLocalCoordinates = item.convertSceneToLocalCoordinates(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
				final float touchAreaLocalX = touchAreaLocalCoordinates[Constants.VERTEX_INDEX_X];
				final float touchAreaLocalY = touchAreaLocalCoordinates[Constants.VERTEX_INDEX_Y];

				item.onAreaTouched(pSceneTouchEvent, touchAreaLocalX, touchAreaLocalY);
			}

		}
	}

	@Override
	public void onScrollStarted(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {

	}

	@Override
	public void onScroll(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {

		PointF start = new PointF();
		start.x = this.easeEntity.getX();
		start.y = this.easeEntity.getY();
		PointF end = new PointF();
		end.x = start.x - (pDistanceX * this.easeMultiplicator);
		end.y = start.y;
		this.moveEaseEntity(start, end);
	}

	@Override
	public void onScrollFinished(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {

		this.mVelocityTracker.computeCurrentVelocity(1000);
		float velocity;
		velocity = this.mVelocityTracker.getXVelocity();

		float cameraWidth = this.gameCamera.getWidth();

		// scroll left
		if (velocity > 0) {
			// fast scroll
			if (Math.abs(velocity) > LevelSelector.THRESHOLD_FLING_VELOCITY) {
				this.movePageLeft();
			} else {
				// if more then half page was scrolled
				if (this.gameCamera.getXMax() < ((this.getCurrentPageIndex() * cameraWidth) + (cameraWidth / 2))) {
					this.movePageLeft();
				}
				// jump back
				else {
					this.moveToCurrentPage();
				}
			}

		}
		// scroll right
		else {
			// fast scroll
			if (Math.abs(velocity) > LevelSelector.THRESHOLD_FLING_VELOCITY) {
				this.movePageRight();
			}
			// slow scroll
			else {
				// if more then half page was scrolled
				if (this.gameCamera.getXMin() > ((this.getCurrentPageIndex() * cameraWidth) + (cameraWidth / 2))) {
					this.movePageRight();
				}
				// jump back
				else {
					this.moveToCurrentPage();
				}
			}

		}

		this.mVelocityTracker.clear();

	}

	public void removeLevels() {
		for (LevelItem item : this.levelItems) {
			item.removeTouchListener(this);
			this.detachChild(item);
		}
		this.levelItems.clear();
	}

	public void setLevels(final List<Puzzle> puzzles) {
		this.removeLevels();

		for (Puzzle puzzle : puzzles) {
			LevelItem item = new LevelItem(this.vertexBufferObjectManager, puzzle);
			item.addTouchListener(this);
			this.levelItems.add(item);
		}
		this.initLevelSelector();
	}

	private boolean moveToCurrentPage() {
		Log.d(this.getClass().getName(), "Move to current page");
		return this.moveToPage(this.getCurrentPageIndex());
	}

	private boolean movePageLeft() {
		Log.d(this.getClass().getName(), "Move page left");
		return this.moveToPage(this.getCurrentPageIndex() - 1);
	}

	private boolean movePageRight() {
		Log.d(this.getClass().getName(), "Move page right");
		return this.moveToPage(this.getCurrentPageIndex() + 1);
	}

	public int getCurrentPageIndex() {
		return this.currentPageIndex;
	}

	private boolean moveToPage(final int pageIndex) {
		boolean success = false;
		int oldPageIndex = this.getCurrentPageIndex();
		if (this.isValidPageIndex(pageIndex)) {
			this.currentPageIndex = pageIndex;
			success = true;
		} else {
			Log.d(this.getClass().getName(), "Invalid page-index " + pageIndex);
			success = false;
		}

		Log.d(this.getClass().getName(), "Move to current page " + oldPageIndex + " to page " + this.getCurrentPageIndex() + " [" + this.getPagesCount() + " pages with " + this.levelItems.size() + " items]");

		this.moveCameraToPage(this.getCurrentPageIndex());

		if (success) {
			this.fireValueChanged(oldPageIndex, this.getCurrentPageIndex());
		}
		return success;
	}

	private void moveCameraToPage(final int pageIndex) {
		PointF start = new PointF();
		start.x = this.gameCamera.getCenterX();
		start.y = this.gameCamera.getCenterY();
		PointF end = new PointF();
		end.x = (this.gameCamera.getWidth() * pageIndex) + (this.gameCamera.getWidth() / 2);
		end.y = start.y;
		this.moveEaseEntity(start, end);
	}

	private void moveEaseEntity(final PointF start, final PointF end) {

		this.easeEntity.setPosition(start.x, start.y);
		// Log.d(this.getClass().getName(), "Moving camera from " + start +
		// " to " + end);
		MoveModifier moveModiefier = new MoveModifier(0.15f, start.x, end.x, start.y, end.y, this.easeFunction);
		this.easeEntity.clearEntityModifiers();
		this.easeEntity.registerEntityModifier(moveModiefier);
	}

	private boolean isValidPageIndex(final int pageIndex) {
		return (pageIndex >= 0) && (pageIndex < this.getPagesCount());
	}

	@Override
	public void touchPerformed(final InputEvent event) {
		if (event.getSource() instanceof LevelItem) {
			LevelItem selectedItem = (LevelItem) event.getSource();
			this.firePuzzleSelected(selectedItem.getPuzzle());
		}
	}

	public void addPuzzleSelectionListener(final PuzzleSelectionListener listener) {
		this.listeners.addListener(PuzzleSelectionListener.class, listener);
	}

	public void removePuzzleSelectionListener(final PuzzleSelectionListener listener) {
		this.listeners.removeListener(PuzzleSelectionListener.class, listener);
	}

	public void addValueChangedListener(final ValueChangedListener<Integer> listener) {
		this.listeners.addListener(ValueChangedListener.class, listener);
	}

	public void removeValueChangedListener(final ValueChangedListener<Integer> listener) {
		this.listeners.removeListener(ValueChangedListener.class, listener);
	}

	protected void firePuzzleSelected(final Puzzle puzzle) {
		PuzzleSelectionEvent event = new PuzzleSelectionEvent(this, puzzle);
		for (PuzzleSelectionListener listener : this.listeners.getListeners(PuzzleSelectionListener.class)) {
			listener.puzzleSelected(event);
		}
	}

	@SuppressWarnings("unchecked")
	protected void fireValueChanged(final int oldValue, final int newValue) {
		ValueChangedEvent<Integer> event = new ValueChangedEvent<Integer>(this, oldValue, newValue);
		for (ValueChangedListener<Integer> listener : this.listeners.getListeners(ValueChangedListener.class)) {
			listener.valueChanged(event);
		}
	}
}