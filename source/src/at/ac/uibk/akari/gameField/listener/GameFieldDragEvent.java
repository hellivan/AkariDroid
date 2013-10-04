package at.ac.uibk.akari.gameField.listener;

import android.graphics.Point;

public class GameFieldDragEvent {

	private Object source;
	private Point lastCell;
	private Point currentCell;

	public GameFieldDragEvent(final Object source, final Point lastCell, final Point currentCell) {
		this.source = source;
		this.lastCell = lastCell;
		this.currentCell = currentCell;
	}

	public Point getLastCell() {
		return this.lastCell;
	}

	public Object getSource() {
		return this.source;
	}

	public Point getCurrentCell() {
		return this.currentCell;
	}

}
