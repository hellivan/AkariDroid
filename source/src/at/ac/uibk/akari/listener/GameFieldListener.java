package at.ac.uibk.akari.listener;

import java.util.EventListener;

public interface GameFieldListener extends EventListener {

	public void gameFieldTouched(GameFieldTouchEvent event);

}
