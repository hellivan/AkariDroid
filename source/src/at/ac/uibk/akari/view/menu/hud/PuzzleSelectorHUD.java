package at.ac.uibk.akari.view.menu.hud;

import java.util.HashSet;
import java.util.Set;

import org.andengine.entity.Entity;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import at.ac.uibk.akari.listener.MenuItemSeletedEvent.ItemType;
import at.ac.uibk.akari.utils.TextureLoader;
import at.ac.uibk.akari.utils.TextureLoader.TextureType;

public class PuzzleSelectorHUD extends AbstractHUD {

	public PuzzleSelectorHUD(final int width, final VertexBufferObjectManager vertexBufferObjectManager) {
		super(width, vertexBufferObjectManager);
	}

	private static final int BUTTONS_SIZE = 55;
	private static final int BORDER_INSET_X = 25;
	private static final int BORDER_INSET_Y = 8;

	@Override
	protected Set<HUDButton> initHUDButtons(final int desiredWidth) {
		Set<HUDButton> buttons = new HashSet<HUDButton>();

		buttons.add(new HUDButton(PuzzleSelectorHUD.BORDER_INSET_X, PuzzleSelectorHUD.BORDER_INSET_Y, PuzzleSelectorHUD.BUTTONS_SIZE, PuzzleSelectorHUD.BUTTONS_SIZE, this.vertexBufferObjectManager, TextureLoader.getInstance().getTexture(TextureType.MENU_ICONS, 0, 1), ItemType.BACK));

		return buttons;
	}

	@Override
	protected Set<Entity> initHUDItems(final int desiredWidth) {
		return new HashSet<Entity>();
	}

	@Override
	protected int getDesiredHUDHeight() {
		return PuzzleSelectorHUD.BUTTONS_SIZE + (2 * PuzzleSelectorHUD.BORDER_INSET_Y);
	}

}
