package at.ac.uibk.akari.view.menu.hud;

import java.util.Set;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;

import at.ac.uibk.akari.listener.InputEvent;
import at.ac.uibk.akari.listener.MenuItemSeletedEvent;
import at.ac.uibk.akari.listener.MenuItemSeletedEvent.ItemType;
import at.ac.uibk.akari.listener.MenuListener;
import at.ac.uibk.akari.listener.TouchListener;
import at.ac.uibk.akari.utils.ListenerList;
import at.ac.uibk.akari.utils.TextureLoader;
import at.ac.uibk.akari.utils.TextureLoader.TextureType;

public abstract class AbstractHUD extends HUD implements TouchListener {

	private Set<HUDButton> hudButtons;
	private Set<Entity> hudItems;
	private int desiredWidth;

	protected ListenerList listeners;
	protected VertexBufferObjectManager vertexBufferObjectManager;

	private boolean enabled;

	public AbstractHUD(final int width, final VertexBufferObjectManager vertexBufferObjectManager) {
		this.listeners = new ListenerList();
		this.vertexBufferObjectManager = vertexBufferObjectManager;
		this.desiredWidth = width;
		this.initGUI();
		this.setEnabled(true);
	}

	protected abstract Set<HUDButton> initHUDButtons(final int desiredWidth);

	protected abstract Set<Entity> initHUDItems(final int desiredWidth);

	public abstract int getDesiredHUDHeight();

	private void initGUI() {
		this.hudButtons = this.initHUDButtons(this.desiredWidth);
		this.hudItems = this.initHUDItems(this.desiredWidth);

		Sprite sprite = new Sprite(0, 0, this.desiredWidth + 1, this.getDesiredHUDHeight(), TextureLoader.getInstance().getTexture(TextureType.HUD_BACKGROUND, 0, 0), this.vertexBufferObjectManager);
		this.attachChild(sprite);
		Line line = new Line(0, this.getDesiredHUDHeight(), this.desiredWidth + 1, this.getDesiredHUDHeight(), this.vertexBufferObjectManager);
		line.setColor(Color.WHITE);
		line.setLineWidth(5);
		this.attachChild(line);

		for (HUDButton button : this.hudButtons) {
			this.attachChild(button);
			this.registerTouchArea(button);
			button.addTouchListener(this);

		}
		for (Entity item : this.hudItems) {
			this.attachChild(item);
		}

		this.setTouchAreaBindingOnActionDownEnabled(true);

	}

	public void addPuzzleControlListener(final MenuListener listener) {
		this.listeners.addListener(MenuListener.class, listener);
	}

	public void removePuzzleControlListener(final MenuListener listener) {
		this.listeners.removeListener(MenuListener.class, listener);
	}

	protected void fireMenuItemSelected(final ItemType type) {
		if (!this.isEnabled()) {
			return;
		}

		MenuItemSeletedEvent event = new MenuItemSeletedEvent(this, type);
		for (MenuListener listener : this.listeners.getListeners(MenuListener.class)) {
			listener.menuItemSelected(event);
		}
	}

	@Override
	public void touchPerformed(final InputEvent event) {
		if (event.getSource() instanceof HUDButton) {
			HUDButton pressedButton = (HUDButton) event.getSource();
			this.fireMenuItemSelected(pressedButton.getItemType());
		}
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
		for (HUDButton button : this.hudButtons) {
			button.setEnabled(enabled);

		}
	}

	public boolean isEnabled() {
		return this.enabled;
	}

}
