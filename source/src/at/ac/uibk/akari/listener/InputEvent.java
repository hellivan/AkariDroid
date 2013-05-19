package at.ac.uibk.akari.listener;

public class InputEvent {

	private Object source;

	public InputEvent(final Object source) {
		this.source = source;
	}

	public Object getSource() {
		return source;
	}
}
