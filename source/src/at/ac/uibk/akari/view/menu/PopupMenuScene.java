package at.ac.uibk.akari.view.menu;

import java.util.ArrayList;
import java.util.List;

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

public class PopupMenuScene extends MenuScene implements IOnMenuItemClickListener {
	private ListenerList listeners;

	private VertexBufferObjectManager vertexBufferObjectManager;

	private List<ItemType> itemTypes;

	public PopupMenuScene(final Camera camera, final VertexBufferObjectManager vertexBufferObjectManager, final List<ItemType> itemTypes) {
		super(camera);
		this.itemTypes = itemTypes;
		this.listeners = new ListenerList();
		this.vertexBufferObjectManager = vertexBufferObjectManager;
		this.initGUI();
	}

	private void initGUI() {
		FontType fontType = FontType.DROID_48_BLACK;
		float sizePessed = 1.1f;
		float sizeNormal = 1f;

		List<IMenuItem> menuItems = new ArrayList<IMenuItem>();
		for (ItemType itemType : this.itemTypes) {
			IMenuItem item = new ScaleMenuItemDecorator(new TextMenuItem(itemType.ordinal(), FontLoader.getInstance().getFont(fontType), itemType.getItemText(), this.vertexBufferObjectManager), sizePessed, sizeNormal);
			item.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
			menuItems.add(item);
		}

		int bgWidth = 500;
		int bgHeight = 300;
		PopupBackground backGround = new PopupBackground(this.getCamera().getCenterX() - bgWidth / 2, this.getCamera().getCenterY() - bgHeight / 2, bgWidth, bgHeight, this.vertexBufferObjectManager);
		this.attachChild(backGround);
		for (IMenuItem menuItem : menuItems) {
			this.addMenuItem(menuItem);
		}

		this.buildAnimations();
		this.setBackgroundEnabled(false);

		this.setOnMenuItemClickListener(this);
	}

	@Override
	public boolean onMenuItemClicked(final MenuScene pMenuScene, final IMenuItem pMenuItem, final float pMenuItemLocalX, final float pMenuItemLocalY) {
		if (pMenuScene == this) {
			for (ItemType itemType : this.itemTypes) {
				if (pMenuItem.getID() == itemType.ordinal()) {
					this.fireMenuItemSelected(itemType);
					break;
				}
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
