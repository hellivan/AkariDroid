package at.ac.uibk.akari.listener;

import java.util.EventListener;

public interface TouchListener extends EventListener {
	
	public void touchPerformed(final InputEvent event);

}
