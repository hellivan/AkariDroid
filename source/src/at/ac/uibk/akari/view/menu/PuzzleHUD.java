package at.ac.uibk.akari.view.menu;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import at.ac.uibk.akari.listener.InputEvent;
import at.ac.uibk.akari.listener.PuzzleControlListener;
import at.ac.uibk.akari.listener.TouchListener;
import at.ac.uibk.akari.utils.ListenerList;

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

	public PuzzleHUD(final int width, final VertexBufferObjectManager vertexBufferObjectManager) {
		this.listeners = new ListenerList();
		this.vertexBufferObjectManager = vertexBufferObjectManager;
		this.desiredWidth = width;
		this.initGUI();
	}

	private void initGUI() {
		this.pauseButton = new HUDButton(PuzzleHUD.BORDER_INSET_X, PuzzleHUD.BORDER_INSET_Y, PuzzleHUD.BUTTONS_SIZE, PuzzleHUD.BUTTONS_SIZE, this.vertexBufferObjectManager);

		int timerWidth = 3 * PuzzleHUD.BUTTONS_SIZE;
		int timerPos = this.desiredWidth / 2 - timerWidth / 2;
		this.timerButton = new HUDButton(timerPos, PuzzleHUD.BORDER_INSET_Y, timerWidth, PuzzleHUD.BUTTONS_SIZE, this.vertexBufferObjectManager);

		this.helpButton = new HUDButton(this.desiredWidth - PuzzleHUD.BUTTONS_SIZE - PuzzleHUD.BORDER_INSET_X, PuzzleHUD.BORDER_INSET_Y, PuzzleHUD.BUTTONS_SIZE, PuzzleHUD.BUTTONS_SIZE, this.vertexBufferObjectManager);

		this.attachChild(this.pauseButton);
		this.attachChild(this.timerButton);
		this.attachChild(this.helpButton);

		this.registerTouchArea(this.pauseButton);
		this.registerTouchArea(this.helpButton);

		this.pauseButton.addTouchListener(this);
		this.helpButton.addTouchListener(this);

	}

	public void addPuzzleControlListener(final PuzzleControlListener listener) {
		this.listeners.addListener(PuzzleControlListener.class, listener);
	}

	public void removePuzzleControlListener(final PuzzleControlListener listener) {
		this.listeners.removeListener(PuzzleControlListener.class, listener);
	}

	protected void firePauseGame() {
		InputEvent event = new InputEvent(this);
		for (PuzzleControlListener listener : this.listeners.getListeners(PuzzleControlListener.class)) {
			listener.pausePuzzle(event);
		}
	}

	protected void fireHelpGame() {
		InputEvent event = new InputEvent(this);
		for (PuzzleControlListener listener : this.listeners.getListeners(PuzzleControlListener.class)) {
			listener.helpPuzzle(event);
		}
	}

	@Override
	public void touchPerformed(InputEvent event) {
		if (event.getSource() == this.pauseButton) {
			this.firePauseGame();
		} else if (event.getSource() == this.helpButton) {
			this.fireHelpGame();
		}
	}
}
