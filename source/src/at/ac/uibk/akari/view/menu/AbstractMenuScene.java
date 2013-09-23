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
import at.ac.uibk.akari.common.menu.MenuItem;
import at.ac.uibk.akari.listener.MenuItemSeletedEvent;
import at.ac.uibk.akari.listener.MenuListener;
import at.ac.uibk.akari.utils.FontLoader;
import at.ac.uibk.akari.utils.FontLoader.FontType;
import at.ac.uibk.akari.utils.ListenerList;

public abstract class AbstractMenuScene extends MenuScene implements IOnMenuItemClickListener {

	protected ListenerList listeners;

	private VertexBufferObjectManager vertexBufferObjectManager;

	private List<MenuItem> menuItems;

	public AbstractMenuScene(final Camera camera, final VertexBufferObjectManager vertexBufferObjectManager, final List<MenuItem> menuItems) {
		super(camera);
		this.menuItems = menuItems;
		this.listeners = new ListenerList();
		this.vertexBufferObjectManager = vertexBufferObjectManager;
		this.initGUI();
	}

	private void initGUI() {

		this.setSceneOptions();

		FontType fontType = this.getItemsFontType();
		float sizePessed = 1.1f;
		float sizeNormal = 1f;

		List<IMenuItem> menuItems = new ArrayList<IMenuItem>();

		for (MenuItem item : this.menuItems) {
			IMenuItem menuItem = new ScaleMenuItemDecorator(new TextMenuItem(item.ordinal(), FontLoader.getInstance().getFont(fontType), item.getText(), this.vertexBufferObjectManager), sizePessed, sizeNormal);
			menuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
			menuItems.add(menuItem);
		}

		for (IMenuItem menuItem : menuItems) {
			this.addMenuItem(menuItem);
		}

		this.buildAnimations();

		this.setOnMenuItemClickListener(this);
	}

	protected abstract void setSceneOptions();

	protected abstract FontType getItemsFontType();

	public List<MenuItem> getItemTypes() {
		return this.menuItems;
	}

	@Override
	public boolean onMenuItemClicked(final MenuScene pMenuScene, final IMenuItem pMenuItem, final float pMenuItemLocalX, final float pMenuItemLocalY) {
		if (pMenuScene == this) {
			for (MenuItem itemType : this.menuItems) {
				if (pMenuItem.getID() == itemType.ordinal()) {
					this.fireMenuItemSelected(itemType);
					break;
				}
			}
		}
		return true;
	}

	protected void fireMenuItemSelected(final MenuItem itemType) {
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

	public VertexBufferObjectManager getVertexBufferObjectManager() {
		return this.vertexBufferObjectManager;
	}
}
