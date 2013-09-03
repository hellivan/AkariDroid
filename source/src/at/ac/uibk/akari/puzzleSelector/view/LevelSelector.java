package at.ac.uibk.akari.puzzleSelector.view;

import java.util.List;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.Entity;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.util.modifier.ease.EaseQuadOut;
import org.andengine.util.modifier.ease.IEaseFunction;

import android.graphics.PointF;
import android.util.Log;
import android.view.VelocityTracker;

public class LevelSelector extends Entity implements IScrollDetectorListener {

	private static int THRESHOLD_FLING_VELOCITY = 500;

	private static final boolean MOVE_OLD = false;

	private List<LevelItem> levelItems;

	private int columnsOnPages;
	private int rowsOnPages;

	private int currentPageIndex;

	private Camera gameCamera;

	private SurfaceScrollDetector mScrollDetector;
	private VelocityTracker mVelocityTracker;

	private IPageChangeListener mPageChangeListener;

	private Entity easeEntity;
	private IEaseFunction easeFunction;
	private float easeMultiplicator;

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

	public LevelSelector(final List<LevelItem> items, final int cols, final int rows, final Camera camera) {
		if ((items == null) || (camera == null)) {
			throw new NullPointerException();
		}
		if ((cols == 0) || (rows == 0) || (items.size() == 0)) {
			throw new IllegalArgumentException();
		}

		this.levelItems = items;
		this.columnsOnPages = cols;
		this.rowsOnPages = rows;
		this.gameCamera = camera;
		this.mScrollDetector = new SurfaceScrollDetector(this);
		this.currentPageIndex = 0; // start at the zeroth page
		this.buildLevelSelector();
		if (!LevelSelector.MOVE_OLD) {
			this.easeFunction = EaseQuadOut.getInstance();
			this.easeMultiplicator = 2;
			this.easeEntity = new Entity();
			this.easeEntity.setPosition(this.gameCamera.getCenterX(), this.gameCamera.getCenterY());
			this.attachChild(this.easeEntity);
			this.gameCamera.setChaseEntity(this.easeEntity);
		}
	}

	public void setmPageChangeListener(final IPageChangeListener mPageChangeListener) {
		this.mPageChangeListener = mPageChangeListener;
	}

	private void buildLevelSelector() {

		int column = 0;
		int row = 0;
		int pageIndex = 0;

		for (Sprite item : this.levelItems) {

			float selectorPosX = this.getX();
			float selectorPosY = this.getY();

			float cameraWidth = this.gameCamera.getWidth();
			float cameraHeight = this.gameCamera.getHeight();

			float itemPosX = selectorPosX;
			itemPosX += (column + 1) * (cameraWidth / (this.getColumnsOnPages() + 1));
			itemPosX -= item.getWidth() / 2;
			itemPosX += pageIndex * cameraWidth;

			float itemPosY = selectorPosY;
			itemPosY += (row + 1) * (cameraHeight / (this.getRowsOnPages() + 1));
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
	}

	@Override
	public void onScrollStarted(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {

	}

	@Override
	public void onScroll(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {

		if (LevelSelector.MOVE_OLD) {
			this.gameCamera.offsetCenter(-pDistanceX, 0);
		} else {
			PointF start = new PointF();
			start.x = this.easeEntity.getX();
			start.y = this.easeEntity.getY();
			PointF end = new PointF();
			end.x = start.x - (pDistanceX * this.easeMultiplicator);
			end.y = start.y;
			this.moveEaseEntity(start, end);
		}
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
		if (LevelSelector.MOVE_OLD) {
			float displacementX = (this.gameCamera.getWidth() * this.getCurrentPageIndex()) - this.gameCamera.getXMin();
			// moving camera to pageIndex
			this.gameCamera.offsetCenter(displacementX, 0);

		} else {
			PointF start = new PointF();
			start.x = this.gameCamera.getCenterX();
			start.y = this.gameCamera.getCenterY();
			PointF end = new PointF();
			end.x = (this.gameCamera.getWidth() * this.getCurrentPageIndex()) + (this.gameCamera.getWidth() / 2);
			end.y = start.y;
			this.moveEaseEntity(start, end);

		}

		// register listeners
		if (this.mPageChangeListener != null) {
			this.mPageChangeListener.onPageChange(this.currentPageIndex);
		}

		return success;
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

	public interface IPageChangeListener {
		public void onPageChange(int pageIndex);
	}
}