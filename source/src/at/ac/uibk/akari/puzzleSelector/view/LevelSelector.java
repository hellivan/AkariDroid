package at.ac.uibk.akari.puzzleSelector.view;

import java.util.ArrayList;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.input.touch.detector.SurfaceScrollDetector;

import android.view.VelocityTracker;

public class LevelSelector extends Entity implements IScrollDetectorListener {

	private static int THRESHOLD_FLING_VELOCITY = 250;

	private ArrayList<Sprite> levelItems;

	private int columnsOnPages;
	private int rowsOnPages;

	private int currentPageIndex;

	private Camera gameCamera;

	private SurfaceScrollDetector mScrollDetector;
	private VelocityTracker mVelocityTracker;

	private IPageChangeListener mPageChangeListener;

	private Entity easeEntity;

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

	public LevelSelector(final ArrayList<Sprite> items, final int cols, final int rows, final Camera camera) {
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
		this.mVelocityTracker = VelocityTracker.obtain();
		this.currentPageIndex = 0; // start at the zeroth page
		this.buildLevelSelector();
		this.easeEntity = new Entity();
		this.attachChild(this.easeEntity);
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
		this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
		this.mVelocityTracker.addMovement(pSceneTouchEvent.getMotionEvent());
	}

	@Override
	public void onScrollStarted(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
	}

	@Override
	public void onScroll(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
		this.gameCamera.offsetCenter(-pDistanceX, 0);
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
		return this.moveToPage(this.getCurrentPageIndex());
	}

	private boolean movePageLeft() {
		return this.moveToPage(this.getCurrentPageIndex() - 1);
	}

	private boolean movePageRight() {
		return this.moveToPage(this.getCurrentPageIndex() + 1);
	}

	public int getCurrentPageIndex() {
		return this.currentPageIndex;
	}

	private boolean moveToPage(final int pageIndex) {
		boolean success = false;
		if (this.isValidPageIndex(pageIndex)) {
			this.currentPageIndex = pageIndex;
			success = true;
		} else {
			System.out.println("Invalid page-index " + pageIndex);
			success = false;
		}

		boolean moveOld = false;

		System.out.println("Move to current page " + this.getCurrentPageIndex() + " of " + this.getPagesCount() + " pages [" + this.levelItems.size() + "]");
		float displacementX = (this.gameCamera.getWidth() * this.getCurrentPageIndex()) - this.gameCamera.getXMin();
		if (moveOld) {
			// moving camera to pageIndex
			this.gameCamera.offsetCenter(displacementX, 0);

		} else {
			float startX = this.gameCamera.getCenterX();
			float startY = this.gameCamera.getCenterY();

			this.easeEntity.setPosition(startX, startY);

			this.gameCamera.setChaseEntity(this.easeEntity);

			MoveModifier moveModiefier = new MoveModifier(0.2f, startX, startX + displacementX, startY, startY) {
				@Override
				protected void onModifierFinished(final IEntity pItem) {
					System.out.println("LevelSelector.moveToPage(...).new MoveModifier() {...}.onModifierFinished()");
					super.onModifierFinished(pItem);
					LevelSelector.this.gameCamera.setChaseEntity(null);
				}
			};
			this.easeEntity.clearEntityModifiers();
			this.easeEntity.registerEntityModifier(moveModiefier);
		}

		// register listeners
		if (this.mPageChangeListener != null) {
			this.mPageChangeListener.onPageChange(this.currentPageIndex);
		}

		return success;
	}

	private boolean isValidPageIndex(final int pageIndex) {
		return (pageIndex >= 0) && (pageIndex < this.getPagesCount());
	}

	public interface IPageChangeListener {
		public void onPageChange(int pageIndex);
	}
}