package at.ac.uibk.akari.puzzleSelector.listener;

import java.util.EventListener;

public interface ValueChangedListener<T> extends EventListener {

	public void valueChanged(final ValueChangedEvent<T> event);

}
