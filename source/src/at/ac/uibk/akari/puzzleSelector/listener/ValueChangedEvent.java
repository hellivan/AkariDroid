package at.ac.uibk.akari.puzzleSelector.listener;

public class ValueChangedEvent<T> {

	private Object source;
	private T oldValue;
	private T newValue;

	public ValueChangedEvent(final Object source, final T oldValue, final T newValue) {
		super();
		this.source = source;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public T getOldValue() {
		return this.oldValue;
	}

	public T getNewValue() {
		return this.newValue;
	}

	public Object getSource() {
		return this.source;
	}

}
