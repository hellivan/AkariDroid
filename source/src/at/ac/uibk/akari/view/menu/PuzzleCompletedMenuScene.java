package at.ac.uibk.akari.view.menu;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import at.ac.uibk.akari.listener.MenuItemSeletedEvent;
import at.ac.uibk.akari.listener.MenuItemSeletedEvent.ItemType;
import at.ac.uibk.akari.listener.MenuListener;
import at.ac.uibk.akari.utils.ListenerList;
import at.ac.uibk.akari.utils.TextureLoader;
import at.ac.uibk.akari.utils.TextureLoader.TextureType;

public class PuzzleCompletedMenuScene extends MenuScene implements IOnMenuItemClickListener {

	private ListenerList listeners;

	private VertexBufferObjectManager vertexBufferObjectManager;

	private IMenuItem nextMenuItem;
	private IMenuItem repeatMenuItem;
	private IMenuItem stopMenuItem;

	public PuzzleCompletedMenuScene(final Camera camera, final VertexBufferObjectManager vertexBufferObjectManager) {
		super(camera);
		this.listeners = new ListenerList();
		this.vertexBufferObjectManager = vertexBufferObjectManager;
		this.initGUI();
	}

	private void initGUI() {
		this.nextMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(0, 100, 100, TextureLoader.getInstance().getTexture(TextureType.LAMP, 0, 0), this.vertexBufferObjectManager), 1.2f, 1);
		this.repeatMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(1, 100, 100, TextureLoader.getInstance().getTexture(TextureType.LAMP, 1, 0), this.vertexBufferObjectManager), 1.2f, 1);
		this.stopMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(2, 100, 100, TextureLoader.getInstance().getTexture(TextureType.LAMP, 2, 0), this.vertexBufferObjectManager), 1.2f, 1);

		this.addMenuItem(this.nextMenuItem);
		this.addMenuItem(this.repeatMenuItem);
		this.addMenuItem(this.stopMenuItem);

		this.buildAnimations();
		this.setBackgroundEnabled(false);

		this.setOnMenuItemClickListener(this);
	}

	@Override
	public boolean onMenuItemClicked(final MenuScene pMenuScene, final IMenuItem pMenuItem, final float pMenuItemLocalX, final float pMenuItemLocalY) {
		if (pMenuScene == this) {
			if (pMenuItem == this.nextMenuItem) {
				this.fireMenuItemSelected(ItemType.NEXT);
			} else if (pMenuItem == this.repeatMenuItem) {
				this.fireMenuItemSelected(ItemType.REPLAY);
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
