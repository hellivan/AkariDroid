package at.ac.uibk.akari.gameField.listener;

import android.graphics.Point;

public class GameFieldTouchEvent {

	private Object source;
	private Point touchCell;

	public GameFieldTouchEvent(final Object source, final Point touchCell) {
		this.source = source;
		this.touchCell = touchCell;
	}

	public Point getTouchCell() {
		return this.touchCell;
	}

	public Object getSource() {
		return this.source;
	}

}
