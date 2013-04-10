package at.ac.uibk.akari.listener;

import android.graphics.Point;
import at.ac.uibk.akari.view.GameField;

public class GameFieldDragEvent {

	private GameField source;
	private Point lastCell;
	private Point currentCell;

	public GameFieldDragEvent(final GameField source, final Point lastCell, final Point currentCell) {
		this.source = source;
		this.lastCell = lastCell;
		this.currentCell = currentCell;
	}

	public Point getLastCell() {
		return this.lastCell;
	}

	public GameField getSource() {
		return this.source;
	}

	public Point getCurrentCell() {
		return this.currentCell;
	}

}
