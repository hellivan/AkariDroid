package at.ac.uibk.akari.listener;

import android.graphics.Point;
import android.graphics.PointF;
import at.ac.uibk.akari.view.GameField;

public class GameFieldTouchEvent {

	private GameField source;
	private Point touchCell;
	private PointF touchPosition;

	public GameFieldTouchEvent(final GameField source, final Point touchCell, final PointF touchPosition) {
		this.source = source;
		this.touchCell = touchCell;
		this.touchPosition = touchPosition;
	}

	public Point getTouchCell() {
		return this.touchCell;
	}

	public PointF getTouchPosition() {
		return this.touchPosition;
	}

	public GameField getSource() {
		return this.source;
	}

}
