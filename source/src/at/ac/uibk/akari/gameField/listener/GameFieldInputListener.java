package at.ac.uibk.akari.gameField.listener;

import java.util.EventListener;

public interface GameFieldInputListener extends EventListener {

	public void gameFieldTouched(final GameFieldTouchEvent event);

	public void gameFieldDragged(final GameFieldDragEvent event);

}
