package at.ac.uibk.akari.listener;

import android.graphics.Point;
import at.ac.uibk.akari.view.GameField;

public class GameFieldTouchEvent {

	private GameField source;
	private Point touchCell;

	public GameFieldTouchEvent(final GameField source, final Point touchCell) {
		this.source = source;
		this.touchCell = touchCell;
	}

	public Point getTouchCell() {
		return this.touchCell;
	}

	public GameField getSource() {
		return this.source;
	}

}
