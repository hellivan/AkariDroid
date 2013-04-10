package at.ac.uibk.akari.controller;

import android.graphics.Point;
import android.util.Log;
import at.ac.uibk.akari.listener.GameFieldDragEvent;
import at.ac.uibk.akari.listener.GameFieldListener;
import at.ac.uibk.akari.listener.GameFieldTouchEvent;
import at.ac.uibk.akari.view.GameField;

public class GameFieldController extends AbstractController implements GameFieldListener {

	private GameField gameField;

	public GameFieldController(final GameField gameField) {
		this.gameField = gameField;
	}

	public void setGameField(final GameField gameField) {
		this.gameField = gameField;
	}

	@Override
	public void gameFieldTouched(final GameFieldTouchEvent event) {
		if (event.getSource().equals(this.gameField)) {
			Point cellPosition = event.getTouchCell();

			Log.d(this.getClass().toString(), "GameField touched at " + cellPosition.toString());
			if (this.gameField.isLampAt(cellPosition)) {
				this.gameField.removeLampAt(cellPosition);
			} else {
				this.gameField.setLampAt(cellPosition);
			}

		}
	}

	@Override
	public boolean start() {
		this.gameField.addGameFieldListener(this);
		return true;
	}

	@Override
	public boolean stop() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void gameFieldDragged(final GameFieldDragEvent event) {
		if (event.getSource().equals(this.gameField)) {
			Point lastCell = event.getLastCell();
			Point currentCell = event.getCurrentCell();
			Log.d(this.getClass().toString(), "GameField dragged from  " + lastCell.toString() + " to " + currentCell.toString());

			// this.gameField.removeLampAt(lastCell);
			// this.gameField.setLampAt(currentCell);
		}

	}
}
