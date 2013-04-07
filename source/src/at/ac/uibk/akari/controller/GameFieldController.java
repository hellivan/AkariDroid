package at.ac.uibk.akari.controller;

import android.util.Log;
import at.ac.uibk.akari.listener.GameFieldListener;
import at.ac.uibk.akari.listener.GameFieldTouchEvent;
import at.ac.uibk.akari.view.GameField;

public class GameFieldController implements GameFieldListener {

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
			Log.d(this.getClass().toString(), "GameField touched at " + event.getCellPosition().x + "x" + event.getCellPosition().y);
			this.gameField.setLampAt(event.getCellPosition());
		}
	}

	public void start() {
		this.gameField.addGameFieldListener(this);
	}
}
