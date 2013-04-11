package at.ac.uibk.akari.listener;

import java.util.EventListener;

public interface GameFieldInputListener extends EventListener {

	public void gameFieldTouched(GameFieldTouchEvent event);

	public void gameFieldDragged(GameFieldDragEvent event);

}
