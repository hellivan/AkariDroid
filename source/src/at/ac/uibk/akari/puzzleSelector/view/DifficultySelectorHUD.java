package at.ac.uibk.akari.puzzleSelector.view;

import java.util.HashSet;
import java.util.Set;

import org.andengine.entity.Entity;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import at.ac.uibk.akari.common.menu.DefaultMenuItem;
import at.ac.uibk.akari.utils.TextureLoader;
import at.ac.uibk.akari.utils.TextureLoader.TextureType;
import at.ac.uibk.akari.view.menu.hud.AbstractHUD;
import at.ac.uibk.akari.view.menu.hud.HUDButton;

public class DifficultySelectorHUD extends AbstractHUD {

	public DifficultySelectorHUD(final int width, final VertexBufferObjectManager vertexBufferObjectManager) {
		super(width, vertexBufferObjectManager);
	}

	private static final int BUTTONS_SIZE = 55;
	private static final int BORDER_INSET_X = 25;
	private static final int BORDER_INSET_Y = 8;

	@Override
	protected Set<HUDButton> initHUDButtons(final int desiredWidth) {
		Set<HUDButton> buttons = new HashSet<HUDButton>();

		buttons.add(new HUDButton(DifficultySelectorHUD.BORDER_INSET_X, DifficultySelectorHUD.BORDER_INSET_Y, DifficultySelectorHUD.BUTTONS_SIZE, DifficultySelectorHUD.BUTTONS_SIZE, this.vertexBufferObjectManager, TextureLoader.getInstance().getTexture(TextureType.MENU_ICONS, 0, 1), DefaultMenuItem.BACK));

		return buttons;
	}

	@Override
	protected Set<Entity> initHUDItems(final int desiredWidth) {
		return new HashSet<Entity>();

	}

	@Override
	public int getDesiredHUDHeight() {
		return DifficultySelectorHUD.BUTTONS_SIZE + (2 * DifficultySelectorHUD.BORDER_INSET_Y);
	}

}
