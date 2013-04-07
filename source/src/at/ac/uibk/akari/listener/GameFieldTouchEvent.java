package at.ac.uibk.akari.listener;

import android.graphics.Point;
import android.graphics.PointF;
import at.ac.uibk.akari.view.GameField;

public class GameFieldTouchEvent {

	private GameField source;
	private Point cellPosition;
	private PointF touchPosition;

	public GameFieldTouchEvent(final GameField source, final Point cellPosition, final PointF touchPosition) {
		this.source = source;
		this.cellPosition = cellPosition;
		this.touchPosition = touchPosition;
	}

	public Point getCellPosition() {
		return this.cellPosition;
	}

	public PointF getTouchPosition() {
		return this.touchPosition;
	}

	public GameField getSource() {
		return this.source;
	}

}
