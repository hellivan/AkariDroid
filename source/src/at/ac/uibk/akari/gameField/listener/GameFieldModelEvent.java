package at.ac.uibk.akari.gameField.listener;

import at.ac.uibk.akari.core.GameFieldModel;

public class GameFieldModelEvent {

	private Object source;
	private GameFieldModel gameFieldModel;
	private long secondsElapsed;

	public GameFieldModelEvent(final Object source, final GameFieldModel gameFieldModel, final long secondsElapsed) {
		super();
		this.source = source;
		this.gameFieldModel = gameFieldModel;
		this.secondsElapsed = secondsElapsed;
	}

	public Object getSource() {
		return this.source;
	}

	public GameFieldModel getGamefieldModel() {
		return this.gameFieldModel;
	}

	public long getSecondsElapsed() {
		return this.secondsElapsed;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.gameFieldModel == null) ? 0 : this.gameFieldModel.hashCode());
		result = (prime * result) + (int) (this.secondsElapsed ^ (this.secondsElapsed >>> 32));
		result = (prime * result) + ((this.source == null) ? 0 : this.source.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		GameFieldModelEvent other = (GameFieldModelEvent) obj;
		if (this.gameFieldModel == null) {
			if (other.gameFieldModel != null) {
				return false;
			}
		} else if (!this.gameFieldModel.equals(other.gameFieldModel)) {
			return false;
		}
		if (this.secondsElapsed != other.secondsElapsed) {
			return false;
		}
		if (this.source == null) {
			if (other.source != null) {
				return false;
			}
		} else if (!this.source.equals(other.source)) {
			return false;
		}
		return true;
	}

}
