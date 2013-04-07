package at.ac.uibk.akari.utils;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class ListenersList {

	private Object[] listeners;

	public ListenersList() {
		this.listeners = new Object[0];
	}

	public synchronized <T extends EventListener> void addListener(final Class<T> t, final T listener) {
		int oldSize = this.listeners.length;
		Object[] newListeners = new Object[oldSize + 2];
		System.arraycopy(this.listeners, 0, newListeners, 0, oldSize);
		newListeners[oldSize] = t;
		newListeners[oldSize + 1] = listener;
		this.listeners = newListeners;

	}

	@SuppressWarnings("unchecked")
	public synchronized <T extends EventListener> List<T> getListeners(final Class<T> t) {
		List<T> listeners = new ArrayList<T>(this.getListenerCount(t));
		for (int i = 0; i < this.listeners.length; i += 2) {
			if (this.listeners[i] == t) {
				listeners.add((T) this.listeners[i + 1]);
			}
		}
		return listeners;
	}

	public synchronized int getListenerCount() {
		return this.listeners.length / 2;
	}

	public synchronized <T extends EventListener> int getListenerCount(final Class<T> t) {
		int listenerCount = 0;
		for (int i = 0; i < this.listeners.length; i += 2) {
			if (this.listeners[i] == t) {
				listenerCount++;
			}
		}
		return listenerCount;
	}

	public synchronized <T extends EventListener> void removeListener(final Class<T> t, final T listener) {
		if (this.listeners.length < 1) {
			return;
		}
		int deleteIndex = -1;

		for (int i = 0; i < this.listeners.length; i += 2) {
			if (this.listeners[i] == t && this.listeners[i + 1].equals(listener)) {
				deleteIndex = i;
				break;
			}
		}
		if (deleteIndex > -1) {
			int oldSize = this.listeners.length;
			Object[] newListeners = new Object[oldSize - 2];

			System.arraycopy(this.listeners, 0, newListeners, 0, deleteIndex);

			if (deleteIndex < newListeners.length) {
				System.arraycopy(this.listeners, deleteIndex + 2, newListeners, deleteIndex, newListeners.length - deleteIndex);
			}
			this.listeners = newListeners;
		}
	}

}
