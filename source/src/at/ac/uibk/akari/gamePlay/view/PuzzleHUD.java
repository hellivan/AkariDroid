package at.ac.uibk.akari.gamePlay.view;

import java.util.HashSet;
import java.util.Set;

import org.andengine.entity.Entity;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.HorizontalAlign;

import at.ac.uibk.akari.common.view.AbstractHUD;
import at.ac.uibk.akari.common.view.DefaultMenuItem;
import at.ac.uibk.akari.common.view.HUDButton;
import at.ac.uibk.akari.common.view.HUDToggleButton;
import at.ac.uibk.akari.common.view.IHUDButton;
import at.ac.uibk.akari.stopClock.listener.StopClockEvent;
import at.ac.uibk.akari.stopClock.listener.StopClockUpdateListener;
import at.ac.uibk.akari.stopClock.model.StopClockModel;
import at.ac.uibk.akari.utils.FontLoader;
import at.ac.uibk.akari.utils.FontLoader.FontType;
import at.ac.uibk.akari.utils.StringUtils;
import at.ac.uibk.akari.utils.TextureLoader;
import at.ac.uibk.akari.utils.TextureLoader.TextureType;

public class PuzzleHUD extends AbstractHUD implements StopClockUpdateListener {

	private static final int BUTTONS_SIZE = 55;
	private static final int BORDER_INSET_X = 25;
	private static final int BORDER_INSET_Y = 8;
	private static final int BUTTONS_SEPARATOR = 10;

	private Text timerText;
	private StopClockModel currentStopClock;

	public PuzzleHUD(final int width, final VertexBufferObjectManager vertexBufferObjectManager) {
		super(width, vertexBufferObjectManager);
	}

	@Override
	protected Set<IHUDButton> initHUDButtons(final int desiredWidth) {
		Set<IHUDButton> buttons = new HashSet<IHUDButton>();

		buttons.add(new HUDButton(PuzzleHUD.BORDER_INSET_X, PuzzleHUD.BORDER_INSET_Y, PuzzleHUD.BUTTONS_SIZE, PuzzleHUD.BUTTONS_SIZE, this.vertexBufferObjectManager, TextureLoader.getInstance().getTexture(TextureType.MENU_ICONS, 0, 3), DefaultMenuItem.PAUSE));

		buttons.add(new HUDButton(desiredWidth - PuzzleHUD.BORDER_INSET_X - PuzzleHUD.BUTTONS_SIZE, PuzzleHUD.BORDER_INSET_Y, PuzzleHUD.BUTTONS_SIZE, PuzzleHUD.BUTTONS_SIZE, this.vertexBufferObjectManager, TextureLoader.getInstance().getTexture(TextureType.MENU_ICONS, 0, 2), DefaultMenuItem.HELP));

		buttons.add(new HUDToggleButton(desiredWidth - PuzzleHUD.BORDER_INSET_X - PuzzleHUD.BUTTONS_SIZE - PuzzleHUD.BUTTONS_SEPARATOR - PuzzleHUD.BUTTONS_SIZE, PuzzleHUD.BORDER_INSET_Y, PuzzleHUD.BUTTONS_SIZE, PuzzleHUD.BUTTONS_SIZE, this.vertexBufferObjectManager, TextureLoader.getInstance().getTextureRegion(TextureType.MENU_ICONS), TextureType.MENU_ICONS.getTileNumber(0, 0), TextureType.MENU_ICONS.getTileNumber(0, 1), DefaultMenuItem.NEXT));

		return buttons;
	}

	@Override
	protected Set<Entity> initHUDItems(final int desiredWidth) {

		this.timerText = new Text(0, 0, FontLoader.getInstance().getFont(FontType.DROID_48_WHITE), StringUtils.convertSecondsToTimeString(0), 11, new TextOptions(HorizontalAlign.CENTER), this.vertexBufferObjectManager);
		this.timerText.setX((desiredWidth / 2) - (this.timerText.getWidth() / 2));
		this.timerText.setY(PuzzleHUD.BORDER_INSET_Y);

		Set<Entity> entities = new HashSet<Entity>();
		entities.add(this.timerText);
		return entities;
	}

	public void setStopClockModel(final StopClockModel stopClock) {
		if (this.currentStopClock != null) {
			this.currentStopClock.removeStopClockUpdateListener(this);
		}

		this.currentStopClock = stopClock;
		this.currentStopClock.addStopClockUpdateListener(this);

	}

	@Override
	public void stopClockUpdated(final StopClockEvent event) {
		if (event.getSource() == this.currentStopClock) {
			this.timerText.setText(StringUtils.convertSecondsToTimeString(event.getCurrentClockSeconds()));
			this.timerText.setX((this.getDesiredHUDWidth() / 2) - (this.timerText.getWidth() / 2));
		}
	}

	@Override
	public int getDesiredHUDHeight() {
		return PuzzleHUD.BUTTONS_SIZE + (2 * PuzzleHUD.BORDER_INSET_Y);
	}

}
