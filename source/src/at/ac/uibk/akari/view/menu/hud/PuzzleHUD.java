package at.ac.uibk.akari.view.menu.hud;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import at.ac.uibk.akari.listener.InputEvent;
import at.ac.uibk.akari.listener.MenuItemSeletedEvent;
import at.ac.uibk.akari.listener.MenuItemSeletedEvent.ItemType;
import at.ac.uibk.akari.listener.MenuListener;
import at.ac.uibk.akari.listener.TouchListener;
import at.ac.uibk.akari.utils.ListenerList;
import at.ac.uibk.akari.utils.TextureLoader;
import at.ac.uibk.akari.utils.TextureLoader.TextureType;

public class PuzzleHUD extends HUD implements TouchListener {

	private static final int BUTTONS_SIZE = 60;
	private static final int BORDER_INSET_X = 30;
	private static final int BORDER_INSET_Y = 15;

	private HUDButton pauseButton;
	private HUDButton helpButton;
	private HUDButton timerButton;
	private int desiredWidth;

	protected ListenerList listeners;
	protected VertexBufferObjectManager vertexBufferObjectManager;

	private boolean enabled;

	public PuzzleHUD(final int width, final VertexBufferObjectManager vertexBufferObjectManager) {
		this.listeners = new ListenerList();
		this.vertexBufferObjectManager = vertexBufferObjectManager;
		this.desiredWidth = width;
		this.initGUI();
		this.setEnabled(true);
	}

	private void initGUI() {
		this.pauseButton = new HUDButton(PuzzleHUD.BORDER_INSET_X, PuzzleHUD.BORDER_INSET_Y, PuzzleHUD.BUTTONS_SIZE, PuzzleHUD.BUTTONS_SIZE, this.vertexBufferObjectManager, TextureLoader.getInstance().getTexture(TextureType.MENU_ICONS, 0, 1));

		int timerWidth = 3 * PuzzleHUD.BUTTONS_SIZE;
		int timerPos = this.desiredWidth / 2 - timerWidth / 2;
		this.timerButton = new HUDButton(timerPos, PuzzleHUD.BORDER_INSET_Y, timerWidth, PuzzleHUD.BUTTONS_SIZE, this.vertexBufferObjectManager, TextureLoader.getInstance().getTexture(TextureType.LAMP, 1, 0));

		this.helpButton = new HUDButton(this.desiredWidth - PuzzleHUD.BUTTONS_SIZE - PuzzleHUD.BORDER_INSET_X, PuzzleHUD.BORDER_INSET_Y, PuzzleHUD.BUTTONS_SIZE, PuzzleHUD.BUTTONS_SIZE, this.vertexBufferObjectManager, TextureLoader.getInstance().getTexture(TextureType.MENU_ICONS, 0, 0));

		this.attachChild(this.pauseButton);
		this.attachChild(this.timerButton);
		this.attachChild(this.helpButton);

		this.registerTouchArea(this.pauseButton);
		this.registerTouchArea(this.helpButton);

		this.setTouchAreaBindingOnActionDownEnabled(true);

		this.pauseButton.addTouchListener(this);
		this.helpButton.addTouchListener(this);

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
		if (event.getSource() == this.pauseButton) {
			this.fireMenuItemSelected(ItemType.PAUSE);
		} else if (event.getSource() == this.helpButton) {
			this.fireMenuItemSelected(ItemType.HELP);
		}
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		this.helpButton.setEnabled(enabled);
		this.pauseButton.setEnabled(enabled);
	}

	public boolean isEnabled() {
		return enabled;
	}
}
