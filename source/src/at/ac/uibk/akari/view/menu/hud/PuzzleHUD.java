package at.ac.uibk.akari.view.menu.hud;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.color.Color;

import at.ac.uibk.akari.listener.InputEvent;
import at.ac.uibk.akari.listener.MenuItemSeletedEvent;
import at.ac.uibk.akari.listener.MenuItemSeletedEvent.ItemType;
import at.ac.uibk.akari.listener.MenuListener;
import at.ac.uibk.akari.listener.StopClockEvent;
import at.ac.uibk.akari.listener.StopClockUpdateListener;
import at.ac.uibk.akari.listener.TouchListener;
import at.ac.uibk.akari.utils.FontLoader;
import at.ac.uibk.akari.utils.FontLoader.FontType;
import at.ac.uibk.akari.utils.ListenerList;
import at.ac.uibk.akari.utils.StopClockModel;
import at.ac.uibk.akari.utils.TextureLoader;
import at.ac.uibk.akari.utils.TextureLoader.TextureType;

public class PuzzleHUD extends HUD implements TouchListener, StopClockUpdateListener {

	private static final int BUTTONS_SIZE = 55;
	private static final int BORDER_INSET_X = 25;
	private static final int BORDER_INSET_Y = 8;

	private HUDButton pauseButton;
	private HUDButton helpButton;
	private Text timerText;
	private int desiredWidth;

	protected ListenerList listeners;
	protected VertexBufferObjectManager vertexBufferObjectManager;

	private boolean enabled;

	private StopClockModel currentStopClock;

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
		this.timerText = new Text(timerPos, PuzzleHUD.BORDER_INSET_Y, FontLoader.getInstance().getFont(FontType.DROID_48_WHITE), "00:00", new TextOptions(HorizontalAlign.CENTER), this.vertexBufferObjectManager);

		this.helpButton = new HUDButton(this.desiredWidth - PuzzleHUD.BUTTONS_SIZE - PuzzleHUD.BORDER_INSET_X, PuzzleHUD.BORDER_INSET_Y, PuzzleHUD.BUTTONS_SIZE, PuzzleHUD.BUTTONS_SIZE, this.vertexBufferObjectManager, TextureLoader.getInstance().getTexture(TextureType.MENU_ICONS, 0, 0));

		Sprite sprite = new Sprite(0, 0, this.desiredWidth + 1, PuzzleHUD.BUTTONS_SIZE + (2 * PuzzleHUD.BORDER_INSET_Y), TextureLoader.getInstance().getTexture(TextureType.HUD_BACKGROUND, 0, 0), this.vertexBufferObjectManager);
		this.attachChild(sprite);
		Line line = new Line(0, PuzzleHUD.BUTTONS_SIZE + (2 * PuzzleHUD.BORDER_INSET_Y), this.desiredWidth + 1, PuzzleHUD.BUTTONS_SIZE + (2 * PuzzleHUD.BORDER_INSET_Y), this.vertexBufferObjectManager);
		line.setColor(Color.WHITE);
		line.setLineWidth(5);
		this.attachChild(line);

		this.attachChild(this.pauseButton);
		this.attachChild(this.timerText);
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

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
		this.helpButton.setEnabled(enabled);
		this.pauseButton.setEnabled(enabled);
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setStopClockModel(final StopClockModel stopClock) {
		if (this.currentStopClock != null) {
			this.currentStopClock.removeStopClockUpdateListener(this);
		}

		this.currentStopClock = stopClock;
		this.currentStopClock.addStopClockUpdateListener(this);

	}

	private String convertSecondsToTimeString(final long secondsTimer) {
		StringBuffer buffer = new StringBuffer();
		long mSeconds = secondsTimer;
		long mMinutes = mSeconds / 60;
		int mHours = (int) (mMinutes / 60);
		int mDays = mHours / 24;

		int days = mDays; // days
		int hours = mHours % 24; // hours
		int minutes = (int) (mMinutes % 60); // minutes
		int seconds = (int) (mSeconds % 60); // seconds

		if (days > 0) {
			buffer.append(String.format("%02d:", days));
		}
		if (days > 0 || hours > 0) {
			buffer.append(String.format("%02d:", hours));
		}
		buffer.append(String.format("%02d:", minutes));
		buffer.append(String.format("%02d", seconds));

		return buffer.toString();
	}

	@Override
	public void stopClockUpdated(final StopClockEvent event) {
		if (event.getSource() == this.currentStopClock) {
			this.timerText.setText(this.convertSecondsToTimeString(event.getCurrentClockSeconds()));
		}
	}
}
