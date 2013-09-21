package at.ac.uibk.akari.view.menu.hud;

import java.util.HashSet;
import java.util.Set;

import org.andengine.entity.Entity;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.HorizontalAlign;

import at.ac.uibk.akari.listener.MenuItemSeletedEvent.ItemType;
import at.ac.uibk.akari.listener.StopClockEvent;
import at.ac.uibk.akari.listener.StopClockUpdateListener;
import at.ac.uibk.akari.utils.FontLoader;
import at.ac.uibk.akari.utils.FontLoader.FontType;
import at.ac.uibk.akari.utils.StopClockModel;
import at.ac.uibk.akari.utils.StringUtils;
import at.ac.uibk.akari.utils.TextureLoader;
import at.ac.uibk.akari.utils.TextureLoader.TextureType;

public class PuzzleHUD extends AbstractHUD implements StopClockUpdateListener {

	private static final int BUTTONS_SIZE = 55;
	private static final int BORDER_INSET_X = 25;
	private static final int BORDER_INSET_Y = 8;

	private Text timerText;
	private StopClockModel currentStopClock;

	public PuzzleHUD(final int width, final VertexBufferObjectManager vertexBufferObjectManager) {
		super(width, vertexBufferObjectManager);
	}

	@Override
	protected Set<HUDButton> initHUDButtons(final int desiredWidth) {
		Set<HUDButton> buttons = new HashSet<HUDButton>();

		buttons.add(new HUDButton(PuzzleHUD.BORDER_INSET_X, PuzzleHUD.BORDER_INSET_Y, PuzzleHUD.BUTTONS_SIZE, PuzzleHUD.BUTTONS_SIZE, this.vertexBufferObjectManager, TextureLoader.getInstance().getTexture(TextureType.MENU_ICONS, 0, 1), ItemType.PAUSE));

		buttons.add(new HUDButton(desiredWidth - PuzzleHUD.BUTTONS_SIZE - PuzzleHUD.BORDER_INSET_X, PuzzleHUD.BORDER_INSET_Y, PuzzleHUD.BUTTONS_SIZE, PuzzleHUD.BUTTONS_SIZE, this.vertexBufferObjectManager, TextureLoader.getInstance().getTexture(TextureType.MENU_ICONS, 0, 0), ItemType.HELP));

		return buttons;
	}

	@Override
	protected Set<Entity> initHUDItems(final int desiredWidth) {

		int timerWidth = 3 * PuzzleHUD.BUTTONS_SIZE;
		int timerPos = (desiredWidth / 2) - (timerWidth / 2);
		this.timerText = new Text(timerPos, PuzzleHUD.BORDER_INSET_Y, FontLoader.getInstance().getFont(FontType.DROID_48_WHITE), StringUtils.convertSecondsToTimeString(0), 11, new TextOptions(HorizontalAlign.CENTER), this.vertexBufferObjectManager);

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
		}
	}

	@Override
	public int getDesiredHUDHeight() {
		return PuzzleHUD.BUTTONS_SIZE + (2 * PuzzleHUD.BORDER_INSET_Y);
	}

}
