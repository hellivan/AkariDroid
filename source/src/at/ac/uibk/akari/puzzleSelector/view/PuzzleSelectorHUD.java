package at.ac.uibk.akari.puzzleSelector.view;

import java.util.HashSet;
import java.util.Set;

import org.andengine.entity.Entity;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.HorizontalAlign;

import at.ac.uibk.akari.listener.MenuItemSeletedEvent.ItemType;
import at.ac.uibk.akari.utils.FontLoader;
import at.ac.uibk.akari.utils.FontLoader.FontType;
import at.ac.uibk.akari.utils.TextureLoader;
import at.ac.uibk.akari.utils.TextureLoader.TextureType;
import at.ac.uibk.akari.view.menu.hud.AbstractHUD;
import at.ac.uibk.akari.view.menu.hud.HUDButton;

public class PuzzleSelectorHUD extends AbstractHUD {

	public PuzzleSelectorHUD(final int width, final VertexBufferObjectManager vertexBufferObjectManager) {
		super(width, vertexBufferObjectManager);
	}

	private static final int BUTTONS_SIZE = 55;
	private static final int BORDER_INSET_X = 25;
	private static final int BORDER_INSET_Y = 8;

	private Text pageIndicator;

	@Override
	protected Set<HUDButton> initHUDButtons(final int desiredWidth) {
		Set<HUDButton> buttons = new HashSet<HUDButton>();

		buttons.add(new HUDButton(PuzzleSelectorHUD.BORDER_INSET_X, PuzzleSelectorHUD.BORDER_INSET_Y, PuzzleSelectorHUD.BUTTONS_SIZE, PuzzleSelectorHUD.BUTTONS_SIZE, this.vertexBufferObjectManager, TextureLoader.getInstance().getTexture(TextureType.MENU_ICONS, 0, 1), ItemType.BACK));

		return buttons;
	}

	@Override
	protected Set<Entity> initHUDItems(final int desiredWidth) {
		int timerWidth = 2 * PuzzleSelectorHUD.BUTTONS_SIZE;
		int timerPos = (desiredWidth / 2) - (timerWidth / 2);
		this.pageIndicator = new Text(timerPos, PuzzleSelectorHUD.BORDER_INSET_Y, FontLoader.getInstance().getFont(FontType.DROID_48_WHITE), "0/0", 10, new TextOptions(HorizontalAlign.CENTER), this.vertexBufferObjectManager);

		Set<Entity> entities = new HashSet<Entity>();
		entities.add(this.pageIndicator);
		return entities;

	}

	@Override
	public int getDesiredHUDHeight() {
		return PuzzleSelectorHUD.BUTTONS_SIZE + (2 * PuzzleSelectorHUD.BORDER_INSET_Y);
	}

	public void setIndicatorIndex(final int pageIndex, final int pages) {
		this.pageIndicator.setText((pageIndex + 1) + "/" + pages);
	}

}
