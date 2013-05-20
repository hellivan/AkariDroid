package at.ac.uibk.akari.listener;

import java.util.EventListener;

public interface GameFieldInputListener extends EventListener {

	public void gameFieldTouched(final GameFieldTouchEvent event);

	public void gameFieldDragged(final GameFieldDragEvent event);

}
