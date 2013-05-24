package at.ac.uibk.akari.view.menu;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.TextMenuItem;
import org.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.opengl.GLES20;
import at.ac.uibk.akari.listener.MenuItemSeletedEvent;
import at.ac.uibk.akari.listener.MenuItemSeletedEvent.ItemType;
import at.ac.uibk.akari.listener.MenuListener;
import at.ac.uibk.akari.utils.FontLoader;
import at.ac.uibk.akari.utils.FontLoader.FontType;
import at.ac.uibk.akari.utils.ListenerList;
import at.ac.uibk.akari.view.Cell;
import at.ac.uibk.akari.view.Cell.State;

public class PuzzlePauseMenuScene extends MenuScene implements IOnMenuItemClickListener {

	private ListenerList listeners;

	private VertexBufferObjectManager vertexBufferObjectManager;

	private IMenuItem continueMenuItem;
	private IMenuItem resetMenuItem;
	private IMenuItem stopMenuItem;

	public PuzzlePauseMenuScene(final Camera camera, final VertexBufferObjectManager vertexBufferObjectManager) {
		super(camera);
		this.listeners = new ListenerList();
		this.vertexBufferObjectManager = vertexBufferObjectManager;
		this.initGUI();
	}

	private void initGUI() {

		FontType fontType = FontType.DROID_48_BLACK;
		float sizePessed = 1.1f;
		float sizeNormal = 1f;

		this.continueMenuItem = new ScaleMenuItemDecorator(new TextMenuItem(0, FontLoader.getInstance().getFont(fontType), "Continue", this.vertexBufferObjectManager), sizePessed, sizeNormal);
		this.continueMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		this.resetMenuItem = new ScaleMenuItemDecorator(new TextMenuItem(1, FontLoader.getInstance().getFont(fontType), "Reset", this.vertexBufferObjectManager), sizePessed, sizeNormal);
		this.resetMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		this.stopMenuItem = new ScaleMenuItemDecorator(new TextMenuItem(2, FontLoader.getInstance().getFont(fontType), "Stop", this.vertexBufferObjectManager), sizePessed, sizeNormal);
		this.stopMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		int bgWidth = 500;
		int bgHeight = 300;
		Cell cell = new Cell(this.getCamera().getCenterX() - bgWidth / 2, this.getCamera().getCenterY() - bgHeight / 2, bgWidth, bgHeight, this.vertexBufferObjectManager);
		cell.setCellState(State.LAMP);
		this.attachChild(cell);
		this.addMenuItem(this.continueMenuItem);
		this.addMenuItem(this.resetMenuItem);
		this.addMenuItem(this.stopMenuItem);

		this.buildAnimations();
		this.setBackgroundEnabled(false);

		this.setOnMenuItemClickListener(this);
	}

	@Override
	public boolean onMenuItemClicked(final MenuScene pMenuScene, final IMenuItem pMenuItem, final float pMenuItemLocalX, final float pMenuItemLocalY) {
		if (pMenuScene == this) {
			if (pMenuItem == this.continueMenuItem) {
				this.fireMenuItemSelected(ItemType.CONTINUE);
			} else if (pMenuItem == this.resetMenuItem) {
				this.fireMenuItemSelected(ItemType.RESET);
			} else if (pMenuItem == this.stopMenuItem) {
				this.fireMenuItemSelected(ItemType.STOP);
			}
		}
		return true;
	}

	private void fireMenuItemSelected(final ItemType itemType) {
		MenuItemSeletedEvent event = new MenuItemSeletedEvent(this, itemType);
		for (MenuListener listener : this.listeners.getListeners(MenuListener.class)) {
			listener.menuItemSelected(event);
		}
	}

	public void addMenuListener(final MenuListener listener) {
		this.listeners.addListener(MenuListener.class, listener);
	}

	public void removeMenuListener(final MenuListener listener) {
		this.listeners.removeListener(MenuListener.class, listener);
	}
}